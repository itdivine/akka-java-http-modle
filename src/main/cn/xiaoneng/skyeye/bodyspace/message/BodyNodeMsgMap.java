package cn.xiaoneng.skyeye.bodyspace.message;

import cn.xiaoneng.skyeye.util.BaseMessage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by liangyongheng on 2016/8/11 15:25.
 */
public class BodyNodeMsgMap extends BaseMessage implements Serializable {

    private String nt_id;

    private Map<String,BodyNodeMsg> nodeMap = new HashMap<>();

    public BodyNodeMsgMap(){

    }

    public BodyNodeMsgMap(String msgId) {

        this.setMsgId(msgId);
    }

    public Map<String, BodyNodeMsg> getNodeMap() {
        return nodeMap;
    }

    /**
     * nt关联的ntid集合
     */
    private Set<String> relatedNtSet = new HashSet<>();

    /**
     * nt关联的各个维度账号
     */
    private Map<String,String> accountNumMap = new HashMap<>();


    public Set<String> getRelatedNtSet() {
        return relatedNtSet;
    }

    public void setRelatedNtSet(Set<String> relatedNtSet) {
        this.relatedNtSet = relatedNtSet;
    }

    public Map<String, String> getAccountNumMap() {
        return accountNumMap;
    }

    public void setAccountNumMap(Map<String, String> accountNumMap) {
        this.accountNumMap = accountNumMap;
    }

    public String getNt_id() {
        return nt_id;
    }

    public void setNt_id(String nt_id) {
        this.nt_id = nt_id;
    }
}
