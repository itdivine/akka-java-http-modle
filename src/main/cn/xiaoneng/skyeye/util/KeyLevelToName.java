package cn.xiaoneng.skyeye.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Created by liangyongheng on 2016/12/20 15:54.
 */
public class KeyLevelToName {

    public static String keyNames[] = new String[]{"首页", "列表页", "商品页", "购物车页", "订单页", "支付页", "支付成功页"};

    public static String getName(int keyLevel) {

        if (keyLevel > 0 && keyLevel < 8) {

            return keyNames[keyLevel - 1];
        } else {
            return "";
        }
    }

    public static JSONObject getKeyLevelNameJsons() {

        JSONObject result = new JSONObject();

        JSONArray array = new JSONArray();

        for (int i = 0; i < keyNames.length; i++) {

            JSONObject json = new JSONObject();
            json.put("key", i + 1);
            json.put("keylevel", i + 1);
            json.put("name", keyNames[i]);
//            json.put("subset",null);

            array.add(json);
        }
        result.put("data", array);
        return result;
    }
}
