package remote;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.pattern.Patterns;
import akka.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

/**
 * Created by XuYang on 2017/8/27.
 */
public class AccessAgent {

    protected final static Logger log = LoggerFactory.getLogger(AccessAgent.class);

    private ActorSystem system;
    private AccessConfig config;
    private ActorRef mediator; //总线
    private ActorRef clusterListener; //集群监听

    public AccessAgent(ActorSystem system, AccessConfig config) {
        this.system = system;
        this.config = config;
        clusterListener = system.actorOf(Props.create(ClusterListener.class), "clusterListener");
        mediator = DistributedPubSub.get(system).mediator();
    }


    /**
     * 通过总线发布消息
     * @param message
     * @param actorPath
     * @return
     */
    public Object sendPostMessage(Object message, String actorPath) {

        Object receiveMessage = null;

        try {
            // 发送消息
            Timeout timeout = new Timeout(Duration.create(5000, "millisecond"));

            DistributedPubSubMediator.Publish publishMsg = DistributedPubSubMediator.Publish.apply(actorPath, message);

            Future<Object> futureResult = Patterns.ask(mediator, publishMsg, timeout);


            receiveMessage = Await.result(futureResult, timeout.duration());


        } catch (Exception e) {

        }

        return receiveMessage;

    }
}
