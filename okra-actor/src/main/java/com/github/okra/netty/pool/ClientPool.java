package com.github.okra.netty.pool;

import com.github.okra.netty.codec.Decoder;
import com.github.okra.netty.codec.Encoder;
import com.github.okra.netty.serilizer.HessianSerialize;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutionException;

public class ClientPool {
  private final EventLoopGroup group = new NioEventLoopGroup();
  private final Bootstrap strap =
      new Bootstrap()
          .group(group)
          .channel(NioSocketChannel.class)
          .option(ChannelOption.TCP_NODELAY, true)
          .option(ChannelOption.SO_KEEPALIVE, true);
  private final ChannelPoolMap<InetSocketAddress, FixedChannelPool> poolMap =
      new AbstractChannelPoolMap<InetSocketAddress, FixedChannelPool>() {
        @Override
        protected FixedChannelPool newPool(InetSocketAddress key) {
          return new FixedChannelPool(
              strap.remoteAddress(key),
              new ChannelPoolHandler() {
                @Override
                public void channelReleased(Channel ch) throws Exception {
                  ch.writeAndFlush(Unpooled.EMPTY_BUFFER);
                }

                @Override
                public void channelAcquired(Channel ch) throws Exception {}

                @Override
                public void channelCreated(Channel ch) throws Exception {
                  SocketChannel channel = (SocketChannel) ch;
                  channel.config().setKeepAlive(true);
                  channel.config().setTcpNoDelay(true);
                  channel.config().setConnectTimeoutMillis(5000);
                  channel
                      .pipeline()
                      .addLast(Encoder.getEncoder(new HessianSerialize()))
                      .addLast(Decoder.getDecoder(new HessianSerialize()));
                }
              },
              Integer.MAX_VALUE);
        }
      };

  public Channel getChannel(InetSocketAddress address) {
    Future<Channel> channelFuture = poolMap.get(address).acquire();
    try {
      return channelFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
      return null;
    }
  }

  public void release(Channel ch) {
    ch.flush();
    InetSocketAddress socketAddress = (InetSocketAddress) ch.remoteAddress();
    poolMap.get(socketAddress).release(ch);
  }
}
