package cn.xiaoneng.nskyeye.access;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import cn.xiaoneng.nskyeye.access.example.bean.EVS;
import cn.xiaoneng.nskyeye.access.remote.Message;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.runtime.Statics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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

            if (message instanceof Message) {

                getSender().tell("{\"status\":200,\"body\":\"success\"}", getSelf());
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
}