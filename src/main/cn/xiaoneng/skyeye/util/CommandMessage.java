package cn.xiaoneng.skyeye.util;


import akka.actor.ActorRef;

import java.io.Serializable;

/**
 * Created by xuyang on 2016/8/5.
 */
public class CommandMessage extends BaseMessage implements Serializable {


    private ActorRef callback;

    public CommandMessage(String operation, long timeToLive, String msgId) {

        super(msgId, operation, timeToLive);
    }

    public CommandMessage(String operation, long timeToLive, String msgId, ActorRef callback) {

        super(msgId, operation, timeToLive);
        this.callback = callback;
    }

    public ActorRef getCallback() {
        return callback;
    }

    public void setCallback(ActorRef callback) {
        this.callback = callback;
    }
}
