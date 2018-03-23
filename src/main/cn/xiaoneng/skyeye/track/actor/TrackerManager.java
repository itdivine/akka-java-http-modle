package cn.xiaoneng.skyeye.track.actor;

import akka.actor.*;
import akka.routing.FromConfig;
import akka.routing.SmallestMailboxPool;
import cn.xiaoneng.skyeye.collector.service.CollectorHandler;
import cn.xiaoneng.skyeye.enterprise.message.IsRegistMessage;
import cn.xiaoneng.skyeye.monitor.Monitor;
import cn.xiaoneng.skyeye.monitor.MonitorCenter;
import cn.xiaoneng.skyeye.monitor.Node;
import cn.xiaoneng.skyeye.temple.ListProcessor;
import cn.xiaoneng.skyeye.temple.ListMessage;
import cn.xiaoneng.skyeye.track.bean.NTTrackInfo;
import cn.xiaoneng.skyeye.track.message.TrackMessage;
import cn.xiaoneng.skyeye.track.message.TrackMessage.*;
import cn.xiaoneng.skyeye.track.service.TrackReportPVProcessor;
import cn.xiaoneng.skyeye.util.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

/**
 * 跟踪管理器（企业单子）
 *
 * Created by xuyang on 2016/8/8.
 */
public class TrackerManager extends AbstractActor {

    protected final Logger log = LoggerFactory.getLogger(getSelf().path().toStringWithoutAddress());

    private static Monitor monitor = MonitorCenter.getMonitor(Node.TrackerManager);

    protected ActorRef reportPVProcessor = null;

//    protected ActorRef getUserProcessor = getContext().actorOf(Props.createEVS(GetTrackRouter.class), ActorNames.GetUser);

    protected ActorRef listProcessor = getContext().actorOf(Props.create(ListProcessor.class), ActorNames.ListProcessor);


    @Override
    public void preStart() throws Exception {
        initSpace();
        super.preStart();
    }

    /**
     * 创建预制的所有跟踪器
     * 一个导航空间一个跟踪器
     */
    private void initSpace() {

        log.info("path " + getSelf().path());

        reportPVProcessor = getContext().actorOf(new SmallestMailboxPool(3).props(Props.create(TrackReportPVProcessor.class)), ActorNames.TrackReportPVProcessor);

        // 创建NT跟踪器
        NTTrackInfo ntTrackInfo = new NTTrackInfo(ActorNames.NT_BODYSPACE, 1);
        ActorRef ref = getContext().actorOf(Props.create(NTTracker.class, ntTrackInfo), ActorNames.NT_BODYSPACE);
        IsRegistMessage isRegistMessage = new IsRegistMessage(true, ref.path().toString(), ref, 10);
        listProcessor.tell(isRegistMessage, getSelf());


        // 创建导航空间跟踪器
        /*TrackInfo trackInfo = null;
        Set<String> spaceNames = NavigationSpaceConfig.getInstance().getItem().keySet();
        for(String spaceName:spaceNames) {

            trackInfo = new TrackInfo(spaceName, 1);
            ref = getContext().actorOf(Props.createEVS(Tracker.class, trackInfo), spaceName);

            isRegistMessage = new IsRegistMessage(true, ref.path().toString(), ref, 10);
            listProcessor.tell(isRegistMessage, getSelf());
        }*/
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .matchAny(this::onReceive)
                .build();
    }
    public void onReceive(Object message) {

        log.debug("Receive message: " + message);

        try {

            if(message instanceof String) {
                log.debug("Receive message: " + message + " " + getSelf().path().toStringWithoutAddress());
                processHTTPCommand((String) message);

            } else if(message instanceof CommandMessage) {
                log.debug("Receive message: " + (CommandMessage)message + " " + getSelf().path().toStringWithoutAddress());
                processCommand((CommandMessage) message);

            } else if (message instanceof CreateTrackMessage) {
                log.debug("Receive message: " + (CreateTrackMessage)message + " " + getSelf().path().toStringWithoutAddress());
                createTrack((CreateTrackMessage)message);

            } else if (message instanceof RemoveTrackMessage) {
                log.debug("Receive message: " + (RemoveTrackMessage)message + " " + getSelf().path().toStringWithoutAddress());
                removeTrack((RemoveTrackMessage)message);

            } else {
                log.debug("Warn: Receive message: " + message + " " + getSelf().path().toStringWithoutAddress());
                getSender().tell("{\"status\":415,\"body\":\"\"}", getSelf());
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            getSender().tell("{\"status\":415,\"body\":\"\"}", getSelf());
        }
    }

    private void removeTrack(RemoveTrackMessage message) {
        getContext().stop(getContext().getChild(message.name));
        getSender().tell("{\"status\":200,\"body\":\"\"}", getSelf());
    }

    private void createTrack(CreateTrackMessage message) {
        try {

            ActorRef ref = getContext().actorOf(Props.create(Tracker.class, message.info), message.info.getName());
            getSender().tell(new TrackMessageResult(true, message.info), getSelf());

            IsRegistMessage isRegistMessage = new IsRegistMessage(true, ref.path().toString(), ref, 10);
            listProcessor.tell(isRegistMessage, getSelf());

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        }
    }

    protected void processHTTPCommand(String message) {

        try {
            JSONObject messageJson = JSON.parseObject(message);
            String method = messageJson.getString("method");

            if (Statics.isNullOrEmpty(method)) {
                log.info("method is null, message= " + message);
                getSender().tell("{\"status\":415,\"body\":\"\"}", getSelf());
                return;
            }

            switch (method) {

//                case HTTPCommand.POST:
//
//                    break;

                case HTTPCommand.GET:
                    list(messageJson);
                    break;

                default:
                    getSender().tell("{\"status\":415,\"body\":\"\"}", getSelf());

            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        }
    }

    protected void processCommand(CommandMessage message) {

        try {
            String operation = message.getOperation();
            if(Statics.isNullOrEmpty(operation)) {
                message.getCallback().tell("{\"status\":415}", getSelf());
                return;
            }

            switch (operation) {

                case Operation.DELETE:
                    getContext().stop(getSender());
                    message.getCallback().tell("{\"status\":200}", getSelf());
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


    /**
     * 创建导航空间时，通知事件：创建跟踪器
     * @param navName
     */
    protected void create(String navName) {

        try {

            // 跟踪器是否已经创建
            Option<ActorRef> navOption = getContext().child(navName);

            if(navOption.isEmpty()) {

                ActorRef actorRef = getContext().actorOf(Props.create(Tracker.class), navName);

                // 注册
                IsRegistMessage isRegistMessage = new IsRegistMessage(true, actorRef.path().toString(), actorRef, 10);
                listProcessor.tell(isRegistMessage, getSelf());

                // 确认
                actorRef.tell(new CommandMessage(Operation.CREATE, 10, null, getSender()), getSelf());

            } else {
                getSender().tell("{\"status\":201,\"body\":\"\"}", getSelf());
            }


        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            // TODO HTTP 4xx
            getSender().tell("{\"status\":400,\"body\":\"\"}", getSelf());
        }
    }
}
