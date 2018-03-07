package cn.xiaoneng.skyeye.enterprise.actor;

import akka.actor.*;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.ShardRegion;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.SnapshotOffer;
import cn.xiaoneng.skyeye.access.controller.EvsManagerControl;
import cn.xiaoneng.skyeye.enterprise.bean.EVSInfo;
import cn.xiaoneng.skyeye.enterprise.message.EVSCommandMessage;
import cn.xiaoneng.skyeye.enterprise.message.IsRegistMessage;
import cn.xiaoneng.skyeye.util.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;



/**
 * 企业虚拟空间 - 企业单例
 * Created by Administrator on 2016/7/25.
 */

public class EVS extends AbstractPersistentActor {

    protected final Logger log = LoggerFactory.getLogger(getSelf().path().toStringWithoutAddress());

//    private String siteId;
    private EVSInfo evsInfo;

//    private ActorRef mediator;

//    private ActorRef evsStore; //@test 注释存储

    public EVS() {}

    @Override
    public String persistenceId() {
        log.info("persistenceId = " + this.getSelf().path().toStringWithoutAddress());
        return this.getSelf().path().toStringWithoutAddress();
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        init();
    }

    public static final class Create implements Serializable {
        public final EVSInfo evsInfo;
        public Create(EVSInfo evsInfo) {
            this.evsInfo = evsInfo;
        }
    }

    public static final class Update implements Serializable {
        public final EVSInfo evsInfo;
        public Update(EVSInfo evsInfo) {
            this.evsInfo = evsInfo;
        }
    }

    public static final class Get implements Serializable {
        public final String siteId;
        public Get(String siteId) {
            this.siteId = siteId;
        }
    }
    public static final class Delete implements Serializable {
        public final String siteId;
        public Delete(String siteId) {
            this.siteId = siteId;
        }
    }

    public static final class Result implements Serializable {
        public final int code;
        public final EVSInfo evsInfo;
        public Result(int code, EVSInfo evsInfo) {
            this.code = code;
            this.evsInfo = evsInfo;
        }
    }

    /**
     * 初始化主体空间管理器、导航空间管理器、跟踪记录管理器、采集器
     */
    private void init() {

        try {
            ActorPath path = getSelf().path();
            String siteId = path.name();
            log.info("EVS init success, path = " + path);


            //订阅集群事件：可能不需要了，没有Actor会通过路径访问
            ActorRef mediator = DistributedPubSub.get(this.getContext().system()).mediator();
            mediator.tell(new DistributedPubSubMediator.Subscribe(siteId, ActorNames.NSkyEye, getSelf()), getSelf());

//            evsStore = getContext().actorOf(Props.createEVS(EVSStoreActor.class), ActorNames.EVSSERVICE);
//            evsStore.tell(new EVSCommandMessage(Operation.CREATE, 10, evsInfo), getSelf());

            //百度关键词次数从配额中获取
//            long keyWordCount = evsInfo.getQuota().getBaidu_keyword_count();
//            getContext().actorOf(Props.createEVS(Collector.class, new Object[]{evsInfo.getSiteId(), CollectorStatus.ON, keyWordCount}), ActorNames.COLLECTOR);

            Thread.sleep(100);

//            getContext().actorOf(Props.createEVS(NavigationSpaceManager.class), ActorNames.NavigationManager);
//            getContext().actorOf(Props.createEVS(BodySpaceManager.class), ActorNames.BODYSPACEMANAGER);
//            getContext().actorOf(Props.createEVS(TrackerManager.class), ActorNames.TrackerManager);
//            getContext().actorOf(Props.createEVS(FunActor.class), ActorNames.AUTH);
//            getContext().actorOf(Props.createEVS(KafkaManager.class), ActorNames.KafkaManager);

            //发布企业信息
//            EvsInfoChangedEvent event = new EvsInfoChangedEvent(evsInfo);
//            this.getContext().system().eventStream().publish(event);

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }
    }

