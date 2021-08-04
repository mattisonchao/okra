package com.github.okra;

import static com.github.okra.evn.RaftCommand.REQUEST_VOTE_RS;

import com.github.okra.evn.NodeState;
import com.github.okra.evn.RaftCommand;
import com.github.okra.modal.Endpoint;
import com.github.okra.modal.Message;
import com.github.okra.model.AppendEntriesArg;
import com.github.okra.model.AppendEntriesResult;
import com.github.okra.model.LogEntry;
import com.github.okra.model.Proposal;
import com.github.okra.model.RequestVoteArg;
import com.github.okra.model.RequestVoteResult;
import com.github.okra.option.NodeOption;
import com.github.okra.store.RocksStore;
import com.github.okra.store.Store;
import io.netty.util.HashedWheelTimer;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Raft Node.
 *
 * @author mattison
 * @version 1.0
 */
public class Node extends Actor {
  private static final Logger logger = LoggerFactory.getLogger(Node.class);
  private final Consensus consensus = Consensus.create(this);
  private final StateTransformer stateTransformer = StateTransformer.of(this);
  private final Store store = new RocksStore();
  private volatile Integer commitIndex = 0;
  private volatile Integer lastApplied = 0;
  private volatile NodeState state = NodeState.FOLLOWER;
  private volatile Instant electionResetEvent = Instant.now();
  private final Map<Endpoint, Integer> nextIndex = new ConcurrentHashMap<>();
  private final Map<Endpoint, Integer> matchIndex = new ConcurrentHashMap<>();
  private final Map<RaftCommand, Proposal> proposals = new ConcurrentHashMap<>();
  private final HashedWheelTimer timer = new HashedWheelTimer(new DefaultThreadFactory("timer-"));
  private NodeOption option;
  private List<Endpoint> peerIds;

  @Override
  protected void preStart() {
    this.setId(option.getSelf());
    this.peerIds = option.getPeers();
  }

  @Override
  protected void afterStart() {
    startElectionTimoutTimer();
  }

  public void loadOption(NodeOption option) {
    this.option = option;
  }

