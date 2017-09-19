package cn.xiaoneng.skyeye.enterprise.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import cn.xiaoneng.skyeye.access.Message.EVSProtocol.EVSListGet;
import cn.xiaoneng.skyeye.access.remote.Message;
import cn.xiaoneng.skyeye.temple.ListMessage;
import cn.xiaoneng.skyeye.temple.ListProcessor;
import cn.xiaoneng.skyeye.util.ActorNames;
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
                .match(Message.class, msg -> onReceive(msg))
                .matchAny(msg -> onReceive(msg))
                .build();
    }

    public void onReceive(Object message) {

        try {
            log.info("Receive message: " + getSender());

            if (message instanceof EVSListGet) {

                list((EVSListGet)message);
//                getSender().tell("{\"code\":200,\"body\":\"{status:success}\"}", getSelf());
//                processHTTPCommand((String) message);

            } else {
                unhandled(message);
//                getSender().tell("{\"status\":415,\"body\":\"\"}", getSelf());
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            getSender().tell("{\"status\":415,\"body\":\"\"}", getSelf());
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
            // TODO HTTP 4xx
        }
    }
}