package com.github.okra.store;

import com.github.okra.modal.Endpoint;
import com.github.okra.model.LogEntry;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;

public interface Store {
  Integer getCurrentTerm();

  Integer setCurrentTerm(Integer term);

  Optional<Endpoint> getVoteFor();

  void voteFor(Endpoint candidate);

  void clearVoteFor();

  void addLog(LogEntry logEntry);

  void addLogs(List<LogEntry> logEntries);

  Optional<LogEntry> log(Integer index);

  List<LogEntry> logs();

  List<LogEntry> subLog(Integer startIndex, Integer endIndex);

  Integer logSize();

  Integer lastLogIndex();
}
