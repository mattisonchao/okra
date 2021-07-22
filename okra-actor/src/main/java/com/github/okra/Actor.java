package com.github.okra;

import com.github.okra.disruptor.MessageEvent;
import com.github.okra.modal.Message;
import com.github.okra.netty.pool.ClientPool;
import com.lmax.disruptor.EventHandler;
import io.netty.channel.Channel;
import java.net.InetSocketAddress;

public abstract class Actor implements EventHandler<MessageEvent> {

  private final ClientPool clientPool = new ClientPool();
  protected Integer port = 7878;
  MailBox mailBox = null;

  public abstract void preStart();

  public void deploy() {
    preStart();
    new MailBox(this, port);
  }

  public void send(InetSocketAddress receiver, Message message) {
    Channel channel = clientPool.getChannel(receiver);
    channel
        .writeAndFlush(message)
        .addListener(
            (event) -> {
              if (event.isSuccess()) {
                clientPool.release(channel);
              }
            });
  }

  @Override
  public void onEvent(MessageEvent event, long sequence, boolean endOfBatch) throws Exception {
    receive(event.getMessage());
  }

  public abstract void receive(Message event);

  public void destroy() {
    mailBox.destroy();
  }
}
