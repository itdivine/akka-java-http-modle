package cn.xiaoneng.skyeye.config.container;

import cn.xiaoneng.skyeye.collector.config.ItemConfigKeyAction;
import cn.xiaoneng.skyeye.config.db.MySqlDataAccess;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class KeyPageContainer implements Container {

    private static KeyPageContainer _instance = null;
    private static ReentrantReadWriteLock _lockinstance = new ReentrantReadWriteLock();
    private static HashMap<String, List<ItemConfigKeyAction>> _allquerys = new HashMap<>();

    protected final static org.slf4j.Logger log = LoggerFactory.getLogger(KeyPageContainer.class);

    public static KeyPageContainer getInstance() {
        try {
            _lockinstance.writeLock().lock();

            if (_instance == null)
                _instance = new KeyPageContainer();

        } catch (Exception e) {
            log.info("New MainProcess Exception " + e.toString());
        } finally {
            _lockinstance.writeLock().unlock();
        }

        return _instance;
    }

    private KeyPageContainer() {

        HashMap<String, List<ItemConfigKeyAction>> queryData = MySqlDataAccess.getConfigKeyAction();

        if (queryData != null) {
            _allquerys.putAll(queryData);
        }
    }

    public List<ItemConfigKeyAction> getConfigKeyAction(String siteid) {
        if (siteid == null) {
            return null;
        }
        if (!_allquerys.containsKey(siteid)) {

            List<ItemConfigKeyAction> keyConfig = MySqlDataAccess.getConfigKeyList(siteid);

            _allquerys.put(siteid, keyConfig);
        }
        return _allquerys.get(siteid);
    }

    public void updateAllquery(HashMap<String, List<ItemConfigKeyAction>> allquerys) {
        if (allquerys != null && allquerys.size() > 0)
            _allquerys = allquerys;
    }

    public Map<String, List<ItemConfigKeyAction>> getConfigMap() {
        return this._allquerys;
    }

    public List<ItemConfigKeyAction> getAllItems() {

        List<ItemConfigKeyAction> itemList = new ArrayList<>();

        for (List<ItemConfigKeyAction> list : _allquerys.values()) {

            itemList.addAll(list);
        }

        return itemList;
    }

    public void refreshCache(String siteid) {

        List<ItemConfigKeyAction> keyConfig = MySqlDataAccess.getConfigKeyList(siteid);

        _allquerys.put(siteid,keyConfig);

    }


}

