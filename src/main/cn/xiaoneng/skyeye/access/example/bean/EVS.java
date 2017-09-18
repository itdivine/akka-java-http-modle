package cn.xiaoneng.skyeye.access.example.bean;

/**
 * Created by divine on 2017/8/22.
 */
public class EVS {

    String siteId;

    String name;

    public EVS(String siteId, String name) {
        this.siteId = siteId;
        this.name = name;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "EVS{" +
                "siteId='" + siteId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
