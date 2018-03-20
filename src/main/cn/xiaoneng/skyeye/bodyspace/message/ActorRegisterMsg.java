package cn.xiaoneng.skyeye.bodyspace.message;

import akka.actor.ActorRef;

/**
 * Created by liangyongheng on 2016/8/2 14:55.
 */
public class ActorRegisterMsg {

    private String path;

    private ActorRef actor;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ActorRef getActor() {
        return actor;
    }

    public void setActor(ActorRef actor) {
        this.actor = actor;
    }
}
