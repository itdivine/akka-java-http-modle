package cn.xiaoneng.skyeye.navigation.message;

import cn.xiaoneng.skyeye.util.BaseMessage;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xuyang on 2016/8/10.
 */
public class BackNavNodeListMsg extends BaseMessage implements Serializable {

    // key：navSpacreName value: navNode
    private List<BackNavNodeMsg> navNodes;

    public BackNavNodeListMsg(String msgId, List<BackNavNodeMsg> list, long timeToLive) {

        super(msgId, null, timeToLive);
        this.navNodes = list;
    }

    public List<BackNavNodeMsg> getNavNodes() {
        return navNodes;
    }



    @Override
    public String toString() {
        return "BackNavNodeListMsg{" +
                "navNodes=" + navNodes + super.toString() +
                '}';
    }
}
