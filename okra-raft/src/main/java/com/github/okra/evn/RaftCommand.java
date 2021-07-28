package com.github.okra.evn;

import com.github.okra.model.AppendEntriesArg;
import com.github.okra.model.AppendEntriesResult;
import com.github.okra.model.RequestVoteArg;
import com.github.okra.model.RequestVoteResult;
import java.util.Arrays;
import java.util.Optional;

public enum RaftCommand {
  APPEND_ENTRIES_RQ(AppendEntriesArg.class),
  APPEND_ENTRIES_RS(AppendEntriesResult.class),
  REQUEST_VOTE_RQ(RequestVoteArg.class),
  REQUEST_VOTE_RS(RequestVoteResult.class),
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
