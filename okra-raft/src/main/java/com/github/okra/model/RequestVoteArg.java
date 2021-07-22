package com.github.okra.model;

import java.net.InetSocketAddress;

public class RequestVoteArg {
  private Integer term;
  private InetSocketAddress candidateId;
  private Integer lastLogIndex;
  private Integer lastLogTerm;

  public Integer getTerm() {
    return term;
  }

  public void setTerm(Integer term) {
    this.term = term;
  }

  public InetSocketAddress getCandidateId() {
    return candidateId;
  }

  public void setCandidateId(InetSocketAddress candidateId) {
    this.candidateId = candidateId;
  }

  public Integer getLastLogIndex() {
    return lastLogIndex;
  }

  public void setLastLogIndex(Integer lastLogIndex) {
    this.lastLogIndex = lastLogIndex;
  }

  public Integer getLastLogTerm() {
    return lastLogTerm;
  }

  public void setLastLogTerm(Integer lastLogTerm) {
    this.lastLogTerm = lastLogTerm;
  }

  @Override
  public String toString() {
    return "RequestVoteArg{"
        + "term="
        + term
        + ", candidateId="
        + candidateId
        + ", lastLogIndex="
        + lastLogIndex
        + ", lastLogTerm="
        + lastLogTerm
        + '}';
  }
}
