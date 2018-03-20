package cn.xiaoneng.skyeye.track.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/11/24.
 */
public class Session {


    private String sid;
    private int pagecount;  // web导航节点数量
    private List<Map<String,String>> tracks = new ArrayList<>();

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public int getPagecount() {
        return pagecount;
    }

    public void setPagecount(int pagecount) {
        this.pagecount = pagecount;
    }

    public List<Map<String, String>> getTracks() {
        return tracks;
    }

    public void setTracks(List<Map<String, String>> tracks) {
        this.tracks = tracks;
    }
}
