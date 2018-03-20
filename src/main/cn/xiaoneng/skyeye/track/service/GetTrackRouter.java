package cn.xiaoneng.skyeye.track.service;//package cn.xiaoneng.skyeye.track.service;
//
//import akka.actor.ActorRef;
//import akka.actor.ActorSelection;
//import akka.actor.UntypedActor;
//import akka.cluster.pubsub.DistributedPubSub;
//import akka.cluster.pubsub.DistributedPubSubMediator;
//import akka.pattern.Patterns;
//import akka.util.Timeout;
//import cn.xiaoneng.skyeye.auth.Util.AuthCodeConstants;
//import cn.xiaoneng.skyeye.bodyspace.message.BodyNodeMsgMap;
//import cn.xiaoneng.skyeye.monitor.Monitor;
//import cn.xiaoneng.skyeye.monitor.MonitorCenter;
//import cn.xiaoneng.skyeye.monitor.Node;
//import cn.xiaoneng.skyeye.navigation.config.NavigationSpaceConfig;
//import cn.xiaoneng.skyeye.track.message.GetUserTrackMessage;
//import cn.xiaoneng.skyeye.track.message.RecordMessage;
//import cn.xiaoneng.skyeye.util.*;
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import scala.concurrent.Await;
//import scala.concurrent.Future;
//import scala.concurrent.duration.Duration;
//
//import java.util.*;
//
///**
// * Created by xuyang on 2016/8/11.
// *
// * 查询导航节点被谁访问过
// *
// */
//public class GetTrackRouter extends UntypedActor {
//
//    private ActorRef mediator;
//    protected final static Logger log = LoggerFactory.getLogger(GetTrackRouter.class);
//    private static Monitor monitor = MonitorCenter.getMonitor(Node.GetTrackRouter);
//
//    /**
//     * key：请求消息ID
//     * value：聚合所有返回的消息
//     */
//    private Map<String, TrackFullFillment> trackLogAggregatorMap = new HashMap<String, TrackFullFillment>();
//
//    @Override
//    public void preStart() throws Exception {
//        super.preStart();
//
//        mediator = DistributedPubSub.get(this.getContext().system()).mediator();
//        mediator.tell(new DistributedPubSubMediator.Subscribe(getSelf().path().toStringWithoutAddress(), ActorNames.NSkyEye, getSelf()), getSelf());
//
//        log.info("GetTrackRouter init success, path = " + getSelf().path().toStringWithoutAddress());
//    }
//
//
//    @Override
//    public void onReceive(Object message) {
//
//        long start = System.currentTimeMillis();
//
//        log.info("Receive message: " + message);
//
//        // 1.给主体空间发送一个账号
////        if (message instanceof GetUserTrackMessage) {
////
////            // Message Request Visitor's tracks
////            getUserTrack((GetUserTrackMessage)message);
////
////        } else
//        if(message instanceof String) {
//
//            // 一、HTTP请求访客轨迹
//            // 1、去主体空间查询访客关联账号
//            processHTTPCommand((String) message);
//            monitor.newWriteTime("getUserTrack", System.currentTimeMillis()-start, true);
//
//        }else if (message instanceof BodyNodeMsgMap) {
//
//            // 2、主体空间返回账号列表
//            sendMsgToTrack((BodyNodeMsgMap)message);
//            monitor.newWriteTime("sendMsgToTrack", System.currentTimeMillis()-start, true);
//
//        } else if (message instanceof RecordMessage) {
//
//            // 3、聚合跟踪器返回的查询结果,返回给原始查询者
//            fill((RecordMessage)message);
//            monitor.newWriteTime("fill", System.currentTimeMillis()-start, true);
//        }
//
//    }
//
//    private void processHTTPCommand(String message) {
//
//        try {
//            JSONObject messageJson = JSON.parseObject(message);
//            String method = messageJson.getString("method");
//            String token = messageJson.getString("token");
//
//            if (Statics.isNullOrEmpty(method)) {
//                log.info("method is null, message= " + message);
//                getSender().tell("{\"status\":415,\"body\":\"\"}", getSelf());
//                return;
//            }
//
//            switch (method) {
//
//                case Operation.GET:
//
//                    String authCode = AuthCodeConstants.ORDER_PRICE;
//                    boolean showPrice = validateAuthBeforeDoHttp(token, authCode);
//
//                    Map<String, String> bodyMap = new HashMap<>();
//                    JSONObject bodyJson = messageJson.getJSONObject("body");
//
//                    int page = (Integer)bodyJson.remove("page");
//                    int per_page = (Integer)bodyJson.remove("per_page");
//                    String nav = (String)bodyJson.remove("nav");
//
//                    for (Map.Entry<String, Object> entry : bodyJson.entrySet()) {
//                        String key = entry.getKey();
//                        String value = (String) entry.getValue();
//                        bodyMap.put(key, value);
//                    }
//
//                    getUserTrack(new GetUserTrackMessage(bodyMap, nav, page, per_page, showPrice, getSender()));
//                    break;
//
//                default:
//                    log.info("Invalid Message Operation: " + method);
//                    getSender().tell("{\"status\":415}", getSelf());
//            }
//
//        } catch (Exception e) {
//            log.error("Exception " + e.getMessage());
//        }
//    }
//
//
//    private void fill(RecordMessage message) {
//
//        try {
//            String msgId = message.getMsgId();
//            if (!trackLogAggregatorMap.containsKey(msgId)) {
//                log.info("msgId is not exist, msgId= " + msgId);
//            }
//
//            TrackFullFillment trackFullFillment = trackLogAggregatorMap.get(msgId);
//
//            trackFullFillment.backMessages.add(message);
//
//            if (trackFullFillment.backMessages.size() == trackFullFillment.count) {
//
//                JSONObject obj = new JSONObject();
//                obj.put("status", 200);
//                obj.put("body", trackFullFillment.getInfos());
//                log.debug(obj.toString());
//                trackFullFillment.fromMessage.getCallback().tell(obj.toString(), getSelf());
//            }
//
//
//        } catch (Exception e) {
//            log.error("Exception " + e.getMessage());
//        }
//    }
//
//    private void sendMsgToTrack(BodyNodeMsgMap message) {
//
//        try {
////            String msgId = message.getMsgId();
////            TrackFullFillment trackFullFillment = trackLogAggregatorMap.remove(msgId);
////
////            if(trackFullFillment == null)
////            {
////                log.info("trackFullFillment == null, msgId= " + msgId);
////                return;
////            }
////
////            trackFullFillment.bodyNodeMsgMap = message;
//
//            ActorSelection selection = getContext().actorSelection("../" + ActorNames.NT_BODYSPACE);
//            selection.tell(message, getSelf());
//
//
//
////            Set<String> trackNames = NavigationSpaceConfig.getInstance().getItem().keySet();
////
////            for(String name:trackNames) {
////                ActorSelection selection = getContext().actorSelection("../" + name);
////                selection.tell(message, getSelf());
////            }
//
//
//
//        } catch (Exception e) {
//            log.error("Exception " + e.getMessage());
//        }
//    }
//
//    /**
//     * 查询一个账号的关联账号列表
//     * @param message
//     */
//    private void  getUserTrack(GetUserTrackMessage message) {
//
////        int trackCount = NavigationSpaceConfig.getInstance().getItem().size();
////        TrackFullFillment trackFullFillment = new TrackFullFillment(
////                message,trackCount, new ArrayList<RecordMessage>());
////        trackLogAggregatorMap.put(message.getMsgId(), trackFullFillment);
//
//        ActorSelection selection = getContext().actorSelection("../../" + ActorNames.BODYSPACEMANAGER);
//        selection.tell(message, getSelf());
//
//    }
//
//
//
//    /**
//     * 消息聚合
//     */
//    class TrackFullFillment {
//
//        // 原始请求消息：有消息ID  callback
//        private GetUserTrackMessage fromMessage;
//
//        private BodyNodeMsgMap bodyNodeMsgMap;
//
//        // 聚合的消息数量:跟踪器的数量
//        private int count;
//
//        // 聚合的消息
//        private List<RecordMessage> backMessages;
//
//        public TrackFullFillment(GetUserTrackMessage fromMessage, int count, List<RecordMessage> backMessages) {
//            this.fromMessage = fromMessage;
//            this.count = count;
//            this.backMessages = backMessages;
//        }
//
//        public JSONArray getInfos() {
//
//            JSONArray array = new JSONArray();
//
//            if(backMessages == null)
//                return array;
//
//            try {
//                for (RecordMessage message: backMessages) {
////                    array.add(message.toJsonArray());
//                }
//
//            } catch (Exception e) {
//                log.error("Exception " + e.getMessage());
//            }
//
//            return array;
//        }
//    }
//
//    public static void main(String[] arrays) {
//        JSONArray array = new JSONArray();
//        array.add("ddd");
//        JSONObject obj = new JSONObject();
//        obj.put("status", 200);
//        obj.put("body", array);
//        log.debug(obj.toString());
//    }
//
//}