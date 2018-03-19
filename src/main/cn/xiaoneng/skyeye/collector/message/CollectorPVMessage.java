package cn.xiaoneng.skyeye.collector.message;//package cn.xiaoneng.skyeye.collector.message;
//
//import cn.xiaoneng.skyeye.util.PVMessage;
//
//import java.io.Serializable;
//import java.util.Map;
//
///**
// * Created by liangyongheng on 2016/8/5 21:34.
// */
//public class CollectorPVMessage  implements Serializable{
//
//    private Map<String,Integer> fieldMap;
//
//    private PVMessage message;
//
//    public CollectorPVMessage(Map<String,Integer> fieldMap, PVMessage message) {
//
//        this.fieldMap = fieldMap;
//
//        this.message = message;
//    }
//
//    public Map<String, Integer> getFieldMap() {
//        return fieldMap;
//    }
//
//    public void setFieldMap(Map<String, Integer> fieldMap) {
//        this.fieldMap = fieldMap;
//    }
//
//    public PVMessage getMessage() {
//        return message;
//    }
//
//    public void setMessage(PVMessage message) {
//        this.message = message;
//    }
//}
