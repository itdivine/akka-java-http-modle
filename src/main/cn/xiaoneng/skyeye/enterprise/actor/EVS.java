package cn.xiaoneng.skyeye.enterprise.actor;

import akka.actor.*;
import akka.cluster.sharding.ShardRegion;
import akka.http.javadsl.model.StatusCode;
import akka.http.javadsl.model.StatusCodes;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.RecoveryCompleted;
import akka.persistence.SnapshotOffer;
import cn.xiaoneng.skyeye.access.code.CustomStateCode;
import cn.xiaoneng.skyeye.access.controller.EvsManagerController;
import cn.xiaoneng.skyeye.base.BaseMessage;
import cn.xiaoneng.skyeye.enterprise.bean.EVSInfo;
import cn.xiaoneng.skyeye.enterprise.message.IsRegistMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 企业虚拟空间 - 企业单例
 * Created by Administrator on 2016/7/25.
 */

public class EVS extends AbstractPersistentActor {

    protected final Logger log = LoggerFactory.getLogger(getSelf().path().toStringWithoutAddress());

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

    public static final class Create extends BaseMessage {
        public final EVSInfo evsInfo;
        public Create(EVSInfo evsInfo) {
            this.evsInfo = evsInfo;
        }
    }

    public static final class Update extends BaseMessage {
        public final EVSInfo evsInfo;
        public Update(EVSInfo evsInfo) {
            this.evsInfo = evsInfo;
        }
    }

    public static final class Get extends BaseMessage {
        public final String siteId;
        public Get(String siteId) {
            this.siteId = siteId;
        }
    }
    public static final class Delete extends BaseMessage {
        public final String siteId;
        public Delete(String siteId) {
            this.siteId = siteId;
        }
    }
    /**
     * 查询导航空间列表
     */
    public static final class EVSListGet extends BaseMessage {
        public final int page;
        public final int per_page;
        public EVSListGet(int page, int per_page) {
            this.page = page;
            this.per_page = per_page;
        }
        @Override
        public String toString() {
            return "EVSListGet{" + "page=" + page + ", per_page=" + per_page + '}';
        }
    }

    public static final class Result extends BaseMessage {
        public final StatusCode code;
        public final EVSInfo evsInfo;
        public Result(StatusCode code, EVSInfo evsInfo) {
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
            log.info("EVS init success, path = " + path);

//            String siteId = path.name();



            //订阅集群事件：可能不需要了，没有Actor会通过路径访问
//            ActorRef mediator = DistributedPubSub.get(this.getContext().system()).mediator();
//            mediator.tell(new DistributedPubSubMediator.Subscribe(siteId, ActorNames.NSkyEye, getSelf()), getSelf());

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
                //事件源恢复
                //.match(Create.class, msg -> this.createEVS(((Create)msg).evsInfo))
                .match(RecoveryCompleted.class, msg -> log.info("EVS RecoveryCompleted: " + evsInfo))
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
                .matchAny(msg -> log.info("EVS matchAny: " + msg))
                .build();
    }

    public void createEVS(EVSInfo evsInfo) {
        log.debug("createEVS: " + evsInfo);
        if(this.evsInfo != null) {
            getSender().tell(new EVS.Result(StatusCodes.OK, evsInfo), getSelf());
            return;
        } else {
            this.evsInfo = evsInfo;
            getSender().tell(new EVS.Result(StatusCodes.OK, evsInfo), getSelf());

            //注册企业
            ActorSelection actor = getContext().getSystem().actorSelection("/user/enterprises/singleton/listProcessor");
            actor.tell(new IsRegistMessage(true, getSelf().path().toString(), getSelf(), 10), getSelf());

            saveSnapshot(evsInfo);
        }
    }

    public void updateEVS(EVSInfo evsInfo) {
        log.debug("updateEVS: " + evsInfo);
        this.evsInfo = evsInfo;
        getSender().tell(new EVS.Result(StatusCodes.OK, evsInfo), getSelf());
        saveSnapshot(evsInfo);
    }

    public void getEVS() {
        if(evsInfo == null) {
            getSender().tell(new EVS.Result(CustomStateCode.EVS_NOT_EXSIT, evsInfo), getSelf());
            getContext().parent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
        }
        else {
            getSender().tell(new EVS.Result(StatusCodes.OK, evsInfo), getSelf());
        }
    }

    public void deleteEVS(Delete msg) {
        log.debug("deleteEVS: " + evsInfo.getSiteId());
        //1.通知EvsManager删除缓存列表中的siteId
        ActorSelection actor = getContext().getSystem().actorSelection(EvsManagerController.enterprisesProxyPath);
        actor.tell(msg, getSelf());
        //2.停止EVS Actor
        getContext().parent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
        //3.返回结果
        getSender().tell(new EVS.Result(StatusCodes.OK, evsInfo), getSelf());
    }
}





