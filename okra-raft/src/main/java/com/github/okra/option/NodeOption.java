package com.github.okra.option;

import com.github.okra.modal.Endpoint;
import java.net.InetSocketAddress;
import java.util.List;

public class NodeOption {
  private Endpoint self;
  private List<Endpoint> peers;

  public Endpoint getSelf() {
    return self;
  }

  public void setSelf(Endpoint self) {
    this.self = self;
  }

  public List<Endpoint> getPeers() {
    return peers;
  }

  public void setPeers(List<Endpoint> peers) {
    this.peers = peers;
  }
}
