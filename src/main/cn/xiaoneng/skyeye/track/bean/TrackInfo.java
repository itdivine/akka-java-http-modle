package cn.xiaoneng.skyeye.track.bean;

import akka.actor.ActorRef;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by xuyang on 2016/8/15.
 */
public class TrackInfo implements Serializable {

    protected final static Logger log = LoggerFactory.getLogger(TrackInfo.class);

    /**
     * 必须是导航空间名字 （唯一）
     */
    private String name;

    /**
     * 状态  [-1释放 0关闭 1开启]
     */
    private int status;


    public TrackInfo(String name, int status) {
        this.name = name;
        this.status = status;
    }


    public String getName() {
        return name;
    }

    public int getStatus() {
        return status;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
