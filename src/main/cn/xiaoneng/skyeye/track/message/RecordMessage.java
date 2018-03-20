package cn.xiaoneng.skyeye.track.message;

import cn.xiaoneng.skyeye.track.bean.RecordInfoFull;
import cn.xiaoneng.skyeye.util.BaseMessage;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

/**
 * 跟踪器返回指定NT_ID的所有导航节点信息
 *
 * Created by xuyang on 2016/8/11.
 */
public class RecordMessage extends BaseMessage implements Serializable {


    private RecordInfoFull recordInfoFull;

    public RecordInfoFull getRecordInfoFull() {
        return recordInfoFull;
    }

    public void setRecordInfoFull(RecordInfoFull recordInfoFull) {
        this.recordInfoFull = recordInfoFull;
    }

    public RecordMessage(RecordInfoFull recordInfoFull, String msgId) {
        super(msgId, null, 10);
        this.recordInfoFull = recordInfoFull;
    }


    public static void main(String[] args){

        String s = "{\"a\":\"1\",\"b\":\"1\"}";

        JSONObject oo = (JSONObject) JSON.parse(s);
        System.out.println(oo);

        String mm = "[{\"nt_id\":\"nt_03\",\"navNodeInfo\":{\"visitedCount\":0,\"createTime\":1471506062353,\"nodeIdFromParams\":\"1063680838\",\"deadline\":0,\"params\":{\"ntalkerparam\":\"\",\"title\":\"\",\"url\":\"\"},\"nodeId\":\"2106821193\"},\"time\":1471506062363,\"map\":{\"uname\":\"\",\"userlevel\":\"\",\"siteid\":\"\",\"subscribe_time\":\"\",\"groupname\":\"\",\"sid\":\"\"}},{\"nt_id\":\"nt_03\",\"navNodeInfo\":{\"visitedCount\":0,\"createTime\":1471507343334,\"nodeIdFromParams\":\"1277055812\",\"deadline\":0,\"params\":{\"ntalkerparam\":\"\",\"title\":\"\",\"url\":\"\"},\"nodeId\":\"54789825\"},\"time\":1471507343336,\"map\":{\"uname\":\"\",\"userlevel\":\"\",\"siteid\":\"\",\"subscribe_time\":\"\",\"groupname\":\"\",\"sid\":\"\"}}]";
        System.out.println((JSONObject) JSON.parse(mm));
    }

}
