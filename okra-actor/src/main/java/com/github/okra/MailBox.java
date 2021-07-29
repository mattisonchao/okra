package com.github.okra;

import com.github.okra.disruptor.MessageEvent;
import com.github.okra.modal.Message;
import com.github.okra.netty.codec.Decoder;
import com.github.okra.netty.codec.Encoder;
import com.github.okra.netty.serilizer.HessianSerialize;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.util.concurrent.Executors;

public class MailBox {
  private final EventLoopGroup boss = new NioEventLoopGroup();
  private final EventLoopGroup worker = new NioEventLoopGroup();
  private final ServerBootstrap bootstrap = new ServerBootstrap();

  private final Disruptor<MessageEvent> disruptor =
      new Disruptor<>(
          MessageEvent::new,
          1024 * 1024,
          Executors.defaultThreadFactory(),
          ProducerType.SINGLE,
          new YieldingWaitStrategy());

  public MailBox(EventHandler<MessageEvent> handler, Integer port, Runnable afterStart) {
    disruptor.handleEventsWith(handler);
    disruptor.start();
    bootstrap
        .group(boss, worker)
        .channel(NioServerSocketChannel.class)
        .option(ChannelOption.SO_BACKLOG, 1024)
        .childHandler(
            new ChannelInitializer<SocketChannel>() {
              @Override
              protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline()
                    .addLast(Encoder.getEncoder(new HessianSerialize()))
                    .addLast(Decoder.getDecoder(new HessianSerialize()))
                    .addLast(
                        new SimpleChannelInboundHandler<Message>() {
                          @Override
                          protected void channelRead0(ChannelHandlerContext ctx, Message msg)
                              throws Exception {
                            putMessage(msg);
                          }
                        });
              }
            });
    bootstrap
        .bind(port)
        .addListener(
            (future) -> {
              if (future.isSuccess()) {
                afterStart.run();
              }
            });
  }

  public void putMessage(Message message) {
    RingBuffer<MessageEvent> ringBuffer = disruptor.getRingBuffer();
    ringBuffer.publishEvent(
        (messageEvent, sequence) -> {
          messageEvent.setMessage(message);
        });
  }

  public void destroy() {
    boss.shutdownGracefully();
    worker.shutdownGracefully();
  }
}
