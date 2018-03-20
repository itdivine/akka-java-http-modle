package cn.xiaoneng.skyeye.track.message;

import cn.xiaoneng.skyeye.util.BaseMessage;

import java.io.Serializable;

/**
 * Created by xuyang on 2016/8/12.
 */
public class GetRecordInfosMessage extends BaseMessage implements Serializable {


    public GetRecordInfosMessage(String msgId) {

        super(msgId, null, 10);
    }


    @Override
    public String toString() {
        return "GetRecordInfosMessage " + super.toString();
    }
}
