package com.github.okra;

import com.github.okra.disruptor.MessageEvent;
import com.github.okra.modal.Message;
import com.github.okra.netty.pool.ClientPool;
import com.lmax.disruptor.EventHandler;
import io.netty.channel.Channel;
import java.net.InetSocketAddress;

public abstract class Actor implements EventHandler<MessageEvent> {

  private final ClientPool clientPool = new ClientPool();
  private InetSocketAddress id = new InetSocketAddress("127.0.0.1", 9981);
  MailBox mailBox = null;

  protected abstract void preStart();

  public void deploy() {
    preStart();
    MailBox mailBox = new MailBox(this, id.getPort(), this::afterStart);
  }

  protected abstract void afterStart();

  protected void send(Message message) {
    Channel channel = clientPool.getChannel(message.getReceiver());
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

  protected abstract void receive(Message event);

  public void destroy() {
    mailBox.destroy();
  }

  public InetSocketAddress getId() {
    return id;
  }

  public void setId(InetSocketAddress id) {
    this.id = id;
  }
}
