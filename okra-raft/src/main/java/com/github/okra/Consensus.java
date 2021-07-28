package com.github.okra;

import com.google.common.collect.Lists;
import java.net.InetSocketAddress;

import com.github.okra.evn.NodeState;
import com.github.okra.evn.RaftCommand;
import com.github.okra.modal.Message;
import com.github.okra.modal.MessageBuilder;
import com.github.okra.model.AppendEntriesArg;
import com.github.okra.model.LogEntry;
import com.github.okra.model.Proposal;
import com.github.okra.model.RequestVoteArg;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Consensus {

  private static final Logger logger = LoggerFactory.getLogger(Consensus.class);
  private static final ThreadPoolExecutor connectionPool =
      new ThreadPoolExecutor(
          100,
          120,
          30L,
          TimeUnit.MINUTES,
          new LinkedBlockingQueue<>(100),
          Executors.defaultThreadFactory(),
          (r, executor) -> logger.warn("Queue is full."));

  private Consensus() {}

  private Node node;

  public static Consensus create(Node node) {
    Consensus consensus = new Consensus();
    consensus.setNode(node);
    return consensus;
  }

  private Node getNode() {
    return node;
  }

  private void setNode(Node node) {
    this.node = node;
  }

  public void startLeaderElection() {
    node.setState(NodeState.CANDIDATE);
    int savedCurrentTerm = node.getStore().getCurrentTerm() + 1;
    node.getStore().setCurrentTerm(savedCurrentTerm);
    node.getStore().voteFor(node.getId());
    node.setElectionResetEvent(Instant.now());
    logger.info(
        "Node become candidate , the current term is {} , the logs = {}",
        savedCurrentTerm,
        node.getStore().logs());
    int lastLogIndex = node.getStore().lastLogIndex();
    Optional<LogEntry> log = node.getStore().log(lastLogIndex);
    RequestVoteArg requestVoteArg =
        new RequestVoteArg(
            savedCurrentTerm,
            node.getId(),
            lastLogIndex,
            log.isPresent() ? log.get().getTerm() : 0);
    Proposal proposal = Proposal.createProposal(savedCurrentTerm);
    List<Message> messages =
        node.getPeerIds().stream()
            .map(
                (peerId) -> {
                  logger.info("Send RequestVote to peer {}", peerId.toString());
                  return MessageBuilder.create()
                      .id(proposal.getId())
                      .receiver(peerId)
                      .sender(node.getId())
                      .content(requestVoteArg)
                      .build();
                })
            .collect(Collectors.toList());
    CompletableFuture.runAsync(
            () -> messages.parallelStream().forEach(message -> node.send(message)), connectionPool)
        .whenComplete(
            (unused, throwable) -> {
              if (throwable == null) {
                node.submitNewProposal(RaftCommand.REQUEST_VOTE_RS, proposal);
                return;
              }
              throwable.printStackTrace();
            });
  }

  public void sendHeartbeats(int termStarted) {
    Proposal proposal = Proposal.createProposal(termStarted);
    CompletableFuture.runAsync(
            () ->
                node.getPeerIds().parallelStream()
                    .forEach(
                        peer -> {
                          Integer peerNextIndex = node.getNextIndex().get(peer);
                          int prevLogIndex = peerNextIndex - 1;
                          LogEntry currentEntry =
                              node.getStore().log(prevLogIndex).orElse(LogEntry.emptyEntry());
                          int prevLogTerm = currentEntry.getTerm();
                          List<LogEntry> entries =
                              node.getStore().subLog(peerNextIndex, node.getStore().logSize());
                          AppendEntriesArg appendEntriesArg = new AppendEntriesArg();
                          appendEntriesArg.setTerm(termStarted);
                          appendEntriesArg.setLeaderId(node.getId());
                          appendEntriesArg.setPrevLogIndex(prevLogIndex);
                          appendEntriesArg.setPrevLogTerm(prevLogTerm);
                          appendEntriesArg.setEntries(entries);
                          appendEntriesArg.setLeaderCommit(node.getCommitIndex());
                          logger.info(
                              "Send AppendEntries Request to server {} , nextIndex = {} , args = {}",
                              peer,
                              peerNextIndex,
                              appendEntriesArg);
                          Message message =
                              MessageBuilder.create()
                                  .id(proposal.getId())
                                  .receiver(peer)
                                  .sender(node.getId())
                                  .content(appendEntriesArg)
                                  .build();
                          node.send(message);
                        }),
            connectionPool)
        .whenComplete(
            (unused, throwable) -> {
              if (throwable == null) {
                node.submitNewProposal(RaftCommand.APPEND_ENTRIES_RS, proposal);
                return;
              }
              throwable.printStackTrace();
            });
    ;
  }
}
