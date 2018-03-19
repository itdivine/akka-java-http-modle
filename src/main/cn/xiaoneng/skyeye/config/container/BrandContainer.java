package cn.xiaoneng.skyeye.config.container;

import cn.xiaoneng.skyeye.config.db.MySqlDataAccess;
import cn.xiaoneng.skyeye.config.model.Brand;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by liangyongheng on 2016/8/30 20:02.
 */
public class BrandContainer implements Container {

    private static BrandContainer _instance = null;

    private HashMap<String,Map<String,Brand>> _allquerys  = new HashMap<String,Map<String,Brand>>();
    private static ReentrantReadWriteLock _lockinstance = new ReentrantReadWriteLock();

    protected final static org.slf4j.Logger log = LoggerFactory.getLogger(BrandContainer.class);
    public static BrandContainer getInstance()
    {
        try {
            _lockinstance.writeLock().lock();

            if(_instance==null)
                _instance = new BrandContainer();

        } catch (Exception e) {
            log.info("New BrandContainer Exception " + e.toString());
        }finally {
            _lockinstance.writeLock().unlock();
        }

        return _instance;
    }

    private BrandContainer(){
        initConfigData();
    }

    public Map<String,Brand> getBranks(String siteid)
    {
        return _allquerys.get(siteid);
    }

    public void updateConfig(HashMap<String, Map<String, Brand>> configs) {
        if(configs != null && configs.size() > 0)
            _allquerys = configs;
    }

    public Map<String,Map<String,Brand>> getConfigMap() {

        return _allquerys;
    }

    private void initConfigData() {
        HashMap<String,Map<String,Brand>> queryData = MySqlDataAccess.getConfigBrand();

        if (queryData != null) {
            _allquerys.putAll(MySqlDataAccess.getConfigBrand());
        }

    }

}
