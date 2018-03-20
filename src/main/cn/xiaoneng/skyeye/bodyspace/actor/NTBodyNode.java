package cn.xiaoneng.skyeye.bodyspace.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import cn.xiaoneng.skyeye.bodyspace.message.*;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liangyongheng on 2016/10/22 14:57.
 */
public class NTBodyNode extends BodyNode {

    protected final static Logger log = LoggerFactory.getLogger(NTBodyNode.class);
    private static Monitor monitor = MonitorCenter.getMonitor(Node.NTBodyNode);

    private NTBodyNodeModel model;

    private ActorRef mediator;

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

    public void onReceive(Object message) {

        long start = System.currentTimeMillis();

        try {

            if (message instanceof NTRelatedMsg) {

                log.debug("receive NTRelatedMsg " + (NTRelatedMsg)message);

                NTRelatedMsg msg = (NTRelatedMsg) message;

                //如果当前nt对应login为空,则创建login,并关联nt账号
                if (model.getLoginId() == null) {

                    model.setLoginId(msg.getLoginId());
//                model.setRelateNt_id(msg.getNt_id());

                    model.addRelateNt_id(msg.getNt_id());
                } else {

                    //如果login账号相同则进行关联，否则不关联。
                    if (model.getLoginId().equals(msg.getLoginId()) && msg.getNt_id() != null) {
//                    model.setRelateNt_id(msg.getNt_id());
                        model.addRelateNt_id(msg.getNt_id());
                    }
                }

                saveRelation();

                monitor.newWriteTime("NTRelatedMsg", System.currentTimeMillis()-start, true);

            } else if (message instanceof BodyNodeCreateMsg) {

                log.debug("receive BodyNodeCreateMsg " + (BodyNodeCreateMsg)message);

                BodyNodeCreateMsg msg = (BodyNodeCreateMsg) message;
                String nt_id = msg.getNt_id();
                String msg_loginId = msg.getLoginId();
                Map<String, String> accountNumMap = msg.getAccountNumMap();

                if (this.model == null) {

                    // DB中查找节点
                    NTBodyNodeModel dbModel = getNodeFromDB(nt_id);

                    if(dbModel == null) {
                        this.model = new NTBodyNodeModel();
                        model.setCreateTime(msg.getMsgtime());
                        model.setId(msg.getId());
                        model.setNt_id(nt_id);
                        model.setLoginId(msg_loginId);
                        model.setAccountNumMap(accountNumMap);
                        model.setSiteId(Statics.getSiteId(getSelf().path().elements().iterator()));

                        if (accountNumMap != null) {
                            model.getCookieSet().add(accountNumMap.get("cookie"));
                        }

                        saveNode();

                    } else {
                        model = dbModel;
                    }

                } else {

                    merge(model, msg_loginId, accountNumMap);
                }

                PVResultMsg resutlMsg = new PVResultMsg();
                resutlMsg.setBodyNode(model);
                resutlMsg.setMsgId(msg.getMsgId());
                resutlMsg.setSpaceName(msg.getSpaceName());
                resutlMsg.setData_status(1);

                getSender().tell(resutlMsg, getSelf());

                monitor.newWriteTime("BodyNodeCreateMsg", System.currentTimeMillis()-start, true);

            } else if (message instanceof GetUserTrackMessage) {

                log.debug("receive GetUserTrackMessage " + (GetUserTrackMessage)message);

                //查询关联账号
                BodyNodeMsgMap rtnMsg = new BodyNodeMsgMap(((GetUserTrackMessage) message).getMsgId());

                rtnMsg.setNt_id(model.getId());
                rtnMsg.getAccountNumMap().putAll(this.model.getAccountNumMap());
                rtnMsg.getRelatedNtSet().addAll(this.model.getRelateNtSet());

                getSender().tell(rtnMsg, getSelf());

                monitor.newWriteTime("GetUserTrackMessage", System.currentTimeMillis()-start, true);

            }else if (message instanceof String) {

                log.debug("receive String message " + message);

                JSONObject json = JSON.parseObject((String) message);

                String method = json.getString("method");

                if (HTTPCommand.GET.equals(method)) {

                    String rtnStr = "{\"body\" : " + this.model.toJsonStr() + ", \"status\" : 200}";

                    getSender().tell(rtnStr, getSelf());
                }

                monitor.newWriteTime("HTTP.GET", System.currentTimeMillis()-start, true);

            } else if(message instanceof CreateNodeFromDB) {

                log.debug("receive CreateNodeFromDB " + (CreateNodeFromDB)message);

                String nt_id = ((CreateNodeFromDB) message).getNt_id();
                String msgId = (((CreateNodeFromDB) message).getMessage()).getMsgId();
                BodyNodeMsgMap rtnMsg = new BodyNodeMsgMap(msgId);
                rtnMsg.setNt_id(nt_id);

                model = getNodeFromDB(nt_id);
                if(model==null) {
                    getSender().tell(rtnMsg, getSelf());

                    //TODO 销毁当前Actor

                    monitor.newWriteTime("CreateNodeFromDB", System.currentTimeMillis()-start, true);
                    return;

                } else {

                    rtnMsg.getAccountNumMap().putAll(this.model.getAccountNumMap());
                    rtnMsg.getRelatedNtSet().addAll(this.model.getRelateNtSet());
                    getSender().tell(rtnMsg, getSelf());
                    monitor.newWriteTime("CreateNodeFromDB", System.currentTimeMillis()-start, true);
                }
            }

        } catch (Exception e) {
            log.error("exception " + e.getMessage());
//            getSender().tell("error!", getSelf());

        }
    }

    /**
     * 内存model合并http请求中新增的数据
     * @param model
     * @param msg_loginId
     * @param accountNumMap
     * @return true：有新增(需要更新DB)   false：没有改变
     */
    private boolean merge(NTBodyNodeModel model, String msg_loginId, Map<String, String> accountNumMap) {

        boolean ischange = false;

        //保留有值的loginId
        String model_loginId = model.getLoginId();

        if(Statics.isNullOrEmpty(model_loginId) && !Statics.isNullOrEmpty(msg_loginId)) {
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

    private NTBodyNodeModel getNodeFromDB(String nt_id) {

        NTBodyNodeModel model = null;

        try {

            String labs = ":Body:" + ActorNames.NT_BODYSPACE;
            HashMap<String,Object> map = new HashMap();
            map.put("siteId", Statics.getSiteId(getSelf().path().elements().iterator()));
            map.put("id", nt_id);

            model = Neo4jDataAccess.getNTBodyNodeModel(labs, map);
            if(model!=null)
                log.debug(model.toString());

        } catch(Exception e) {
            log.error(e.getMessage());
        }

        return model;
    }

    /**
     * 保存节点信息
     */
    private void saveNode() {
        HashMap<String,Object> map = new HashMap();

        map.put("id", model.getId());
        map.put("siteId", Statics.getSiteId(getSelf().path().elements().iterator()));
        map.put("createTime", model.getCreateTime());
        Neo4jDataAccess.setBodyNode("nt", map);
    }

    /**
     * 保存nt节点间关系
     */
    private void saveRelation() {

        if (this.model.getRelateNtSet() != null && !this.model.getRelateNtSet().isEmpty()) {

            for (String relationId : model.getRelateNtSet()) {

                HashMap<String,Object> relationMap = new HashMap<>();
                relationMap.put("id1", model.getId());
                relationMap.put("id2", relationId);
                relationMap.put("siteId", Statics.getSiteId(getSelf().path().elements().iterator()));

                Neo4jDataAccess.setBodyBondRelation("nt", relationMap);
            }
        }
    }
}
