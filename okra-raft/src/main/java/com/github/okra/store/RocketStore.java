package com.github.okra.store;

import com.github.okra.model.LogEntry;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;

public class RocketStore implements Store {


  @Override
  public Integer getCurrentTerm() {
    return null;
  }

  @Override
  public Integer setCurrentTerm(Integer term) {
    return null;
  }

  @Override
  public Optional<InetSocketAddress> getVoteFor() {
    return Optional.empty();
  }

  @Override
  public void voteFor(InetSocketAddress candidate) {}

  @Override
  public void clearVoteFor() {}

  @Override
  public void addLog(LogEntry logEntry) {}

  @Override
  public void addLogs(List<LogEntry> logEntries) {}

  @Override
  public Optional<LogEntry> log(Integer index) {
    return Optional.empty();
  }

  @Override
  public List<LogEntry> logs() {
    return null;
  }

  @Override
  public List<LogEntry> subLog(Integer startIndex, Integer endIndex) {
    return null;
  }

  @Override
  public Integer logSize() {
    return null;
  }

  @Override
  public Integer lastLogIndex() {
    return null;
  }
}
