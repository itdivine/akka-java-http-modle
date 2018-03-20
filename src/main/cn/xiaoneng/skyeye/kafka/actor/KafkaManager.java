package cn.xiaoneng.skyeye.kafka.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.RoundRobinPool;
import cn.xiaoneng.skyeye.kafka.bean.ChatOrder;
import cn.xiaoneng.skyeye.kafka.service.RealTimeAnalyzeService;
import cn.xiaoneng.skyeye.util.COMMON;
import cn.xiaoneng.skyeye.util.PVMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by xuyang on 2016/11/3.
 */
public class KafkaManager extends AbstractActor {

    private ActorRef serviceRouter = null;

    protected final static Logger log = LoggerFactory.getLogger(KafkaManager.class);


    @Override
    public void preStart() throws Exception {
        initSpace();
        super.preStart();
    }

    /**
     * 创建业务处理路由*
     *
     * @return
     */
    private void initSpace() {

        if (serviceRouter == null) {

            serviceRouter = getContext().actorOf(new RoundRobinPool(COMMON.KAFKA_ServiceSize).props(Props.create(RealTimeAnalyzeService.class)), "router");
        }
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .matchAny(this::onReceive)
                .build();
    }
    public void onReceive(Object message) {

        try {

            log.info("Receive message: " + message + " " + getSender());

           if(message instanceof PVMessage) {
               serviceRouter.tell(message, getSender());
           }
           else if(message instanceof ChatOrder) {
               serviceRouter.tell(message, getSender());
           }
           else {
                log.error("不支持的消息类型: " + message);
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        }
    }

}