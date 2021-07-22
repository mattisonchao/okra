package com.github.okra;

import com.github.okra.evn.NodeState;
import com.github.okra.evn.RaftCommand;
import com.github.okra.modal.Message;
import com.github.okra.store.RocketStore;
import com.github.okra.store.Store;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Node extends Actor {
  private static final Logger logger = LoggerFactory.getLogger(Node.class);
  private final Consensus consensus = Consensus.create(this);
  public final Store store = new RocketStore();
  private volatile Integer commitIndex = 0;
  private volatile Integer lastApplied = 0;
  private volatile NodeState state = NodeState.FOLLOWER;
  private final Map<InetSocketAddress, Integer> nextIndex = new ConcurrentHashMap<>();
  private final Map<InetSocketAddress, Integer> matchIndex = new ConcurrentHashMap<>();
  private final List<InetSocketAddress> peerIds = new ArrayList<>();
  private InetSocketAddress id = new InetSocketAddress("127.0.0.1", 9981);

  @Override
  public void preStart() {}

  @Override
  public void receive(Message event) {
    switch (RaftCommand.getRaftCommand(event.getData())) {
      case APPEND_ENTRIES:
        break;
      case REQUEST_VOTE:
        break;
      default:
        logger.warn(" Receive unKnow message. the message is  {} ", event.getData());
    }
  }
}
