package cn.xiaoneng.skyeye.config.container;

import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LanguageConfig implements Container {

    private static LanguageConfig _instance = null;

    private Map<String, String> _allquerys = new HashMap<String, String>();
    private static ReentrantReadWriteLock _lockinstance = new ReentrantReadWriteLock();

    protected final static org.slf4j.Logger log = LoggerFactory.getLogger(LanguageConfig.class);

    public static LanguageConfig getInstance() {
        try {
            _lockinstance.writeLock().lock();

            if (_instance == null)
                _instance = new LanguageConfig();

        } catch (Exception e) {
            log.info("New LanguageConfig Exception " + e.toString());
        } finally {
            _lockinstance.writeLock().unlock();
        }

        return _instance;
    }

    private LanguageConfig() {
    }

    public String getLanguage(String siteId) {

        if (_allquerys.containsKey(siteId)) {
            log.info("language get: " + siteId + " - " + _allquerys.get(siteId));
            return _allquerys.get(siteId);
        } else {
            log.info("language get: " + siteId + " - cn");
            return "cn";
        }
    }

    public void updateConfig(Map<String, String> configs) {
        if (configs != null && configs.size() > 0) {
            _allquerys = configs;
            for (Entry<String, String> entry : configs.entrySet())
                log.info("language set: " + entry.getKey() + "-" + entry.getValue());
        }
    }

    public Map<String,String> get_allquerys() {
        return this._allquerys;
    }

}