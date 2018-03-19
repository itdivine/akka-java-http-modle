package cn.xiaoneng.skyeye.collector.model;

import java.io.Serializable;

/**
 * 采集器模型
 * <p>
 * Created by xy on 2016/8/5 16:23.
 */
public class CollectorModel implements Serializable {

    /**
     * 企业id
     */
    private String siteId;

    /**
     * 采集器状态
     */
    private int status;

    public CollectorModel() {}

    public CollectorModel(String siteId, int status) {
        this.siteId = siteId;
        this.status = status;
    }

    public String getSiteId() {return siteId;}
    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }

}