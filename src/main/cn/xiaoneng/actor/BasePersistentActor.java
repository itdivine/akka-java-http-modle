package cn.xiaoneng.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.japi.Procedure;
import akka.persistence.*;
import cn.xiaoneng.actor.protocol.SaveSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;


/**
 * Created by JinKai on 2016/10/18.
 */
public abstract class BasePersistentActor extends AbstractPersistentActor implements IBaseActor, IMediator {
    protected final Logger log = LoggerFactory.getLogger(getSelf().path().toStringWithoutAddress());

    protected String VersionUID = null;

    //已订阅topic集合
    private Set<String> topics = new HashSet<String>();

    //获得系统总线
    private ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();

    @Override
    public AbstractActor.Receive createReceiveRecover() {
        return receiveBuilder()
                //恢复快照
                .match(SnapshotOffer.class, ss -> recoverSnapshot())
                .build();
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .matchEquals(SaveSnapshot.class, s -> saveSnapShot())
                .matchAny(msg -> persistAsync(msg, new Procedure<Object>() {
                    @Override
                    public void apply(Object param) throws Exception {
                        onReceive(param);
                    }
                }))
                .build();
    }

//    @Override
//    public void onReceiveCommand(Object msg) throws Throwable {
//        log.debug("Receive message: [{}]", msg);
//        try{
//            if(msg instanceof SaveSnapshot){
//                saveSnapShot();
//            }else if(msg instanceof SnapshotProtocol.Response || msg instanceof DistributedPubSubMediator.SubscribeAck || msg instanceof DistributedPubSubMediator.UnsubscribeAck){
//                log.debug("System Message :[{}] ",msg);
//
//            }else{
//                persistAsync(msg, new Procedure<Object>() {
//                    @Override
//                    public void apply(Object param) throws Exception {
//                        onReceiveMessage(param);
//                    }
//                });
//            }
//        }catch(Exception e){
//            log.warn("Exception :"+e.getMessage());
//            throw e;
//        }
//
//    }

//    @Override
//    public void onReceiveRecover(Object msg) throws Throwable {
//
//    }

    @Override
    public String persistenceId() {
        return getSelf().path().toStringWithoutAddress();
    }

    @Override
    public String getVersionId() {
        return VersionUID;
    }

    @Override
    public ActorRef getMediator() {
        return mediator;
    }

    @Override
    public void subscribe(String topic, String group, ActorRef subscriber, ActorRef receiver) {
        if(group == null){
            mediator.tell(new DistributedPubSubMediator.Subscribe(topic, subscriber), receiver);
        }else{
            mediator.tell(new DistributedPubSubMediator.Subscribe(topic, group, subscriber), receiver);
        }
        topics.add(topic);
    }

    @Override
    public void subscribe(String topic, String group) {
        subscribe(topic,group,getSelf(),getSelf());
    }

    @Override
    public void subscribe(ActorRef actor, ActorRef receiver) {
        String topic = actor.path().toStringWithoutAddress();
        subscribe(topic,null,actor,receiver);
    }

    @Override
    public void subscribe(String topic) {
        subscribe(topic,null);
    }

    @Override
    public void subscribe() {
        subscribe(getSelf(),getSelf());
    }

    @Override
    public void unsubscribe(String topic, String group, ActorRef subscriber, ActorRef receiver) {
        if(group == null){
            mediator.tell(new DistributedPubSubMediator.Unsubscribe(topic, subscriber), receiver);
        }else{
            mediator.tell(new DistributedPubSubMediator.Unsubscribe(topic,group, subscriber), receiver);
        }
        topics.remove(topic);
    }

    @Override
    public void unsubscribe(ActorRef actor, ActorRef receiver) {
        String topic = actor.path().toStringWithoutAddress();
        unsubscribe(topic,null,actor,receiver);
    }

    @Override
    public void unsubscribe(String topic, String group) {
        unsubscribe(topic,group,getSelf(), ActorRef.noSender());
    }

    @Override
    public void unsubscribe(String topic) {
        unsubscribe(topic,null,getSelf(), ActorRef.noSender());
    }

    @Override
    public void unsubscribe() {
        unsubscribe(getSelf(),getSelf());
    }

    @Override
    public Set<String> getTopics() {
        return topics;
    }

    @Override
    public void publish(String topic, Object msg) {
        this.mediator.tell(new DistributedPubSubMediator.Publish(topic,msg),getSelf());
    }

    @Override
    public void publish(String topic, Object msg, ActorRef sender) {
        this.mediator.tell(new DistributedPubSubMediator.Publish(topic,msg),sender);
    }

    @Override
    public void tell(String actorPath, Object msg) {
        mediator.tell(new DistributedPubSubMediator.Send(actorPath,msg),getSelf());
    }

}