  @Override
  protected void receive(Message event) {
    switch (RaftCommand.getRaftCommand(event.getContent())) {
      case APPEND_ENTRIES_RQ:
        AppendEntriesArg appendEntriesArg = (AppendEntriesArg) event.getContent();
        logger.info("Receive AppendEntry request, {}", appendEntriesArg);
        if (appendEntriesArg.getTerm() > store.getCurrentTerm()) {
          logger.info("Current term is before result term");
          stateTransformer.becomeFollower(appendEntriesArg.getTerm());
          return;
        }
        if (appendEntriesArg.getTerm().equals(store.getCurrentTerm())) {
          if (state != NodeState.FOLLOWER) {
            stateTransformer.becomeFollower(appendEntriesArg.getTerm());
          }
          setElectionResetEvent(Instant.now());
          if (appendEntriesArg.getPrevLogIndex() == -1
              || (appendEntriesArg.getPrevLogIndex() < store.logSize()
                  && appendEntriesArg
                      .getPrevLogTerm()
                      .equals(store.log(appendEntriesArg.getPrevLogIndex()).get().getTerm()))) {
            int logInsertIndex = appendEntriesArg.getPrevLogIndex() + 1;
            int newEntriesIndex = 0;
            while (true) {
              if (logInsertIndex >= store.logSize()
                  || newEntriesIndex >= appendEntriesArg.getEntries().size()) {
                break;
              }
              Optional<LogEntry> log = store.log(logInsertIndex);
              if (log.isPresent()
                  && !log.get()
                      .getTerm()
                      .equals(appendEntriesArg.getEntries().get(newEntriesIndex).getTerm())) {
                break;
              }
              logInsertIndex++;
              newEntriesIndex++;
            }
            if (newEntriesIndex < appendEntriesArg.getEntries().size()) {
              logger.info(
                  "From index {} to insert log {}",
                  logInsertIndex,
                  appendEntriesArg
                      .getEntries()
                      .subList(logInsertIndex, appendEntriesArg.getEntries().size()));
              store.addLogs(
                  appendEntriesArg
                      .getEntries()
                      .subList(newEntriesIndex, appendEntriesArg.getEntries().size()));
              logger.info("The current Log content is {}", store.logs());
            }
            if (appendEntriesArg.getLeaderCommit() > commitIndex) {
              commitIndex = Math.min(appendEntriesArg.getLeaderCommit(), store.logSize() - 1);
              logger.info(" Setting commitIndex to {}", commitIndex);
            }
            AppendEntriesResult appendEntriesResult = new AppendEntriesResult();
            appendEntriesResult.setTerm(store.getCurrentTerm());
            appendEntriesResult.setSuccess(true);
            logger.info(" reply appendEntry {}", appendEntriesArg);
            Message reply = event.reply(appendEntriesResult);
            send(reply);
            return;
          } else {
            logger.info(
                "Node term {} is grater than request term {}",
                store.getCurrentTerm(),
                appendEntriesArg.getTerm());
            AppendEntriesResult appendEntriesResult = new AppendEntriesResult();
            appendEntriesResult.setTerm(store.getCurrentTerm());
            appendEntriesResult.setSuccess(false);
            Message reply = event.reply(appendEntriesResult);
            send(reply);
          }
        }

        break;
      case APPEND_ENTRIES_RS:
        checkAndGetProposal(event)
            .ifPresent(
                proposal -> {
                  AppendEntriesResult result = (AppendEntriesResult) event.getContent();
                  if (result.getTerm() > proposal.getTerm()) {
                    logger.info("Current term is before result term");
                    stateTransformer.becomeFollower(result.getTerm());
                    return;
                  }
                  if (state == NodeState.LEADER && proposal.getTerm().equals(result.getTerm())) {
                    if (result.getSuccess()) {
                      Endpoint peer = event.getSender();
                      Integer peerNextIndex = nextIndex.get(peer);
                      nextIndex.put(
                          peer,
                          peerNextIndex + store.subLog(peerNextIndex, store.logSize()).size());
                      matchIndex.put(peer, nextIndex.getOrDefault(peer, 0) - 1);
                      logger.info(
                          "Receive append entries result, nextIndex = {} , matchIndex = {}",
                          nextIndex.get(peer),
                          matchIndex.get(peer));
                      for (int index = commitIndex + 1; index < store.logSize(); index++) {
                        Optional<LogEntry> log = store.log(index);
                        assert log.isPresent();
                        LogEntry logEntry = log.get();
                        if (logEntry.getTerm().equals(store.getCurrentTerm())) {
                          AtomicInteger counter = new AtomicInteger();
                          int indexCP = index;
                          peerIds.forEach(
                              (forEachPeer) -> {
                                int peerMatchIndex = matchIndex.getOrDefault(forEachPeer, -1);
                                if (peerMatchIndex > indexCP) {
                                  counter.incrementAndGet();
                                }
                              });
                          if (counter.get() > peerIds.size() / 2) {
                            commitIndex = index;
                          }
                        }
                      }
                    } else {
                      nextIndex.put(event.getSender(), nextIndex.get(event.getSender()) - 1);
                    }
                  }
                });
        break;
      case REQUEST_VOTE_RQ:
        RequestVoteArg requestVoteArg = (RequestVoteArg) event.getContent();
        Integer lastLogIndex = store.lastLogIndex();
        Integer lastLogTerm = store.log(lastLogIndex).orElse(LogEntry.emptyEntry()).getTerm();
        Optional<Endpoint> voteFor = store.getVoteFor();
        logger.info(
            "Receive RequestVote = {}, currentTerm = {} , votedFor = {} , log index/term = {}/{} ",
            requestVoteArg,
            store.getCurrentTerm(),
            voteFor,
            lastLogIndex,
            lastLogTerm);
        if (requestVoteArg.getTerm() > store.getCurrentTerm()) {
          logger.info("Current term is less than requestVote term, to become follower");
          stateTransformer.becomeFollower(requestVoteArg.getTerm());
        }
        if (store.getCurrentTerm().equals(requestVoteArg.getTerm())
                && (!voteFor.isPresent() || voteFor.get() == requestVoteArg.getCandidateId())
                && requestVoteArg.getLastLogTerm() > lastLogTerm
            || (requestVoteArg.getLastLogTerm().equals(lastLogTerm)
                && requestVoteArg.getLastLogIndex() >= lastLogIndex)) {
          store.voteFor(requestVoteArg.getCandidateId());
          setElectionResetEvent(Instant.now());
          RequestVoteResult requestVoteResult = new RequestVoteResult();
          requestVoteResult.setTerm(store.getCurrentTerm());
          requestVoteResult.setVoteGranted(true);
          send(event.reply(requestVoteResult));
        } else {
          RequestVoteResult requestVoteResult = new RequestVoteResult();
          requestVoteResult.setTerm(store.getCurrentTerm());
          requestVoteResult.setVoteGranted(false);
          send(event.reply(requestVoteResult));
        }
        break;
      case REQUEST_VOTE_RS:
        checkAndGetProposal(event)
            .ifPresent(
                proposal -> {
                  RequestVoteResult result = (RequestVoteResult) event.getContent();
                  if (result.getTerm() > proposal.getTerm()) {
                    logger.info("current term is before result term");
                    stateTransformer.becomeFollower(result.getTerm());
                    return;
                  }
                  if (result.getTerm().equals(proposal.getTerm())) {
                    if (result.getVoteGranted()) {
                      int votes = proposal.getCounter().incrementAndGet();
                      if (votes > peerIds.size() / 2) {
                        stateTransformer.becomeLeader();
                        logger.info("Node to become leader by {} vote.", votes);
                      }
                    }
                  }
                });
        break;
      default:
        logger.warn(" Receive unKnow message. the message is  {} ", event.getContent());
    }
  }

