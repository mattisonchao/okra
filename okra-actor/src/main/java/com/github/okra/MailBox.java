package com.github.okra;

import com.github.okra.disruptor.MessageEvent;
import com.github.okra.modal.Message;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.Executors;

public class MailBox {

    private final Disruptor<MessageEvent> disruptor = new Disruptor<>(
            MessageEvent::new,
            1024 * 1024,
            Executors.defaultThreadFactory(),
            ProducerType.SINGLE,
            new YieldingWaitStrategy());

    public MailBox(EventHandler<MessageEvent> handler) {
        disruptor.handleEventsWith(handler);
        disruptor.start();
    }

    public void putMessage(Message message) {
        RingBuffer<MessageEvent> ringBuffer = disruptor.getRingBuffer();
        ringBuffer.publishEvent((messageEvent, sequence) -> {
            messageEvent.setMessage(message);
        });
    }

}
