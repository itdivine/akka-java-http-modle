package cn.xiaoneng.skyeye.track.actor;

import akka.actor.*;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.http.javadsl.model.StatusCodes;
import akka.routing.SmallestMailboxPool;
import cn.xiaoneng.skyeye.App;
import cn.xiaoneng.skyeye.access.Message.TrackProtocal;
import cn.xiaoneng.skyeye.bodyspace.message.BodyNodeMsgMap;
import cn.xiaoneng.skyeye.collector.config.ItemConfigKeyAction;
import cn.xiaoneng.skyeye.collector.service.CollectorCopy;
import cn.xiaoneng.skyeye.config.container.KeyPageContainer;
import cn.xiaoneng.skyeye.db.Neo4jDataAccess;
import cn.xiaoneng.skyeye.kafka.bean.ChatOrder;
import cn.xiaoneng.skyeye.monitor.Monitor;
import cn.xiaoneng.skyeye.monitor.MonitorCenter;
import cn.xiaoneng.skyeye.monitor.Node;
import cn.xiaoneng.skyeye.navigation.bean.NavNodeInfo;
import cn.xiaoneng.skyeye.navigation.bean.NavigationSpaceInfo;
import cn.xiaoneng.skyeye.navigation.config.NavigationSpaceConfig;
import cn.xiaoneng.skyeye.track.bean.*;
import cn.xiaoneng.skyeye.track.message.GetRecordInfosMessage;
import cn.xiaoneng.skyeye.track.message.GetUserTrackMessage;
import cn.xiaoneng.skyeye.track.message.KPIMessage;
import cn.xiaoneng.skyeye.track.message.RecordMessage;
import cn.xiaoneng.skyeye.track.service.HttpHandler;
import cn.xiaoneng.skyeye.util.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 查询用户跟踪记录
 *
 * 1、给主体空间管理器发送消息：查询一个账号的关联账号列表
 * 2.给所有已开启的跟踪器发送消息：查询当前账号（以及其关联账号）的跟踪记录列表
 * 3、返回给查询者
 */
public class NTTracker extends AbstractActor {

    protected final Logger log = LoggerFactory.getLogger(getSelf().path().toStringWithoutAddress());
    private static Monitor monitor = MonitorCenter.getMonitor(Node.NTTracker);

    private ActorRef httpHandler;
    private ActorRef mediator;
    private NTTrackInfo trackInfo;
//    private EVSInfo evsInfo; //企业信息，配额限制
    private String siteId;

    private static final int showCount = 1; // 返回最近N次的来访轨迹

    /**
     * key：请求消息ID
     * value：聚合所有返回的消息
     */
    private Map<String, RecordFullFillment> recordFullFillmentMap = new HashMap<>();

    public NTTracker(NTTrackInfo trackInfo) {

        this.trackInfo = trackInfo;
        this.httpHandler = getContext().actorOf(new SmallestMailboxPool(3).props(Props.create(HttpHandler.class)), "httpHandler");
    }

