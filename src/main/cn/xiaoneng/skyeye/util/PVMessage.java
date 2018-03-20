package cn.xiaoneng.skyeye.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liangyongheng on 2016/8/10 11:39.
 */
public class PVMessage extends BaseMessage implements Serializable {

//    public PVMessage() {
//        super(null, null, 10);
//    }

    //key:qq value{id,ntid}
//    private Map<String, Map<String, String>> bodyNodeInfo = new HashMap<>();

    private Map<String,String> bodyNodeInfo = new HashMap<>();

    //key 导航空间名  value:JSONArray
    private Map<String, String> navigationInfo = new HashMap<>();

    private Map<String, Object> otherInfo = new HashMap<>();

    public static final String BODYNODE = "bodynode";

    public static final String NAVIGATION = "navigation";

    public static final String OTHERINFO = "otherinfo";

    public long sendTime = 0L;


//    public Map<String, Map<String, String>> getBodyNodeInfo() {
//        return bodyNodeInfo;
//    }
    public Map<String, String> getBodyNodeInfo() {
        return bodyNodeInfo;
    }

//    public void setBodyNodeInfo(Map<String, Map<String, String>> bodyNodeInfo) {
//        this.bodyNodeInfo = bodyNodeInfo;
//    }
    public void setBodyNodeInfo(Map<String, String> bodyNodeInfo) {
        this.bodyNodeInfo = bodyNodeInfo;
    }

    public Map<String, String> getNavigationInfo() {
        return navigationInfo;
    }

    public void setNavigationInfo(Map<String, String> navigationInfo) {
        this.navigationInfo = navigationInfo;
    }

    public Map<String, Object> getOtherInfo() {
        return otherInfo;
    }

    public void setOtherInfo(Map<String, Object> otherInfo) {
        this.otherInfo = otherInfo;
    }
}
