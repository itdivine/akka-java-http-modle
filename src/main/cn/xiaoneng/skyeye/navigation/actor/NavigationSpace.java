package cn.xiaoneng.skyeye.navigation.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.ClusterSharding;
import cn.xiaoneng.skyeye.enterprise.message.IsRegistMessage;
import cn.xiaoneng.skyeye.monitor.Monitor;
import cn.xiaoneng.skyeye.monitor.MonitorCenter;
import cn.xiaoneng.skyeye.monitor.Node;
import cn.xiaoneng.skyeye.navigation.bean.NavNodeInfo;
import cn.xiaoneng.skyeye.navigation.bean.NavigationSpaceInfo;
import cn.xiaoneng.skyeye.navigation.event.NavInfoChangedEvent;
import cn.xiaoneng.skyeye.navigation.message.CreateNavNodeMsg;
import cn.xiaoneng.skyeye.temple.*;
import cn.xiaoneng.skyeye.util.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

/**
 * Created by xuyang on 2016/8/3.
 */
public class NavigationSpace extends AbstractActor {

    protected final Logger log = LoggerFactory.getLogger(getSelf().path().toStringWithoutAddress());
    private static Monitor monitor = MonitorCenter.getMonitor(Node.NavigationSpace);

//    private ActorRef navSpaceList = getContext().actorOf(Props.create(ListProcessor.class), ActorNames.NavNodes);

    private ActorRef mediator;
    private NavigationSpaceInfo navInfo;
    private ActorRef shardRegion = ClusterSharding.get(getContext().getSystem()).shardRegion(ActorNames.NavigationNode);

    public NavigationSpace(NavigationSpaceInfo navInfo) {
        this.navInfo = navInfo;
    }

