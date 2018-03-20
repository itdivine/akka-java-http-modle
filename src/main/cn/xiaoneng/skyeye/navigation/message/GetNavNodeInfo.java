package cn.xiaoneng.skyeye.navigation.message;

import cn.xiaoneng.skyeye.util.BaseMessage;

import java.io.Serializable;

/**
 * 获取导航节点信息
 * 请求者：Record
 *
 * Created by xuyang on 2016/8/18.
 */
public class GetNavNodeInfo extends BaseMessage implements Serializable {

    public GetNavNodeInfo(String msgId) {
        super(msgId, 10);
    }

}
