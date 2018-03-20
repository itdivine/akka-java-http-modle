package cn.xiaoneng.skyeye.bodyspace.message;

import cn.xiaoneng.skyeye.bodyspace.model.BodyNodeModel;
import cn.xiaoneng.skyeye.util.BaseMessage;

/**
 * Created by liangyongheng on 2016/9/23 11:05.
 */
public class PVResultMsg extends BaseMessage{

    private BodyNodeModel bodyNode;

    //1->操作成功， 0->操作失败
    private int data_status;

    private String site_id;

    private String spaceName;

    public BodyNodeModel getBodyNode() {
        return bodyNode;
    }

    public void setBodyNode(BodyNodeModel bodyNode) {
        this.bodyNode = bodyNode;
    }

    public int getData_status() {
        return data_status;
    }

    public void setData_status(int data_status) {
        this.data_status = data_status;
    }

    public String getSite_id()   {
        return site_id;
    }

    public void setSite_id(String site_id) {
        this.site_id = site_id;
    }

    public String getSpaceName() {
        return spaceName;
    }

    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
    }
}
