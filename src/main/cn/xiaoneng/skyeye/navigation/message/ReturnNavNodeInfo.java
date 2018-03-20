package cn.xiaoneng.skyeye.navigation.message;

import cn.xiaoneng.skyeye.navigation.bean.NavNodeInfo;
import cn.xiaoneng.skyeye.util.BaseMessage;

import java.io.Serializable;

/**
 * Created by xuyang on 2016/8/18.
 */
public class ReturnNavNodeInfo extends BaseMessage implements Serializable {

    private NavNodeInfo navNodeInfo;

    public ReturnNavNodeInfo(String msgId, NavNodeInfo navNodeInfo) {
        super(msgId, 10);
        this.navNodeInfo = navNodeInfo;
    }

    public NavNodeInfo getNavNodeInfo() {
        return navNodeInfo;
    }

    public void setNavNodeInfo(NavNodeInfo navNodeInfo) {
        this.navNodeInfo = navNodeInfo;
    }
}
