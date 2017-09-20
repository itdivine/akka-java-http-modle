package cn.xiaoneng.actor;

import akka.actor.AbstractActor;
import akka.persistence.AbstractPersistentActor;

/**
 *
 * 提供持久化actor
 *
 * Created by JinKai on 2016/7/27.
 */
public abstract class StatusActor extends AbstractPersistentActor {

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
//
//        //start、stop、pause 工作状态
//        //需要持久化改变actor状态的消息
//        onReceiveMessage(msg);
//    }

    public abstract void onReceiveMessage(Object msg);

}
