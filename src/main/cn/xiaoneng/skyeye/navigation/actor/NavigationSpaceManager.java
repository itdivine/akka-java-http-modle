package cn.xiaoneng.skyeye.navigation.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.routing.FromConfig;
import akka.routing.SmallestMailboxPool;
import cn.xiaoneng.skyeye.enterprise.message.IsRegistMessage;
import cn.xiaoneng.skyeye.monitor.Monitor;
import cn.xiaoneng.skyeye.monitor.MonitorCenter;
import cn.xiaoneng.skyeye.monitor.Node;
import cn.xiaoneng.skyeye.navigation.bean.NavigationSpaceInfo;
import cn.xiaoneng.skyeye.navigation.config.NavigationSpaceConfig;
import cn.xiaoneng.skyeye.navigation.message.NavSpaceFieldMsg;
import cn.xiaoneng.skyeye.navigation.service.NavigationPVRouter;
import cn.xiaoneng.skyeye.temple.ListMessage;
import cn.xiaoneng.skyeye.temple.ListProcessor;
import cn.xiaoneng.skyeye.util.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.util.Map;

/**
 * 导航空间管理器 （企业单例）
 *
 * Created by xuyang on 2016/8/3.
 */
public class NavigationSpaceManager extends AbstractActor {

    protected final Logger log = LoggerFactory.getLogger(getSelf().path().toStringWithoutAddress());
    private static Monitor monitor = MonitorCenter.getMonitor(Node.NavigationSpaceManager);

    private ActorRef mediator;
    private ActorRef reportPVProsessor;

    @Override
    public void preStart() throws Exception {
        init();
        super.preStart();
    }

    /**
     * 创建预制的所有导航空间
     * 可以通过后台页面配置必须的导航空间
     */
    private void init() {

        log.debug("path: " + getSelf().path());

        // 注册总线
        mediator = DistributedPubSub.get(this.getContext().system()).mediator();
        mediator.tell(new DistributedPubSubMediator.Subscribe(getSelf().path().toStringWithoutAddress(), ActorNames.NSkyEye, getSelf()), getSelf());

//        reportPVProsessor = getContext().actorOf(FromConfig.getInstance().props(Props.create(NavigationPVRouter.class)), ActorNames.NavReportPVProsessor);
        reportPVProsessor = getContext().actorOf(new SmallestMailboxPool(3).props(Props.create(NavigationPVRouter.class)), ActorNames.NavReportPVProsessor);

        Map<String, NavigationSpaceInfo> item = NavigationSpaceConfig.getInstance().getItem();

        for (Map.Entry<String, NavigationSpaceInfo> entry : item.entrySet()) {

            String spaceName = entry.getKey();

            NavigationSpaceInfo navigationSpaceInfo = entry.getValue();

            ActorRef ref = getContext().actorOf(Props.create(NavigationSpace.class, navigationSpaceInfo), spaceName);

            // 注册: 通知采集器更新字段列表
            NavSpaceFieldMsg fieldMsg = new NavSpaceFieldMsg(spaceName, navigationSpaceInfo.getParams());
            this.getContext().system().eventStream().publish(fieldMsg);

            // 注册导航空间
            listProcessor.tell(new IsRegistMessage(true, ref.path().toString(), ref, 10), getSelf());
        }

        log.debug("NavigationSpaceManager init success, path = " + getSelf().path());
    }