    @Override
    public void preStart() throws Exception {

        //发布事件
        NavInfoChangedEvent event = new NavInfoChangedEvent(navInfo);
        this.getContext().system().eventStream().publish(event);

        //总线
        mediator = DistributedPubSub.get(this.getContext().system()).mediator();
        mediator.tell(new DistributedPubSubMediator.Subscribe(getSelf().path().toStringWithoutAddress(), ActorNames.NSkyEye, getSelf()), getSelf());

        log.debug("NavigationSpace init success, path = " + getSelf().path());

        super.preStart();
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .matchAny(this::onReceive)
                .build();
    }
    public void onReceive(Object message) {

        try {

            log.debug("Receive message: " + message);

            if(message == null) {
                return;
            }

            if(message instanceof String) {

                processHTTPCommand((String)message);

            } else if(message instanceof CommandMessage) {

                processCommand((CommandMessage) message);

            } else if(message instanceof CreateNavNodeMsg) {

                long start = System.currentTimeMillis();
                monitor.newWriteTime("CreateNavNodeMsg_pre", start-((CreateNavNodeMsg)message).getCreateTime(), true);

                createNavNode((CreateNavNodeMsg)message);
                monitor.newWriteTime("CreateNavNodeMsg", System.currentTimeMillis()-start, true);

            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }
    }

    private void processCommand(CommandMessage message) {

        long start = System.currentTimeMillis();

        try {
            String operation = message.getOperation();
            if(Statics.isNullOrEmpty(operation)) {
                message.getCallback().tell("{\"status\":415}", getSelf());
                return;
            }

            switch (operation) {

                // 验证创建导航空间
                case Operation.CREATE:
                    message.getCallback().tell("{\"status\":200,\"body\":" + navInfo.toJSONString() + "}", getSelf());
                    monitor.newWriteTime("CREATE", System.currentTimeMillis()-start, true);
                    break;

                // 查询导航空间信息
                case Operation.GET:
                    DocumentMessage documentMessage = new DocumentMessage(null, 10, navInfo.toJSONString(), ((CommandMessage)message).getMsgId());
                    getSender().tell(documentMessage, getSelf());
                    monitor.newWriteTime("GET", System.currentTimeMillis()-start, true);
                    break;

                // 删除自定义导航节点
                case Operation.DELETE:
                    getContext().stop(getSender());
                    message.getCallback().tell("{\"status\":200}", getSelf());
                    monitor.newWriteTime("DELETE", System.currentTimeMillis()-start, true);
                    break;

                default:
                    log.info("Invalid Message Operation: " + operation);
                    message.getCallback().tell("{\"status\":415}", getSelf());
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
            message.getCallback().tell("{\"status\":400}", getSelf());
        }
    }

    private void processHTTPCommand(String message) {

        long start = System.currentTimeMillis();

        try {
            JSONObject messageJson = JSON.parseObject(message);
            String method = messageJson.getString("method");
            JSONObject params = messageJson.getJSONObject("params");


            if (Statics.isNullOrEmpty(method)) {
                log.info("method is null, message= " + message);
                getSender().tell("{\"status\":415,\"body\":\"\"}", getSelf());
                return;
            }

            switch (method) {

                case HTTPCommand.GET:


                    // 查询导航空间信息
                    getSender().tell("{\"status\":200,\"body\":" + navInfo.toJSONString() + "}", getSelf());

                    // 查询导航节点列表
//                  list(messageJson);

                    monitor.newWriteTime("HTTP.GET", System.currentTimeMillis()-start, true);

                    break;

                // 修改导航空间信息
                case HTTPCommand.PUT: case HTTPCommand.PATCH:
                    JSONObject bodyJson = messageJson.getJSONObject("body");
                    NavigationSpaceInfo rawNavigation = JSON.parseObject(bodyJson.toString(), NavigationSpaceInfo.class);
                    navInfo.update(rawNavigation);
                    getSender().tell("{\"status\":200,\"body\":" + navInfo.toJSONString() + "}", getSelf());
                    monitor.newWriteTime("HTTP.PUT", System.currentTimeMillis()-start, true);
                    break;

                // 删除导航空间
                case HTTPCommand.DELETE:
                    getContext().parent().tell(new CommandMessage(Operation.DELETE, 10, null, getSender()), getSelf());
                    monitor.newWriteTime("HTTP.DELETE", System.currentTimeMillis()-start, true);
                    break;

                // 创建自定义导航节点
                case HTTPCommand.POST:
                    createNavNodeByHTTP(messageJson);
                    monitor.newWriteTime("HTTP.POST", System.currentTimeMillis()-start, true);
                    break;

                default:

            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage() + "  message= " + message);
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }
    }

    private void createNavNodeByHTTP(JSONObject messageJson) {

        try {
            JSONObject bodyJson = messageJson.getJSONObject("body");
            if(bodyJson == null) {
                log.info("body is null, message= " + messageJson);
                // TODO HTTP返回“message”:” Invalid API key”
                getSender().tell("{\"status\":400,\"body\":\"\"}", getSelf());
                return;
            }

            NavNodeInfo navNodeInfo = JSON.parseObject(bodyJson.toString(), NavNodeInfo.class);
            shardRegion.tell(navNodeInfo, getSender());
            getSender().tell("{\"status\":200,\"body\":\"\"}", getSelf());

            /*// 导航节点是否已经创建
            Option<ActorRef> navOption = getContext().child(navNodeInfo.getId());
            if(navOption.isEmpty()) {
                ActorRef actorRef = getContext().actorOf(Props.create(NavigationNode.class, navNodeInfo),navNodeInfo.getId());
                // 确认
                actorRef.tell(new CommandMessage(Operation.CREATE, 10, null, getSender()), getSelf());
            } else {
                getSender().tell("{\"status\":201,\"body\":\"\"}", getSelf());
            }*/


        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
            getSender().tell("{\"status\":400,\"body\":\"\"}", getSelf());
        }
    }

    private void createNavNode(CreateNavNodeMsg message) {

        ActorRef actorRef = null;

        try {
            shardRegion.tell(message, getSender());

//            NavNodeInfo navNodeInfo = message.getNavNodeInfo();

            /*// 内存中查找导航节点
            Option<ActorRef> navOption = getContext().child(navNodeInfo.getId());

            if(navOption.isEmpty()) {
                actorRef = getContext().actorOf(Props.create(NavigationNode.class, navNodeInfo.getName()), navNodeInfo.getId());

                // 当需要获取导航空间下所有节点列表时，需要上报
                navSpaceList.tell(new IsRegistMessage(true, actorRef.path().toString(), actorRef, 10), getSender());
            } else {
                actorRef = navOption.get();
            }

            actorRef.tell(message, getSender());*/


        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }
    }

    /*private void createNavNode(CreateNavNodeMsg message) {

        ActorRef actorRef = null;

        try {

            NavNodeInfo navNodeInfo = message.get_navNodeInfo();

            // 内存中查找导航节点
            Option<ActorRef> navOption = getContext().child(navNodeInfo.getId());


            if(navOption.isEmpty()) {
                // TODO DB中查找导航节点

                // TODO 如果DB中是NULL，创建
                actorRef = getContext().actorOf(Props.createEVS(NavigationNode.class, navNodeInfo),navNodeInfo.getId());

                // TODO 如果DB不是NULL,从DB加载，并把merge的数据存入DB中

            } else {
                actorRef = navOption.get();
            }

            getSender().tell(new BackNavNodeMsg(message.getMsgId(), navInfo.getName(), actorRef, 10), getSelf());

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            getSender().tell(new BackNavNodeMsg(message.getMsgId(), navInfo.getName(), actorRef, 10), getSelf());
        }
    }*/


}
