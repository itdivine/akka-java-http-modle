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

    private String navId;
    private String navSpaceName;

    public GetNavNodeInfo(String msgId, String navId, String navSpaceName) {
        super(msgId, 10);
        this.navId = navId;
        this.navSpaceName = navSpaceName;
    }

    public String getNavId() {
        return navId;
    }

    public void setNavId(String navId) {
        this.navId = navId;
    }

    public String getNavSpaceName() {
        return navSpaceName;
    }

    public void setNavSpaceName(String navSpaceName) {
        this.navSpaceName = navSpaceName;
    }
}
