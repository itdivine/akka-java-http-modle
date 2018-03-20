package cn.xiaoneng.skyeye.track.message;

import cn.xiaoneng.skyeye.track.bean.RecordInfo;
import cn.xiaoneng.skyeye.util.BaseMessage;

import java.io.Serializable;

/**
 * Created by xuyang on 2016/8/12.
 */
public class AddRecordInfoMessage extends BaseMessage implements Serializable {

    private RecordInfo recordInfo;

    public AddRecordInfoMessage(RecordInfo recordInfo) {
        this.recordInfo = recordInfo;
    }

    public RecordInfo getRecordInfo() {
        return recordInfo;
    }

    public void setRecordInfo(RecordInfo recordInfo) {
        this.recordInfo = recordInfo;
    }

    @Override
    public String toString() {
        return "AddRecordInfoMessage{" +
                "recordInfo=" + recordInfo + super.toString() +
                '}';
    }
}
