package cn.xiaoneng.skyeye.config.container;

import cn.xiaoneng.skyeye.collector.model.Source;
import cn.xiaoneng.skyeye.config.db.MySqlDataAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ConfigSourcemap implements Container {

    private static ConfigSourcemap _instance = null;
    protected final static Logger log = LoggerFactory.getLogger(ConfigSourcemap.class);

    private Map<String, Map<Integer, Set<Source>>> _allquerys = new ConcurrentHashMap<>();

    private static ReentrantReadWriteLock _lockinstance = new ReentrantReadWriteLock();

    public static ConfigSourcemap getInstance() {
        try {
            _lockinstance.writeLock().lock();

            if (_instance == null)
                _instance = new ConfigSourcemap();

        } catch (Exception e) {
            log.info("Exception " + e.toString());
        } finally {
            _lockinstance.writeLock().unlock();
        }

        return _instance;
    }

    private ConfigSourcemap() {

        initConfigData();
    }

    public void updateConfig(Map<String, Map<Integer, Set<Source>>> configs) {

        if (configs != null && configs.size() > 0)
            _allquerys = configs;
    }

    public Map<Integer, Set<Source>> getConfigSourcemap(String siteId) {

        return _allquerys.get(siteId);
    }

    private void initConfigData() {

        Map<String, Map<Integer, Set<Source>>> queryData = MySqlDataAccess.getConfigSourceMap();

        if (queryData != null) {
            _allquerys.putAll(queryData);
        }
    }

    public void updateSource(Source source) {

        if(!_allquerys.containsKey(source.siteid))
            return;

        Map<Integer, Set<Source>> sourceConfigs = _allquerys.get(source.siteid);
        for(Map.Entry<Integer, Set<Source>> entry : sourceConfigs.entrySet()) {
            for(Source cacheSource:entry.getValue()) {
                if(cacheSource.pk_config_source == source.pk_config_source) {
                    cacheSource = source;
                    break;
                }
            }
        }
    }

    public void saveSource(Source source) {

        if(!_allquerys.containsKey(source.siteid))
            return;

        Set<Source> sources;
        Map<Integer, Set<Source>> sourceConfigs = _allquerys.get(source.siteid);

        if (sourceConfigs.containsKey(source.type))
            sources = sourceConfigs.get(source.type);
        else {
            sources = new HashSet<>();
            sourceConfigs.put(source.type, sources);
        }

        sources.add(source);
    }

    public void deleteSource(String siteId, int pk_config_source) {

        if(!_allquerys.containsKey(siteId))
            return;

        Map<Integer, Set<Source>> sourceConfigs = _allquerys.get(siteId);

        for (Set<Source> sourceSet : sourceConfigs.values()) {
            for(Source cacheSource : sourceSet) {
                if(cacheSource.pk_config_source == pk_config_source)
                    sourceSet.remove(cacheSource);
            }
        }
    }
}