    @Override
    public void preStart() {

        try {
            super.preStart();
//            this.getContext().system().eventStream().subscribe(getSelf(), EvsInfoChangedEvent.class);

            //Logger[/system/sharding/EVS/18/kf_1003/tracks/nt]
            scala.collection.Iterator<String> it = getSelf().path().elements().iterator();
            it.next();  it.next();  it.next();  it.next();
            siteId = it.next();

            // 不再用路径  kf_1003/handler
            String topic = siteId + ActorNames.SLASH + ActorNames.TrackerManager + ActorNames.SLASH + getSelf().path().name();
            mediator = DistributedPubSub.get(this.getContext().system()).mediator();
            mediator.tell(new DistributedPubSubMediator.Subscribe(topic, ActorNames.NSkyEye, getSelf()), getSelf());

            log.info("NTTracker init success, topic = " + topic);

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }
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

            if (message instanceof PVMessage) {
                // 1、上报轨迹接口
                log.debug("Receive: PVMessage " + (PVMessage) message);
                matchChatOrder((PVMessage) message);
                createRecord((PVMessage) message);
                monitor.newWriteTime("createRecord", System.currentTimeMillis() - start, true);

            } else if (message instanceof CommandMessage) {

                // 2、获取Track信息
                log.debug("Receive: CommandMessage.get " + (CommandMessage) message);
                processCommand((CommandMessage) message);
                monitor.newWriteTime("GET", System.currentTimeMillis() - start, true);

            } else if (message instanceof TrackProtocal.Get) {

                // 3、查询用户轨迹 by nt_id
                // 3-1、查询账号关联列表
                log.debug("Receive: TrackProtocal.Get " + message);
                processHTTPRequest((TrackProtocal.Get) message);
                monitor.newWriteTime("TrackProtocal.Get", System.currentTimeMillis() - start, true);

            } else if (message instanceof BodyNodeMsgMap) {

                // 3-2、主体空间返回账号列表,查询内存和DB中Record
                log.debug("Receive: BodyNodeMsgMap " + (BodyNodeMsgMap) message);
                getRecord((BodyNodeMsgMap) message);
                monitor.newWriteTime("getRecord", System.currentTimeMillis() - start, true);

            } else if (message instanceof KPIMessage) {

                // 3 KPI返回统计来访、咨询
                KPIMessage kpiMessage = (KPIMessage) message;
                String msgId = kpiMessage.getMsgId();
                log.debug("Receive: KPIMessage " + kpiMessage);
                RecordFullFillment recordFullFillment = recordFullFillmentMap.get(kpiMessage.getMsgId());
                if (recordFullFillment != null) {
                    recordFullFillment.setKpiMessage(kpiMessage);
                    fill(recordFullFillment, msgId);
                }

                monitor.newWriteTime("getRecord", System.currentTimeMillis() - start, true);

            } else if (message instanceof RecordMessage) {

                // 3-3、聚合Record完整后返回
                RecordMessage recordMessage = (RecordMessage) message;
                log.debug("Receive: RecordMessage " + recordMessage);
                String msgId = recordMessage.getMsgId();
                RecordFullFillment recordFullFillment = recordFullFillmentMap.get(msgId);

                if(recordFullFillment == null)
                    return;

                recordFullFillment.setBackMessages(getSender(), recordMessage.getRecordInfoFull());

                fill(recordFullFillment, msgId);
                monitor.newWriteTime("fill", System.currentTimeMillis() - start, true);

            }
            /*else if (message instanceof EvsInfoChangedEvent) {

                log.debug("Receive: RecordMessage " + (EvsInfoChangedEvent) message);
                evsInfo = ((EvsInfoChangedEvent) message).get_evsInfo();
                monitor.newWriteTime("EvsInfoChangedEvent", System.currentTimeMillis() - start, true);
            }*/


        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }
    }

    private void processHTTPRequest(TrackProtocal.Get message) {

        try {
            boolean showPrice = true;
            log.debug("showPrice=" + showPrice);

            getUserTrack(new GetUserTrackMessage(message.nt_id, message.nav, message.start_page,
                    message.page, message.per_page, showPrice, getSender()));

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }
    }

    /**
     * 查询一个账号的关联账号列表
     */
    private void  getUserTrack(GetUserTrackMessage message) {

        RecordFullFillment recordFullFillment = new RecordFullFillment(message);
        recordFullFillmentMap.put(message.getMsgId(), recordFullFillment);

        ActorSelection selection = getContext().actorSelection("../../" + ActorNames.BODYSPACEMANAGER);
        selection.tell(message, getSelf());

    }

    private void getRecord(BodyNodeMsgMap message) {

        try {

            // TODO 查询关联的其他nt_id的轨迹，合并排序

            // 获取nt_id
            String nt_id = message.getNt_id();
            if (nt_id == null || nt_id.isEmpty()) {
                log.info("nt_id is null, getUserTrackMessage= " + message);
                getSender().tell(Statics.apiKeyError(), getSelf());
                return;
            }

            RecordFullFillment recordFullFillment = recordFullFillmentMap.get(message.getMsgId());

            //查询计算引擎
            httpHandler.tell(recordFullFillment.getGetUserTrackMessage(),getSelf());


            // 获取sid集合
            Map<String, LinkedHashMap<String, List<ActorRef>>> nt_sids_Records = trackInfo.getNt2sid();
            LinkedHashMap<String, List<ActorRef>> sids_Records = nt_sids_Records.get(nt_id);

            int cacheSidCount = 0;
            if(sids_Records != null)
                cacheSidCount = sids_Records.size();


            // 真正需要获取的Record列表
            TreeMap<String, LinkedHashMap<ActorRef, RecordInfoFull>> recordMap = new TreeMap<>();

            int page = recordFullFillment.getGetUserTrackMessage().getPage();
            int per_page = recordFullFillment.getGetUserTrackMessage().getPer_page();
            if(per_page > 10)
                per_page = 10;

            //需要请求Record的数量
            int count = 0;
            int start = (page-1)*per_page;

            int skip;
            int limit = per_page;

            // 内存中获取
            if(start < cacheSidCount) {
//              for (int i = start; i < cacheSidCount; i++) {
                int i=-1;
                for(Map.Entry<String, List<ActorRef>> entry: sids_Records.entrySet()) {

                    i++;
                    if(i<start || i>=cacheSidCount)
                        continue;

                    String sid = entry.getKey();
                    List<ActorRef> recordRefList = entry.getValue();
                    if (recordRefList == null)
                        continue;

                    LinkedHashMap<ActorRef, RecordInfoFull> map = new LinkedHashMap<>();

                    for (ActorRef recordRef : recordRefList) {
                        map.put(recordRef, null);
                    }

                    count += map.size();

                    recordMap.put(sid, map);

                    if (i > per_page)
                        break;
                }

                skip = recordMap.size();
                limit = per_page - skip;

            } else {
                skip = start;
            }

            if(limit > 0)
            {
                //DB中获取
                log.info("Find from neo4j, nt_id=" + nt_id);

                TreeMap<String, List<RecordInfoFull>> sid2Records = getTracksFromDB(nt_id, skip, limit);

                recordFullFillment.setDbRecordMap(sid2Records);

            }

            recordFullFillment.set(message, recordMap, count);

            //　获取每个Record的信息
            for (Map.Entry<String, LinkedHashMap<ActorRef, RecordInfoFull>> entry : recordMap.entrySet()) {
                Map<ActorRef, RecordInfoFull> map = entry.getValue();
                for (Map.Entry<ActorRef, RecordInfoFull> entry1 : map.entrySet()) {
                    ActorRef ref = entry1.getKey();
                    ref.tell(new GetRecordInfosMessage(message.getMsgId()), getSelf());
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
     * 从Neo4j中分页查询一个访客的轨迹
     * @param nt_id
     * @param skip
     * @param limit
     * @return key=sid, value=RecordList
     */
    private TreeMap<String, List<RecordInfoFull>> getTracksFromDB(String nt_id, int skip, int limit) {

        TreeMap<String, List<RecordInfoFull>> recordMap = null;

        try {
            recordMap = new TreeMap<>();

            HashMap<String,Object> map = new HashMap();
            map.put("siteId",siteId);
            map.put("id",nt_id);

            List<String> sidList = Neo4jDataAccess.getSidList(map, skip, limit);
            if(sidList == null || sidList.size()<=0)
                return null;

            for(String sid:sidList) {
                map.put("sid", sid);
                List<RecordInfoFull> list = Neo4jDataAccess.getSidTrack(map);
                recordMap.put(sid,list);
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }

        return recordMap;
    }

    private void fill(RecordFullFillment recordFullFillment, String msgId) {

        if (recordFullFillment.count == recordFullFillment.nowCount && recordFullFillment.getKpiMessage()!=null) {

            //解析咨询发起页
            ActorRef callback = recordFullFillment.getUserTrackMessage.getCallback();
            String startPage =  recordFullFillment.getUserTrackMessage.getStartpage();
            String data = recordFullFillment.getInfos(startPage);
            String nt = recordFullFillment.getGetUserTrackMessage().getId();
            if(Statics.isNullOrEmpty(data) || data.equals("{}") || !data.contains("sessions")) {
                //查找副本数据
                ActorSelection copy = getContext().actorSelection("../../" + ActorNames.COLLECTOR + "/copy");
                copy.tell(new CollectorCopy.Get(nt, callback), getSelf());

            } else {
                TrackProtocal.Result result = new TrackProtocal.Result(StatusCodes.OK, data);
                callback.tell(result, getSelf());
            }

            recordFullFillmentMap.remove(msgId);
        }
    }

    private void processCommand(CommandMessage message) {

        try {
            String operation = message.getOperation();
            if(Statics.isNullOrEmpty(operation)) {
                message.getCallback().tell("{\"status\":415}", getSelf());
                return;
            }

            switch (operation) {

                case Operation.GET:
                    DocumentMessage documentMessage = new DocumentMessage(null, 10, trackInfo.toJSONString(), message.getMsgId());
                    getSender().tell(documentMessage, getSelf());
                    break;

                default:
                    log.info("Invalid Message Operation: " + operation);
                    message.getCallback().tell("{\"status\":415}", getSelf());
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
            message.getCallback().tell("{\"status\":400}", getSelf());
        }
    }

    /**
     * 匹配咨询订单
     * @param pvMessage
     */
    private void matchChatOrder(PVMessage pvMessage) {

        String ntId;
//        String sid;
        String siteId;
        String ntLabs = ":Body:nt";
        String orderLabs = ":Navigation:Order";
        String webLabs = ":Navigation:Web";

        try {
            ntId = pvMessage.getBodyNodeInfo().get("nt");
//            sid = (String)pvMessage.getOtherInfo().get("sid");
            siteId = (String)pvMessage.getOtherInfo().get("siteid");

            HashMap<String,Object> ntMap = new HashMap();
            ntMap.put("siteId",siteId);
            ntMap.put("id",ntId);

            long time = System.currentTimeMillis() - 3*24*60*60*1000L;

            Map map = pvMessage.getNavigationInfo();
            //上报咨询事件
            if(map.containsKey(ActorNames.Chat)) {
                JSONObject chatJson = ((JSONArray)map.get(ActorNames.Chat)).getJSONObject(0);

                //1.1查找访客3天内最近一次下单
                NavNodeInfo orderInfo = Neo4jDataAccess.getRecentlyAccessedNavNode(ntLabs, ":Navigation:Order", ntMap, time);
                if(orderInfo != null) {
                    //1.2 查找订单节点关联的咨询节点
                    HashMap<String,Object> orderMap = new HashMap();orderMap.put("siteId",siteId);orderMap.put("oi",orderInfo.getParams().get("oi"));
                    List<NavNodeInfo> chatInfo = Neo4jDataAccess.getNavNode(":Navigation:Order", ":Navigation:Chat", orderMap, null, "CP");
                    if(chatInfo == null) {
                        //1.3 匹配成功,创建咨询节点和订单节点的关系
                        HashMap<String,Object> chatMap = new HashMap(); chatMap.put("siteId",siteId);chatMap.put("converid",chatJson.getString("converid"));
                        HashMap<String,Object> relationMap = new HashMap(); relationMap.put("keylevel",3);relationMap.put("time",System.currentTimeMillis());
                        Neo4jDataAccess.setRelation(orderLabs, ":Navigation:Chat", orderMap, chatMap, "CP", relationMap);

                        //1.4 通知计算云咨询订单事件
                        //查找订单节点和nt节点的sid
                        Map visitMap = Neo4jDataAccess.getRelation(ntLabs, orderLabs, null,orderMap, "VISIT", null );
                        if(visitMap==null)
                            return;
                        ntMap.put("sid",visitMap.get("sid"));

                        List<RecordInfoFull> records = Neo4jDataAccess.getSidTrack(ntMap);
                        List<NavNodeInfo> webNodes = Neo4jDataAccess.getNavNode(orderLabs, ":Navigation:Web", orderMap, null, "EXIST");
                        notifyChatOrderEvent(records, webNodes, chatJson, orderInfo, ntMap);
                    }
                }
            }
            else if(map.containsKey(ActorNames.Order)) {

                JSONObject orderJson = ((JSONArray)map.get(ActorNames.Order)).getJSONObject(0);
                JSONObject webJson = ((JSONArray)map.get(ActorNames.Web)).getJSONObject(0);
                int keylevel = webJson.getIntValue("keylevel");

                //2.1查找3天内最近一次咨询节点
                NavNodeInfo chatInfo = Neo4jDataAccess.getRecentlyAccessedNavNode(":Body:nt", ":Navigation:Chat", ntMap, time);
                if(chatInfo!=null) {
                    //2.2查找咨询节点关联的订单节点
                    HashMap<String,Object> chatMap = new HashMap(); chatMap.put("siteId",siteId);chatMap.put(FieldConstants.CONVERID,chatInfo.getParams().get(FieldConstants.CONVERID));
                    List<NavNodeInfo> orderInfoList = Neo4jDataAccess.getNavNode(":Navigation:Chat",":Navigation:Order", chatMap, null, "CP");
                    if(orderInfoList == null) {
                        //匹配成功
                        HashMap<String,Object> orderMap = new HashMap();orderMap.put("siteId",siteId);orderMap.put("oi",orderJson.get(FieldConstants.ORDERID));
                        HashMap<String,Object> relationMap = new HashMap(); relationMap.put(FieldConstants.KEYLEVEL,keylevel);relationMap.put("time",System.currentTimeMillis());
                        Neo4jDataAccess.setRelation(":Navigation:Order", ":Navigation:Chat", orderMap, chatMap, "CP", relationMap);
                        notifyChatOrderEvent(map, chatInfo, ntMap);

                    } else {
                        String reportOrderId = orderJson.getString("oi");
                        String dbOrderId = (String)orderInfoList.get(0).getParams().get("oi");

                        if(reportOrderId.equalsIgnoreCase(dbOrderId)) {
                            int reportKeylevel = ((JSONArray)map.get(ActorNames.Web)).getJSONObject(0).getIntValue(FieldConstants.KEYLEVEL);
                            HashMap<String,Object> orderMap = new HashMap();orderMap.put("siteId",siteId);orderMap.put("oi",reportOrderId);
//                            Map order_chat_map = new HashedMap();
//                            order_chat_map.putAll(chatMap);
//                            order_chat_map.putAll(orderMap);
//                            Set<Integer> levels = Neo4jDataAccess.getLevels(order_chat_map);

                            List<NavNodeInfo> webNodes = Neo4jDataAccess.getNavNode(orderLabs, webLabs, orderMap, null, "EXIST");
                            if(webNodes == null || webNodes.size()==0)
                                return;
                            Set<Long> levels = new HashSet();
                            for(NavNodeInfo webNode:webNodes) {
                                //上报订单也已经被创建和关联，需要过滤掉
                                if(webNode.getCreateTime() != pvMessage.getCreateTime())
                                    levels.add((Long)webNode.getParams().get(FieldConstants.KEYLEVEL));
                            }

                            if(!levels.contains(reportKeylevel)) {
                                //匹配成功,关系已经存在，不需要再次创建
                                notifyChatOrderEvent(map, chatInfo, ntMap);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.warn(er[i].toString());
            }
        }
    }

    private void notifyChatOrderEvent(Map map, NavNodeInfo chatInfo, HashMap<String, Object> ntMap) {

        try{
            JSONObject orderJson = ((JSONArray)map.get(ActorNames.Order)).getJSONObject(0);
            JSONObject webJson = ((JSONArray)map.get(ActorNames.Web)).getJSONObject(0);
            JSONObject ipJson = ((JSONArray)map.get(ActorNames.IP)).getJSONObject(0);
            JSONObject sourceJson = ((JSONArray)map.get(ActorNames.Source)).getJSONObject(0);
            JSONObject terminalJson = ((JSONArray)map.get(ActorNames.Terminal)).getJSONObject(0);

            String siteId = (String)ntMap.get("siteId");
            ChatOrder chatOrder = new ChatOrder();
            chatOrder.nt = (String)ntMap.get("id");
            chatOrder.cs_template_id = (String)chatInfo.getParams().get("templateid");

            JSONArray suppliers = JSON.parseArray((String)chatInfo.getParams().get("suppliers"));
            for(int i=0;i<suppliers.size();i++) {
                Map<String,String> supplierMap = new HashMap();
                supplierMap.put("rs_cs_id", suppliers.getJSONObject(i).getString("supplierid"));
                supplierMap.put("rs_cs", suppliers.getJSONObject(i).getString("suppliername"));
                chatOrder.rs_list.add(supplierMap);
            }

            chatOrder.vs_startpage_url = (String)chatInfo.getParams().get("startpageurl");
            chatOrder.conversation_time = (Long)chatInfo.getParams().get("starttime");
            ItemConfigKeyAction keyItem = ItemConfigKeyAction.getKeyItem(siteId, chatOrder.vs_startpage_url, KeyPageContainer.getInstance(), null);
            if (keyItem != null) {
                chatOrder.keylevel = keyItem.keylevel;
                chatOrder.keyname = keyItem.keyname;
            }

            chatOrder.order_time = System.currentTimeMillis();
            chatOrder.order_type = getOrderType(chatOrder.order_time, chatOrder.conversation_time);
            chatOrder.order_id = orderJson.getString("oi");
            chatOrder.order_price = orderJson.getString("op");

            if(ipJson != null) {
                chatOrder.ip = ipJson.getString("ip");
                chatOrder.country = ipJson.getString("country");
                chatOrder.province = ipJson.getString("province");
                chatOrder.city = ipJson.getString("city");
            }
            if(sourceJson!=null) {
                chatOrder.keyword = sourceJson.getString("keyword");
                chatOrder.source = sourceJson.getString("source");
                chatOrder.ref = sourceJson.getString("ref");
                chatOrder.ref = sourceJson.getString("ref");
            }
            if(terminalJson!=null) {
                chatOrder.tml = terminalJson.getString("tml");
            }

            if(webJson!=null) {
                //订单keylevel
                chatOrder.order_level = webJson.getLong("keylevel");
                JSONObject event_propertie = (JSONObject) JSON.toJSON(chatOrder);
                JSONArray event_properties = new JSONArray();
                event_properties.add(event_propertie);

                JSONObject eventJson = new JSONObject();
                eventJson.put("event_name", "chat_order");
                eventJson.put("event_time", System.currentTimeMillis());
                eventJson.put("event_siteid", siteId);
                eventJson.put("event_distinctid", String.valueOf((int)(Math.random()*1000000000)));
                eventJson.put("event_properties", event_properties);
                //{event_name:"",event_time:"",event_siteid:"",event_distinctid:"",event_properties:[{……}] }

                App.ntKafkaProducer.send(eventJson.toString());
                log.info("Send kafka msg: " + eventJson.toJSONString());
                //kafkaSelection.tell(chatOrder, getSelf());
            }
        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.warn(er[i].toString());
            }
        }
    }

    private void notifyChatOrderEvent(List<RecordInfoFull> records, List<NavNodeInfo> webNodes, JSONObject chatJson, NavNodeInfo orderInfo, HashMap<String, Object> ntMap) {

        if(webNodes == null)
            return;

        String siteId = (String)ntMap.get("siteId");
        ChatOrder chatOrder = new ChatOrder();
        chatOrder.nt = (String)ntMap.get("id");
        chatOrder.cs_template_id = chatJson.getString("templateid");

        JSONArray suppliers = JSON.parseArray(chatJson.getString("suppliers"));
        for(int i=0;i<suppliers.size();i++) {
            Map<String,String> supplierMap = new HashMap();
            supplierMap.put("rs_cs_id", suppliers.getJSONObject(i).getString("supplierid"));
            supplierMap.put("rs_cs", suppliers.getJSONObject(i).getString("suppliername"));
            chatOrder.rs_list.add(supplierMap);
        }

        chatOrder.vs_startpage_url = chatJson.getString("startpageurl");
        chatOrder.conversation_time = chatJson.getLong("starttime");
        ItemConfigKeyAction keyItem = ItemConfigKeyAction.getKeyItem(siteId, chatOrder.vs_startpage_url, KeyPageContainer.getInstance(), null);
        if (keyItem != null) {
            chatOrder.keylevel = keyItem.keylevel;
            chatOrder.keyname = keyItem.keyname;
        }

        chatOrder.order_time = (long)orderInfo.getParams().get("createTime");
        chatOrder.order_type = getOrderType(chatOrder.order_time, chatOrder.conversation_time);
        chatOrder.order_id = (String)orderInfo.getParams().get("oi");
        chatOrder.order_price = (String)orderInfo.getParams().get("op");

        for(RecordInfoFull record:records) {
            if(record.getNavNodeInfo().getSpaceName().equals(ActorNames.IP)) {
                chatOrder.ip = (String)record.getNavNodeInfo().getParams().get("ip");
                chatOrder.country = (String)record.getNavNodeInfo().getParams().get("country");
                chatOrder.province = (String)record.getNavNodeInfo().getParams().get("province");
                chatOrder.city = (String)record.getNavNodeInfo().getParams().get("city");
            }
            else if(record.getNavNodeInfo().getSpaceName().equals(ActorNames.Source)) {
                chatOrder.keyword = (String)record.getNavNodeInfo().getParams().get("keyword");
                chatOrder.source = (String)record.getNavNodeInfo().getParams().get("source");
                chatOrder.ref = (String)record.getNavNodeInfo().getParams().get("ref");
                chatOrder.ref = (String)record.getNavNodeInfo().getParams().get("ref");
            }
            else if(record.getNavNodeInfo().getSpaceName().equals(ActorNames.Terminal)) {
                chatOrder.tml = (String)record.getNavNodeInfo().getParams().get("tml");
            }
        }

        //5.查找订单节点关联的web节点


        //ActorSelection kafkaSelection = getContext().actorSelection("../../../" + ActorNames.KafkaManager);
        for(NavNodeInfo webNode:webNodes) {
            //订单keylevel
            chatOrder.order_level = (long)webNode.getParams().get("keylevel");
            JSONObject event_propertie = (JSONObject) JSON.toJSON(chatOrder);
            JSONArray event_properties = new JSONArray();
            event_properties.add(event_propertie);

            JSONObject eventJson = new JSONObject();
            eventJson.put("event_name", "chat_order");
            eventJson.put("event_time", System.currentTimeMillis());
            eventJson.put("event_siteid", siteId);
            eventJson.put("event_distinctid", String.valueOf((int)(Math.random()*1000000000)));
            eventJson.put("event_properties", event_properties);
            //{event_name:"",event_time:"",event_siteid:"",event_distinctid:"",event_properties:[{……}] }

            App.ntKafkaProducer.send(eventJson.toString());
            log.info("Send kafka msg: " + eventJson.toJSONString());
            //kafkaSelection.tell(chatOrder, getSelf());
        }
    }

    /**
     * @param order_time
     * @param conversation_time
     * @return  1:当天订单 2:24小时订单  3:三天内订单  -1:不符合任何要求
     */
    private int getOrderType(long order_time, long conversation_time) {

        if(Statics.isSameDate(new Date(order_time), new Date(conversation_time))) {
            return 1;
        }
        if(Statics.is24Hours(order_time, conversation_time)) {
            return 2;
        }
        if(Statics.is3Day(order_time, conversation_time)) {
            return 3;
        }
        return -1;
    }

    private void createRecord(PVMessage message) {

        String navId;
        String indexParam;
        NavigationSpaceInfo navigationSpaceInfo;
        LinkedHashMap<String, List<ActorRef>> sid2Record;
        List<ActorRef> recordRefList;

        try {
            Map<String, Object> otherInfo = message.getOtherInfo();
            Map<String, String> bodyInfo = message.getBodyNodeInfo();

            String sid = (String)otherInfo.get("sid");
            String nt_id = bodyInfo.get("nt");

            // 添加sid
            Map<String, LinkedHashMap<String, List<ActorRef>>> nt2sid2Record = trackInfo.getNt2sid();
            if(nt2sid2Record.containsKey(nt_id)) {
                sid2Record = nt2sid2Record.get(nt_id);
            } else {
                sid2Record = new LinkedHashMap<>();
                nt2sid2Record.put(nt_id,sid2Record);
            }

            // 添加Record引用
            if(sid2Record.containsKey(sid)) {
                recordRefList = sid2Record.get(sid);
            } else {
                recordRefList = new ArrayList<>();
                sid2Record.put(sid,recordRefList);
            }

            // 创建Record
            Map map = message.getNavigationInfo();
            Set<String> reportNavNameSet = map.keySet(); //前端上报的导航空间名字
            Map<String, NavigationSpaceInfo> item = NavigationSpaceConfig.getInstance().getItem();
            List<ActorRef> tempRecordRefs = new ArrayList<>();
            JSONArray array;
            //JSONObject obj = null;
            for(String navSpaceName: reportNavNameSet) {

                array = (JSONArray)map.get(navSpaceName);
                /*if(json.startsWith("[")) {
                    array = JSON.parseArray(json);
                } else {
                    obj = JSON.parseObject(json);
                    array = new JSONArray();
                    array.add(obj);
                }*/

                for(int i=0;i<array.size();i++) {
                    navigationSpaceInfo = item.get(navSpaceName);
                    if(navigationSpaceInfo==null)
                        continue;

                    indexParam = navigationSpaceInfo.getIndexParam(); //导航空间的索引参数名
                    if(indexParam != null) {
                        navId = array.getJSONObject(i).getString(indexParam);

                    } else {
                        JSONObject navParams = array.getJSONObject(i);
                        StringBuffer sb = new StringBuffer();
                        for(Map.Entry<String, Object> entry:navParams.entrySet()) {
                            sb.append(entry.getValue());
                        }
                        navId = HashAlgorithms.mixHash(sb.toString()) + "";

                    }

                    RecordInfo recordInfo = new RecordInfo(nt_id, null, navSpaceName, navId, otherInfo, message.getCreateTime());
                    ActorRef recordRef = getContext().actorOf(Props.create(Record.class, recordInfo));
                    recordRef.tell("save", getSelf());

                    // 把web导航空间放在第一位
                    if(navSpaceName.equals(ActorNames.Web))
                    {
                        tempRecordRefs.add(0, recordRef);
                    }
                    else
                    {
                        tempRecordRefs.add(recordRef);
                    }
                }
            }

            recordRefList.addAll(tempRecordRefs);


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
    class RecordFullFillment {

        private GetUserTrackMessage getUserTrackMessage;

        private KPIMessage kpiMessage;

        // 原始请求消息：有消息ID
        private BodyNodeMsgMap secondMessage;

        // 内存:Map<sid, Map<请求的Record列表, 返回的Record信息>>
        private TreeMap<String, LinkedHashMap<ActorRef, RecordInfoFull>> recordMap;

        // 聚合的消息数量
        private int count;

        // 实时聚合的消息数量,当nowCount等于count时，聚合完毕
        private int nowCount;

        // Neo4j
        private TreeMap<String, List<RecordInfoFull>> dbRecordMap;

        public RecordFullFillment(GetUserTrackMessage message) {
            getUserTrackMessage = message;
        }

        public GetUserTrackMessage getGetUserTrackMessage() {
            return getUserTrackMessage;
        }



        public void set(BodyNodeMsgMap secondMessage, TreeMap<String, LinkedHashMap<ActorRef, RecordInfoFull>> recordMap, int count) {
            this.secondMessage = secondMessage;
            this.count = count;
            this.recordMap = recordMap;
        }


        private void setBackMessages(ActorRef sender, RecordInfoFull recordMessage) {
            try {
                nowCount++;

                String sid = (String)recordMessage.getMap().get("sid");
                if(sid==null)
                    return;

                Map<ActorRef, RecordInfoFull> recordMessages = recordMap.get(sid);
                recordMessages.put(sender, recordMessage);

            } catch (Exception e) {
                log.error("Exception " + e.getMessage());
                StackTraceElement[] er = e.getStackTrace();
                for (int i = 0; i < er.length; i++) {
                    log.info(er[i].toString());
                }
            }
        }

        private String getInfos(String startPage) {

            TreeMap<String, List<RecordInfoFull>> sid2Records = new TreeMap<>();

            // 内存
            if(recordMap!=null && recordMap.size()>0) {
                Set<String> sidSet = recordMap.keySet();
                for(String sid:sidSet) {
                    sid2Records.put(sid, new ArrayList<>(recordMap.get(sid).values()));
                }
            }

            // DB
            if(dbRecordMap !=null && dbRecordMap.size() > 0) {
                sid2Records.putAll(dbRecordMap);
            }

            // 如果没有用户轨迹，返回kpi数据
            if(sid2Records.size() == 0) {
                JSONObject userJson = new JSONObject();
                Map<String,Object> map = kpiMessage.getMap();
                if(map!=null) {
                    for(Map.Entry<String,Object> entry:map.entrySet()) {
                        userJson.put(entry.getKey(), entry.getValue());
                    }
                }
                return userJson.toJSONString();
            }


            /**
             * navSpaceName ：
             *  Empty，return user info
             *  all, return all track
             *  Space name,  return only the request space info
             */

            String navSpaceName = getUserTrackMessage.getNavSpaceName();
            if(null == navSpaceName || navSpaceName.isEmpty()) {
                return getLastInfo(sid2Records);
            }

            if(navSpaceName.equals("all")){
                return getAllInfos(sid2Records, startPage);
            } else {
                return getNavInfos(navSpaceName, sid2Records);
            }

        }

        private String getNavInfos(String searchNavSpaceName, TreeMap<String, List<RecordInfoFull>> sid2Records) {

            JSONObject recordJson;
            JSONArray navArray = new JSONArray();
            JSONArray recordArray;
            LinkedHashMap<String, Integer> idMap; //节点ID,访问次数
            Map<Integer, String> ridMap; // 第几行，节点ID

            long nextPageTime = 0; // 下一个页面的访问时间
            long timelong = 0; // 页面停留时间
            String dv = null; //终端

            try {

                // 逆序返回数据
                NavigableSet<String> sidSet = sid2Records.descendingKeySet();
                for(String sid:sidSet) {
                    recordArray = new JSONArray();
                    idMap = new LinkedHashMap<>();
                    ridMap = new HashMap<>();

                    List<RecordInfoFull> rifs = sid2Records.get(sid);

                    for(RecordInfoFull recordInfoFull:rifs) {
                        NavNodeInfo navInfo = recordInfoFull.getNavNodeInfo();
                        String nodeSpaceName = navInfo.getSpaceName();
                        if(nodeSpaceName.equals(ActorNames.Terminal)) {
                            dv = (String)navInfo.getParams().get(FieldConstants.DEVICE);
                            break;
                        }
                    }


                    for(RecordInfoFull recordInfoFull:rifs) {

                        NavNodeInfo navInfo = recordInfoFull.getNavNodeInfo();

                        // 节点访问时间
                        long time = recordInfoFull.getTime();
                        String nodeSpaceName = navInfo.getSpaceName();

                        //停留时长
                        if(nodeSpaceName.equals(ActorNames.Web)) {

                            if (nextPageTime == 0) {
                                nextPageTime = time;
                            } else {
                                timelong = time - nextPageTime;
                                nextPageTime = time;
                            }
                        }


                        if(!searchNavSpaceName.equals(nodeSpaceName))
                            continue;

                        // 统计一次来访，每个节点被访问次数
                        String navId = navInfo.getId();
                        ridMap.put(recordArray.size(), navId);
                        if(idMap.containsKey(navId)) {
                            int count = idMap.get(navId);
                            idMap.put(navId, ++count);
                        } else {
                            idMap.put(navId, 1);
                        }

                        Map<String, Object> nodeParams = navInfo.getParams();
                        if(!getUserTrackMessage.isShowPrice()) {
                            nodeParams.remove(FieldConstants.ORDERPRICE);
                        }

                        recordJson = new JSONObject();
                        recordJson.putAll(nodeParams);
                        recordJson.put(FieldConstants.DEVICE, dv);
                        recordJson.put("time", time);
                        recordJson.put("timelong", 0);
                        recordArray.add(recordJson);

                        if(recordArray.size() > 1) {
                            JSONObject lastRecordJson = recordArray.getJSONObject(recordArray.size()-2);
                            lastRecordJson.put("timelong", timelong);
                        }
                    }

                    if(recordArray.size() > 0) {
                        JSONObject lastRecordJson = recordArray.getJSONObject(recordArray.size() - 1);
                        lastRecordJson.put("timelong", timelong);
                    }



                    for(int i=0;i<recordArray.size();i++) {
                        recordArray.getJSONObject(i).put("currentCount", idMap.get(ridMap.get(i)));
                    }

                    if(recordArray.size() > 0)
                        navArray.add(recordArray);

                }

            } catch (Exception e) {
                log.error("Exception " + e.getMessage());
                StackTraceElement[] er = e.getStackTrace();
                for (int i = 0; i < er.length; i++) {
                    log.info(er[i].toString());
                }
            }

            return navArray.toJSONString();
        }


        /**
         * 返回最近一次来访的info信息，不包含导航
         * @return
         */
        public String getLastInfo(TreeMap<String, List<RecordInfoFull>> sid2Records) {

            JSONObject userJson = new JSONObject();
            JSONArray sessionArray = new JSONArray();
            JSONArray infos;
            JSONObject info;
            JSONObject sessionJson;

            try {

                userJson.put("visitCount", sid2Records.size());
                userJson.put("sessions", sessionArray);

                // 来访位置展示的轨迹导航空间
                Set<String> spaceNames = NavigationSpaceConfig.getInstance().getSpaceNames();

                // 逆序返回数据
                NavigableSet<String> sidSet = sid2Records.descendingKeySet();
                String[] sidArray = sidSet.toArray(new String[0]);

                for(String sid:sidArray) {
                    int webNodeCount = 0;
                    info = new JSONObject();
                    infos = new JSONArray();
                    infos.add(info);
                    sessionJson = new JSONObject();
                    sessionJson.put("infos", infos);
                    sessionArray.add(sessionJson);

                    List<RecordInfoFull> rifs = sid2Records.get(sid);

                    int maxLevel = 1;
                    String maxLevelName = "";
                    long nextPageTime = 0; // 下一个页面的访问时间
                    long timelong = 0; // 页面停留时间
                    long totalTimelong = 0; // 总计停留时间
                    String loadPageUrl = "";
                    String loadPageTitle = "";


                    for(RecordInfoFull recordInfoFull:rifs) {

                        info.putAll(recordInfoFull.getMap());

                        NavNodeInfo navInfo = recordInfoFull.getNavNodeInfo();
                        Map<String, Object> nodeParams = navInfo.getParams();

                        long time = recordInfoFull.getTime(); // 节点访问时间
                        String spaceName = navInfo.getSpaceName();

                        if(spaceName==null) {
                            log.error("NavNode's navSpaceName is null " + navInfo);
                            continue;
                        }

                        // 产品位置：来访信息
                        if(spaceNames.contains(spaceName)) {
                            info.putAll(nodeParams);
                            continue;
                        }

                        // 产品位置：网页信息
                        if(loadPageUrl.isEmpty() && spaceName.equals(ActorNames.Web)) {
                            loadPageUrl = (String)nodeParams.get("url");
                            loadPageTitle = (String)nodeParams.get("ttl");
                        }

                        if(spaceName.equals(ActorNames.Web)) {

                            // 访问页面数
                            webNodeCount++;

                            int level = 0;
                            // 最大访问层级
                            Object keylevel = nodeParams.get("keylevel");
                            if (keylevel != null) {

                                if(keylevel instanceof String)
                                    level = Integer.parseInt((String)keylevel);
                                else if(keylevel instanceof Long)
                                    level = (int)keylevel;
                                else if(keylevel instanceof Integer)
                                    level = (int)keylevel;
                            }
                            if (maxLevel < level) {
                                maxLevel = level;
                                maxLevelName = (String)nodeParams.get("keyname");
                            }

                            //停留时长
                            if (nextPageTime == 0) {
                                nextPageTime = time;
                            } else {
                                timelong = time - nextPageTime;
                                nextPageTime = time;
                                totalTimelong += timelong;
                            }
                        }
                    }

                    info.put("loadPageUrl", loadPageUrl);
                    info.put("loadPageTitle", loadPageTitle);
                    info.put("webNodeCount", webNodeCount);
                    info.put("maxLevel", maxLevel);
                    info.put("totalTimelong", totalTimelong);
                    info.put("maxLevelName", maxLevelName);

                    break;
                }


            } catch (Exception e) {
                log.error("Exception " + e.getMessage());
                StackTraceElement[] er = e.getStackTrace();
                for (int i = 0; i < er.length; i++) {
                    log.info(er[i].toString());
                }
            }

            return JSON.toJSONString(userJson,true);
        }


        /**
         * 来访合并,来访倒序，PV正序
         * @return
         */
        public String getAllInfos(TreeMap<String, List<RecordInfoFull>> sid2Records, String startPage) {

            JSONObject userJson = new JSONObject();
            JSONArray sessionArray = new JSONArray();
            JSONArray infos;
            JSONObject info;
            JSONObject sessionJson;
            JSONArray pvArray;
            String dv = null; //终端
            Map<Integer, String> ridMap; // 第几行，pgid(网页ID)
            LinkedHashMap<String, Integer> idMap; //(网页ID),访问次数

            try {

                userJson.put("visitCount", sid2Records.size());
                userJson.put("sessions", sessionArray);
                Map<String,Object> map = kpiMessage.getMap();
                if(map!=null) {
                    for(Map.Entry<String,Object> entry:map.entrySet()) {
                        userJson.put(entry.getKey(), entry.getValue());
                    }
                }

                // 来访位置展示的轨迹导航空间
                Set<String> spaceNames = NavigationSpaceConfig.getInstance().getSpaceNames();

                // 逆序返回数据
                NavigableSet<String> sidSet = sid2Records.descendingKeySet();
                String[] sidArray = sidSet.toArray(new String[0]);

                for(String sid:sidArray) {
                    idMap = new LinkedHashMap<>();
                    ridMap = new HashMap<>();
                    int webNodeCount = 0;
                    info = new JSONObject();
                    infos = new JSONArray();
                    infos.add(info);
                    sessionJson = new JSONObject();
                    sessionJson.put("infos", infos);
                    sessionArray.add(sessionJson);
                    pvArray = new JSONArray();
                    sessionJson.put("records", pvArray);

                    List<RecordInfoFull> rifs = sid2Records.get(sid);

                    int maxLevel = 1;
                    String maxLevelName = "";
                    long nextPageTime = 0; // 下一个页面的访问时间
                    long timelong = 0; // 页面停留时间
                    long totalTimelong = 0; // 总计停留时间
                    JSONObject pvJson = null;

                    for(RecordInfoFull recordInfoFull:rifs) {
                        NavNodeInfo navInfo = recordInfoFull.getNavNodeInfo();
                        String nodeSpaceName = navInfo.getSpaceName();
                        if(nodeSpaceName.equals(ActorNames.Terminal)) {
                            dv = (String)navInfo.getParams().get(FieldConstants.DEVICE);
                            break;
                        }
                    }

                    for(RecordInfoFull recordInfoFull:rifs) {

                        info.putAll(recordInfoFull.getMap());
                        NavNodeInfo navInfo = recordInfoFull.getNavNodeInfo();
                        Map<String, Object> nodeParams = navInfo.getParams();

                        long time = recordInfoFull.getTime(); // 节点访问时间
                        String spaceName = navInfo.getSpaceName();

                        if(spaceName==null) {
                            log.error("NavNode's navSpaceName is null " + navInfo);
                            continue;
                        }

                        // 产品位置：来访信息
                        if(spaceNames.contains(spaceName)) {
                            if(info.containsKey("source") && nodeParams.containsKey("source")) {
                                //只取第一个PV的来源，防止被网站内部跳转的直接输入覆盖
                            } else {
                                info.putAll(nodeParams);
                            }

                            continue;
                        }

                        // 产品位置：网页信息
                        if(null == pvJson || spaceName.equals(ActorNames.Web)) {
                            pvJson = new JSONObject();
                            pvArray.add(pvJson);

                            // 统计一次来访，每个网页被访问次数
                            String pageId = (String)nodeParams.get("pgid");
                            ridMap.put(webNodeCount, pageId);
                            if(idMap.containsKey(pageId)) {
                                int count = idMap.get(pageId);
                                idMap.put(pageId, ++count);
                            } else {
                                idMap.put(pageId, 1);
                            }
                        }

                        pvJson.putAll(nodeParams);


                        if(!getUserTrackMessage.isShowPrice() && nodeParams.containsKey(FieldConstants.ORDERPRICE)) {
                            pvJson.remove(FieldConstants.ORDERPRICE);
                        }

                        if(spaceName.equals(ActorNames.Web)) {

                            // 访问页面数
                            webNodeCount++;

                            int level = 0;
                            // 最大访问层级  图数据库node.asMap()有一个bug，会把value的类型从int转换成long
                            Object keylevel = nodeParams.get("keylevel");
                            String keyName = (String)nodeParams.get("keyname");
                            if (keylevel != null) {

                                if(keylevel instanceof String)
                                    level = Integer.parseInt((String)keylevel);
                                else if(keylevel instanceof Long) {
                                    level = (int)((long) keylevel);
                                }
                                else if(keylevel instanceof Integer)
                                    level = (int)keylevel;
                            }
                            if (maxLevel < level) {
                                maxLevel = level;
                                maxLevelName = keyName;
                            }

                            //停留时长
                            if (nextPageTime == 0) {
                                nextPageTime = time;
                            } else {
                                timelong = time - nextPageTime;
                                nextPageTime = time;
                                totalTimelong += timelong;
                            }

                            if(pvArray.size() > 1) {
                                JSONObject lastPVJson = pvArray.getJSONObject(pvArray.size()-2);
                                lastPVJson.put("timelong", timelong);
                            }

                            pvJson.put("keylevel", level);
                            pvJson.put("keyname", keyName);
                        }

                        pvJson.put(FieldConstants.DEVICE, dv);
                        pvJson.put("timelong", 0);
                        pvJson.put("time", time);
                    }

                    info.put("webNodeCount", webNodeCount);
                    info.put("maxLevel", maxLevel);
                    info.put("totalTimelong", totalTimelong);
                    info.put("maxLevelName", maxLevelName);
                    if(pvArray.size() > 1) {
                        info.put("landingpage", pvArray.getJSONObject(0).getString("url"));
                    }

                    //咨询发起页
                    if(startPage != null && !"".equals(startPage)) {
                        ItemConfigKeyAction keyPage = getKeyPage(siteId, startPage);
                        if(keyPage!=null) {
                            info.put("startpage_"+ FieldConstants.KEYLEVEL, keyPage.keylevel);
                            info.put("startpage_"+ FieldConstants.KEYNAME, keyPage.keyname);
                        }
                    }

                    log.info("currentCount pvArray.size=" + pvArray.size());
                    for(int i=0;i<pvArray.size();i++) {
                        pvArray.getJSONObject(i).put("currentCount", idMap.get(ridMap.get(i)));
                    }
                }

                // 一个访客，两次同时来访合并
                mergeSession(sessionArray);



            } catch (Exception e) {
                log.error("Exception " + e.getMessage());
                StackTraceElement[] er = e.getStackTrace();
                for (int i = 0; i < er.length; i++) {
                    log.info(er[i].toString());
                }
            }

            return JSON.toJSONString(userJson,true);
        }

        private void mergeSession(JSONArray sessionArray) {

            try {
                long pre_startTime = 0;
                long pre_endTime = 0;
                JSONObject pre_session = null;

                for(int i=0;i<sessionArray.size();i++) {

                    JSONObject now_session = sessionArray.getJSONObject(i);
                    JSONArray now_records = now_session.getJSONArray("records");
                    long now_startTime = now_records.getJSONObject(0).getLong("time");
                    long now_endTime = now_records.getJSONObject(now_records.size()-1).getLong("time");

                    if(pre_startTime == 0) {
                        pre_session = now_session;
                        pre_startTime = now_startTime;
                        pre_endTime = now_endTime;
                        continue;
                    }

                    // 两次来访有时间交集
                    if((pre_startTime<now_startTime && now_startTime<pre_endTime)
                            || (pre_startTime<now_endTime && now_endTime<pre_endTime)) {

                        JSONArray pre_infosJson = pre_session.getJSONArray("infos");
                        JSONArray now_infosJson = now_session.getJSONArray("infos");

                        JSONArray pre_records = pre_session.getJSONArray("records");

                        TreeMap<Long,JSONObject> pvMap = new TreeMap<>();
                        JSONObject tempRecord = null;

                        for(int j=0;j<pre_records.size();j++) {
                            tempRecord = pre_records.getJSONObject(j);
                            pvMap.put(tempRecord.getLong("time"), tempRecord);
                        }

                        for(int j=0;j<now_records.size();j++) {
                            tempRecord = now_records.getJSONObject(j);
                            pvMap.put(tempRecord.getLong("time"), tempRecord);
                        }

                        sessionArray.remove(i-1); //删除上一次来访
                        sessionArray.remove(i-1); //删除当前来访

                        JSONObject sessionJson = new JSONObject();
                        sessionArray.set(i-1, sessionJson); //添加合并后来访

                        pre_infosJson.addAll(now_infosJson);
                        sessionJson.put("infos", pre_infosJson);

                        Collection<JSONObject> c = pvMap.values();
                        JSONArray recordsArray = new JSONArray();
                        recordsArray.addAll(Arrays.asList(c));
                        sessionJson.put("records", recordsArray);

                        log.info(sessionJson.toString());
                    }
                }
                log.info(sessionArray.toString());
            } catch (Exception e) {
                log.error("Exception " + e.getMessage());
                StackTraceElement[] er = e.getStackTrace();
                for (int i = 0; i < er.length; i++) {
                    log.info(er[i].toString());
                }
            }

        }

        public void setKpiMessage(KPIMessage KPIMessage) {
            this.kpiMessage = KPIMessage;
        }

        public KPIMessage getKpiMessage() {
            return kpiMessage;
        }

        public void setDbRecordMap(TreeMap<String,List<RecordInfoFull>> dbRecordMap) {
            this.dbRecordMap = dbRecordMap;
        }
    }


    private ItemConfigKeyAction getKeyPage(String siteId, String url) {


        return ItemConfigKeyAction.getKeyItem(siteId, url, KeyPageContainer.getInstance(), null);

//        if (keyItem != null) {
//            navigationJson.getJSONObject("Web").put(FieldConstants.KEYLEVEL, keyItem.keylevel);
//            navigationJson.getJSONObject("Web").put(FieldConstants.KEYNAME, keyItem.keyname);
//        }
    }
}

