package com.github.okra;

public class Consensus {
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
}
