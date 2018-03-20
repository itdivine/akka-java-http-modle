package cn.xiaoneng.skyeye.enterprise.message;

import akka.actor.ActorRef;
import cn.xiaoneng.skyeye.util.BaseMessage;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/8/1.
 */
public class IsRegistMessage extends BaseMessage implements Serializable {


    private String path;

    private ActorRef evsRef;

    private boolean isRegist = true; // true: 注册  false:取消注册

    public boolean isRegist() {
        return isRegist;
    }

    public String getPath() {
        return path;
    }

    public ActorRef getEvsRef() {
        return evsRef;
    }

    public IsRegistMessage(boolean isRegist, String path, ActorRef evsRef, long timeToLive) {
        this.evsRef = evsRef;
        this.path = path;
        this.isRegist = isRegist;
    }
}
