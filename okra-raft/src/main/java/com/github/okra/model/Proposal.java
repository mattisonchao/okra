package com.github.okra.model;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Proposal {
  private String id;
  private Integer term;
  private AtomicInteger counter;

  private Proposal() {}

  public static Proposal createProposal(Integer term) {
    Proposal proposal = new Proposal();
    proposal.setTerm(term);
    proposal.setId(UUID.randomUUID().toString());
    proposal.setCounter(new AtomicInteger());
    return proposal;
  }

  public Integer getTerm() {
    return term;
  }

  public void setTerm(Integer term) {
    this.term = term;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public AtomicInteger getCounter() {
    return counter;
  }

  public void setCounter(AtomicInteger counter) {
    this.counter = counter;
  }
}
