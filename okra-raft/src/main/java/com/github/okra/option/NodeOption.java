package com.github.okra.option;

import java.net.InetSocketAddress;
import java.util.List;

public class NodeOption {
  private InetSocketAddress self;
  private List<InetSocketAddress> peers;

  public InetSocketAddress getSelf() {
    return self;
  }

  public void setSelf(InetSocketAddress self) {
    this.self = self;
  }

  public List<InetSocketAddress> getPeers() {
    return peers;
  }

  public void setPeers(List<InetSocketAddress> peers) {
    this.peers = peers;
  }
}
