package com.github.okra;

import com.github.okra.disruptor.MessageEvent;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;

public abstract class Actor implements EventHandler<MessageEvent>, WorkHandler<MessageEvent> {

  public final MailBox mailBox = new MailBox(this);

  abstract void preStart();

  public void send() {}

  @Override
  public void onEvent(MessageEvent event) throws Exception {
    receive(event);
  }

  abstract void receive(MessageEvent event);
}
