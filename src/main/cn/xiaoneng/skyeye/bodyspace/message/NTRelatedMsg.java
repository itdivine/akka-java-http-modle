package cn.xiaoneng.skyeye.bodyspace.message;

import cn.xiaoneng.skyeye.util.BaseMessage;

/**
 * Created by liangyongheng on 2016/10/24 14:42.
 */
public class NTRelatedMsg extends BaseMessage {

    private String nt_id;

    private String loginId;

    public NTRelatedMsg (String nt_id, String loginId) {

        this.nt_id = nt_id;

        this.loginId = loginId;
    }

    public String getNt_id() {
        return nt_id;
    }

    public void setNt_id(String nt_id) {
        this.nt_id = nt_id;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }
}
