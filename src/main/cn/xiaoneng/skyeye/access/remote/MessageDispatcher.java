package cn.xiaoneng.skyeye.access.remote;

import akka.actor.*;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.ClusterSharding;
import akka.pattern.Patterns;
import akka.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

/**
 * 单例类
 *    负责把control的消息发送给Server
 *
 * Created by XuYang on 2017/8/27.
 */
public class MessageDispatcher {

    protected final static Logger log = LoggerFactory.getLogger(MessageDispatcher.class);

    private ActorSystem system;
    private ActorRef mediator; //总线
    private Timeout timeout = new Timeout(Duration.create(5000, "millisecond"));

    private static class MessageDispatcherHolder {
        public static MessageDispatcher instance = new MessageDispatcher();
    }

    private MessageDispatcher() {}

    public static MessageDispatcher getInstance() {
        return MessageDispatcherHolder.instance;
    }

    public void init(ActorSystem system) {
        this.system = system;
        system.actorOf(Props.create(ClusterListener.class), "clusterListener");//集群监听
        mediator = DistributedPubSub.get(system).mediator();
    }

    /**
     * 通过总线发布消息
     * @param message 消息内容
     * @return
     */
    public Object publishMsg(Message message) {
        Object receiveMessage = null;
        try {
            DistributedPubSubMediator.Publish publishMsg = new DistributedPubSubMediator.Publish(message.getActorPath(), message.getBody(), true);
            Future<Object> futureResult = Patterns.ask(mediator, publishMsg, timeout);
            receiveMessage = Await.result(futureResult, timeout.duration());
        } catch (Exception e) {
            log.error("Exception: " + e.getMessage());
        }
        return receiveMessage;
    }

    /**
     * ActorSelect发送消息
     * @param message 消息内容
     * @return
     */
    public Object sendMsg(Message message) {
        Object receiveMessage = null;
        try {
            ActorSelection actor = system.actorSelection(message.getActorPath());
            Future<Object> futureResult = Patterns.ask(actor, message.getBody(), timeout);
            receiveMessage = Await.result(futureResult, timeout.duration());
        } catch (Exception e) {
            log.error("Exception: " + e.getMessage());
        }
        return receiveMessage;
    }

    /**
     * 通过ShardRegion给分片Actor发送消息
     * @param message 消息内容
     * @param typeName Actor类型名
     * @return
     */
    public Object sendShardMsg(Message message, String typeName) {
        Object receiveMessage = null;
        try {
            ActorRef shardRegion = ClusterSharding.get(system).shardRegion(typeName);
            Future<Object> futureResult = Patterns.ask(shardRegion, message.getBody(), timeout);
            receiveMessage = Await.result(futureResult, timeout.duration());
        } catch (Exception e) {
            log.error("Exception: " + e.getMessage());
        }
        return receiveMessage;
    }
}
