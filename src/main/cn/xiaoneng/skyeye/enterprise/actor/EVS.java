package cn.xiaoneng.skyeye.enterprise.actor;

import akka.actor.*;
import akka.cluster.sharding.ShardRegion;
import akka.http.javadsl.model.StatusCodes;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.RecoveryCompleted;
import akka.persistence.SaveSnapshotSuccess;
import akka.persistence.SnapshotOffer;
import cn.xiaoneng.skyeye.access.Message.CollectorProtocal;
import cn.xiaoneng.skyeye.access.code.CustomStateCode;
import cn.xiaoneng.skyeye.access.controller.EvsManagerController;
import cn.xiaoneng.skyeye.bodyspace.actor.BodySpaceManager;
import cn.xiaoneng.skyeye.collector.actor.Collector;
import cn.xiaoneng.skyeye.collector.util.CollectorStatus;
import cn.xiaoneng.skyeye.enterprise.bean.EVSInfo;
import cn.xiaoneng.skyeye.navigation.actor.NavigationSpaceManager;
import cn.xiaoneng.skyeye.track.actor.TrackerManager;
import cn.xiaoneng.skyeye.util.ActorNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.xiaoneng.skyeye.access.Message.EVSProtocal.*;

/**
 * 企业虚拟空间 - 企业单例
 *     分片、可恢复、负载均衡
 *
 * Created by xy on 2016/7/25.
 */
public class EVS extends AbstractPersistentActor {

    private EVSInfo evsInfo;
    protected final Logger log = LoggerFactory.getLogger(getSelf().path().toStringWithoutAddress());

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

    /**
     * 初始化主体空间管理器、导航空间管理器、跟踪记录管理器、采集器
     */
    private void init() {
        String siteId = getSelf().path().name();
        log.info("EVS init success " + siteId);

        //激活Collector失败
//        String path = getSelf().path() + "/" + ActorNames.COLLECTOR;
//        getContext().actorSelection(path).tell(CollectorProtocal.Get.class, getSender());
//        getContext().actorOf(Props.create(Collector.class), ActorNames.COLLECTOR);
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
                //.match(Create.class, msg -> this.createEVS(((Create)msg).info)) //事件源恢复
                .match(RecoveryCompleted.class, msg -> log.info("EVS RecoveryCompleted: " + evsInfo))
                .matchAny(msg -> log.info("EVS unhandled: " + msg))
                .build();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Create.class, msg -> createEVS(msg.evsInfo))
                .match(Update.class, msg -> updateEVS(msg.evsInfo))
                .match(Get.class, msg -> getEVS())
                .match(Delete.class, msg -> deleteEVS(msg))
                .match(SaveSnapshotSuccess.class, msg -> log.info("EVS SaveSnapshotSuccess: " + evsInfo))
                .matchAny(msg -> log.info("EVS unhandled: " + msg))
                .build();
    }

    private void createEVS(EVSInfo evsInfo) {
        log.debug("createEVS: " + evsInfo);
        if(this.evsInfo == null) {
            updateEVS(evsInfo);
            createEVSSonActor();
        } else {
            getSender().tell(new Result(StatusCodes.CREATED, evsInfo), getSelf());
        }
    }

    private void createEVSSonActor() {
        //百度关键词次数从配额中获取
//            long keyWordQuota = info.getQuota().getBaidu_keyword_count();
        getContext().actorOf(Props.create(Collector.class, new Object[]{evsInfo.getSiteId(), CollectorStatus.ON}), ActorNames.COLLECTOR);
        getContext().actorOf(Props.create(BodySpaceManager.class), ActorNames.BODYSPACEMANAGER);
        getContext().actorOf(Props.create(TrackerManager.class), ActorNames.TrackerManager);
        getContext().actorOf(Props.create(NavigationSpaceManager.class), ActorNames.NavigationManager);

//            getContext().actorOf(Props.create(FunActor.class), ActorNames.AUTH);
//            getContext().actorOf(Props.create(KafkaManager.class), ActorNames.KafkaManager);
    }

    private void updateEVS(EVSInfo evsInfo) {
        log.debug("updateEVS: " + evsInfo);
        this.evsInfo = evsInfo;
        saveSnapshot(evsInfo);
        getSender().tell(new Result(StatusCodes.OK, evsInfo), getSelf());
    }

    private void getEVS() {
        if(evsInfo == null) {
            getSender().tell(new Result(CustomStateCode.NOT_EXSIT, evsInfo), getSelf());
            getContext().parent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
        } else {
            getSender().tell(new Result(StatusCodes.OK, evsInfo), getSelf());
        }
    }

    private void deleteEVS(Delete msg) {
        log.debug("deleteEVS: " + msg.siteId);
        //1.通知EvsManager删除缓存列表中的siteId
        ActorSelection actor = getContext().getSystem().actorSelection(EvsManagerController.enterprisesProxyPath);
        actor.tell(msg, getSelf());
        //2.停止EVS Actor
        getContext().parent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
        //3.返回结果
        getSender().tell(new Result(StatusCodes.OK, evsInfo), getSelf());
    }
}