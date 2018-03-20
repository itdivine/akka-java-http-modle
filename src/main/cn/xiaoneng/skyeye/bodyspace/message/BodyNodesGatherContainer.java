package cn.xiaoneng.skyeye.bodyspace.message;

import akka.actor.ActorRef;
import com.alibaba.fastjson.JSONArray;

/**
 * Created by liangyongheng on 2016/11/16 17:15.
 */
public class BodyNodesGatherContainer {

    private String msgid;

    private int count;

    private ActorRef sender;
    private JSONArray info = new JSONArray();


    public BodyNodesGatherContainer(String msgid, int count, ActorRef sender) {
        this.msgid = msgid;
        this.count = count;
        this.sender = sender;
    }

    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public ActorRef getSender() {
        return sender;
    }

    public void setSender(ActorRef sender) {
        this.sender = sender;
    }

    public JSONArray getInfo() {
        return info;
    }

    public void setInfo(JSONArray info) {
        this.info = info;
    }


    //    public JSONArray toJsonArray() {
//        JSONArray result = new JSONArray();
//        if (this.getInfo() == null || this.getInfo().size() == 0) {
//            return result;
//        }
//        for (JSONObject msg : getInfo()) {
//
//            result.add(msg);
//        }
//        return result;
//    }
//
//    public JSONObject toJSonObject() {
//        if (getInfo().size() == 0) {
//            return new JSONObject();
//        } else {
//            StringBuffer buffer = new StringBuffer();
//            for (JSONObject json : getInfo()) {
//                buffer.append(json.toJSONString());
//                System.out.println(json.toJSONString());
//            }
//            return JSONObject.parseObject(buffer.toString());
//        }
//    }
}
