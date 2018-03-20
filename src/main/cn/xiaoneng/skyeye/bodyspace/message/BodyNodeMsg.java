package cn.xiaoneng.skyeye.bodyspace.message;


import cn.xiaoneng.skyeye.util.BaseMessage;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by liangyongheng on 2016/8/1 21:35.
 */
public class BodyNodeMsg extends BaseMessage implements Serializable{

    private String id;
    private String nt_id;

    private long createtime;

    private String spaceName;

    private Set<String> relatedNtSet;

    private boolean isRelatedNode;

    public BodyNodeMsg(String id, String nt_id,String msgId) {

        this.setId(id);
        this.setNt_id(nt_id);
        this.setMsgId(msgId);
    }

    public BodyNodeMsg(String id, String nt_id,Set<String> relatedNT_id,String msgId) {
        this.setId(id);
        this.setNt_id(nt_id);
        this.relatedNtSet = relatedNT_id;
        this.setMsgId(msgId);

    }

    public BodyNodeMsg() {}

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

    public long getCreatetime() {
        return createtime;
    }

    public void setCreatetime(long createtime) {
        this.createtime = createtime;
    }

    public Set<String> getRelatedNtSet() {
        return relatedNtSet;
    }

    public void setRelatedNtSet(Set<String> relatedNtSet) {
        this.relatedNtSet = relatedNtSet;
    }

    public boolean isRelatedNode() {
        return isRelatedNode;
    }

    public void setRelatedNode(boolean relatedNode) {
        isRelatedNode = relatedNode;
    }
}
