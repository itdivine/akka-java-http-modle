package cn.xiaoneng.skyeye.navigation.bean;

import cn.xiaoneng.skyeye.util.HashAlgorithms;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by xuyang on 2016/8/4.
 */
    public class NavNodeInfo implements Serializable {

    private String siteId; // 企业ID
    private String spaceName; // 导航空间名字

    private String id; // 节点ID

    private long createTime;  // 创建时间

    private long deadline;  // 销毁时间

    @JSONField(serialize=false)
    private long visitedCount; // 被访问次数

    protected final static Logger log = LoggerFactory.getLogger(NavNodeInfo.class);


    /**
     * 导航节点的详细信息
     */
    private Map<String, Object> params;

    public NavNodeInfo(){}

    public NavNodeInfo(String siteId, String id, Map<String, Object> params, long time) {
        this.siteId = siteId;
        this.params = params;
        createTime = time;
        if(null == id || id.isEmpty())
            this.id = getNodeIdFromParams(params);
        else
            this.id = id;
    }

    /**
     * 比较两个导航节点的属性值是否一样 【重点：商品价格改变；增加属性值】
     * @param httpInfo
     * @return
     */
    public boolean compare(NavNodeInfo httpInfo) {

        try {

            if(httpInfo == null)
                return false;

            if(httpInfo.params == null || this.params == null)
                return false;

            Set<String> set = params.keySet();
            for(String key : set) {
                if(!params.get(key).equals(httpInfo.params.get(key)))
                    return false;
            }

            Set<String> httpSet = httpInfo.params.keySet();
            for(String key : httpSet) {
                if(!httpInfo.params.get(key).equals(params.get(key)))
                    return false;
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        }

        return true;
    }

    public String toJSONString() {
        return JSON.toJSONString(this);
    }

    public void update(NavNodeInfo info) {
        this.deadline = deadline;
        this.params = params;
    }

    public String getSpaceName() {
        return spaceName;
    }

    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public void setVisitedCount(long visitedCount) {
        this.visitedCount = visitedCount;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public void setDeadline(int deadline) {
        this.deadline = deadline;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public String getSiteId() {
        return siteId;
    }

    public String getId() {
        return id;
    }

    public long getCreateTime() {
        return createTime;
    }

    public long getDeadline() {
        return deadline;
    }

    public long getVisitedCount() {
        return visitedCount;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "NavNodeInfo{" +
                "siteId='" + siteId + '\'' +
                ", id='" + id + '\'' +
                ", createTime=" + createTime +
                ", deadline=" + deadline +
                ", visitedCount=" + visitedCount +
                ", params=" + params +
                '}';
    }

    public String getNodeIdFromParams(Map<String, Object> params) {

        StringBuffer sb = new StringBuffer();
        for(Map.Entry<String, Object> entry:params.entrySet()) {
            sb.append(entry.getValue());
        }
        return HashAlgorithms.mixHash(sb.toString()) + "";
    }

    /**
     * Create Neo4j Map of this nodeInfo
     * @return Map
     */
    public Map toNeo4jMap() {
        Map map = new HashMap<>();
        map.put("siteId", siteId);
        map.put("id", id);
        map.put("createTime", createTime);
        //map.putAll(params);

        String key;
        Object value;
        for(Map.Entry<String, Object> entry:params.entrySet()) {
            key = entry.getKey();
            value = entry.getValue();
            if(value instanceof JSONArray) {
                map.put(key, ((JSONArray)value).toJSONString());
            } else if(value instanceof JSONObject){
                map.put(key, ((JSONObject)value).toJSONString());
            } else {
                map.put(key, value);
            }
        }

        return map;
    }
}
