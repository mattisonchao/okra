package com.github.okra.model;

public class AppendEntriesResult {
  private Integer term;
  private Integer success;

  public Integer getTerm() {
    return term;
  }

  public void setTerm(Integer term) {
    this.term = term;
  }

  public Integer getSuccess() {
    return success;
  }

  public void setSuccess(Integer success) {
    this.success = success;
  }

  @Override
  public String toString() {
    return "AppendEntriesResult{" + "term=" + term + ", success=" + success + '}';
  }
}
