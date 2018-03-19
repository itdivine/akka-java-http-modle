package cn.xiaoneng.skyeye.config.container;

import cn.xiaoneng.skyeye.config.model.Catagory;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class CatagoryContainer implements Container {

    private static CatagoryContainer _instance = null;

    private HashMap<String, Map<String, Catagory>> _allquerys = new HashMap<String, Map<String, Catagory>>();
    private HashMap<String, Long> _allquerysupdatetimes = new HashMap<String, Long>();

    private static ReentrantReadWriteLock _lockgetqueryurl = new ReentrantReadWriteLock();
    private static ReentrantReadWriteLock _lockinstance = new ReentrantReadWriteLock();

    public static long DB_CONFIG_CACHETIME = 10 * 60 * 1000L; // 更新Keypage的周期:每10分钟查DB更新10*60*1000L
    protected final static org.slf4j.Logger log = LoggerFactory.getLogger(CatagoryContainer.class);

    public static CatagoryContainer getInstance() {
        try {
            _lockinstance.writeLock().lock();

            if (_instance == null)
                _instance = new CatagoryContainer();

        } catch (Exception e) {
            log.info("New MainProcess Exception " + e.toString());
        } finally {
            _lockinstance.writeLock().unlock();
        }

        return _instance;
    }

    private CatagoryContainer() {
    }

    public Map<String, Catagory> getCatagorys(String siteid) {
        try {
            _lockgetqueryurl.writeLock().lock();

            if (siteid == null)
                return null;

            long now = new java.util.Date().getTime();

            //从DB中更新信息
            if (!_allquerysupdatetimes.containsKey(siteid) || (_allquerysupdatetimes.get(siteid) + DB_CONFIG_CACHETIME) < now) {
                log.info("Start getConfigKeyAction " + siteid);

                Map<String, Catagory> configs = _allquerys.get(siteid);
                _allquerysupdatetimes.put(siteid, now);

                if (configs != null && configs.size() > 0) {
                    _allquerys.put(siteid, configs);
                }
            }

            return _allquerys.get(siteid);

        } catch (Exception e) {
            log.info("Exception " + e.toString());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        } finally {
            _lockgetqueryurl.writeLock().unlock();
        }

        return null;

    }

    public Map<String,Map<String,Catagory>> getConfigMap() {

        return _allquerys;
    }

}

