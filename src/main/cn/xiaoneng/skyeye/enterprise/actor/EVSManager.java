package cn.xiaoneng.skyeye.enterprise.actor;

import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.ClusterSharding;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.RecoveryCompleted;
import akka.persistence.SnapshotOffer;
import cn.xiaoneng.skyeye.enterprise.bean.EVSInfo;
import static cn.xiaoneng.skyeye.enterprise.message.EVSProtocal.*;
import cn.xiaoneng.skyeye.util.Statics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * 企业虚拟空间管理器 - 集群单例
 * <p>
 * Created by Administrator on 2016/7/25.
 */
public class EVSManager extends AbstractPersistentActor {

    protected final Logger log = LoggerFactory.getLogger(getSelf().path().toStringWithoutAddress());

    //EVS分片
//    private final ActorRef evsRegion;
    private ActorRef shardRegion = ClusterSharding.get(getContext().getSystem()).shardRegion("EVS");

//    protected ActorRef listProcessor = getContext().actorOf(Props.create(ListProcessor.class), ActorNames.ListProcessor);

    // siteId EVSActorRef 可以被优化掉，通过判断子EVS Actor是否存在
    private static Map<String,Create> evsMap = new HashMap<>();

    @Override
    public String persistenceId() {
        log.info("persistenceId = " + this.getSelf().path().toStringWithoutAddress());
        return this.getSelf().path().toStringWithoutAddress();
    }

    @Override
    public void preStart() throws Exception {

        // /user/enterprises
        log.info("EVSManager init success, path = " + getSelf().path().toStringWithoutAddress());
        log.info("EVSManager init success, path = " + getSelf().path());

        super.preStart();

        //中介者模式(Mediator)：用一个中介对象来分装一系列的对象交互。中介者使各对象不需要显示地相互引用，从而使其耦合松散
        //订阅集群事件
        ActorRef mediator = DistributedPubSub.get(this.getContext().system()).mediator();
        mediator.tell(new DistributedPubSubMediator.Subscribe(getSelf().path().toStringWithoutAddress(), "NSkyEye", getSelf()), getSelf());

    }

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
                .match(SnapshotOffer.class, s -> this.evsMap = (Map)s.snapshot())
                .match(RecoveryCompleted.class, msg -> log.info("EVSManager RecoveryCompleted: " + evsMap))
                .matchAny(msg -> log.info("EVSManager unhandled: " + msg))
                .build();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchAny(msg -> onReceive(msg))
                .build();
    }

    public void onReceive(Object message) {

        try {
            log.info("Receive message: " + getSender());

            if (message instanceof EVSListGet) {
                list((EVSListGet)message);
            } else if (message instanceof Create)  {
                createEVS((Create)message);
            } else if (message instanceof Get)  {
//                ActorRef shardRegion = ClusterSharding.get(getContext().getSystem()).shardRegion("EVS");
                shardRegion.tell(message, getSender());
            } else if (message instanceof Delete)  {
//                evsMap.remove(((EVS.Delete) message).siteId);
            } else {
                getSender().tell("{\"code\":40001,\"body\":\"请求资源不存在\"}", getSelf());
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            getSender().tell("{\"code\":40001,\"body\":\"请求资源不存在\"}", getSelf());
        }
    }

    protected void createEVS(Create message) {

        try {
            EVSInfo evsInfo = message.evsInfo;

            String siteId = evsInfo.getSiteId();
            if (Statics.isNullOrEmpty(siteId)) {
                getSender().tell("{\"code\":400,\"body\":\"\"}", getSelf());
                return;
            }

            shardRegion.tell(message, getSender());

            evsMap.put(siteId, message);
            saveSnapshot(evsMap);

            /*if (evsMap.contains(evsInfo.getSiteId())) {
                //企业已经被创建，返回201
                getSender().tell(new EVS.Result(201, null), getSelf());

            } else {
                //单服务节点创建企业
                //ActorRef evsRegion = getContext().actorOf(Props.createEVS(EVS.class), evsInfo.getSiteId());

                //分片创建企业
//                evsRegion.tell(new EVS.Create(evsInfo), getSender());
                evsRegion.tell(message, getSender());
                evsMap.add(evsInfo.getSiteId());

//                IsRegistMessage isRegistMessage = new IsRegistMessage(true, evsRegion.path().toString(), evsRegion, 10);
//                listProcessor.tell(isRegistMessage, getSelf());
            }*/

            //getSender().tell(new EVS.Result(true, evsInfo), getSelf());

            //initDefaultSourceScript(evsInfo.getSiteId());

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            getSender().tell("{\"code\":400,\"body\":\"\"}", getSelf());
        }
    }

    /**
     * 查询子Actor列表
     */
    protected void list(EVSListGet message) {

        try {
            int page = message.page;
            int per_page = message.per_page;
//            ListMessage listMessage = new ListMessage(page, per_page, 10);
//            listProcessor.forward(listMessage, getContext());

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            getSender().tell("{\"code\":40001,\"body\":\"请求资源不存在\"}", getSelf());
        }
    }
}