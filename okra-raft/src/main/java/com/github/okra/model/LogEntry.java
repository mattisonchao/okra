package com.github.okra.model;

import java.io.Serializable;

public class LogEntry implements Serializable {
  private Command command;
  private Integer term;

  public static LogEntry emptyEntry() {
    LogEntry logEntry = new LogEntry();
    logEntry.setTerm(-1);
    return logEntry;
  }

  public Command getCommand() {
    return command;
  }

  public void setCommand(Command command) {
    this.command = command;
  }

  public Integer getTerm() {
    return term;
  }

  public void setTerm(Integer term) {
    this.term = term;
  }

  @Override
  public String toString() {
    return "LogEntry{" + "command=" + command + ", term=" + term + '}';
  }
}
