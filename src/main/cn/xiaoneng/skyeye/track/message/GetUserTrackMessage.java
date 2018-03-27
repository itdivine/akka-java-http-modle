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

    //主体节点id
    private String id;
    private ActorRef callback;

    private int page;
    private int per_page;
    private String navSpaceName;
    private boolean showPrice;
    private String startpage;


    public GetUserTrackMessage(String id, String nav, String startpage, int page, int per_page, boolean showPrice, ActorRef callback) {
        super(null, null, 10);
        this.page = page;
        this.per_page = per_page;
        this.navSpaceName = nav;
        this.startpage = startpage;
        this.id = id;
        this.showPrice = showPrice;
        this.callback = callback;
    }

    public boolean isShowPrice() {
        return showPrice;
    }

    public void setShowPrice(boolean showPrice) {
        this.showPrice = showPrice;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
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

    public String getStartpage() {
        return startpage;
    }

    public void setStartpage(String startpage) {
        this.startpage = startpage;
    }

    @Override
    public String toString() {
        return "GetUserTrackMessage{" +
                "id='" + id + '\'' +
                ", callback=" + callback +
                ", page=" + page +
                ", per_page=" + per_page +
                ", navSpaceName='" + navSpaceName + '\'' +
                ", showPrice=" + showPrice +
                ", start_page='" + startpage + '\'' +
                '}';
    }
}
