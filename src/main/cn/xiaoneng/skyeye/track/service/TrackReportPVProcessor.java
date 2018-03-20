package cn.xiaoneng.skyeye.track.service;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import cn.xiaoneng.skyeye.bodyspace.message.PVResultMsg;
import cn.xiaoneng.skyeye.db.Neo4jDataAccess;
import cn.xiaoneng.skyeye.monitor.Monitor;
import cn.xiaoneng.skyeye.monitor.MonitorCenter;
import cn.xiaoneng.skyeye.monitor.Node;
import cn.xiaoneng.skyeye.navigation.bean.NavNodeInfo;
import cn.xiaoneng.skyeye.navigation.message.BackNavNodeListMsg;
import cn.xiaoneng.skyeye.navigation.message.BackNavNodeMsg;
import cn.xiaoneng.skyeye.util.ActorNames;
import cn.xiaoneng.skyeye.util.PVMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 上报PV处理器
 * 发送PV给主体空间管理器，聚合所有主体节点的引用
 * 发送PV给导航空间管理器，聚合所有导航节点的引用
 * 发送PV给kafka
 *
 * Created by xuyang on 2016/8/8.
 */
public class TrackReportPVProcessor extends AbstractActor {

    protected final static Monitor monitor = MonitorCenter.getMonitor(Node.TrackReportPVProcessor);
    protected final static Logger log = LoggerFactory.getLogger(TrackReportPVProcessor.class);

