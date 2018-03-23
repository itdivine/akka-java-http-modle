package cn.xiaoneng.skyeye.bodyspace.actor;

import akka.actor.*;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import cn.xiaoneng.skyeye.bodyspace.message.*;
import cn.xiaoneng.skyeye.bodyspace.model.BodyNodeModel;
import cn.xiaoneng.skyeye.bodyspace.model.NTBodyNodeModel;
import cn.xiaoneng.skyeye.db.Neo4jDataAccess;
import cn.xiaoneng.skyeye.monitor.Monitor;
import cn.xiaoneng.skyeye.monitor.MonitorCenter;
import cn.xiaoneng.skyeye.monitor.Node;
import cn.xiaoneng.skyeye.track.message.GetUserTrackMessage;
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
import java.util.Map;
import java.util.Set;


/**
 * 主体节点
 * Created by liangyongheng on 2016/7/26 21:21.
 */
public class BodyNode extends AbstractActor {

    protected final static Logger log = LoggerFactory.getLogger(BodyNode.class);
    private static Monitor monitor = MonitorCenter.getMonitor(Node.BodyNode);

    private BodyNodeModel model = null;


    private ActorRef mediator;

    //TODO CookieNode使用 临时解决，需要放入到model中，并让存储更新轨迹bean包
    private String _loginId;

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

