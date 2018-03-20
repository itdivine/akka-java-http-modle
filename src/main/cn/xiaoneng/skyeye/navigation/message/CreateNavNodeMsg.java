package cn.xiaoneng.skyeye.navigation.message;

import cn.xiaoneng.skyeye.navigation.bean.NavNodeInfo;
import cn.xiaoneng.skyeye.util.BaseMessage;

/**
 * Created by xuyang on 2016/8/10.
 */
public class CreateNavNodeMsg extends BaseMessage {

    private NavNodeInfo navNodeInfo;

    public CreateNavNodeMsg(NavNodeInfo navNodeInfo, long timeToLive, String msgId) {

        super(msgId, null, timeToLive);
        this.navNodeInfo = navNodeInfo;
    }


    public NavNodeInfo getNavNodeInfo() {
        return navNodeInfo;
    }

    public void setNavNodeInfo(NavNodeInfo navNodeInfo) {
        this.navNodeInfo = navNodeInfo;
    }


    @Override
    public String toString() {
        return "CreateNavNodeMsg{" +
                "navNodeInfo=" + navNodeInfo + super.toString() +
                '}';
    }
}
