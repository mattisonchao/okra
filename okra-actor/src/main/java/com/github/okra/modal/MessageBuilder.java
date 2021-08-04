package com.github.okra.modal;

import java.util.UUID;

public final class MessageBuilder {
  private String id;
  private Endpoint sender;
  private Endpoint receiver;
  private Object content;

  private MessageBuilder() {}

  public static MessageBuilder create() {
    return new MessageBuilder();
  }

  public MessageBuilder randomId() {
    this.id = UUID.randomUUID().toString();
    return this;
  }

  public MessageBuilder id(String id) {
    this.id = id;
    return this;
  }

  public MessageBuilder sender(Endpoint sender) {
    this.sender = sender;
    return this;
  }

  public MessageBuilder receiver(Endpoint receiver) {
    this.receiver = receiver;
    return this;
  }

  public MessageBuilder content(Object content) {
    this.content = content;
    return this;
  }

  public Message build() {
    Message message = new Message();
    message.setId(id);
    message.setSender(sender);
    message.setReceiver(receiver);
    message.setContent(content);
    return message;
  }
}
