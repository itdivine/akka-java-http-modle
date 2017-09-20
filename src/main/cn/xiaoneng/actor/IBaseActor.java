package cn.xiaoneng.actor;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * actor简单接口
 * Created by JinKai on 2016/10/18.
 */
public interface IBaseActor {

    /**
     * 获得当前版本
     * @return
     */
    String getVersionId();


    /**
     * 获得系统总线
     * @return
     */
    ActorRef getMediator();

    /**
     * 接收消息
     * @param message
     */
    void onReceive(Object message);


    /**
     * 发送消息并且接收返回值
     *
     * 慎用该方法
     *
     * @param actor 消息目标
     * @param msg 消息
     * @param timeout 超时时间
     * @return
     * @throws Exception
     */
    default  Object ask(ActorRef actor, Object msg, Long timeout) throws Exception {
        Object receiveMessage = null;
        // 发送消息
        Future<Object> result = Patterns.ask(actor, msg, timeout);
        receiveMessage = Await.result(result, Duration.create(timeout, TimeUnit.MILLISECONDS));
        return receiveMessage;
    }

    void saveSnapShot();

    void recoverSnapshot();



}
