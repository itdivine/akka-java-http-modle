package cn.xiaoneng.skyeye.enterprise.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.cluster.sharding.ShardRegion;
import akka.japi.Option;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.RecoveryCompleted;
import akka.persistence.SnapshotOffer;
import cn.xiaoneng.skyeye.access.Message.EVSProtocol.EVSListGet;
import cn.xiaoneng.skyeye.enterprise.bean.EVSInfo;
import cn.xiaoneng.skyeye.enterprise.message.IsRegistMessage;
import cn.xiaoneng.skyeye.temple.ListMessage;
import cn.xiaoneng.skyeye.temple.ListProcessor;
import cn.xiaoneng.skyeye.util.ActorNames;
import cn.xiaoneng.skyeye.util.Statics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 企业虚拟空间管理器 - 集群单例
 * <p>
 * Created by Administrator on 2016/7/25.
 */
public class EVSManager extends AbstractPersistentActor {

    protected final Logger log = LoggerFactory.getLogger(getSelf().path().toStringWithoutAddress());

    //EVS分片
    private final ActorRef evsRegion;

    protected ActorRef listProcessor = getContext().actorOf(Props.create(ListProcessor.class), ActorNames.ListProcessor);

    // siteId EVSActorRef 可以被优化掉，通过判断子EVS Actor是否存在
    private static List<String> evsList = new ArrayList<String>();

    @Override
    public String persistenceId() {
        log.info("persistenceId = " + this.getSelf().path().toStringWithoutAddress());
        return this.getSelf().path().toStringWithoutAddress();
    }

    static ShardRegion.MessageExtractor messageExtractor = new ShardRegion.MessageExtractor() {

        @Override
        public String entityId(Object message) {

            if (message instanceof EVS.Create) {
                return String.valueOf(((EVS.Create) message).evsInfo.getSiteId());

            } else if (message instanceof EVS.Get) {
                return ((EVS.Get) message).siteId;
            }
            return null;
        }

        @Override
        public Object entityMessage(Object message) {
            if (message instanceof EVS.Create)
                return message;
            else
                return message;
        }

        @Override
        public String shardId(Object message) {
            if (message instanceof EVS.Create) {
                String siteId = ((EVS.Create) message).evsInfo.getSiteId();
                return getShardId(siteId);
            } else if (message instanceof EVS.Get) {
                String siteId = ((EVS.Get) message).siteId;
                return getShardId(siteId);
            } else {
                return null;
            }
        }
    };

    private static String getShardId(String siteId) {
        int numberOfShards = 100;
        int shardId = Math.abs(siteId.hashCode() % numberOfShards);
        System.out.println("shardId: " + shardId);
        return String.valueOf(shardId);
    }

    public EVSManager() {
        //创建Actor分片
        ActorSystem system = getContext().getSystem();
        //Option<String> roleOption = Option.none();
        ClusterShardingSettings settings = ClusterShardingSettings.create(system);
        evsRegion = ClusterSharding.get(system)
                .start(
                        "EVS",
                        Props.create(EVS.class),
                        settings,
                        messageExtractor);
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
//                .match(SnapshotOffer.class, s -> evsList = (List<String>)s.snapshot())
                .match(RecoveryCompleted.class, msg -> log.info("EVSManager RecoveryCompleted: " + evsList))
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
            } else if (message instanceof EVS.Create)  {
                createEVS((EVS.Create)message);
            } else if (message instanceof EVS.Get)  {
                evsRegion.tell(message, getSender());
            } else if (message instanceof EVS.Delete)  {
                evsList.remove(((EVS.Delete) message).siteId);
                saveSnapshot(evsList);
            } else {
                getSender().tell("{\"code\":40001,\"body\":\"请求资源不存在\"}", getSelf());
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            getSender().tell("{\"code\":40001,\"body\":\"请求资源不存在\"}", getSelf());
        }
    }

    protected void createEVS(EVS.Create message) {

        try {
            EVSInfo evsInfo = message.evsInfo;

            String siteId = evsInfo.getSiteId();
            if (Statics.isNullOrEmpty(siteId)) {
                getSender().tell("{\"code\":400,\"body\":\"\"}", getSelf());
                return;
            }

            if (evsList.contains(evsInfo.getSiteId())) {
                //企业已经被创建，返回201
                getSender().tell(new EVS.Result(201, null), getSelf());

            } else {
                //单服务节点创建企业
                //ActorRef evsRegion = getContext().actorOf(Props.createEVS(EVS.class), evsInfo.getSiteId());

                //分片创建企业
//                evsRegion.tell(new EVS.Create(evsInfo), getSender());
                evsRegion.tell(message, getSender());
                evsList.add(evsInfo.getSiteId());
                saveSnapshot(evsList);

//                IsRegistMessage isRegistMessage = new IsRegistMessage(true, evsRegion.path().toString(), evsRegion, 10);
//                listProcessor.tell(isRegistMessage, getSelf());
            }

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
            ListMessage listMessage = new ListMessage(page, per_page, 10);
            listProcessor.forward(listMessage, getContext());

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            getSender().tell("{\"code\":40001,\"body\":\"请求资源不存在\"}", getSelf());
        }
    }
}