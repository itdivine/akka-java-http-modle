package cn.xiaoneng.skyeye.bodyspace.message;

import cn.xiaoneng.skyeye.util.BaseMessage;

import java.io.Serializable;

/**
 * Created by liangyongheng on 2016/8/10 20:54.
 */
public class NTMessage extends BaseMessage implements Serializable {

    private String nt_id;

    private String[] spaces;

    public NTMessage(String nt_id, String[] spaces) {
        this.nt_id = nt_id;

        this.spaces = spaces;
    }

    public String getNt_id() {
        return nt_id;
    }

    public void setNt_id(String nt_id) {
        this.nt_id = nt_id;
    }

    public String[] getSpaces() {
        return spaces;
    }

    public void setSpaces(String[] spaces) {
        this.spaces = spaces;
    }
}
