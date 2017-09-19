package cn.xiaoneng.skyeye.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Administrator on 2016/7/21.
 */
public abstract class BaseMessage implements Serializable {


    // 生成策略一：UUID   策略二：业务ID+时间戳 (AKKA推荐方案二)
    protected String msgId;    // 唯一标识，应用程序自定义。
    protected String operation;// 自定义操作：CREATE GET DELETE UPDATE LIST
    protected int version;     // 消息版本号
    protected long timeToLive; // 有效期
    protected long createTime = System.currentTimeMillis();    // 创建时间
    protected boolean requestAck;

    public BaseMessage(){this.msgId = UUID.randomUUID().toString();}

    public BaseMessage(String msgId, long timeToLive){
        this.msgId = msgId;
        this.timeToLive = timeToLive;
        this.version = 1;
    }

    public BaseMessage(String msgId, String operation, long timeToLive) {

        if(null == msgId || "".equals(msgId)) {
            this.msgId = UUID.randomUUID().toString();
        } else {
            this.msgId = msgId;
        }
        this.operation = operation;
        this.timeToLive = timeToLive;
        this.version = 1;
    }

    /**
     * 是否过期
     */
    public boolean isExpired() {

        long elapsed = System.currentTimeMillis() - createTime;
        return elapsed > timeToLive;
    }

    @Override
    public String toString() {
        return "BaseMessage{" +
                "msgId='" + msgId + '\'' +
                ", operation='" + operation + '\'' +
                ", version=" + version +
                ", timeToLive=" + timeToLive +
                ", createTime=" + createTime +
                '}';
    }

    public JSONObject toJson() {
        return (JSONObject) JSON.toJSON(this);
    }

    public void setRequestAck(boolean requestAck) {
        this.requestAck = requestAck;
    }

    public boolean isRequestAck() {
        return requestAck;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getMsgId() {
        return msgId;
    }

    public String getOperation() {
        return operation;
    }

    public int getVersion() {
        return version;
    }

    public long getTimeToLive() {
        return timeToLive;
    }

    public long getCreateTime() {
        return createTime;
    }
}
