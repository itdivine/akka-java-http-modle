package cn.xiaoneng.skyeye.enterprise.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
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
public class EVSManager extends AbstractActor {

    protected final Logger log = LoggerFactory.getLogger(getSelf().path().toStringWithoutAddress());


    //EVS分片
//    private final ActorRef evsRegion;

    private ActorRef mediator;

    protected ActorRef listProcessor;

    // siteId evsActorRef
    private static List<String> evsList = new ArrayList<String>();

//    static ShardRegion.MessageExtractor messageExtractor = new ShardRegion.MessageExtractor() {
//
//        @Override
//        public String entityId(Object message) {
//            if (message instanceof EVS.Create) {
//                System.out.println("entityId: " + ((EVS.Create) message).evsInfo.getSiteId());
//                return String.valueOf(((EVS.Create) message).evsInfo.getSiteId());
//            }
//            else
//                return null;
//        }
//
//        @Override
//        public Object entityMessage(Object message) {
//            if (message instanceof EVS.Create) {
//                System.out.println("entityMessage: " + ((EVS.Create) message).evsInfo.getSiteId());
//                return message;
//            }
//            else
//                return message;
//        }
//
//        @Override
//        public String shardId(Object message) {
//            if (message instanceof EVS.Create) {
//                System.out.println("shardId: " + ((EVS.Create) message).evsInfo.getSiteId());
//                return ((EVS.Create) message).evsInfo.getSiteId();
//            } else {
//                return null;
//            }
//        }
//    };

//    public EVSManager() {
//        //创建Actor分片
//        ActorSystem system = getContext().getSystem();
//        Option<String> roleOption = Option.none();
//        ClusterShardingSettings settings = ClusterShardingSettings.create(system);
//        evsRegion = ClusterSharding.get(system)
//                .start(
//                        "EVS",
//                        Props.create(EVS.class),
//                        settings,
//                        messageExtractor);
//    }



    @Override
    public void preStart() throws Exception {

        log.info("EVSManager init success, path = " + getSelf().path().toStringWithoutAddress());

        super.preStart();
        listProcessor = getContext().actorOf(Props.create(ListProcessor.class), ActorNames.ListProcessor);

        mediator = DistributedPubSub.get(this.getContext().system()).mediator();
        mediator.tell(new DistributedPubSubMediator.Subscribe(getSelf().path().toStringWithoutAddress(), "NSkyEye", getSelf()), getSelf());

    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
//                .match(EVSListGet.class, msg -> list(msg))
                .matchAny(msg -> onReceive(msg))
                .build();
    }

    public void onReceive(Object message) {

        try {
            log.info("Receive message: " + getSender());

            if (message instanceof EVSListGet) {
                list((EVSListGet)message);

            } else if (message instanceof EVS.Create)  {
                create((EVS.Create)message);
            } else {
                getSender().tell("{\"code\":40001,\"body\":\"请求资源不存在\"}", getSelf());
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            getSender().tell("{\"code\":40001,\"body\":\"请求资源不存在\"}", getSelf());
        }
    }

    protected void create(EVS.Create message) {

        try {
            EVSInfo evsInfo = message.evsInfo;

            String siteId = evsInfo.getSiteId();
            if (Statics.isNullOrEmpty(siteId)) {
                getSender().tell("{\"code\":400,\"body\":\"\"}", getSelf());
                return;
            }

            if (evsList.contains(siteId)) {
                getSender().tell(new EVS.Result(201, null), getSelf());

            } else {
                createEVS(evsInfo);

                //getSender().tell(new EVS.Result(true, evsInfo), getSelf());

                //initDefaultSourceScript(evsInfo.getSiteId());
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            getSender().tell("{\"code\":400,\"body\":\"\"}", getSelf());
        }
    }

    private void createEVS(EVSInfo evsInfo) {
        if (evsList.contains(evsInfo.getSiteId())) {
            //企业已经被创建，返回201
            getSender().tell("{\"code\":201,\"body\":\"\"}", getSelf());

        } else {
            ActorRef evsRegion = getContext().actorOf(Props.create(EVS.class), evsInfo.getSiteId());
            evsRegion.tell(new EVS.Create(evsInfo), getSender());
            evsList.add(evsInfo.getSiteId());

            IsRegistMessage isRegistMessage = new IsRegistMessage(true, evsRegion.path().toString(), evsRegion, 10);
            listProcessor.tell(isRegistMessage, getSelf());
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