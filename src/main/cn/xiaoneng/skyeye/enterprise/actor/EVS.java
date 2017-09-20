package cn.xiaoneng.skyeye.enterprise.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import cn.xiaoneng.skyeye.enterprise.bean.EVSInfo;
import cn.xiaoneng.skyeye.enterprise.message.EVSCommandMessage;
import cn.xiaoneng.skyeye.util.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

//import cn.xiaoneng.skyeye.auth.actor.FunActor;
//import cn.xiaoneng.skyeye.bodyspace.actor.BodySpaceManager;
//import cn.xiaoneng.skyeye.collector.actor.Collector;
//import cn.xiaoneng.skyeye.collector.util.CollectorStatus;
//import cn.xiaoneng.skyeye.config.db.MySqlDataAccess;
//import cn.xiaoneng.skyeye.enterprise.event.EvsInfoChangedEvent;
//import cn.xiaoneng.skyeye.enterprise.service.EVSStoreActor;


/**
 * 企业虚拟空间 - 企业单例
 * Created by Administrator on 2016/7/25.
 */

public class EVS extends AbstractActor {

    protected final Logger log = LoggerFactory.getLogger(getSelf().path().toStringWithoutAddress());

    private EVSInfo evsInfo;

    private ActorRef mediator;

//    private ActorRef evsStore; //@test 注释存储

    public EVS() {}

    @Override
    public void preStart() throws Exception {
        init();
        super.preStart();
    }



    public static final class Create implements Serializable {
        public final EVSInfo evsInfo;
        public Create(EVSInfo evsInfo) {
            this.evsInfo = evsInfo;

        }
    }

    public static final class Result implements Serializable {
        public final boolean result;
        public final EVSInfo evsInfo;
        public Result(boolean result, EVSInfo evsInfo) {
            this.result = result;
            this.evsInfo = evsInfo;
        }
    }

    /**
     * 初始化主体空间管理器、导航空间管理器、跟踪记录管理器、采集器
     */
    private void init() {

        try {
            log.info("EVS init success, path = " + getSelf().path());

            //订阅集群事件 add by liangyongheng
            mediator = DistributedPubSub.get(this.getContext().system()).mediator();
            mediator.tell(new DistributedPubSubMediator.Subscribe(getSelf().path().toStringWithoutAddress(), ActorNames.NSkyEye, getSelf()), getSelf());

//            evsStore = getContext().actorOf(Props.create(EVSStoreActor.class), ActorNames.EVSSERVICE);
//            evsStore.tell(new EVSCommandMessage(Operation.CREATE, 10, evsInfo), getSelf());

            //百度关键词次数从配额中获取
//            long keyWordCount = evsInfo.getQuota().getBaidu_keyword_count();
//            getContext().actorOf(Props.create(Collector.class, new Object[]{evsInfo.getSiteId(), CollectorStatus.ON, keyWordCount}), ActorNames.COLLECTOR);

            Thread.sleep(100);

//            getContext().actorOf(Props.create(NavigationSpaceManager.class), ActorNames.NavigationManager);
//            getContext().actorOf(Props.create(BodySpaceManager.class), ActorNames.BODYSPACEMANAGER);
//            getContext().actorOf(Props.create(TrackerManager.class), ActorNames.TrackerManager);
//            getContext().actorOf(Props.create(FunActor.class), ActorNames.AUTH);
//            getContext().actorOf(Props.create(KafkaManager.class), ActorNames.KafkaManager);

            //发布企业信息
//            EvsInfoChangedEvent event = new EvsInfoChangedEvent(evsInfo);
//            this.getContext().system().eventStream().publish(event);

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void createEVS(EVSInfo evsInfo) {
        log.info("Receive message: " + evsInfo);
        this.evsInfo = evsInfo;
        getSender().tell(new EVS.Result(true, evsInfo), getSelf());
    }

//    @Override
//    public Receive createReceiveRecover() {
//        return receiveBuilder()
//                .match(Create.class, msg -> this.createEVS(((Create)msg).evsInfo))
//                .build();
//    }

    @Override
    public Receive createReceive() {

        log.info("EVS: " + getSelf().path());

        return receiveBuilder()
                .match(Create.class, msg -> this.createEVS(msg.evsInfo))
                .match(String.class, msg -> processHTTPCommand(msg))
                .match(CommandMessage.class, msg ->{
                    // 查询企业信息
                    DocumentMessage documentMessage = new DocumentMessage(null, 10, evsInfo.toJSONString(), msg.getMsgId());
                    getSender().tell(documentMessage, getSelf());})
                .build();
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

                    EVSCommandMessage updateEVS = new EVSCommandMessage(Operation.UPDATE, 10, evsInfo);
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

}





