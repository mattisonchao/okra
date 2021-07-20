package com.github.okra;

import com.github.okra.modal.Message;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import java.util.UUID;
import java.util.concurrent.Executors;

public class MailBox {

    private final Disruptor<Message> disruptor = new Disruptor<>(
            Message::new,
            1024 * 1024,
            Executors.defaultThreadFactory(),
            ProducerType.SINGLE,
            new YieldingWaitStrategy());

    public MailBox(EventHandler<Message> handler) {
        disruptor.handleEventsWith(handler);
        disruptor.start();
    }

    public void putMessage(Object data) {
        RingBuffer<Message> ringBuffer = disruptor.getRingBuffer();
        ringBuffer.publishEvent((message, sequence) -> {
            message.setId(UUID.randomUUID().toString());
            message.setData(data);
        });
    }

}
