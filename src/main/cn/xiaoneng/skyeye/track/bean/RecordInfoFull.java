package cn.xiaoneng.skyeye.track.bean;

import cn.xiaoneng.skyeye.navigation.bean.NavNodeInfo;

import java.io.Serializable;
import java.util.Map;

/**
 * 一个NTID跟踪记录的导航节点引用列表
 *
 * 导航节点信息：需要时查询并记录导航信息，添加导航信息超时时间
 *
 * Created by xuyang on 2016/8/10.
 */
public class RecordInfoFull implements Serializable {

    private String body_id;
    private NavNodeInfo navNodeInfo;
    private long time;

    /**
     * 透明参数
     */
    private Map<String, Object> map;

    public RecordInfoFull(String body_id, NavNodeInfo navNodeInfo, Map<String, Object> map, long time) {

        this.body_id = body_id;
        this.navNodeInfo = navNodeInfo;
        this.map = map;
        this.time = time;
    }

    public void setNavNodeInfo(NavNodeInfo navNodeInfo) {
        this.navNodeInfo = navNodeInfo;
    }


    public void setBody_id(String body_id) {
        this.body_id = body_id;
    }


    public void setTime(long time) {
        this.time = time;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }


    public String getBody_id() {
        return body_id;
    }

    public NavNodeInfo getNavNodeInfo() {
        return navNodeInfo;
    }

    public long getTime() {
        return time;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    @Override
    public String toString() {
        return "RecordInfo{" +
                ", body_id='" + body_id + '\'' +
                ", navNodeInfo=" + navNodeInfo +
                ", time=" + time +
                ", map=" + map +
                '}';
    }

}
