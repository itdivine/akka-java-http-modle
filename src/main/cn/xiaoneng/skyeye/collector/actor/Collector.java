package cn.xiaoneng.skyeye.collector.actor;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.FromConfig;
import cn.xiaoneng.skyeye.bodyspace.message.BodySpaceFieldMsg;
import cn.xiaoneng.skyeye.collector.model.CollectorModel;
import cn.xiaoneng.skyeye.collector.service.CollectorCopy;
import cn.xiaoneng.skyeye.collector.service.CollectorHandler;
import cn.xiaoneng.skyeye.collector.util.CollectorConstant;
import cn.xiaoneng.skyeye.monitor.Monitor;
import cn.xiaoneng.skyeye.monitor.MonitorCenter;
import cn.xiaoneng.skyeye.monitor.Node;
import cn.xiaoneng.skyeye.navigation.message.NavSpaceFieldMsg;
import cn.xiaoneng.skyeye.util.HTTPCommand;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 采集器
 * <p>
 * Created by liangyongheng on 2016/8/5 16:19.
 */
public class Collector extends UntypedActor {

    protected final static Logger log = LoggerFactory.getLogger(Collector.class);

    private CollectorModel model;

    private ActorRef handler;

    private ActorRef mediator;

    private static Monitor monitor = MonitorCenter.getMonitor(Node.Collector);

    private static Map<String, Long> lastKeyWordCountMap = new ConcurrentHashMap<>();

    //    private static AtomicLong lastKeyWordCount = new AtomicLong();
//
    public static long getLastKeyWordCount(String siteid) {

        if (lastKeyWordCountMap.containsKey(siteid)) {

            return lastKeyWordCountMap.get(siteid);
        }
        return 0;
    }

    public static synchronized void upadteLastKeyWordCount(String siteid) {

        if (lastKeyWordCountMap.containsKey(siteid)) {
            long lastCount = lastKeyWordCountMap.get(siteid);
            lastKeyWordCountMap.put(siteid, --lastCount);
        }
    }


    @Override
    public void preStart() throws Exception {

//        mediator = DistributedPubSub.get(this.getContext().system()).mediator();
//        mediator.tell(new DistributedPubSubMediator.Subscribe(getSelf().path().toStringWithoutAddress(), "NSkyEye", getSelf()), getSelf());

        this.getContext().system().eventStream().subscribe(getSelf(), NavSpaceFieldMsg.class);
        this.getContext().system().eventStream().subscribe(getSelf(), BodySpaceFieldMsg.class);


        super.preStart();
    }

    public Collector(String siteId, int status, long keyWord) {

        model = new CollectorModel();

        model.setSiteId(siteId);
        model.setStatus(status);
        lastKeyWordCountMap.put(siteId,keyWord);

//        handler = getContext().actorOf(Props.createEVS(CollectorHandler.class), "handler");
//        handler = getContext().actorOf(new SmallestMailboxPool(3).props(Props.createEVS(CollectorHandler.class)), "handler");
        handler = getContext().actorOf(FromConfig.getInstance().props(Props.create(CollectorHandler.class, status)), "handler");

        getContext().actorOf(Props.create(CollectorCopy.class), "copy");

        log.debug("collector init success! path:" + getSelf().path());
    }

    @Override
    public void onReceive(Object message) throws Throwable {

        long start = System.currentTimeMillis();

        try {

            if (message instanceof String) {

                log.debug("Receive message: " + message);
                doHttpRequest((String) message);

            } else if (message instanceof NavSpaceFieldMsg) {

                //处理采集字段消息
                log.debug("Receive message: " + message);
                processNavSpaceMsg((NavSpaceFieldMsg) message);

            } else if (message instanceof BodySpaceFieldMsg) {

                log.debug("Receive message: " + message);
                this.model.addBodySpaceFields(((BodySpaceFieldMsg) message).getSpaceFieldSet());
            }

        } catch (Exception e) {

            getSender().tell("{\"body\" : \"服务器错误\",\"status\" : 404}", getSelf());
            log.error("exception " + e.getMessage());

        } finally {
            long end = System.currentTimeMillis();
            long span = end - start;
            if (span > 1000) {
                log.warn("onReceive span=" + span + " msg=" + message);
            }

        }
    }

    /**
     * 处理http请求
     *
     * @param msg
     */
    private void doHttpRequest(String msg) {

        JSONObject json = JSONObject.parseObject(msg);

        String command = json.getString(CollectorConstant.METHOD);

        //查询一个采集器
        if (HTTPCommand.GET.equals(command)) {

            String rtnJson = "{\"body\":{\"status\":" + this.model.getStatus() + "},\"status\":200}";
            getSender().tell(rtnJson, getSelf());

        } else if (HTTPCommand.PUT.equals(command)) {

            //更新采集器状态、字段状态
            update(json.getJSONObject("body"));

        } else if (HTTPCommand.POST.equals(command)) {

        } else {

        }
    }

    private void update(JSONObject json) {

        if (json.containsKey(CollectorConstant.STATUS)) {

            this.model.setStatus(json.getInteger(CollectorConstant.STATUS));
        }
        getSender().tell("{\"body\":{\"status\":" + this.model.getStatus() + "},\"status\":200}", getSelf());
    }

    private void processNavSpaceMsg(NavSpaceFieldMsg message) {

        this.model.addNavSpaceFields(message.getSpaceName(), message.getFieldList());
    }


}
