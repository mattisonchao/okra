package com.github.okra.evn;

import com.github.okra.model.AppendEntriesArg;
import com.github.okra.model.RequestVoteArg;
import java.util.Arrays;
import java.util.Optional;

public enum RaftCommand {
  APPEND_ENTRIES(AppendEntriesArg.class),
  REQUEST_VOTE(RequestVoteArg.class),
  NOT_COMMAND(null);

  private final Class<?> klass;

  RaftCommand(Class<?> klass) {
    this.klass = klass;
  }

  public static RaftCommand getRaftCommand(Object obj) {
    Optional<RaftCommand> raftCommandOptional =
        Arrays.stream(RaftCommand.values())
            .filter(command -> command.klass == obj.getClass())
            .findFirst();
    return raftCommandOptional.orElse(NOT_COMMAND);
  }
}
