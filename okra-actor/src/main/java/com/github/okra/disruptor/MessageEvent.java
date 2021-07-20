package com.github.okra.disruptor;

import com.github.okra.modal.Message;

public class MessageEvent {

    private Message message;

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
