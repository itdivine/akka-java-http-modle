package cn.xiaoneng.skyeye.navigation.service;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import cn.xiaoneng.skyeye.monitor.Monitor;
import cn.xiaoneng.skyeye.monitor.MonitorCenter;
import cn.xiaoneng.skyeye.monitor.Node;
import cn.xiaoneng.skyeye.navigation.bean.NavNodeInfo;
import cn.xiaoneng.skyeye.navigation.bean.NavigationSpaceInfo;
import cn.xiaoneng.skyeye.navigation.config.NavigationSpaceConfig;
import cn.xiaoneng.skyeye.navigation.event.NavInfoChangedEvent;
import cn.xiaoneng.skyeye.navigation.message.BackNavNodeListMsg;
import cn.xiaoneng.skyeye.navigation.message.BackNavNodeMsg;
import cn.xiaoneng.skyeye.navigation.message.CreateNavNodeMsg;
import cn.xiaoneng.skyeye.util.COMMON;
import cn.xiaoneng.skyeye.util.PVMessage;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by xuyang on 2016/8/10.
 */
public class NavigationPVRouter extends AbstractActor {

    protected final static Logger log = LoggerFactory.getLogger(NavigationPVRouter.class);
    private static Monitor monitor = MonitorCenter.getMonitor(Node.NavigationPVRouter);

    /**
     * key：请求消息ID
     * value：聚合所有返回的消息
     */
    private Map<String, NavigationPVFulfillment> fulfillmentEVSMap = new HashMap<String, NavigationPVFulfillment>();

    /**
     * 已开启导航空间信息列表
     * key: 导航空间名字
     */
    private static Map<String, NavigationSpaceInfo> navInfos = new HashMap<>();

    public void preStart() throws Exception {
        // 订阅导航信息改变事件
        super.preStart();
        this.getContext().system().eventStream().subscribe(getSelf(), NavInfoChangedEvent.class);
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .matchAny(this::onReceive)
                .build();
    }
    public void onReceive(Object message) {

        long start = System.currentTimeMillis();

        if (message instanceof NavInfoChangedEvent) {

            NavInfoChangedEvent event = (NavInfoChangedEvent)message;
            NavigationSpaceInfo navInfo = event.getNavInfo();

            if (navInfo.getStatus() == COMMON.ON)
                navInfos.put(navInfo.getName(), navInfo);

            monitor.newWriteTime("NavInfoChangedEvent", System.currentTimeMillis()-start, true);

        } else if (message instanceof PVMessage) {
            // 接收跟踪器上报的PV消息，分发给各个导航空间，创建导航节点
            monitor.newWriteTime("PVMessage_pre", System.currentTimeMillis()- ((PVMessage)message).sendTime, true);
            dispatcherMsg((PVMessage) message);
            monitor.newWriteTime("PVMessage", System.currentTimeMillis()-start, true);

        }else if (message instanceof BackNavNodeMsg){
            // 接收导航节点返回的信息，聚合完整后，返回节点列表消息给跟踪器

            BackNavNodeMsg docMsg = (BackNavNodeMsg)message;

            NavigationPVFulfillment previousFulfillment = fulfillmentEVSMap.get(docMsg.getMsgId());
            previousFulfillment.messages.add(docMsg);

            if(previousFulfillment.messages.size() >= previousFulfillment.count) {

                List<BackNavNodeMsg> list = previousFulfillment.getMessages();

                BackNavNodeListMsg backNavNodeListMsg = new BackNavNodeListMsg(previousFulfillment.msgId, list, 10);
                previousFulfillment.callback.tell(backNavNodeListMsg, getSelf());
                fulfillmentEVSMap.remove(docMsg.getMsgId());
            }

            monitor.newWriteTime("BackNavNodeMsg", System.currentTimeMillis()-start, true);
        }
    }

    /**
     * 消息派发，通知各个空间创建节点
     *
     * @param message
     */
    private void dispatcherMsg(PVMessage message) {

        try {

            String siteId = (String)message.getOtherInfo().get("siteid");

            Map map = message.getNavigationInfo();

            int count = 0; // count: 使用实际匹配到的、开启的导航空间数为标准

            Set<String> navNameSet = navInfos.keySet();  //已开启导航空间名字
            Set<String> reportNavNameSet = map.keySet(); //前端上报的导航空间名字

            for(String name : navNameSet) {
                if (reportNavNameSet.contains(name)) {
                    count++;
                }
            }

            // 1.创建一条聚合器记录
            NavigationPVFulfillment evsFulfillment = new NavigationPVFulfillment(
                    message.getMsgId(), count, new ArrayList<BackNavNodeMsg>(), getSender());

            fulfillmentEVSMap.put(message.getMsgId(), evsFulfillment);

            // 2.分发消息
            JSONArray array = null;
            JSONObject obj = null;
            for(String navSpaceName : navNameSet) {

                if (!reportNavNameSet.contains(navSpaceName))
                    continue;

//                array = (JSONArray)map.get(navName);
                String json = map.get(navSpaceName).toString();
                if(json.startsWith("[")) {
                    array = JSON.parseArray(json);
                } else {
                    obj = JSON.parseObject(json);
                    array = new JSONArray();
                    array.add(obj);
                }

                for(int i=0;i<array.size();i++) {
                    String indexParam = NavigationSpaceConfig.getInstance().getNavigationSpaceInfo(navSpaceName).getIndexParam();
                    JSONObject params = array.getJSONObject(i);
                    String id = params.getString(indexParam);

                    NavNodeInfo navNodeInfo = new NavNodeInfo(siteId, id, JSON.parseObject(params.toJSONString(), Map.class), message.getCreateTime());
                    navNodeInfo.setSpaceName(navSpaceName);
                    CreateNavNodeMsg msg = new CreateNavNodeMsg(navNodeInfo, 10, message.getMsgId());

                    ActorSelection selection = getContext().actorSelection("../../" + navSpaceName);
                    selection.tell(msg, getSelf());
                }
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }
    }


    /**
     * 消息聚合
     */
    class NavigationPVFulfillment {

        // 请求和聚合的消息具有相同的唯一标识
        private String msgId;

        // 聚合的消息数量
        private int count;

        // 聚合的消息
        private List<BackNavNodeMsg> messages;

        // 消息返回给谁
        private ActorRef callback;

        /**
         * @param baseMessages 必须重写toJson()方法
         */
        public NavigationPVFulfillment(String msgId, int count, List<BackNavNodeMsg> baseMessages, ActorRef callback) {
            this.msgId = msgId;
            this.callback = callback;
            this.count = count;
            this.messages = baseMessages;
        }

        public List<BackNavNodeMsg> getMessages() {
            return messages;
        }

        /*public Map<String, ActorRef> getNavNodes() {

            if(messages == null)
                return null;

            Map<String, ActorRef> map = new HashMap<String, ActorRef>();

            try {
                for (BackNavNodeMsg message: messages) {
                    map.put(message.getNavSpaceName(), message.getNavNodeRef());
                }

            } catch (Exception e) {
                log.error("Exception " + e.getMessage());
            }

            return map;
        }*/
    }

}
