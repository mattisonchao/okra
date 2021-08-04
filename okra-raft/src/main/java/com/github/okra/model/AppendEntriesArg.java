package com.github.okra.model;

import com.github.okra.modal.Endpoint;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.List;

public class AppendEntriesArg implements Serializable {
  private Integer term;
  private Endpoint leaderId;
  private Integer prevLogIndex;
  private Integer prevLogTerm;
  private List<LogEntry> entries;
  private Integer leaderCommit;

  public Integer getTerm() {
    return term;
  }

  public void setTerm(Integer term) {
    this.term = term;
  }

  public Endpoint getLeaderId() {
    return leaderId;
  }

  public void setLeaderId(Endpoint leaderId) {
    this.leaderId = leaderId;
  }

  public Integer getPrevLogIndex() {
    return prevLogIndex;
  }

  public void setPrevLogIndex(Integer prevLogIndex) {
    this.prevLogIndex = prevLogIndex;
  }

  public Integer getPrevLogTerm() {
    return prevLogTerm;
  }

  public void setPrevLogTerm(Integer prevLogTerm) {
    this.prevLogTerm = prevLogTerm;
  }

  public List<LogEntry> getEntries() {
    return entries;
  }

  public void setEntries(List<LogEntry> entries) {
    this.entries = entries;
  }

  public Integer getLeaderCommit() {
    return leaderCommit;
  }

  public void setLeaderCommit(Integer leaderCommit) {
    this.leaderCommit = leaderCommit;
  }

  @Override
  public String toString() {
    return "AppendEntriesArg{"
        + "term="
        + term
        + ", leaderId="
        + leaderId
        + ", prevLogIndex="
        + prevLogIndex
        + ", prevLogTerm="
        + prevLogTerm
        + ", entries="
        + entries
        + ", leaderCommit="
        + leaderCommit
        + '}';
  }
}