            if (msg instanceof BodyNodeModel) {
                model = (BodyNodeModel) msg;

            } else if (msg instanceof BodyNodeMsg) {

                BodyNodeMsg message = (BodyNodeMsg) msg;

//                Set<String> newSet = megreSet(message.getRelatedNtSet());

//                getSender().tell(createPVResultMsg(newSet, message.getMsgId()), getSelf());
                doGet(message);

                monitor.newWriteTime("BodyNodeMsg", System.currentTimeMillis() - start, true);

            } else if (msg instanceof BodyNodeCreateMsg) {

                BodyNodeCreateMsg message = (BodyNodeCreateMsg) msg;

                onCreateBodyNode(message);

                monitor.newWriteTime("BodyNodeCreateMsg", System.currentTimeMillis() - start, true);

            } else if (msg instanceof String) {

                JSONObject json = JSON.parseObject((String) msg);

                String method = json.getString("method");

                if (HTTPCommand.GET.equals(method)) {

//                    String rtnStr = "{\"body\" : {\"id\" : \"" + this.model.getId() + "\",\"nt\" : \"" + this.model.getNt_id() + "\"," +
//                            "\"createtime\" : \"" + this.model.getCreateTime() + "\", \"lastvisittime\" : \"" + this.model.getLastVisitTime() + "\"}," +
//                            "\"status\" : 200}";
                    String rtnStr = "{\"body\" : " + JSON.toJSONString(model) + ", \"status\" : 200}";

                    getSender().tell(rtnStr, getSelf());
                }

                monitor.newWriteTime("HTTP.GET", System.currentTimeMillis() - start, true);

            } else if (msg instanceof NTRelatedMsg) {
                //CookieNode
                if (model.getBodySpace().equals(ActorNames.COOKIE_BODYSPACE)) {
                    NTRelatedMsg ntRelatedMsg = (NTRelatedMsg) msg;
                    setRelateByNt(ntRelatedMsg.getNt_id(), ntRelatedMsg.getLoginId());
                    monitor.newWriteTime("NTRelatedMsg", System.currentTimeMillis() - start, true);

                } else if (model.getBodySpace().equals(ActorNames.NT_BODYSPACE)) {
                    log.debug("receive NTRelatedMsg " + (NTRelatedMsg) msg);
                    NTRelatedMsg ntRelatedMsg = (NTRelatedMsg) msg;
                    //如果当前nt对应login为空,则创建login,并关联nt账号
                    NTBodyNodeModel _model = (NTBodyNodeModel) model;
                    if (_model.getLoginId() == null) {
                        _model.setLoginId(ntRelatedMsg.getLoginId());
                        _model.addRelateNt_id(ntRelatedMsg.getNt_id());
                    } else {
                        //如果login账号相同则进行关联，否则不关联。
                        if (_model.getLoginId().equals(ntRelatedMsg.getLoginId()) && ntRelatedMsg.getNt_id() != null) {
                            _model.addRelateNt_id(ntRelatedMsg.getNt_id());
                        }
                    }
                    saveRelation(_model);
                    monitor.newWriteTime("NTRelatedMsg", System.currentTimeMillis() - start, true);
                }
            } else if (msg instanceof GetUserTrackMessage) {

                if (model.getBodySpace().equals(ActorNames.NT_BODYSPACE)) {
                    NTBodyNodeModel _model = (NTBodyNodeModel) model;
                    log.debug("receive GetUserTrackMessage " + (GetUserTrackMessage)msg);

                    //查询关联账号
                    BodyNodeMsgMap rtnMsg = new BodyNodeMsgMap(((GetUserTrackMessage) msg).getMsgId());

                    rtnMsg.setNt_id(_model.getId());
                    rtnMsg.getAccountNumMap().putAll(_model.getAccountNumMap());
                    rtnMsg.getRelatedNtSet().addAll(_model.getRelateNtSet());

                    getSender().tell(rtnMsg, getSelf());

                    monitor.newWriteTime("GetUserTrackMessage", System.currentTimeMillis()-start, true);

                }
            } else if(msg instanceof CreateNodeFromDB) {
                // NTBodyNode
                log.debug("receive CreateNodeFromDB " + (CreateNodeFromDB)msg);

                String nt_id = ((CreateNodeFromDB) msg).getNt_id();
                String msgId = (((CreateNodeFromDB) msg).getMessage()).getMsgId();
                BodyNodeMsgMap rtnMsg = new BodyNodeMsgMap(msgId);
                rtnMsg.setNt_id(nt_id);

                model = getNodeFromDB(nt_id);
                if(model==null) {
                    getSender().tell(rtnMsg, getSelf());

                    //TODO 销毁当前Actor

                    monitor.newWriteTime("CreateNodeFromDB", System.currentTimeMillis()-start, true);
                    return;

                } else {
                    NTBodyNodeModel _model = (NTBodyNodeModel) model;
                    rtnMsg.getAccountNumMap().putAll(_model.getAccountNumMap());
                    rtnMsg.getRelatedNtSet().addAll(_model.getRelateNtSet());
                    getSender().tell(rtnMsg, getSelf());
                    monitor.newWriteTime("CreateNodeFromDB", System.currentTimeMillis()-start, true);
                }
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

    /**
     * 保存nt节点间关系
     */
    private void saveRelation(NTBodyNodeModel _model) {

        if (_model.getRelateNtSet() != null && !_model.getRelateNtSet().isEmpty()) {

            for (String relationId : _model.getRelateNtSet()) {

                HashMap<String, Object> relationMap = new HashMap<>();
                relationMap.put("id1", _model.getId());
                relationMap.put("id2", relationId);
                relationMap.put("siteId", Statics.getSiteId(getSelf().path().elements().iterator()));

                Neo4jDataAccess.setBodyBondRelation("nt", relationMap);
            }
        }
    }

    private void setRelateByNt(String newNt_id, String loginId) {

//        System.out.println("------------------------------------------------loginId : " + loginId);

        if (newNt_id == null || loginId == null) {
            return;
        } else {

            String oldNt_id = this.model.getNt_id();

//            if (newNt_id.equals(oldNt_id)) {
//
//                //通知ntactor创建loginid
//                this.getContext().actorSelection("../../nt/" + oldNt_id).tell(new NTRelatedMsg(null, loginId), getSelf());
//            } else {

            //如果登陆ID相等，创建等价关系
            if (_loginId != null && _loginId.equals(loginId)) {
                //通知nt_actor创建nt之间等价关系
                this.getContext().actorSelection("../../nt/" + oldNt_id).tell(new NTRelatedMsg(newNt_id, loginId), getSelf());
                this.getContext().actorSelection("../../nt/" + newNt_id).tell(new NTRelatedMsg(oldNt_id, loginId), getSelf());
            }
            //如果登陆ID不相等，解绑cookie->旧nt的关系，保留cookie<-旧nt的关系，增加cookie和新nt的关系
            {
                removeRelation();

                model.setNt_id(newNt_id);
                saveRelation(ActorNames.COOKIE_BODYSPACE);

            }

//            }
        }
    }

    private void removeRelation() {

        HashMap<String, Object> map = new HashMap<>();

        map.put("id1", model.getId());
        map.put("id2", model.getNt_id());
        map.put("siteId", model.getSiteId());
        Neo4jDataAccess.deleteBodyBondRelation("cookie", map);
//        Neo4jDataAccess_source.deleteBodyBondRelation("cookie", map);

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

        try {
            if (message.getSpaceName().equals(ActorNames.NT_BODYSPACE)) {
                createNTNode(message);
            } else if (message.getSpaceName().equals(ActorNames.COOKIE_BODYSPACE)) {
                createCookieNode(message);
            } else {
                createBodyNode(message);
            }

        } catch (Exception e) {

            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }
    }

    private void createCookieNode(BodyNodeCreateMsg message) {
        //cookie登陆新的login账号，则绑定新的login账号，和旧的login账号解绑
//                if(model!=null && !model.getNt_id().equals(((BodyNodeCreateMsg) message).getNt_id())) {
//                    model.setNt_id(((BodyNodeCreateMsg) message).getNt_id());
//                }

        createBodyNode((BodyNodeCreateMsg) message);
        _loginId = ((BodyNodeCreateMsg) message).getLoginId();
//        monitor.newWriteTime("BodyNodeCreateMsg", System.currentTimeMillis() - start, true);

    }

    private void createNTNode(BodyNodeCreateMsg message) {
        BodyNodeCreateMsg msg = (BodyNodeCreateMsg) message;
        String nt_id = msg.getNt_id();
        String msg_loginId = msg.getLoginId();
        Map<String, String> accountNumMap = msg.getAccountNumMap();

        if (this.model == null) {

            // DB中查找节点
            NTBodyNodeModel dbModel = getNodeFromDB(nt_id);

            if (dbModel == null) {
                this.model = new NTBodyNodeModel();
                model.setCreateTime(msg.getMsgtime());
                model.setId(msg.getId());
                model.setNt_id(nt_id);
                ((NTBodyNodeModel) model).setLoginId(msg_loginId);
                ((NTBodyNodeModel) model).setAccountNumMap(accountNumMap);
                model.setSiteId(Statics.getSiteId(getSelf().path().elements().iterator()));

                if (accountNumMap != null) {
                    ((NTBodyNodeModel) model).getCookieSet().add(accountNumMap.get("cookie"));
                }

                saveNode();

            } else {
                model = dbModel;
            }

        } else {

            merge((NTBodyNodeModel) model, msg_loginId, accountNumMap);
        }

        PVResultMsg resutlMsg = new PVResultMsg();
        resutlMsg.setBodyNode(model);
        resutlMsg.setMsgId(msg.getMsgId());
        resutlMsg.setSpaceName(msg.getSpaceName());
        resutlMsg.setData_status(1);

        getSender().tell(resutlMsg, getSelf());

//        monitor.newWriteTime("BodyNodeCreateMsg", System.currentTimeMillis()-start, true);

    }

    private NTBodyNodeModel getNodeFromDB(String nt_id) {

        NTBodyNodeModel model = null;

        try {

            String labs = ":Body:" + ActorNames.NT_BODYSPACE;
            HashMap<String, Object> map = new HashMap();
            map.put("siteId", Statics.getSiteId(getSelf().path().elements().iterator()));
            map.put("id", nt_id);

            model = Neo4jDataAccess.getNTBodyNodeModel(labs, map);
            if (model != null)
                log.debug(model.toString());

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return model;
    }

    /**
     * 保存节点信息
     */
    private void saveNode() {
        HashMap<String, Object> map = new HashMap();

        map.put("id", model.getId());
        map.put("siteId", Statics.getSiteId(getSelf().path().elements().iterator()));
        map.put("createTime", model.getCreateTime());
        Neo4jDataAccess.setBodyNode("nt", map);
    }

    /**
     * 内存model合并http请求中新增的数据
     *
     * @param model
     * @param msg_loginId
     * @param accountNumMap
     * @return true：有新增(需要更新DB)   false：没有改变
     */
    private boolean merge(NTBodyNodeModel model, String msg_loginId, Map<String, String> accountNumMap) {

        boolean ischange = false;

        //保留有值的loginId
        String model_loginId = model.getLoginId();

        if (Statics.isNullOrEmpty(model_loginId) && !Statics.isNullOrEmpty(msg_loginId)) {
            model.setLoginId(msg_loginId);
            ischange = true;
        }

        if (model.getAccountNumMap() == null) {

            model.setAccountNumMap(accountNumMap);
        } else {
            model.getAccountNumMap().putAll(accountNumMap);

            model.getCookieSet().add(accountNumMap.get("cookie"));
        }

        return ischange;
    }

    private void createBodyNode(BodyNodeCreateMsg message) {
        if (this.model == null) {
            this.model = new BodyNodeModel();
            this.model.setCreateTime(message.getMsgtime());
            this.model.setBodySpace(message.getSpaceName());
            this.model.setNt_id(message.getNt_id());
            this.model.setId(message.getId());
            this.model.setSiteId(Statics.getSiteId(getSelf().path().elements().iterator()));
        }
        this.model.setLastVisitTime(message.getMsgtime());

        PVResultMsg resutlMsg = new PVResultMsg();
        resutlMsg.setBodyNode(model);
        resutlMsg.setMsgId(message.getMsgId());
        resutlMsg.setSpaceName(message.getSpaceName());
        resutlMsg.setData_status(1);

        getSender().tell(resutlMsg, getSelf());

        saveNode(message.getSpaceName());
        saveRelation(message.getSpaceName());
    }

    private void saveNode(String spaceName) {
        try {
            HashMap<String, Object> map = new HashMap();
            map.put("id", this.model.getId());
            map.put("createTime", this.model.getCreateTime());
            map.put("siteId", this.model.getSiteId());
            Neo4jDataAccess.setBodyNode(spaceName, map);
        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }
    }

    public void saveRelation(String spaceName) {

        try {
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

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }
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

        nodeModel.setBodySpace(model.getBodySpace());
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
