package cn.xiaoneng.skyeye.collector.util;

import cn.xiaoneng.skyeye.util.PVMessage;
import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * Created by liangyongheng on 2016/8/15 10:12.
 */
public class JSonToPVMsgConverter  {

    public static PVMessage convert(String jsonStr) {

        PVMessage result = new PVMessage();
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);

        Map otherJson = jsonObject.getJSONObject("other");
        Map navigationMap = jsonObject.getJSONObject("navigation");
        Map bodyMap = jsonObject.getJSONObject("body");

        result.setOtherInfo(otherJson);
        result.setNavigationInfo(navigationMap);
        result.setBodyNodeInfo(bodyMap);

//        System.out.println(navigationMap);

        return result;

    }

    public static void main(String[] args) {

        String json = "{\n" +
                "        \"other\":{\n" +
                "            \"siteid\":\"kf_3004\",\n" +
                "            \"sid\":\"1234567\"\n" +
                "        },\n" +
                "        \"body\":{\n" +
                "            \"nt\":\"123435\",\n" +
                "            \"login\":\"1233445345\",\n" +
                "            \"cookie\":\"12434565676\"\n" +
                "        },\n" +
                "        \"navigation\":{\n" +
                "            \"Web\":{\n" +
                "                \"url\":\"www.baidu.com\"\n" +
                "            },\n" +
                "            \"LBS\":[{\n" +
                "            \t\"t\":\"\"\n" +
                "            }\n" +
                "            ]\n" +
                "        }\n" +
                "    }";
        PVMessage pv = JSonToPVMsgConverter.convert(json);

        System.out.println(pv.getNavigationInfo().get("Web"));
    }
}
