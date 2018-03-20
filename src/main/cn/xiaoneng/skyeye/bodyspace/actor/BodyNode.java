package cn.xiaoneng.skyeye.bodyspace.actor;

import akka.actor.*;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import cn.xiaoneng.skyeye.bodyspace.message.BodyNodeCreateMsg;
import cn.xiaoneng.skyeye.bodyspace.message.BodyNodeMsg;
import cn.xiaoneng.skyeye.bodyspace.message.PVDataStatus;
import cn.xiaoneng.skyeye.bodyspace.message.PVResultMsg;
import cn.xiaoneng.skyeye.bodyspace.model.BodyNodeModel;
import cn.xiaoneng.skyeye.bodyspace.model.NTBodyNodeModel;
import cn.xiaoneng.skyeye.db.Neo4jDataAccess;
import cn.xiaoneng.skyeye.monitor.Monitor;
import cn.xiaoneng.skyeye.monitor.MonitorCenter;
import cn.xiaoneng.skyeye.monitor.Node;
import cn.xiaoneng.skyeye.util.ActorNames;
import cn.xiaoneng.skyeye.util.HTTPCommand;
import cn.xiaoneng.skyeye.util.Statics;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


/**
 * 主体节点
 * Created by liangyongheng on 2016/7/26 21:21.
 */
public class BodyNode extends AbstractActor {

    protected final static Logger log = LoggerFactory.getLogger(BodyNode.class);
    private static Monitor monitor = MonitorCenter.getMonitor(Node.BodyNode);

    BodyNodeModel model = null;

    private ActorRef mediator;


    public BodyNode(BodyNodeModel model) {
        this.model = model;
    }

    public BodyNode() {



    }

    @Override
    public void preStart() throws Exception {
        super.preStart();

        mediator = DistributedPubSub.get(this.getContext().system()).mediator();
        mediator.tell(new DistributedPubSubMediator.Subscribe(getSelf().path().toStringWithoutAddress(), "NSkyEye", getSelf()), getSelf());
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .matchAny(this::onReceive)
                .build();
    }

    public void onReceive(Object msg) {

        long start = System.currentTimeMillis();

        try {
            log.debug(getSelf().path() + " receive message :" + msg.toString());

            if (msg instanceof BodyNodeMsg) {

                BodyNodeMsg message = (BodyNodeMsg) msg;

//                Set<String> newSet = megreSet(message.getRelatedNtSet());

//                getSender().tell(createPVResultMsg(newSet, message.getMsgId()), getSelf());
                doGet(message);

                monitor.newWriteTime("BodyNodeMsg", System.currentTimeMillis()-start, true);

            }
//            else if (msg instanceof NTCommand) {
//
//                NTCommand message = (NTCommand) msg;
//
//                BodyNodeMsg rtnMsg = createBodyNodeMsg();
//
//                rtnMsg.setRelatedNode(message.isRelatedNode());
//
//                getSender().tell(rtnMsg, getSelf());
//
//            }
//            else if (msg instanceof IdCommand) {
//
//                if (model.getId().equals(((IdCommand) msg).getId())) {
//
//                    BodyNodeMsg rtnMsg = createBodyNodeMsg();
//
//                    getSender().tell(rtnMsg, getSelf());
//                }
//            }
            else if (msg instanceof BodyNodeCreateMsg) {

                BodyNodeCreateMsg message = (BodyNodeCreateMsg) msg;

                onCreateBodyNode(message);

                monitor.newWriteTime("BodyNodeCreateMsg", System.currentTimeMillis()-start, true);

            } else if (msg instanceof String) {

                JSONObject json = JSON.parseObject((String) msg);

                String method = json.getString("method");

                if (HTTPCommand.GET.equals(method)) {

                    String rtnStr = "{\"body\" : {\"id\" : \"" + this.model.getId() + "\",\"nt\" : \"" + this.model.getNt_id() + "\"," +
                            "\"createtime\" : \"" + this.model.getCreateTime() + "\", \"lastvisittime\" : \"" + this.model.getLastVisitTime() + "\"}," +
                            "\"status\" : 200}";

                    getSender().tell(rtnStr, getSelf());
                }

                monitor.newWriteTime("HTTP.GET", System.currentTimeMillis()-start, true);
            }

        } catch (Exception e) {

            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
            getSender().tell("error!", getSelf());
        }

    }

//    private BodyNodeMsg createBodyNodeMsg() {
//
//        BodyNodeMsg rtnMsg = new BodyNodeMsg();
//
//        rtnMsg.setNt_id(model.getNt_id());
//        rtnMsg.setId(model.getId());
//        rtnMsg.setRelatedNtSet(model.getRelatedNtSet());
//        rtnMsg.setRelatedNode(false);
//        rtnMsg.setSpaceName(getContext().parent().path().name());
//
//        return rtnMsg;
//
//    }

