package cn.xiaoneng.skyeye.config.container;

import cn.xiaoneng.skyeye.config.db.MySqlDataAccess;
import cn.xiaoneng.skyeye.config.http.ObjectType;
import cn.xiaoneng.skyeye.config.model.HttpConfig;
import cn.xiaoneng.skyeye.util.HQToken;
import cn.xiaoneng.skyeye.util.Statics;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by liangyongheng on 2016/8/31 17:23.
 */
public class HttpConfigContainer implements Container {

    protected final static org.slf4j.Logger log = LoggerFactory.getLogger(HttpConfigContainer.class);

    private static ReentrantReadWriteLock _lockinstance = new ReentrantReadWriteLock();
    private static HttpConfigContainer _instance;
    private static Map<String, HttpConfig> _siteid2config = new ConcurrentHashMap<String, HttpConfig>();

    public static HttpConfigContainer getInstance() {
        try {
            _lockinstance.writeLock().lock();

            if (_instance == null)
                _instance = new HttpConfigContainer();
            _instance.initConfigData();

        } catch (Exception e) {
            log.info("New MainProcess Exception " + e.toString());
        } finally {
            _lockinstance.writeLock().unlock();
        }

        return _instance;
    }

    public boolean hasUrlConfig(String siteid, ObjectType type) {

        HttpConfig config = _siteid2config.get(siteid);
        if (config == null)
            return false;

        String url = null;

        switch (type) {
            case PRODUCT:
                url = config.productUrl;
                break;
            case ORDER:
                url = config.orderUrl;
                break;
            case USER:
                url = config.userUrl;
                break;
        }

        return !Statics.isNullOrEmpty(url);
    }

    public HttpConfig getUrlBySiteid(String siteid) {
        return _siteid2config.get(siteid);
    }

    public void updateHttpConfig(Map<String, HttpConfig> siteid2config) {
        if (null == siteid2config || siteid2config.size() <= 0) {
            return;
        }
        _siteid2config.putAll(siteid2config);
    }

    /**
     * 返回商品信息查询接口
     *
     * @param siteId
     * @param productId
     * @param userId
     * @return
     */
    public String getProductUrl(String siteId, String productId, String userId) {

        HttpConfig config = _siteid2config.get(siteId);
        if (config == null || Statics.isNullOrEmpty(config.productUrl)) {
            log.info("HttpConfig is null, siteid : " + siteId);
            return null;
        }

        // http://malltest.ntalker.com/api/ntalker.php?itemid=[itemid]
        String query = config.productUrl;
        query = query.replace("[uid]", userId);
        query = query.replace("[itemid]", productId);

        return query;
    }

    public String getOrderUrl(String siteId, String orderId, String userId) {

        HttpConfig config = _siteid2config.get(siteId);
        if (config == null || Statics.isNullOrEmpty(config.orderUrl)) {
            log.info("HttpConfig is null, siteid : " + siteId);
            log.info(_siteid2config.toString());
            return null;
        }

        // http://api.edu24ol.com/Order?token=[token]&code=[code]
        String query = config.orderUrl;
        query = query.replace("[code]", orderId);
        query = query.replace("[token]", HQToken.getToken(orderId));

        return query;
    }

    public String getUserUrl(String siteId, String userId) {

        HttpConfig config = _siteid2config.get(siteId);
        if (config == null || Statics.isNullOrEmpty(config.userUrl)) {
            log.info("HttpConfig is null, siteId=" + siteId);
            return null;
        }

        // http://api.edu24ol.com/UserPersonal?passport=[passport]&userid=[userid]
        String query = config.userUrl;
        query = query.replace("[userid]", userId);
        log.info("query=" + query);
        query = query.replace("[token]", HQToken.getToken(userId));
        log.info("query=" + query);

        return query;
    }

    public Map<String, HttpConfig> getConfigMap() {
        return _siteid2config;
    }

    private void initConfigData() {

        Map<String, HttpConfig> queryData = MySqlDataAccess.getHttpConfigs();

        if (queryData != null) {
            getConfigMap().putAll(queryData);
        }
    }


}
