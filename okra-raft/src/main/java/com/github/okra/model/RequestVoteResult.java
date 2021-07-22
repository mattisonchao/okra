package com.github.okra.model;

public class RequestVoteResult {
  private Integer term;
  private Boolean voteGranted;

  public Integer getTerm() {
    return term;
  }

  public void setTerm(Integer term) {
    this.term = term;
  }

  public Boolean getVoteGranted() {
    return voteGranted;
  }

  public void setVoteGranted(Boolean voteGranted) {
    this.voteGranted = voteGranted;
  }

  @Override
  public String toString() {
    return "RequestVoteResult{" + "term=" + term + ", voteGranted=" + voteGranted + '}';
  }
}
