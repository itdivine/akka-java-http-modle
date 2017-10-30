package cn.xiaoneng.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * Created by Cuixuan on 2017/3/9.
 */
public abstract class BaseAbstractActor extends AbstractActor implements IBaseActor,IMediator {

    protected final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    protected String VersionUID = null;
    private Set<String> topics = new HashSet();
    private ActorRef mediator = DistributedPubSub.get(this.getContext().system()).mediator();

    @Override
    public String getVersionId() {
        return this.VersionUID;
    }

    @Override
    public ActorRef getMediator() {
        return this.mediator;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchAny(msg -> onReceive(msg))
                .build();
    }

    @Override
    public void onReceive(Object msg) {
        log.debug("Receive message: [{}]", msg);
    }

    @Override
    public void subscribe(String topic, String group, ActorRef subscriber, ActorRef receiver) {
        if(group == null) {
            this.mediator.tell(new DistributedPubSubMediator.Subscribe(topic, subscriber), receiver);
        } else {
            this.mediator.tell(new DistributedPubSubMediator.Subscribe(topic, group, subscriber), receiver);
        }

        this.topics.add(topic);
    }

    @Override
    public void subscribe(String topic, String group) {
        this.subscribe(topic, group, this.getSelf(), this.getSelf());
    }

    @Override
    public void subscribe(ActorRef actor, ActorRef receiver) {
        String topic = actor.path().toStringWithoutAddress();
        this.subscribe(topic, (String)null, actor, receiver);
    }

    @Override
    public void subscribe(String topic) {
        this.subscribe((String)topic, (String)null);
    }

    @Override
    public void subscribe() {
        this.subscribe(this.getSelf(), this.getSelf());
    }

    @Override
    public void unsubscribe(String topic, String group, ActorRef subscriber, ActorRef receiver) {
        if(group == null) {
            this.mediator.tell(new DistributedPubSubMediator.Unsubscribe(topic, subscriber), receiver);
        } else {
            this.mediator.tell(new DistributedPubSubMediator.Unsubscribe(topic, group, subscriber), receiver);
        }

        this.topics.remove(topic);
    }

    @Override
    public void unsubscribe() {
        this.unsubscribe(this.getSelf(), this.getSelf());
    }

    @Override
    public void unsubscribe(String topic, String group) {
        this.unsubscribe(topic, group, this.getSelf(), ActorRef.noSender());
    }

    @Override
    public void unsubscribe(String topic) {
        this.unsubscribe(topic, (String)null, this.getSelf(), ActorRef.noSender());
    }

    @Override
    public void unsubscribe(ActorRef actor, ActorRef receiver) {
        String topic = actor.path().toStringWithoutAddress();
        this.unsubscribe(topic, (String)null, actor, receiver);
    }

    @Override
    public Set<String> getTopics() {
        return this.topics;
    }

    @Override
    public void publish(String topic, Object msg) {
        this.mediator.tell(new DistributedPubSubMediator.Publish(topic, msg), this.getSelf());
    }

    @Override
    public void publish(String topic, Object msg, ActorRef sender) {
        this.mediator.tell(new DistributedPubSubMediator.Publish(topic, msg), sender);
    }

    @Override
    public void tell(String actorPath, Object msg) {
        this.mediator.tell(new DistributedPubSubMediator.Send(actorPath, msg), this.getSelf());
    }
}
