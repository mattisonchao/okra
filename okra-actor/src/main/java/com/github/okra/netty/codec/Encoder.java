package com.github.okra.netty.codec;

import com.github.okra.modal.Message;
import com.github.okra.netty.serilizer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class Encoder extends MessageToByteEncoder<Message> {
  private Serializer serializer;

  private Encoder() {}

  private Serializer getSerializer() {
    return serializer;
  }

  private void setSerializer(Serializer serializer) {
    this.serializer = serializer;
  }

  public static MessageToByteEncoder<Message> getEncoder(Serializer serializer) {
    Encoder encoder = new Encoder();
    encoder.setSerializer(serializer);
    return encoder;
  }

  @Override
  protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
    byte[] bytes = serializer.serialize(msg);
    out.writeInt(bytes.length);
    out.writeBytes(bytes);
  }
}
