package com.github.okra.model;

public class LogEntry {
  private Command command;
  private Integer term;

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
