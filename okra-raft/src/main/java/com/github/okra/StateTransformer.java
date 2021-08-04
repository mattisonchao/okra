package com.github.okra;

import com.github.okra.evn.NodeState;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateTransformer {
  private static final Logger logger = LoggerFactory.getLogger(StateTransformer.class);

  private StateTransformer() {}

  private Node node;

  public static StateTransformer of(Node node) {
    StateTransformer stateTransformer = new StateTransformer();
    stateTransformer.setNode(node);
    return stateTransformer;
  }

  public Node getNode() {
    return node;
  }

  public void setNode(Node node) {
    this.node = node;
  }

  public void becomeFollower(Integer term) {
    node.setState(NodeState.FOLLOWER);
    node.getStore().setCurrentTerm(term);
    node.getStore().clearVoteFor();
    node.setElectionResetEvent(Instant.now());
    logger.info(
        "Node become follower,  the term is {}, the log is {}",
        node.getStore().getCurrentTerm(),
        node.getStore().logs());
    node.startElectionTimoutTimer();
  }

  public void becomeLeader() {
    node.setState(NodeState.LEADER);
    node.getPeerIds()
        .forEach(
            peer -> {
              node.getNextIndex().put(peer, node.getStore().logSize());
              node.getMatchIndex().put(peer, -1);
            });
    logger.info(
        "Node become leader, the term={}, nextIndex={}, matchIndex= {} log={}",
        node.getStore().getCurrentTerm(),
        node.getNextIndex(),
        node.getMatchIndex(),
        node.getStore().logs());
    node.startHeartbeatsTimer();
  }

}
