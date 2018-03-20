package cn.xiaoneng.skyeye.track.message;

import akka.actor.ActorRef;
import cn.xiaoneng.skyeye.bodyspace.message.BodyNodeMsg;
import cn.xiaoneng.skyeye.util.BaseMessage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xuyang on 2016/8/10.
 */
public class CreateRecordMessage extends BaseMessage implements Serializable {

//    private ActorRef navNode;

    private String navId;

    private Map<String, String> bodyNodeInfo = new HashMap<>();

    private Map<String, Object> otherInfo = new HashMap<String, Object>();

    /*public CreateRecordMessage(ActorRef navNode, Map<String, Map<String, String>> bodyNodeInfo, Map<String, String> otherInfo) {
        this.navNode = navNode;
        this.bodyNodeInfo = bodyNodeInfo;
        this.otherInfo = otherInfo;
    }*/

    public CreateRecordMessage(String navId, Map<String, String> bodyNodeInfo, Map<String, Object> otherInfo) {
        this.navId = navId;
        this.bodyNodeInfo = bodyNodeInfo;
        this.otherInfo = otherInfo;
    }

//    public ActorRef getNavNode() {
//        return navNode;
//    }
//    public void setNavNode(ActorRef navNode) {
//        this.navNode = navNode;
//    }


    public String getNavId() {
        return navId;
    }

    public void setNavId(String navId) {
        this.navId = navId;
    }

    public Map<String, String> getBodyNodeInfo() {
        return bodyNodeInfo;
    }

    public Map<String, Object> getOtherInfo() {
        return otherInfo;
    }

    public void setBodyNodeInfo(Map<String, String> bodyNodeInfo) {
        this.bodyNodeInfo = bodyNodeInfo;
    }

    public void setOtherInfo(Map<String, Object> otherInfo) {
        this.otherInfo = otherInfo;
    }

    @Override
    public String toString() {
        return "CreateRecordMessage{" +
//                "navNode=" + navNode +
                ", bodyNodeInfo=" + bodyNodeInfo +
                ", otherInfo=" + otherInfo + super.toString() +
                '}';
    }
}
