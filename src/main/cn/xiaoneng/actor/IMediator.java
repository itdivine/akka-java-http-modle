package cn.xiaoneng.actor;

import akka.actor.ActorRef;

import java.util.Set;

/**
 * Created by JinKai on 2016/9/18.
 */
public interface IMediator {
    /**
     * 订阅Topic
     * @param topic 主题
     * @param group 主题分组
     * @param subscriber 与topic绑定的actor
     * @param receiver 接收topic订阅成功的actor
     */
    void subscribe(String topic, String group, ActorRef subscriber, ActorRef receiver);

    /**
     * 当前actor订阅某分组topic
     * @param topic 主题
     * @param group 主题分组
     */
    void subscribe(String topic, String group);


    void subscribe(ActorRef actor, ActorRef receiver);

    /**
     * 当前actor订阅topic
     * @param topic
     */
    void subscribe(String topic);

    /**
     * 发布本身actor
     */
    void subscribe();


    /**
     * 反订阅topic
     * @param topic 主题
     * @param group 主题分组
     * @param subscriber 与topic绑定的actor
     * @param receiver 接收topic订阅成功的actor
     */
    void unsubscribe(String topic, String group, ActorRef subscriber, ActorRef receiver);

    /**
     * 取消本身actor订阅
     */
    void unsubscribe();

    /**
     * 当前actor反订阅group分组topic
     * @param topic
     * @param group
     */
    void unsubscribe(String topic, String group);

    /**
     * 当前actor反订阅topic
     * @param topic
     */
    void unsubscribe(String topic);

    void unsubscribe(ActorRef actor, ActorRef receiver);

    /**
     * 获得当前已订阅topic
     * @return
     */
    Set<String> getTopics();

    /**
     * 当前actor向topic广播消息
     * @param topic
     * @param msg
     */
    void publish(String topic, Object msg);

    /**
     * sender向topic广播消息
     * @param topic
     * @param msg
     * @param sender
     */
    void publish(String topic, Object msg, ActorRef sender);

    /**
     * 根据actor路径发布消息
     * @param actorPath
     * @param msg
     */
    void tell(String actorPath, Object msg);


}
