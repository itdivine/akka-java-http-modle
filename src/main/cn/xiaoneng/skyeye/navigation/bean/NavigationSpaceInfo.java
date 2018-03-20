package cn.xiaoneng.skyeye.navigation.bean;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by xuyang on 2016/8/3.
 */
public class NavigationSpaceInfo implements Serializable {

    /**
     * 名字 （唯一）
     */
    private String name;

    /**
     * 旧版：指定采集字段中，某个字段为唯一索引（选填）
     * 新版：每个导航空间id为唯一索引
     */
    private String indexParam;

    /**
     * 状态：-1释放 0关闭 1开启
     */
    private int status;

    /**
     * 采集字段列表
     */
    private Set<String> params;

    /**
     * 创建者ID
     */
    private String creatorId;

    /**
     * 创建时间
     */
    private long createtime;

    public NavigationSpaceInfo() {}

    /**
     * 预创建
     */
    public NavigationSpaceInfo(String name, int status, Set<String> params, String indexParam) {
        this.name = name;
        this.status = status;
        this.params = params;
        this.indexParam = indexParam;
        this.creatorId = "system";
        this.createtime = System.currentTimeMillis();
    }

    /**
     * HTTP创建
     */
    public NavigationSpaceInfo(NavigationSpaceInfo navSpaceInfo) {
        update(navSpaceInfo);
    }

    public void update(NavigationSpaceInfo navSpaceInfo) {
        status = navSpaceInfo.getStatus();
        indexParam = navSpaceInfo.getIndexParam();
        params = navSpaceInfo.getParams();
    }

    public static void main(String[] args) {

        String json = "{\"siteid\": \"kf_1000\",\"name\": \"LBS导航空间222\",\"indexParam\": \"\",\"creatorId\": \"创建者ID\",\"params\": [\"param1\",\"param2\"]}";

        // 获取JSON内指定字段值
        System.out.println(JSON.parseObject(json).getString("siteid"));

        // 序列化
        NavigationSpaceInfo rawEvs = JSON.parseObject(json, NavigationSpaceInfo.class);
        System.out.println("序列化:  " + rawEvs);

        // 反序列化
        String str = JSON.toJSONString("反序列化:  " + rawEvs);
        System.out.println(str);

    }

    @Override
    public String toString() {
        return "NavigationSpaceInfo{" +
                "name='" + name + '\'' +
                ", indexParam='" + indexParam + '\'' +
                ", status=" + status +
                ", params=" + params +
                ", creatorId='" + creatorId + '\'' +
                ", createtime=" + createtime +
                '}';
    }

    public String toJSONString() {
        return JSON.toJSONString(this);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setParams(Set<String> params) {
        this.params = params;
    }

    public String getName() {
        return name;
    }

    public int getStatus() {
        return status;
    }

    public Set<String> getParams() {
        return params;
    }

    public String  getIndexParam() {
        return indexParam;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public long getCreatetime() {
        return createtime;
    }

    public void setIndexParam(String indexParam) {
        this.indexParam = indexParam;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public void setCreatetime(long createtime) {
        this.createtime = createtime;
    }
}
