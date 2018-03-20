package cn.xiaoneng.skyeye.temple;//package cn.xiaoneng.skyeye.temple;
//
//import akka.actor.ActorRef;
//import akka.actor.ActorSystem;
//import akka.actor.Props;
//import akka.actor.UntypedActor;
//import cn.xiaoneng.skyeye.navigation.actor.NavigationSpaceManager;
//import cn.xiaoneng.skyeye.track.message.GetUserTrackMessage;
//import cn.xiaoneng.skyeye.util.*;
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//
///**
// * Manager模板类
// *
// * Created by xuyang on 2016/8/8.
// */
//public abstract class BaseManagerTemple extends UntypedActor {
//
//    protected final static Logger log = LoggerFactory.getLogger(BaseManagerTemple.class);
//
//    protected ActorRef listProcessor = getContext().actorOf(Props.createEVSWithResponse(NavNodesList.class), ActorNames.ListProcessor);
//
//
//    @Override
//    public void onReceive(Object message) {
//
//        try {
//
//            log.info("Receive message: " + message);
//
//            if(message instanceof String) {
//                processHTTPCommand((String) message);
//
//            } else if(message instanceof CommandMessage) {
//                processCommand((CommandMessage) message);
//
//            } else if(message instanceof PVMessage) {
//                processPVMessage((PVMessage) message);
//
//            } else  if (message instanceof GetUserTrackMessage) {
//                getUserTrack((GetUserTrackMessage)message);
//
//            } else {
//                getSender().tell("{\"status\":415,\"body\":\"\"}", getSelf());
//            }
//
//        } catch (Exception e) {
//            log.error("Exception " + e.getMessage());
//            getSender().tell("{\"status\":415,\"body\":\"\"}", getSelf());
//        }
//    }
//
//    protected void getUserTrack(GetUserTrackMessage message) {
//        getSender().tell("{\"status\":415,\"body\":\"\"}", getSelf());
//    }
//
//    protected void processPVMessage(PVMessage message) {
//        getSender().tell("{\"status\":415,\"body\":\"\"}", getSelf());
//    }
//
//    protected void processHTTPCommand(String message) {
//
//        try {
//            JSONObject messageJson = JSON.parseObject(message);
//            String method = messageJson.getString("method");
//
//            if (Statics.isNullOrEmpty(method)) {
//                log.info("method is null, message= " + message);
//                getSender().tell("{\"status\":415,\"body\":\"\"}", getSelf());
//                return;
//            }
//
//            switch (method) {
//
//                case HTTPCommand.POST:
//                    createEVSWithResponse(messageJson);
//                    break;
//
//                case HTTPCommand.GET:
//                    list(messageJson);
//                    break;
//
//                default:
//                    getSender().tell("{\"status\":415,\"body\":\"\"}", getSelf());
//
//            }
//
//        } catch (Exception e) {
//            log.error("Exception " + e.getMessage());
//        }
//    }
//
//    protected void processCommand(CommandMessage message) {
//
//        try {
//            String operation = message.getOperation();
//            if(Statics.isNullOrEmpty(operation)) {
//                message.getCallback().tell("{\"status\":415}", getSelf());
//                return;
//            }
//
//            switch (operation) {
//
//                case Operation.DELETE:
//                    getContext().stop(getSender());
//                    message.getCallback().tell("{\"status\":200}", getSelf());
//                    break;
//
//                default:
//                    log.info("Invalid Message Operation: " + operation);
//                    message.getCallback().tell("{\"status\":415}", getSelf());
//            }
//
//        } catch (Exception e) {
//            log.error("Exception " + e.getMessage());
//            message.getCallback().tell("{\"status\":400}", getSelf());
//        }
//    }
//
//    protected abstract void createEVSWithResponse(JSONObject messageJson);
//
//    /**
//     *  查询子Actor列表
//     */
//    protected void list(JSONObject messageJson) {
//
//        try {
//            int page = 0;
//            int per_page = 0;
//
//            JSONObject bodyJson = messageJson.getJSONObject("body");
//
//            if(bodyJson != null) {
//                page = bodyJson.getInteger("page");
//                per_page = bodyJson.getInteger("per_page");
//            }
//
//            if(page == 0) {
//                page = 1;
//            }
//
//            if(per_page == 0) {
//                per_page = 30;
//            }
//
//            ListMessage listMessage = new ListMessage(page,per_page,10);
//
//            listProcessor.forward(listMessage, getContext());
//
//        } catch (Exception e) {
//            log.error("Exception " + e.getMessage());
//            // TODO HTTP 4xx
//        }
//    }
//
//
//    public static void main(String[] args){
//
//        try {
//            ActorSystem system = ActorSystem.createEVSWithResponse("test");
//            ActorRef ref = system.actorOf(Props.createEVSWithResponse(NavigationSpaceManager.class), "manager");
//            ref.tell("Go", ActorRef.noSender());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//}
