package com.github.okra.netty.codec;

import com.github.okra.modal.Message;
import com.github.okra.netty.serilizer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class Decoder extends ByteToMessageDecoder {

    private Serializer serializer;

    private Decoder() {
    }

    public static ByteToMessageDecoder getDecoder(Serializer serializer) {
        Decoder decoder = new Decoder();
        decoder.setSerializer(serializer);
        return decoder;
    }

    private Serializer getSerializer() {
        return serializer;
    }

    private void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4) {
            return;
        }
        in.markReaderIndex();
        int length = in.readInt();
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        out.add(serializer.deserialize(bytes, Message.class));
    }
}
