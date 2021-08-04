package com.github.okra.model;

import com.github.okra.modal.Endpoint;
import java.io.Serializable;
import java.net.InetSocketAddress;

public class RequestVoteArg implements Serializable {
  private Integer term;
  private Endpoint candidateId;
  private Integer lastLogIndex;
  private Integer lastLogTerm;

  public RequestVoteArg(
      Integer term, Endpoint candidateId, Integer lastLogIndex, Integer lastLogTerm) {
    this.term = term;
    this.candidateId = candidateId;
    this.lastLogIndex = lastLogIndex;
    this.lastLogTerm = lastLogTerm;
  }

  public Integer getTerm() {
    return term;
  }

  public void setTerm(Integer term) {
    this.term = term;
  }

  public Endpoint getCandidateId() {
    return candidateId;
  }

  public void setCandidateId(Endpoint candidateId) {
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
