package cn.xiaoneng.skyeye.track.message;

import akka.actor.ActorRef;
import cn.xiaoneng.skyeye.util.BaseMessage;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by xuyang on 2016/8/11.
 *
 * 获取用户轨迹信息
 */
public class GetUserTrackMessage extends BaseMessage implements Serializable {

    private Map<String, String> bodyMap;
    private ActorRef callback;

    private int page;
    private int per_page;
    private String navSpaceName;
    private boolean showPrice;


    public GetUserTrackMessage(Map<String, String> bodyMap, String nav, int page, int per_page, boolean showPrice, ActorRef callback) {
        super(null, null, 10);
        this.page = page;
        this.per_page = per_page;
        this.navSpaceName = nav;
        this.bodyMap = bodyMap;
        this.showPrice = showPrice;
        this.callback = callback;
    }

    public boolean isShowPrice() {
        return showPrice;
    }

    public void setShowPrice(boolean showPrice) {
        this.showPrice = showPrice;
    }

    public void setBodyMap(Map<String, String> bodyMap) {
        this.bodyMap = bodyMap;
    }

    public Map<String, String> getBodyMap() {
        return bodyMap;
    }

    public void setCallback(ActorRef callback) {
        this.callback = callback;
    }

    public ActorRef getCallback() {
        return callback;
    }

    public int getPage() {
        return page;
    }

    public int getPer_page() {
        return per_page;
    }

    public String getNavSpaceName() {
        return navSpaceName;
    }

    @Override
    public String toString() {
        return "GetUserTrackMessage{" +
                "bodyMap=" + bodyMap +
                ", callback=" + callback + super.toString() +
                '}';
    }
}
