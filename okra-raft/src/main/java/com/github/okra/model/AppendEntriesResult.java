package com.github.okra.model;

public class AppendEntriesResult {
  private Integer term;
  private Boolean success;

  public Integer getTerm() {
    return term;
  }

  public void setTerm(Integer term) {
    this.term = term;
  }

  public Boolean getSuccess() {
    return success;
  }

  public void setSuccess(Boolean success) {
    this.success = success;
  }

  @Override
  public String toString() {
    return "AppendEntriesResult{" + "term=" + term + ", success=" + success + '}';
  }
}
