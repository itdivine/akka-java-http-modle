package cn.xiaoneng.skyeye.track.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.cluster.sharding.ClusterSharding;
import cn.xiaoneng.skyeye.db.Neo4jDataAccess;
import cn.xiaoneng.skyeye.monitor.Monitor;
import cn.xiaoneng.skyeye.monitor.MonitorCenter;
import cn.xiaoneng.skyeye.monitor.Node;
import cn.xiaoneng.skyeye.navigation.message.GetNavNodeInfo;
import cn.xiaoneng.skyeye.navigation.message.ReturnNavNodeInfo;
import cn.xiaoneng.skyeye.track.bean.RecordInfo;
import cn.xiaoneng.skyeye.track.bean.RecordInfoFull;
import cn.xiaoneng.skyeye.track.message.GetRecordInfosMessage;
import cn.xiaoneng.skyeye.track.message.RecordMessage;
import cn.xiaoneng.skyeye.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by xuyang on 2016/8/8.
 */
public class Record extends AbstractActor {

    protected final Logger log = LoggerFactory.getLogger(getSelf().path().toStringWithoutAddress());
    private static Monitor monitor = MonitorCenter.getMonitor(Node.Record);

    private RecordInfo recordInfo; // navId - RecordInfo
    private Map<String, NavInfoFulfillment> navInfoFulfillmentMap = new HashMap<>(); // 聚合导航节点信息

    public Record(RecordInfo recordInfo) {

        this.recordInfo = recordInfo;
        log.info("Record add: " + recordInfo);
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .matchAny(this::onReceive)
                .build();
    }
    public void onReceive(Object message) {

        long start = System.currentTimeMillis();

        try {
            log.debug("Receive: " + message + " " + recordInfo.toString());

            if (message instanceof String && message.equals("save")) {
                save();
                monitor.newWriteTime("save", System.currentTimeMillis()-start, true);
            }
            else if (message instanceof GetRecordInfosMessage) {
                getInfos((GetRecordInfosMessage) message);
                monitor.newWriteTime("getInfos", System.currentTimeMillis()-start, true);
            }
            else if (message instanceof ReturnNavNodeInfo) {
                fill((ReturnNavNodeInfo) message);
                monitor.newWriteTime("fill", System.currentTimeMillis()-start, true);
            }
            else {
                getSender().tell("{\"status\":415,\"body\":\"\"}", getSelf());
            }


        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        }
    }

    /**
     * 保存Record到Neo4j中
     */
    private void save() {

        try {
            Map<String,Object> map = new HashMap<>();
            map.put("siteId", Statics.getSiteId(getSelf().path().elements().iterator()));
            map.putAll(recordInfo.getMap());
            Neo4jDataAccess.setVisitRelation("nt", recordInfo.getNavSpaceName(), recordInfo.getNt_id(), recordInfo.getNavId(), map);

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        }
    }

    private void fill(ReturnNavNodeInfo message) {

        try {
            String msgId = message.getMsgId();
            NavInfoFulfillment navInfoFulfillment = navInfoFulfillmentMap.get(msgId);

            if(navInfoFulfillment == null)
                return;

            RecordInfoFull recordInfoFull = new RecordInfoFull(recordInfo.getBody_id(),message.getNavNodeInfo(),recordInfo.getMap(),recordInfo.getTime());

            RecordMessage recordMessage = new RecordMessage(recordInfoFull, message.getMsgId());

            navInfoFulfillment.callback.tell(recordMessage, getSelf()); // callback: GetTrackRouter.class

            navInfoFulfillmentMap.remove(msgId);
        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        }

    }

    private void getInfos(GetRecordInfosMessage message) {

        try {
            String msgId = message.getMsgId();
            if(navInfoFulfillmentMap.containsKey(msgId)) {
                log.info("msgId duplication : " + message);
                return;
            }

            NavInfoFulfillment navInfoFulfillment = new NavInfoFulfillment(msgId, getSender());
            navInfoFulfillmentMap.put(msgId, navInfoFulfillment);

            GetNavNodeInfo getMessage = new GetNavNodeInfo(msgId, recordInfo.getNavId(), recordInfo.getNavSpaceName());
            dispatchTo(getMessage);

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        }
    }

    private ActorRef shardRegion = ClusterSharding.get(getContext().getSystem()).shardRegion(ActorNames.NavigationNode);

    private void dispatchTo(GetNavNodeInfo getMessage) {

        try {
//            String path = "../../../" + ActorNames.NavigationManager + "/" + recordInfo.getNavSpaceName() + "/" + recordInfo.getNavId();
//            getContext().actorSelection(path).tell(getMessage, getSelf());

            shardRegion.tell(getMessage, getSelf());
        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        }
    }


    /**
     * 消息聚合
     */
    class NavInfoFulfillment {

        // 请求和聚合的消息具有相同的唯一标识
        private String msgId;

        // 聚合的消息 须重写toJson()方法
//        private List<RecordInfoFull> messages = new ArrayList<>();

        // 消息返回给谁
        private ActorRef callback;

        public NavInfoFulfillment(String msgId, ActorRef callback) {
            this.msgId = msgId;
            this.callback = callback;
        }

        /*public JSONArray getJsonEVSInfos() {

            JSONArray array = new JSONArray();

            if(messages == null)
                return array;

            try {
                for (RecordInfoFull recordInfoFill: messages) {
                    array.add(JSONArray.toJSON(recordInfoFill));
                }

            } catch (Exception e) {
                log.error("Exception " + e.getMessage());
            }

            return array;
        }*/
    }
}
