package cn.xiaoneng.skyeye.bodyspace.message;

import akka.actor.ActorRef;
import cn.xiaoneng.skyeye.util.PVMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liangyongheng on 2016/8/3 17:03.
 */
public class BodyNodeFulFillment {
    private String msgid;

    private int count;

    private ActorRef sender;
    private List<PVResultMsg> info = new ArrayList<>();

    private String loginId;
    private String nt_id;
    private String cookieId;
    private String imei;
    private String dvid;

    private PVMessage message;

    public BodyNodeFulFillment(String msgid, ActorRef sender, PVMessage message, int count) {
        this.msgid = msgid;
        this.sender = sender;
        this.message = message;
        this.count = count;
    }

    public PVMessage getMessage() {
        return message;
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

    public List<PVResultMsg> getInfo() {
        return info;
    }

    public void setInfo(List<PVResultMsg> info) {
        this.info = info;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getNt_id() {
        return nt_id;
    }

    public void setNt_id(String nt_id) {
        this.nt_id = nt_id;
    }

    public String getCookieId() {
        return cookieId;
    }

    public void setCookieId(String cookieId) {
        this.cookieId = cookieId;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getDvid() {
        return dvid;
    }

    public void setDvid(String dvid) {
        this.dvid = dvid;
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
