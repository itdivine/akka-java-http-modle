package cn.xiaoneng.skyeye.bodyspace.message;


import cn.xiaoneng.skyeye.util.BaseMessage;

import java.io.Serializable;

/**
 * Created by liangyongheng on 2016/8/1 17:02.
 */
public class BodySpaceMsg extends BaseMessage implements Serializable{

    private String spaceName;

    private int status;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getSpaceName() {
        return spaceName;
    }

    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
    }
}
