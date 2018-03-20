package cn.xiaoneng.skyeye.track.message;

import cn.xiaoneng.skyeye.util.BaseMessage;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/23.
 */
public class KPIMessage extends BaseMessage implements Serializable {

    private Map<String,Object> map;

    public KPIMessage(String msgId) {
        this.setMsgId(msgId);
    }

    public KPIMessage(String msgId, Map<String,Object> map) {
        this.setMsgId(msgId);
        this.map = map;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }
}