    /**
     * actor崩溃后恢复策略
     * 1.根据快照恢复最近状态信息
     * 2.根据事件源恢复状态信息到崩溃前
     */
    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
                .match(SnapshotOffer.class, s -> this.evsInfo = (EVSInfo)s.snapshot())
//                .match(Create.class, msg -> this.createEVS(((Create)msg).evsInfo))
                .matchAny(msg -> log.info("EVS unhandled: " + msg))
                .build();
    }

    @Override
    public Receive createReceive() {

        log.debug("EVS: " + getSelf().path());

        return receiveBuilder()
                .match(Create.class, msg -> this.createEVS(msg.evsInfo))
                .match(Update.class, msg -> this.updateEVS(msg.evsInfo))
                .match(Get.class, msg -> getEVS())
                .match(Delete.class, msg -> deleteEVS(msg))
                .match(String.class, msg -> processHTTPCommand(msg))
                .match(CommandMessage.class, msg -> processCommandMessage(msg))
                .matchAny(msg -> log.info("EVS matchAny: " + msg))
                .build();
    }

    public void createEVS(EVSInfo evsInfo) {
        log.debug("createEVS: " + evsInfo);
        this.evsInfo = evsInfo;
        getSender().tell(new EVS.Result(200, evsInfo), getSelf());

        //注册企业
        ActorSelection actor = getContext().getSystem().actorSelection("/user/enterprises/singleton/listProcessor");
        actor.tell(new IsRegistMessage(true, getSelf().path().toString(), getSelf(), 10), getSelf());

        saveSnapshot(evsInfo);
    }

    public void updateEVS(EVSInfo evsInfo) {
        log.debug("updateEVS: " + evsInfo);
        this.evsInfo = evsInfo;
        getSender().tell(new EVS.Result(200, evsInfo), getSelf());
        saveSnapshot(evsInfo);
    }

    public void getEVS() {
        log.debug("getEVS: " + evsInfo.getSiteId());
        getSender().tell(new EVS.Result(200, evsInfo), getSelf());
    }

    public void deleteEVS(Delete msg) {
        log.debug("deleteEVS: " + evsInfo.getSiteId());
        //1.通知EvsManager删除缓存列表中的siteId
        ActorSelection actor = getContext().getSystem().actorSelection(EvsManagerControl.enterprisesProxyPath);
        actor.tell(msg, getSelf());
        //2.停止EVS Actor
        getContext().parent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
        //3.返回结果
        getSender().tell(new EVS.Result(200, evsInfo), getSelf());
    }

    private void processHTTPCommand(String message) {

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

                // 查询企业信息
                case HTTPCommand.GET:
                    getSender().tell("{\"status\":200,\"body\":" + evsInfo.toJSONString() + "}", getSelf());
                    break;

                case HTTPCommand.PUT:
                case HTTPCommand.PATCH:

                    JSONObject bodyJson = messageJson.getJSONObject("body");
                    EVSInfo info = JSON.parseObject(bodyJson.toString(), EVSInfo.class);
                    evsInfo.update(info);
                    getSender().tell("{\"status\":200,\"body\":" + evsInfo.toJSONString() + "}", getSelf());

//                    EVSCommandMessage updateEVS = new EVSCommandMessage(Operation.UPDATE, 10, evsInfo);
//                    evsStore.tell(updateEVS, getSelf());

                    //发布企业信息
//                    EvsInfoChangedEvent event = new EvsInfoChangedEvent(evsInfo);
//                    this.getContext().system().eventStream().publish(event);

                    break;

                case HTTPCommand.DELETE:
                    //DB中删除
                    EVSCommandMessage deleteEVS = new EVSCommandMessage(Operation.DELETE, 10, evsInfo);
                    //evsStore.tell(deleteEVS, getSelf());

                    //父actor停止
                    getContext().parent().tell(new CommandMessage(Operation.DELETE, 10, null, getSender()), getSelf());

                    break;

                case HTTPCommand.POST:
                    break;

                default:

            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage() + "  message= " + message);
        }
    }

    private void processCommandMessage(CommandMessage message) {

        try {
            if(message.getOperation().equals(Operation.GET)) {
                DocumentMessage documentMessage = new DocumentMessage(null, 10, evsInfo.toJSONString(), ((CommandMessage)message).getMsgId());
                getSender().tell(documentMessage, getSelf());
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage() + "  message= " + message);
        }
    }

}





