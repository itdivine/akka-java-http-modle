package cn.xiaoneng.skyeye.bodyspace.message;

import cn.xiaoneng.skyeye.util.BaseMessage;

import java.io.Serializable;

/**
 * Created by liangyongheng on 2016/8/10 20:37.
 */
public class NTCommand extends BaseMessage implements Serializable {

    private String nt_id;

    private boolean isRelatedNode;

    public NTCommand(String nt_id,boolean isRelatedNode) {

        this.nt_id = nt_id;

        this.isRelatedNode = isRelatedNode;
    }

    public String getNt_id() {
        return nt_id;
    }

    public void setNt_id(String nt_id) {
        this.nt_id = nt_id;
    }

    public boolean isRelatedNode() {
        return isRelatedNode;
    }

    public void setRelatedNode(boolean relatedNode) {
        isRelatedNode = relatedNode;
    }
}
