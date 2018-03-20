package cn.xiaoneng.skyeye.navigation.message;

import akka.actor.ActorRef;
import cn.xiaoneng.skyeye.navigation.bean.NavNodeInfo;
import cn.xiaoneng.skyeye.util.BaseMessage;

import java.io.Serializable;

/**
 * 导航节点创建BackNavNodeMsg
 * 返回给 NavigationPVRouter
 *
 *
 * Created by xuyang on 2016/8/10.
 */
public class BackNavNodeMsg extends BaseMessage implements Serializable {

    private ActorRef navNodeRef;
    private NavNodeInfo navNodeInfo;
    private int data_status;

    public BackNavNodeMsg(String msgId, NavNodeInfo navNodeInfo, int data_status, ActorRef navNodeRef, long timeToLive) {
        super(msgId, null, timeToLive);
        this.navNodeInfo = navNodeInfo;
        this.navNodeRef = navNodeRef;
        this.data_status = data_status;
    }

    public ActorRef getNavNodeRef() {
        return navNodeRef;
    }

    public void setNavNodeRef(ActorRef navNodeRef) {
        this.navNodeRef = navNodeRef;
    }

    public int getData_status() {
        return data_status;
    }

    public void setData_status(int data_status) {
        this.data_status = data_status;
    }

    public NavNodeInfo getNavNodeInfo() {
        return navNodeInfo;
    }

    @Override
    public String toString() {
        return "BackNavNodeMsg{" +
                "navNodeRef=" + navNodeRef +
                ", navNodeInfo='" + navNodeInfo + '\'' + super.toString() +
                '}';
    }
}
