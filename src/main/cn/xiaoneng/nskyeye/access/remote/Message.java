package cn.xiaoneng.nskyeye.access.remote;

/**
 * Created by XuYang on 2017/8/28.
 */
public class Message {

    /**
     * Actor的path，或者订阅的topic
     */
    private String actorPath;

    /**
     * 消息Bean
     */
    private Object body;

    /**
     * Server响应超时时间
     */
    private long timeout;

    public Message(String actorPath, Object body) {
        this.actorPath = actorPath;
        this.body = body;
        this.timeout = 6000L;
    }

    public Message(String actorPath, Object body, long timeout) {
        this.actorPath = actorPath;
        this.body = body;
        this.timeout = timeout;
    }

    public String getActorPath() {
        return actorPath;
    }
}
