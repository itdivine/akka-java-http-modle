package cn.xiaoneng.skyeye.config.container;

import cn.xiaoneng.skyeye.config.db.MySqlDataAccess;
import cn.xiaoneng.skyeye.config.model.SourceTypeModel;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by liangyongheng on 2016/12/23 11:08.
 */
public class SourceTypeContainer implements Container {


    protected final static org.slf4j.Logger log = LoggerFactory.getLogger(SourceTypeContainer.class);

    private static ReentrantReadWriteLock _lockinstance = new ReentrantReadWriteLock();

    private static SourceTypeContainer instance;

    private static Map<String, List<SourceTypeModel>> cacheMap = new HashMap<>();

    public static SourceTypeContainer getInstance() {

        try {
            _lockinstance.writeLock().lock();

            if (instance == null)
                instance = new SourceTypeContainer();

        } catch (Exception e) {
            log.info("New MainProcess Exception " + e.toString());
        } finally {
            _lockinstance.writeLock().unlock();
        }

        return instance;
    }

    public void refreshCache(String siteid) {

        try {

            //初始化加载所有数据
            if (siteid == null || "".equals(siteid)) {

                List<SourceTypeModel> models = MySqlDataAccess.querySourceTypeListBySiteid(siteid);

                for (SourceTypeModel model : models) {

                    if (cacheMap.containsKey(model.getSiteid())) {

                        cacheMap.get(model.getSiteid()).add(model);
                    }else {

                        List<SourceTypeModel> list = new ArrayList<>();
                        list.add(model);

                        cacheMap.put(model.getSiteid(),list);
                    }
                }
            } else {

                cacheMap.put(siteid, MySqlDataAccess.querySourceTypeListBySiteid(siteid));
            }

        } catch (Exception e) {

            log.debug("exception: " + e.getMessage());
        }
    }

    public String getTypeNameByid(String siteid, int typeid) {

        String result = "";
        if (cacheMap.containsKey(siteid)) {

            List<SourceTypeModel> models = cacheMap.get(siteid);

            result = find(result, typeid, models);
        }
        return result;
    }

    private  String find(String typeName, int typeid, List<SourceTypeModel> list) {

        int pid = -1;

        if (typeid == -1) {
            return typeName;
        }
        for (SourceTypeModel model : list) {

            if (typeid == model.getSource_type_id()) {

                typeName = model.getTypename() + "-" + typeName;
                pid = model.getPid();
                break;
            }
        }
        if (pid != -1) {

            typeName = find(typeName, pid, list);
        }
        return typeName;
    }

    public static void main(String[] args) {

        List<SourceTypeModel> models = new ArrayList<>();

        SourceTypeModel model1 = new SourceTypeModel();
        SourceTypeModel model2 = new SourceTypeModel();
        SourceTypeModel model3 = new SourceTypeModel();
        SourceTypeModel model4 = new SourceTypeModel();

        model1.setPid(0);
        model1.setSource_type_id(1);
        model1.setTypename("A");

        model2.setPid(1);
        model2.setSource_type_id(2);
        model2.setTypename("B");

        model3.setPid(2);
        model3.setSource_type_id(3);
        model3.setTypename("C");

        model4.setPid(3);
        model4.setSource_type_id(4);
        model4.setTypename("D");

        models.add(model1);
        models.add(model2);
        models.add(model3);
        models.add(model4);

//        System.out.println(find("", 4, models));
    }


}
