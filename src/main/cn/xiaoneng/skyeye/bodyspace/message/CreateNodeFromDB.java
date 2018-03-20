package cn.xiaoneng.skyeye.bodyspace.message;

import cn.xiaoneng.skyeye.track.message.GetUserTrackMessage;
import cn.xiaoneng.skyeye.util.BaseMessage;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/1/5.
 */
public class CreateNodeFromDB extends BaseMessage implements Serializable {

    private GetUserTrackMessage message;
    private String nt_id;

    public CreateNodeFromDB(String nt_id, GetUserTrackMessage message) {
        this.message = message;
        this.nt_id = nt_id;
    }

    public GetUserTrackMessage getMessage() {
        return message;
    }

    public String getNt_id() {
        return nt_id;
    }
}