    /**
     * key：请求消息ID
     * value：聚合所有返回的消息
     */
    private Map<String, PVMessageFulfillment> fulfillmentEVSMap = new HashMap<String, PVMessageFulfillment>();


    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .matchAny(this::onReceive)
                .build();
    }
    public void onReceive(Object message) {
        try {
            if(message instanceof PVMessage) {
                //分发给主体空间管理器、导航空间管理器、计算引擎
                log.info("Receive message: " + (PVMessage)message + " " + getSelf().path().toStringWithoutAddress());
                processPVMessage((PVMessage) message);

            } else if (message instanceof BackNavNodeListMsg) {
                //导航空间管理器返回
                log.info("Receive message: " + (BackNavNodeListMsg)message + " " + getSelf().path().toStringWithoutAddress());
                processNavMsg((BackNavNodeListMsg)message);

            } else if (message instanceof List) {
                //主体空间管理器返回
                log.info("Receive message: " + (List<PVResultMsg>)message + " " + getSelf().path().toStringWithoutAddress());
                processBodyMsg((List<PVResultMsg>)message);
            }
        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        }
    }

    /**
     * 分发给主体空间管理器、导航空间管理器、计算引擎
     * @param message
     */
    protected void processPVMessage(PVMessage message) {

        dispatchTo(message);

//        // 发送给NT跟踪器
//        ActorSelection ntSelection = getContext().actorSelection(ActorNames.NT_BODYSPACE);
//        ntSelection.tell(message, getSelf());

        // 方案二
        // 分发给导航跟踪器
//        Map<String, Map<String, String>> map = message.getNavigationInfo();
//        Set<String> reportNavNameSet = map.keySet(); //前端上报的导航空间名字
//
//        for(String navSpaceName: reportNavNameSet) {
//
//            String navId = map.get(navSpaceName).get("id");
//
//            CreateRecordMessage createRecordMessage = new CreateRecordMessage(navId, message.getBodyNodeInfo(), message.getOtherInfo());
//
//            // 给跟踪器发送消息：创建访问记录
//            ActorSelection selection = getContext().actorSelection(navSpaceName);
//            selection.tell(createRecordMessage, getSelf());
//        }

        // 方案一
        //reportPVProcessor.tell(message, getSelf());
    }

    /**
     * 分发给
     * 主体空间管理器：创建主体节点列表，需要聚合后返回
     * 导航空间管理器：创建导航节点列表，需要聚合后返回
     * Kafka处理器: 不需要返回
     *
     * @param pvMessage
     */
    private void dispatchTo(PVMessage pvMessage) {

        long start = System.currentTimeMillis();

        try {
            monitor.newWriteTime("pre_dispatchTo", System.currentTimeMillis()-start, true);

            PVMessageFulfillment pvMessageFulfillment = new PVMessageFulfillment(2, pvMessage, start);

            fulfillmentEVSMap.put(pvMessage.getMsgId(), pvMessageFulfillment);

            pvMessage.sendTime = start;

            ActorSelection navSelection = getContext().actorSelection("../../../" + ActorNames.NavigationManager + "/" + ActorNames.NavReportPVProsessor);
            navSelection.tell(pvMessage, getSelf());

            ActorSelection bodySelection = getContext().actorSelection("../../../" + ActorNames.BODYSPACEMANAGER);
            bodySelection.tell(pvMessage, getSelf());

            Map map = pvMessage.getNavigationInfo();
            if(!map.containsKey(ActorNames.Chat)) {
                ActorSelection kafkaSelection = getContext().actorSelection("../../../" + ActorNames.KafkaManager);
                kafkaSelection.tell(pvMessage, getSelf());
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        } finally {
            monitor.newWriteTime("dispatchTo", System.currentTimeMillis()-start, true);
        }
    }

    private void processNavMsg(BackNavNodeListMsg message) {

        long start = System.currentTimeMillis();

        try {
            String msgId = message.getMsgId();
            PVMessageFulfillment pvMessageFulfillment = fulfillmentEVSMap.get(msgId);

            //创建web和订单节点之间关系
            createNavRalation(message.getNavNodes(), (String)pvMessageFulfillment.getPvMessage().getOtherInfo().get("sid"));

            start = pvMessageFulfillment.start;
            pvMessageFulfillment.setBackNavNodeListMsg((BackNavNodeListMsg) message);
            if(pvMessageFulfillment.getBackBodyListMsg()!=null) {
                // 发送给NT跟踪器
                ActorSelection ntSelection = getContext().actorSelection("../../" + ActorNames.NT_BODYSPACE);
                ntSelection.tell(pvMessageFulfillment.getPvMessage(), getSelf());
                fulfillmentEVSMap.remove(msgId);
                deleteCopy(pvMessageFulfillment.getPvMessage().getBodyNodeInfo().get("nt"));
            }
        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        } finally {

            long end = System.currentTimeMillis();
            monitor.newWriteTime("processNavMsg", end-start, true);
        }
    }

    private void processBodyMsg(List<PVResultMsg> message) {

        long start = 0L;

        try {
            String msgId = ((List<PVResultMsg>) message).get(0).getMsgId();
            PVMessageFulfillment pvMessageFulfillment = fulfillmentEVSMap.get(msgId);
            start = pvMessageFulfillment.start;
            pvMessageFulfillment.setBackBodyListMsg((List<PVResultMsg>) message);
            if(pvMessageFulfillment.getBackNavNodeListMsg()!=null) {
                // 发送给NT跟踪器
                ActorSelection ntSelection = getContext().actorSelection("../../" + ActorNames.NT_BODYSPACE);
                ntSelection.tell(pvMessageFulfillment.getPvMessage(), getSelf());
                fulfillmentEVSMap.remove(msgId);

                deleteCopy(pvMessageFulfillment.getPvMessage().getBodyNodeInfo().get("nt"));
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }

        } finally {

            long end = System.currentTimeMillis();
            monitor.newWriteTime("processBodyMsg", end-start, true);
        }
    }

    private void deleteCopy(String nt) {
        ActorSelection copy = getContext().actorSelection("../../../" + ActorNames.COLLECTOR + "/copy");
        copy.tell(nt, getSelf());
    }

    private void createNavRalation(List<BackNavNodeMsg> list, String sid) {

        NavNodeInfo webNodeInfo = null;
        NavNodeInfo orderNodeInfo = null;

        String spaceName;
        NavNodeInfo navNodeInfo;
        for(BackNavNodeMsg backNavNodeMsg:list) {
            navNodeInfo = backNavNodeMsg.getNavNodeInfo();
            spaceName = navNodeInfo.getSpaceName();
            if(spaceName.equals(ActorNames.Order)) {
                orderNodeInfo = navNodeInfo;
            }
            else if(spaceName.equals(ActorNames.Web)) {
                webNodeInfo = navNodeInfo;
            }
        }

        if(orderNodeInfo!=null &&  webNodeInfo!=null) {

            HashMap<String,Object> orderMap = new HashMap();
            orderMap.put("siteId",orderNodeInfo.getSiteId());
            orderMap.put("oi",orderNodeInfo.getParams().get("oi"));

            HashMap<String,Object> webMap = new HashMap();
            webMap.put("siteId",webNodeInfo.getSiteId());
            webMap.put("pgid",webNodeInfo.getParams().get("pgid"));

//            HashMap<String,Object> relationMap = new HashMap();
//            relationMap.put("sid", sid);

            Neo4jDataAccess.setRelation(":"+ ActorNames.Navigation+":"+ ActorNames.Web, ":"+ ActorNames.Navigation+":"+ ActorNames.Order
                    , webMap, orderMap, "EXIST", null);
        }
    }

    /**
     * 消息聚合:主体节点和导航节点是否存库成功
     */
    class PVMessageFulfillment {

        private long start; //monitor监控开始时间

        private int count; // 聚合的消息数量

        private PVMessage pvMessage; // 聚合的消息

        private BackNavNodeListMsg backNavNodeListMsg;

        private List<PVResultMsg> backBodyListMsg;


        public PVMessageFulfillment(int count, PVMessage pvMessage, long start) {
            this.count = count;
            this.pvMessage = pvMessage;
            this.start = start;
        }

        public List<PVResultMsg> getBackBodyListMsg() {
            return backBodyListMsg;
        }

        public void setBackBodyListMsg(List<PVResultMsg> backBodyListMsg) {
            this.backBodyListMsg = backBodyListMsg;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public PVMessage getPvMessage() {
            return pvMessage;
        }

        public void setPvMessage(PVMessage pvMessage) {
            this.pvMessage = pvMessage;
        }

        public BackNavNodeListMsg getBackNavNodeListMsg() {
            return backNavNodeListMsg;
        }

        public void setBackNavNodeListMsg(BackNavNodeListMsg backNavNodeListMsg) {
            this.backNavNodeListMsg = backNavNodeListMsg;
        }
    }
}