    public void onCreateBodyNode(BodyNodeCreateMsg message) {

        if (this.model == null) {

            this.model = new BodyNodeModel();
            this.model.setCreateTime(message.getMsgtime());
            this.model.setNt_id(message.getNt_id());
            this.model.setId(message.getId());
            this.model.setSiteId(Statics.getSiteId(getSelf().path().elements().iterator()));

        }
        this.model.setLastVisitTime(message.getMsgtime());

        saveNode(message.getSpaceName());
        saveRelation(message.getSpaceName());

        PVResultMsg resutlMsg = new PVResultMsg();

        resutlMsg.setBodyNode(model);
        resutlMsg.setMsgId(message.getMsgId());
        resutlMsg.setSpaceName(message.getSpaceName());
        resutlMsg.setData_status(1);

        getSender().tell(resutlMsg, getSelf());

//        System.out.println("-------------------------"+ this.model.toString());
    }

    private void saveNode(String spaceName) {
        HashMap<String, Object> map = new HashMap();

        map.put("id", this.model.getId());
        map.put("createTime", this.model.getCreateTime());
        map.put("siteId", this.model.getSiteId());
        Neo4jDataAccess.setBodyNode(spaceName, map);
    }

    public void saveRelation(String spaceName) {

        String labs = ":Body:" + ActorNames.NT_BODYSPACE;
        HashMap<String, Object> map = new HashMap<>();
        map.put("siteId", Statics.getSiteId(getSelf().path().elements().iterator()));
        map.put("id", model.getNt_id());

        //No createTime , or else could not search the node.
        NTBodyNodeModel ntModel = Neo4jDataAccess.getNTBodyNodeModel(labs, map);

        // TODO 如果不存在nt节点则先创建  这里有问题，并发时会创建多个
        if (ntModel == null) {
            map.put("createTime", this.model.getCreateTime());
            Neo4jDataAccess.setBodyNode(ActorNames.NT_BODYSPACE, map);
        }

        map.remove("id");
        map.put("id1", model.getId());
        map.put("id2", model.getNt_id());
        Neo4jDataAccess.setBodyBondRelation(spaceName, map);


    }

    private PVResultMsg createPVResultMsg(Set<String> newSet, String msgId) {

        PVResultMsg msg = new PVResultMsg();

        msg.setMsgId(msgId);

        if (newSet.isEmpty()) {

            msg.setData_status(PVDataStatus.NO_CHANGE);
            return msg;
        } else {
            msg.setData_status(PVDataStatus.RELATION_CHANGE);
        }

        BodyNodeModel nodeModel = new BodyNodeModel();

        nodeModel.setId(model.getId());

        nodeModel.setNt_id(model.getNt_id());

//        nodeModel.setRelatedNtSet(newSet);

        Iterator<String> iterator = getSelf().path().getElements().iterator();

        iterator.next();
        String site_id = iterator.next();

        iterator.next();
        String spaceName = iterator.next();

        msg.setSite_id(site_id);

        msg.setSpaceName(spaceName);

        msg.setBodyNode(nodeModel);

        return msg;

    }

    private JSONObject toJSONObject() {

        JSONObject jsonObject = new JSONObject();

        jsonObject.put(BodyNodeModel.ID, this.model.getId());
        jsonObject.put(BodyNodeModel.NT_ID, this.model.getNt_id());
        jsonObject.put(BodyNodeModel.CREATETIME, this.model.getCreateTime());

        return jsonObject;
    }

    private void doGet(BodyNodeMsg msg) {

        JSONObject jsonObject = toJSONObject();

        jsonObject.put("msgId", msg.getMsgId());
        jsonObject.put("id", this.model.getId());
        jsonObject.put("createtime", this.model.getCreateTime());
        jsonObject.put("lastvisittime", this.model.getLastVisitTime());
        getSender().tell(jsonObject.toJSONString(), getSelf());

    }

//    private Set<String> megreSet(Set<String> newSet) {
//
//        Set<String> rtnSet = new HashSet<>();
//
//        if (newSet == null) {
//            return rtnSet;
//        }
//
//        for (String nt_id : newSet) {
//
//            if (!this.model.getRelatedNtSet().contains(nt_id) && !this.model.getNt_id().equals(nt_id)) {
//
//                this.model.getRelatedNtSet().add(nt_id);
//
//                rtnSet.add(nt_id);
//            }
//        }
//
//        return rtnSet;
//
//
////        if (this.model.getRelatedNtSet() != null) {
////            model.getRelatedNtSet().addAll(newSet);
////        } else {
////            model.setRelatedNtSet(newSet);
////
////        }
////        if (model.getRelatedNtSet().contains(model.getNt_id())) {
////            model.getRelatedNtSet().remove(model.getNt_id());
////        }
//
//    }

    public static void main(String[] args) {

        String configName = "App2";
        Config config = ConfigFactory.load().getConfig(configName);
        ActorSystem system = ActorSystem.create("test", config);
//        EVSInfo info = new EVSInfo();
//        info.setSiteId("01");
//        info.setName("牛逼的企业");
////            ActorRef evsRef = system.actorOf(Props.createEVS(EVSManager.class), "enterprises");
//
//
//        system.actorOf(Props.createEVS(EVS.class,new EVSInfo()),info.getSiteId());


        system.actorOf(Props.create(BodyNode.class, new BodyNodeModel()), "bodynode");

    }

    /**
     * 获取企业id
     *
     * @return
     */
//    public String getSiteId() {
//        Iterator<String> iterator = getSelf().path().getElements().iterator();
//        iterator.next();
//        iterator.next();
//        return iterator.next();
//    }


}
