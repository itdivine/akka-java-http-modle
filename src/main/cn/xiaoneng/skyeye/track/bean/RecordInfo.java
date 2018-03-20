package cn.xiaoneng.skyeye.track.bean;


import java.io.Serializable;
import java.util.Map;

/**
 * 一个NTID跟踪记录的导航节点引用列表
 *
 * 导航节点信息：需要时查询并记录导航信息，添加导航信息超时时间
 *
 * Created by xuyang on 2016/8/10.
 */
public class RecordInfo implements Serializable {

    private String nt_id;
    private String body_id; //unused
    private String navId;
    private String navSpaceName;
    private long time;

    /**
     * 透明参数
     */
    private Map<String, Object> map;


    public RecordInfo(String nt_id, String body_id, String navSpaceName, String navId, Map<String, Object> map, long time) {

        this.nt_id = nt_id;
        this.body_id = body_id;
        this.navId = navId;
        this.navSpaceName = navSpaceName;
        this.map = map;
        this.time = time;
    }


    public String getNavId() {
        return navId;
    }

    public void setNavId(String navId) {
        this.navId = navId;
    }

    public void setNt_id(String nt_id) {
        this.nt_id = nt_id;
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

    public String getNt_id() {
        return nt_id;
    }

    public String getBody_id() {
        return body_id;
    }

    public long getTime() {
        return time;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public String getNavSpaceName() {
        return navSpaceName;
    }

    public void setNavSpaceName(String navSpaceName) {
        this.navSpaceName = navSpaceName;
    }

    @Override
    public String toString() {
        return "RecordInfo{" +
                "nt_id='" + nt_id + '\'' +
                ", body_id='" + body_id + '\'' +
                ", navId='" + navId + '\'' +
                ", navSpaceName='" + navSpaceName + '\'' +
                ", time=" + time +
                ", map=" + map +
                '}';
    }
}
