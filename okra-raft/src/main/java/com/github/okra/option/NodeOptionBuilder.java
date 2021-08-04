package com.github.okra.option;

import com.github.okra.modal.Endpoint;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public final class NodeOptionBuilder {
  private Endpoint self;
  private List<Endpoint> peers = new ArrayList<>();

  private NodeOptionBuilder() {}

  public static NodeOptionBuilder getBuilder() {
    return new NodeOptionBuilder();
  }

  public NodeOptionBuilder self(String self) {
    this.self = parseToNetSocket(self);
    return this;
  }

  public NodeOptionBuilder peer(String peer) {
    this.peers.add(parseToNetSocket(peer));
    return this;
  }

  public NodeOption build() {
    NodeOption nodeOption = new NodeOption();
    nodeOption.setSelf(self);
    nodeOption.setPeers(peers);
    return nodeOption;
  }

  private Endpoint parseToNetSocket(String address) {
    try {
      URI uri = new URI("okra://" + address);
      return Endpoint.of(uri.getHost(), uri.getPort());
    } catch (URISyntaxException e) {
      e.printStackTrace();
      throw new RuntimeException("Wrong address");
    }
  }
}
