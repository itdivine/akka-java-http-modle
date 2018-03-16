package cn.xiaoneng.skyeye.enterprise.actor;

import akka.actor.*;
import akka.cluster.sharding.ShardRegion;
import akka.http.javadsl.model.StatusCodes;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.RecoveryCompleted;
import akka.persistence.SnapshotOffer;
import cn.xiaoneng.skyeye.access.code.CustomStateCode;
import cn.xiaoneng.skyeye.access.controller.EvsManagerController;
import cn.xiaoneng.skyeye.enterprise.bean.EVSInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.xiaoneng.skyeye.enterprise.message.EVSProtocal.*;

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

        try {
            String siteId = getSelf().path().name();
            log.info("EVS init success " + siteId);
//

            //百度关键词次数从配额中获取
//            long keyWordQuota = evsInfo.getQuota().getBaidu_keyword_count();
//            getContext().actorOf(Props.createEVS(Collector.class, new Object[]{evsInfo.getSiteId(), CollectorStatus.ON, keyWordQuota}), ActorNames.COLLECTOR);

            Thread.sleep(100);

//            getContext().actorOf(Props.createEVS(NavigationSpaceManager.class), ActorNames.NavigationManager);
//            getContext().actorOf(Props.createEVS(BodySpaceManager.class), ActorNames.BODYSPACEMANAGER);
//            getContext().actorOf(Props.createEVS(TrackerManager.class), ActorNames.TrackerManager);
//            getContext().actorOf(Props.createEVS(FunActor.class), ActorNames.AUTH);
//            getContext().actorOf(Props.createEVS(KafkaManager.class), ActorNames.KafkaManager);


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
                //.match(Create.class, msg -> this.createEVS(((Create)msg).evsInfo)) //事件源恢复
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
                .matchAny(msg -> log.info("EVS unhandled: " + msg))
                .build();
    }

    private void createEVS(EVSInfo evsInfo) {
        log.debug("createEVS: " + evsInfo);
        if(this.evsInfo == null) {
            updateEVS(evsInfo);
        } else {
            getSender().tell(new Result(StatusCodes.CREATED, evsInfo), getSelf());
        }
    }

    private void updateEVS(EVSInfo evsInfo) {
        log.debug("updateEVS: " + evsInfo);
        this.evsInfo = evsInfo;
        saveSnapshot(evsInfo);
        log.info("saveSnapshot: " + evsInfo);
        getSender().tell(new Result(StatusCodes.OK, evsInfo), getSelf());
    }

    private void getEVS() {
        if(evsInfo == null) {
            getSender().tell(new Result(CustomStateCode.EVS_NOT_EXSIT, evsInfo), getSelf());
            getContext().parent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
        } else {
            getSender().tell(new Result(StatusCodes.OK, evsInfo), getSelf());
        }
    }

    public void deleteEVS(Delete msg) {
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





