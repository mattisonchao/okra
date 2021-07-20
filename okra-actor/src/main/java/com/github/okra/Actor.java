package com.github.okra;

import com.github.okra.modal.Message;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;


public abstract class Actor implements EventHandler<Message>, WorkHandler<Message> {

    public final MailBox mailBox = new MailBox(this);

    abstract void preStart();

    public void send() {

    }

    @Override
    public void onEvent(Message event) throws Exception {
        receive(event);
    }

    abstract void receive(Message event);

}
