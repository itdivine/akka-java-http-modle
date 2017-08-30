package cn.xiaoneng.nskyeye.access.remote;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.pattern.Patterns;
import akka.util.Timeout;
import cn.xiaoneng.nskyeye.access.AccessConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

/**
 * Created by XuYang on 2017/8/27.
 */
public class MessageDispatcher {

    protected final static Logger log = LoggerFactory.getLogger(MessageDispatcher.class);

    private ActorSystem system;
    private AccessConfig config;
    private ActorRef mediator; //总线
    private ActorRef clusterListener; //集群监听
    private Timeout timeout = new Timeout(Duration.create(5000, "millisecond"));

    public MessageDispatcher(ActorSystem system, AccessConfig config) {
        this.system = system;
        this.config = config;
//        clusterListener = system.actorOf(Props.create(ClusterListener.class), "clusterListener");
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
            // 发送消息
            DistributedPubSubMediator.Publish publishMsg = new DistributedPubSubMediator.Publish(message.getActorPath(), message, true);
            Future<Object> futureResult = Patterns.ask(mediator, publishMsg, timeout);
            receiveMessage = Await.result(futureResult, timeout.duration());

        } catch (Exception e) {
            log.error("Exception: " + e.getMessage());
        }

        return receiveMessage;
    }

    /**
     * ActorSelect发送消息(Uncheck)
     * @param message 消息内容
     * @return
     */
    public Object sendMsg(Message message) {
        Object receiveMessage = null;
        try {
            ActorSelection actor = system.actorSelection(message.getActorPath());
            Future<Object> futureResult = Patterns.ask(actor, message, timeout);
            receiveMessage = Await.result(futureResult, timeout.duration());
        } catch (Exception e) {
            log.error("Exception: " + e.getMessage());
        }
        return receiveMessage;
    }
}
