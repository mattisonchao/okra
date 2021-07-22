package com.github.okra.modal;

import java.io.Serializable;

public class Message implements Serializable {

  private String id;
  private Object data;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Object getData() {
    return data;
  }

  public void setData(Object data) {
    this.data = data;
  }

  @Override
  public String toString() {
    return "Message{" + "id='" + id + '\'' + ", data=" + data + '}';
  }
}
