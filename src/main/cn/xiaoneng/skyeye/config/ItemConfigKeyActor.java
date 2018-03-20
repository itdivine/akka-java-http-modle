package cn.xiaoneng.skyeye.config;

import akka.actor.AbstractActor;
import cn.xiaoneng.skyeye.config.model.ItemKeyModel;
import cn.xiaoneng.skyeye.util.HTTPCommand;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liangyongheng on 2016/8/24 18:00.
 */
public class ItemConfigKeyActor extends AbstractActor {

    private Map<String, List<ItemKeyModel>> siteItemMap = new HashMap<>();

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .matchAny(this::onReceive)
                .build();
    }
    public void onReceive(Object message) {

        JSONObject json = JSON.parseObject((String) message);
        String method = json.getString("method");

        if (HTTPCommand.POST.equals(method)) {
            updateItemMap(json);

        } else if (HTTPCommand.GET.equals(method)) {

            String siteid = json.getString("siteid");
            String url = json.getString("url");


        }
    }

    private void updateItemMap(JSONObject json) {

        ItemKeyModel item = new ItemKeyModel();

        item.setSiteid(json.getString("siteid"));
        item.setKeyLevel(json.getInteger("keylevel"));
        item.setKeyName(json.getString("keyname"));
        item.setUrlreg(json.getString("urlreg"));
        item.setPageType(json.getString("pagetype"));

        if (siteItemMap.containsKey(item.getSiteid())) {
            siteItemMap.get(item.getSiteid()).add(item);

        } else {
            List<ItemKeyModel> list = new ArrayList<>();
            list.add(item);
            siteItemMap.put(item.getSiteid(), list);
        }
    }
}
