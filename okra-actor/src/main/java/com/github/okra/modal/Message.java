package com.github.okra.modal;

import java.io.Serializable;
import java.net.InetSocketAddress;

/** message */
public class Message implements Serializable {

  private String id;
  private InetSocketAddress sender;
  private InetSocketAddress receiver;
  private Object content;

  public InetSocketAddress getSender() {
    return sender;
  }

  public void setSender(InetSocketAddress sender) {
    this.sender = sender;
  }

  public InetSocketAddress getReceiver() {
    return receiver;
  }

  public void setReceiver(InetSocketAddress receiver) {
    this.receiver = receiver;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Object getContent() {
    return content;
  }

  public void setContent(Object content) {
    this.content = content;
  }

  @Override
  public String toString() {
    return "Message{" + "id='" + id + '\'' + ", data=" + data + '}';
  }

}
