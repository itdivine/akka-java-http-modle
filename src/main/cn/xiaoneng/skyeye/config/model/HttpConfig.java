package cn.xiaoneng.skyeye.config.model;

/**
 * Created by liangyongheng on 2016/8/31 17:22.
 */
public class HttpConfig {
    public String siteid = "";
    public String productUrl = "";
    public String userUrl = "";
    public String orderUrl = "";

    @Override
    public String toString() {
        return "siteId=" + siteid + " productUrl=" + productUrl + " userurl=" + userUrl + " orderUrl=" + orderUrl;
    }

}
