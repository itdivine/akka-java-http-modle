package cn.xiaoneng.skyeye.enterprise.actor;

import akka.actor.ActorRef;
import akka.cluster.sharding.ClusterSharding;
import akka.http.javadsl.model.StatusCodes;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.RecoveryCompleted;
import akka.persistence.SaveSnapshotSuccess;
import akka.persistence.SnapshotOffer;
import static cn.xiaoneng.skyeye.access.Message.EVSProtocal.*;
import cn.xiaoneng.skyeye.util.ActorNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 企业虚拟空间管理器 - 集群单例
 *
 * Created by xy on 2016/7/25.
 */
public class EVSManager extends AbstractPersistentActor {

    protected final Logger log = LoggerFactory.getLogger(getSelf().path().toStringWithoutAddress());

    private ActorRef shardRegion = ClusterSharding.get(getContext().getSystem()).shardRegion(ActorNames.EVS);


    /**
     * 开启的企业集合
     */
    private Set<String> siteIds = new HashSet<>();

    @Override
    public String persistenceId() {
        log.debug("persistenceId = " + this.getSelf().path().toStringWithoutAddress());
        return this.getSelf().path().toStringWithoutAddress();
    }

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
                .match(SnapshotOffer.class, s -> this.siteIds = (Set)s.snapshot())
                .match(RecoveryCompleted.class, msg -> log.info("EVSManager RecoveryCompleted: " + siteIds))
                .matchAny(msg -> log.info("EVSManager unhandled: " + msg))
                .build();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Create.class, msg -> createEVS(msg))
                .match(Get.class, msg -> shardRegion.tell(msg, getSender()))
                .match(EVSListGet.class, msg -> list())
                .match(Delete.class, msg -> delEVS(msg))
                .match(SaveSnapshotSuccess.class, msg -> log.info("EVSManager SaveSnapshotSuccess: " + siteIds))
                .matchAny(msg -> log.info("unhandled msg: " + msg))
                .build();
    }

    /**
     * 删除企业
     * @param msg
     */
    private void delEVS(Delete msg) {
        siteIds.remove(msg.siteId);
        saveSnapshot(siteIds);
    }

    /**
     * 创建企业
     * @param message
     */
    protected void createEVS(Create message) {
        try {
            String siteId = message.evsInfo.getSiteId();
            if(siteIds.contains(siteId)) {
                getSender().tell(new Result(StatusCodes.CREATED, null), getSelf());
            } else {
                siteIds.add(siteId);
                shardRegion.tell(message, getSender());
                saveSnapshot(siteIds);
            }
        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            getSender().tell(new Result(StatusCodes.BAD_REQUEST, null), getSelf());
        }
    }

    /**
     * 查询开启的企业列表
     */
    private void list() {
        getSender().tell(new EVSListResult(StatusCodes.OK, siteIds), getSelf());
    }
}