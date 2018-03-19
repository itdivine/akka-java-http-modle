package cn.xiaoneng.skyeye.config.container;

import cn.xiaoneng.skyeye.collector.model.SubPage;
import cn.xiaoneng.skyeye.config.db.MySqlDataAccess;
import cn.xiaoneng.skyeye.util.Statics;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ConfigSubPage implements Container {

    private static ConfigSubPage _instance = null;

    //key = site:level ; value = List<SubPage> 按照fid从大到小排序
    private HashMap<String, List<SubPage>> _allquerys = new HashMap<String, List<SubPage>>();
    private HashMap<String, Long> _allquerysupdatetimes = new HashMap<String, Long>();

    private static ReentrantReadWriteLock _lockgetqueryurl = new ReentrantReadWriteLock();
    private static ReentrantReadWriteLock _lockinstance = new ReentrantReadWriteLock();

    protected final static org.slf4j.Logger log = LoggerFactory.getLogger(ConfigSubPage.class);

    public static ConfigSubPage getInstance() {
        try {
            _lockinstance.writeLock().lock();

            if (_instance == null)
                _instance = new ConfigSubPage();

        } catch (Exception e) {
            log.info("New ConfigSubPage Exception " + e.toString());
        } finally {
            _lockinstance.writeLock().unlock();
        }

        return _instance;
    }

    private ConfigSubPage() {
        initAllquerys();
    }

    /**
     * 解析子页面类型
     * 查找匹配的siteId和level集合，
     * 按照fid从大到小，遍历所有urlreg，
     * 返回匹配的pageid
     * 返回 -1 表示空
     *
     * @return
     */
    public int getSubKeyId(String siteid, String url, int level) {
        int keyId = -1;

        try {
            if (siteid == null)
                return keyId;

            List<SubPage> tempSubs = null;

            List<SubPage> configs = getSubPageList(siteid);
            if (configs == null || configs.size() == 0)
                return keyId;

            for (SubPage subpage : configs) {
                if (subpage.pagelevel != level)
                    continue;

                if (tempSubs == null)
                    tempSubs = new ArrayList<SubPage>();

                tempSubs.add(subpage);
            }

            if (tempSubs == null || tempSubs.size() == 0)
                return keyId;

            Collections.sort(tempSubs);

            for (SubPage sub : tempSubs) {
                if (Statics.isregex_match(url, sub.urlreg))
                    return sub.pageid;
            }

        } catch (Exception e) {
            log.info("Exception " + e.toString());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }

        return keyId;

    }

    public List<SubPage> getSubPageList(String siteid) {
        return _allquerys.get(siteid);
    }


    public void updateConfig(HashMap<String, List<SubPage>> configs) {
        if (configs != null && configs.size() > 0)
            _allquerys = configs;
    }

    public Map<String, List<SubPage>> getConfigMap() {
        return _allquerys;
    }

    private void initAllquerys() {

        HashMap<String, List<SubPage>> queryData = MySqlDataAccess.getConfigSubPage();

        if (queryData != null) {
            _allquerys.putAll(queryData);
        }

    }
}

