package cn.xiaoneng.actor;

import akka.actor.AbstractActor;
import akka.persistence.AbstractPersistentActorWithAtLeastOnceDelivery;

/**
 * 保证消息可达的 持久化Actor
 * Created by JinKai on 2016/7/27.
 */
public abstract class StatusActorWithAtLeastOnceDelivery extends AbstractPersistentActorWithAtLeastOnceDelivery {

    public final String actorId =  getSelf().path().toStringWithoutAddress();

    private ActorStatus _actor_status = ActorStatus.WORKING;

    public String persistenceId() {
        return actorId;
    }


    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                //需要持久化改变actor状态的消息 start、stop、pause 工作状态
                .matchAny(msg -> onReceiveMessage(msg))
                .build();
    }

//    @Override
//    public void onReceiveCommand(Object msg) throws Throwable {
//        //start、stop、pause操作类消息
//        //业务层消息执行onReceiveMessage方法
//
//        onReceiveMessage(msg);
//
//    }

    public abstract void onReceiveMessage(Object message);

}
