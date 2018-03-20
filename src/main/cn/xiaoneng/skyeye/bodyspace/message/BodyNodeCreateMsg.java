package cn.xiaoneng.skyeye.bodyspace.message;

import cn.xiaoneng.skyeye.util.BaseMessage;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by liangyongheng on 2016/10/24 14:30.
 */
public class BodyNodeCreateMsg extends BaseMessage implements Serializable {

    private String id;

    private String nt_id;

    private String loginId;

    private long msgtime;

    private String spaceName;

    private Map<String, String> accountNumMap;


    public BodyNodeCreateMsg(String id, String nt_id, String spaceName, String msgId, long msgtime) {

        this.setId(id);

        this.setNt_id(nt_id);

        this.setMsgId(msgId);

        this.spaceName = spaceName;

        this.msgtime = msgtime;
    }

    public BodyNodeCreateMsg(String id, String nt_id, String loginId, Map<String, String> accountNumMap, String spaceName, String msgId, long msgtime) {

        this.setId(id);

        this.setNt_id(nt_id);

        this.setLoginId(loginId);

        this.accountNumMap = accountNumMap;

        this.spaceName = spaceName;

        this.setMsgId(msgId);

        this.msgtime = msgtime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNt_id() {
        return nt_id;
    }

    public void setNt_id(String nt_id) {
        this.nt_id = nt_id;
    }

    public String getSpaceName() {
        return spaceName;
    }

    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
    }

    public long getMsgtime() {
        return msgtime;
    }

    public void setMsgtime(long msgtime) {
        this.msgtime = msgtime;
    }


    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public Map<String, String> getAccountNumMap() {
        return accountNumMap;
    }

    public void setAccountNumMap(Map<String, String> accountNumMap) {
        this.accountNumMap = accountNumMap;
    }
}