  public Optional<Proposal> checkAndGetProposal(Message event) {
    String id = event.getId();
    Proposal proposal = proposals.get(REQUEST_VOTE_RS);
    if (proposal == null) {
      logger.warn("Can not find proposal , the type is {} and the id is {} .", REQUEST_VOTE_RS, id);
      return Optional.empty();
    }
    if (!id.equals(proposal.getId())) {
      logger.warn(" proposal  {}  is behind  current {}", id, proposal.getId());
      return Optional.empty();
    }
    return Optional.of(proposal);
  }

  public void startElectionTimoutTimer() {
    int termStarted = store.getCurrentTerm();
    TimerTask task =
        (timeout) -> {
          if (state != NodeState.CANDIDATE && state != NodeState.FOLLOWER) {
            logger.info(
                "Election timer: the node state is {}, So we need to break this timeout", state);
            return;
          }
          if (termStarted != store.getCurrentTerm()) {
            logger.info(
                "Election timer: the term changed, from {} to {} ",
                termStarted,
                store.getCurrentTerm());
            return;
          }
          Instant now = Instant.now();
          if (now.compareTo(electionResetEvent.plusMillis(new Random().nextInt(150 + 1) + 150))
              > 0) {
            consensus.startLeaderElection();
          } else {
            timer.newTimeout(timeout.task(), 10, TimeUnit.MILLISECONDS);
          }
        };
    timer.newTimeout(task, 10, TimeUnit.MILLISECONDS);
  }

  public void startHeartbeatsTimer() {
    int termStarted = store.getCurrentTerm();
    TimerTask task =
        (timeout) -> {
          if (state != NodeState.LEADER) {
            logger.info("Node is not leader");
            timeout.cancel();
            return;
          }
          consensus.sendHeartbeats(termStarted);
          timer.newTimeout(timeout.task(), 10, TimeUnit.MILLISECONDS);
        };
    timer.newTimeout(task, 10, TimeUnit.MILLISECONDS);
  }

  public Integer getCommitIndex() {
    return commitIndex;
  }

  public void setCommitIndex(Integer commitIndex) {
    this.commitIndex = commitIndex;
  }

  public Integer getLastApplied() {
    return lastApplied;
  }

  public void setLastApplied(Integer lastApplied) {
    this.lastApplied = lastApplied;
  }

  public NodeState getState() {
    return state;
  }

  public void setState(NodeState state) {
    this.state = state;
  }

  public Instant getElectionResetEvent() {
    return electionResetEvent;
  }

  public void setElectionResetEvent(Instant electionResetEvent) {
    this.electionResetEvent = electionResetEvent;
  }

  public Map<Endpoint, Integer> getNextIndex() {
    return nextIndex;
  }

  public Map<Endpoint, Integer> getMatchIndex() {
    return matchIndex;
  }

  public List<Endpoint> getPeerIds() {
    return peerIds;
  }

  public Store getStore() {
    return store;
  }

  public void submitNewProposal(RaftCommand expectCommand, Proposal proposal) {
    this.proposals.put(expectCommand, proposal);
  }
}
