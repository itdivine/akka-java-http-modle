//package cn.xiaoneng.skyeye.collector.service;
//
//import akka.actor.ActorRef;
//import akka.actor.UntypedActor;
//import cn.xiaoneng.skyeye.monitor.Monitor;
//import cn.xiaoneng.skyeye.monitor.MonitorCenter;
//import cn.xiaoneng.skyeye.monitor.Node;
//import cn.xiaoneng.skyeye.util.PVMessage;
//import cn.xiaoneng.skyeye.util.Statics;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import org.apache.commons.collections.map.HashedMap;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.Serializable;
//import java.util.Map;
//
//
///**
// * 保存访客最近一个PV的副本
// *
// * 临时方案：解决上报轨迹，必须立刻查询到
// * 保存访客最近一个PV，当NTTrack处理完后，删除访客
// * 当NTTrack查询访客轨迹为空时，来此处查询
// *
// * Created by xuyang on 2018/1/15 16:30.
// */
//public class CollectorCopy extends UntypedActor {
//
//    private static Monitor monitor = MonitorCenter.getMonitor(Node.CollectorCopy);
//    protected final static Logger log = LoggerFactory.getLogger(CollectorCopy.class);
//
//    private Map<String, PVMessage> ntMap = new HashedMap();
//
//
//    @Override
//    public void onReceive(Object message) {
//
//        try {
//            if (message instanceof PVMessage) {
//                //增加 覆盖
//                PVMessage pvMessage = (PVMessage)message;
//                String nt = pvMessage.getBodyNodeInfo().get("nt");
//                ntMap.put(nt, pvMessage);
//
//            } else if(message instanceof String) {
//                //删除
//                String nt = (String)message;
//                if(ntMap.containsKey(nt))
//                    ntMap.remove(nt);
//
//            } else if(message instanceof Get) {
//
//                String data = null;
//                Get msg = (Get)message;
//                if(ntMap.containsKey(msg.nt)) {
//                    data = format(ntMap.get(msg.nt));
//                }
//
//                if(Statics.isNullOrEmpty(data))
//                    data = "{}";
//
//                JSONObject object = new JSONObject();
//                object.put("status", 200);
//                object.put("body", data);
//                msg.callback.tell(object.toString(), getSelf());
//            }
//
//        } catch (Exception e) {
//            log.error("Exception: " + e.getMessage());
//            StackTraceElement[] er = e.getStackTrace();
//            for (int i = 0; i < er.length; i++) {
//                log.warn(er[i].toString());
//            }
//        }
//    }
//
//    private String format(PVMessage pvMessage) {
//
//        if(pvMessage == null)
//            return null;
//
//        long start = System.currentTimeMillis();
//        JSONObject userJson = new JSONObject();
//
//        try {
//            JSONArray sessionArray = new JSONArray();
//            JSONObject sessionJson = new JSONObject();
//            JSONArray infos = new JSONArray();
//            JSONObject info = new JSONObject();
//            infos.add(info);
//
//            userJson.put("sessions", sessionArray);
//            sessionArray.add(sessionJson);
//
//            sessionJson.put("infos", infos);
//            //sessionJson.put("records", pvArray);
//            //pvArray = new JSONArray();
//
//            Map<String, String> navigationInfo = pvMessage.getNavigationInfo();
//            for(Map.Entry entry : navigationInfo.entrySet()) {
//                JSONObject json = ((JSONArray)entry.getValue()).getJSONObject(0);
//                info.putAll(json);
//            }
//
//            info.putAll(pvMessage.getBodyNodeInfo());
//            info.putAll(pvMessage.getOtherInfo());
//        }
//        catch (Exception e) {
//            log.error("exception " + e.getMessage());
//            StackTraceElement[] er = e.getStackTrace();
//            for (int i = 0; i < er.length; i++) {
//                log.warn(er[i].toString());
//            }
//        } finally {
//            monitor.newWriteTime("format", System.currentTimeMillis() - start, true);
//        }
//        return userJson.toJSONString();
//    }
//
//
//
//    public static final class Get implements Serializable {
//        public final String nt;
//        public final ActorRef callback;
//        public Get(String nt, ActorRef callback) {
//            this.nt = nt;
//            this.callback = callback;
//        }
//    }
//}
