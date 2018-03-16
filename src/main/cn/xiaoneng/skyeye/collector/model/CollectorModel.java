package cn.xiaoneng.skyeye.collector.model;

import cn.xiaoneng.skyeye.collector.util.CollectorStatus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 采集器模型
 * <p>
 * Created by liangyongheng on 2016/8/5 16:23.
 */
public class CollectorModel implements Serializable {

    /**
     * 企业id
     */
    private String siteId;


    /**
     * 采集器状态
     */
    private int status;

    /**
     * 导航空间结构map<fieldName,spaceName></>
     */
    private Map<String, String> navSpaceMap = new HashMap<>();

    /**
     * 主体空间结构map
     */
    private Set<String> bodySpaceSet = new HashSet<>();

    public CollectorModel() {

        this.setStatus(CollectorStatus.ON);
    }

    public CollectorModel(String siteId, int status) {

        this.siteId = siteId;

        this.status = status;
    }

    public void addBodySpaceFields(Set<String> field) {
        this.bodySpaceSet.addAll(field);
    }

    public void addNavSpaceFields(Map<String, String> field) {
        this.navSpaceMap.putAll(field);
    }

    public void addNavSpaceFields(String navSpaceName, Set<String> fieldNames) {

        for (String fieldName : fieldNames) {
            this.navSpaceMap.put(fieldName, navSpaceName);
        }
    }

//    public PVMessage toPVMessage(JSONObject jsonMap) {
//        PVMessage message = new PVMessage();
//
//        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
//
//            String fieldName = entry.getKey();
//            String fieldValue = (String) entry.getValue();
//
//            if (bodySpaceSet.contains(fieldName)) {
//                message.getBodyNodeInfo().put(fieldName, fieldValue);
//
//            } else if (navSpaceMap.containsKey(fieldName)) {
//
//                String navSpaceName = navSpaceMap.get(fieldName);
//                if (!message.getNavigationInfo().containsKey(navSpaceName)) {
//
//                    Map<String, String> map = new HashMap<>();
//                    map.put(fieldName, fieldValue);
//
//                    message.getNavigationInfo().put(navSpaceName, map);
//                } else {
//
//                    Map<String, String> map = message.getNavigationInfo().get(navSpaceName);
//                    map.put(fieldName, fieldValue);
//                }
//            } else {
//                message.getOtherInfo().put(fieldName, fieldValue);
//            }
//        }
//        return message;
//    }

//    public String toPVJson(JSONObject jsonMap) {
//
////        PVMessage message = toPVMessage(jsonMap);
//
//        String result = "{\"other\" : " + JSON.toJSONString(message.getOtherInfo()) + "," +
//                " \"body\" : " + JSON.toJSONString(message.getBodyNodeInfo()) + "," +
//                " \"navigation\" : " + JSON.toJSONString(message.getNavigationInfo()) + "}";
//        return result;
//    }

    public String toJsonString() {

        return new String();

//        JSONObject content = new JSONObject();
//
//        content.put("siteId", this.getSiteId());
//
//        content.put("id", this.getId());
//
//        content.put("status", this.getStatus());
//
//        JSONArray array = new JSONArray();
//
//        for (Map.Entry<String, Integer> entry : fieldMap.entrySet()) {
//
//            JSONObject json = new JSONObject();
//
//            json.put("name", entry.getKey());
//
//            json.put("status", entry.getValue());
//
//            array.add(json);
//        }
//        content.put("params", array);
//
//        return content.toJSONString();
    }

//    public Map<String, Integer> getFieldMap() {
//        return this.fieldMap;
//    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public static void main(String[] args) {

        Set<String> set = new HashSet<>();

        set.add("qq");
        set.add("login");
        set.add("nt");

        Set<String> list = new HashSet<>();

        list.add("url");
        list.add("title");


        Set<String> list1 = new HashSet<>();

        list1.add("productId");
        list1.add("productname");

        CollectorModel model = new CollectorModel();

        model.addBodySpaceFields(set);
        model.addNavSpaceFields("Web", list);
        model.addNavSpaceFields("Product", list1);

        Map<String, String> map = new HashMap<>();

        map.put("qq", "123");
        map.put("login", "abd");

        map.put("url", "www.baidu.com");
        map.put("title", "baidu");
        map.put("siteid", "00001");
        map.put("sessionid", "0234234-35435-43534534");
        map.put("productId", "0000123");
//        map.put("pr")

//        System.out.println(model.toPVJson(JSON.to));
    }
}