    protected ActorRef listProcessor = getContext().actorOf(Props.create(ListProcessor.class), ActorNames.ListProcessor);


    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .matchAny(this::onReceive)
                .build();
    }
    public void onReceive(Object message) {

        long start = System.currentTimeMillis();

        try {
            log.debug("Receive message: " + message);

            if(message instanceof String) {
                processHTTPCommand((String) message);

            } else if(message instanceof CommandMessage) {
                processCommand((CommandMessage) message);

            } else {
                getSender().tell("{\"status\":415,\"body\":\"\"}", getSelf());
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            getSender().tell("{\"status\":415,\"body\":\"\"}", getSelf());
        }
    }

    protected void processHTTPCommand(String message) {

        long start = System.currentTimeMillis();

        try {
            JSONObject messageJson = JSON.parseObject(message);
            String method = messageJson.getString("method");

            if (Statics.isNullOrEmpty(method)) {
                log.info("method is null, message= " + message);
                getSender().tell("{\"status\":415,\"body\":\"\"}", getSelf());
                return;
            }

            switch (method) {

                case HTTPCommand.POST:
                    create(messageJson);
                    monitor.newWriteTime("create", System.currentTimeMillis()-start, true);
                    break;

                case HTTPCommand.GET:
                    list(messageJson);
                    monitor.newWriteTime("list", System.currentTimeMillis()-start, true);
                    break;

                default:
                    getSender().tell("{\"status\":415,\"body\":\"\"}", getSelf());

            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        }
    }

    protected void processCommand(CommandMessage message) {

        long start = System.currentTimeMillis();

        try {
            String operation = message.getOperation();
            if(Statics.isNullOrEmpty(operation)) {
                message.getCallback().tell("{\"status\":415}", getSelf());
                return;
            }

            switch (operation) {

                case Operation.DELETE:
                    getContext().stop(getSender());
                    message.getCallback().tell("{\"status\":200,\"body\":\"\"}", getSelf());

                    // 取消注册
                    IsRegistMessage isRegistMessage = new IsRegistMessage(false, getSender().path().toString(), getSender(), 10);
                    listProcessor.tell(isRegistMessage, getSelf());

                    monitor.newWriteTime("DELETE", System.currentTimeMillis()-start, true);

                    break;

                default:
                    log.info("Invalid Message Operation: " + operation);
                    message.getCallback().tell("{\"status\":415}", getSelf());
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            message.getCallback().tell("{\"status\":400}", getSelf());
        }
    }

    /**
     *  查询子Actor列表
     */
    protected void list(JSONObject messageJson) {

        try {
            int page = 0;
            int per_page = 0;

            JSONObject bodyJson = messageJson.getJSONObject("body");

            if(bodyJson != null) {
                page = bodyJson.getInteger("page");
                per_page = bodyJson.getInteger("per_page");
            }

            if(page == 0) {
                page = 1;
            }

            if(per_page == 0) {
                per_page = 30;
            }

            ListMessage listMessage = new ListMessage(page,per_page,10);

            listProcessor.forward(listMessage, getContext());

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            // TODO HTTP 4xx
        }
    }

    protected void create(JSONObject messageJson) {

        try {
            String body = messageJson.getString("body");
            if(body == null) {
                log.info("bodyJson is null, message= " + messageJson);
                // TODO HTTP返回“message”:” Invalid API key”
                getSender().tell("{\"status\":400,\"body\":\"\"}", getSelf());
                return;
            }

            NavigationSpaceInfo rawNavigation = JSON.parseObject(body, NavigationSpaceInfo.class);
            // TODO 效验字段
            String navName = rawNavigation.getName();

            // 导航空间是否已经创建
            Option<ActorRef> navOption = getContext().child(navName);

            if(navOption.isEmpty()) {

                ActorRef actorRef = getContext().actorOf(Props.create(NavigationSpace.class, rawNavigation),navName);

                // 注册
                IsRegistMessage isRegistMessage = new IsRegistMessage(true, actorRef.path().toString(), actorRef, 10);
                listProcessor.tell(isRegistMessage, getSelf());

                // 确认
                actorRef.tell(new CommandMessage(Operation.CREATE, 10, null, getSender()), getSelf());

                NavigationSpaceConfig.getInstance().addItem(rawNavigation);

            } else {
                getSender().tell("{\"status\":201,\"body\":\"\"}", getSelf());
            }


        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            // TODO HTTP 4xx
            getSender().tell("{\"status\":400,\"body\":\"\"}", getSelf());
        }
    }

    /*protected void processPVMessage(PVMessage message) {

        try {
            reportPVProsessor.tell(message, getSender());

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        }
    }*/
}