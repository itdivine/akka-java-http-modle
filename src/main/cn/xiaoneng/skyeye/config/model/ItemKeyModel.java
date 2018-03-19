package cn.xiaoneng.skyeye.config.model;

/**
 * Created by liangyongheng on 2016/8/24 18:11.
 */
public class ItemKeyModel  {

    private String siteid;

    private String urlreg;

    private int keyLevel;

    private String keyName;

    private String pageType;


    public String getSiteid() {
        return siteid;
    }

    public void setSiteid(String siteid) {
        this.siteid = siteid;
    }

    public String getUrlreg() {
        return urlreg;
    }

    public void setUrlreg(String urlreg) {
        this.urlreg = urlreg;
    }

    public int getKeyLevel() {
        return keyLevel;
    }

    public void setKeyLevel(int keyLevel) {
        this.keyLevel = keyLevel;
    }

    public String getPageType() {
        return pageType;
    }

    public void setPageType(String pageType) {
        this.pageType = pageType;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }
}
