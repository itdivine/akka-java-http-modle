package cn.xiaoneng.skyeye.bodyspace.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.ClusterSharding;
import cn.xiaoneng.skyeye.bodyspace.message.*;
import cn.xiaoneng.skyeye.bodyspace.model.BodyNodeModel;
import cn.xiaoneng.skyeye.bodyspace.model.BodySpaceModel;
import cn.xiaoneng.skyeye.bodyspace.service.BodyNodeServiceActor;
import cn.xiaoneng.skyeye.db.Neo4jDataAccess;
import cn.xiaoneng.skyeye.monitor.Monitor;
import cn.xiaoneng.skyeye.monitor.MonitorCenter;
import cn.xiaoneng.skyeye.monitor.Node;
import cn.xiaoneng.skyeye.util.ActorNames;
import cn.xiaoneng.skyeye.util.HTTPCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 主体空间
 * Created by liangyongheng on 2016/7/26 11:28.
 */
public class BodySpace extends AbstractActor {

    protected final Logger log = LoggerFactory.getLogger(getSelf().path().toStringWithoutAddress());
    private static Monitor monitor = MonitorCenter.getMonitor(Node.BodySpace);

    private ActorRef serviceActor;
    private ActorRef mediator;
    private BodySpaceModel model;
    private ActorRef shardRegion = ClusterSharding.get(getContext().getSystem()).shardRegion(ActorNames.BodyNode);

    public BodySpace(BodySpaceModel model) {
        this.model = model;
    }

    public BodySpace() {}

    @Override
    public void preStart() throws Exception {
        super.preStart();

        mediator = DistributedPubSub.get(this.getContext().system()).mediator();
        mediator.tell(new DistributedPubSubMediator.Subscribe(getSelf().path().toStringWithoutAddress(), "NSkyEye", getSelf()), getSelf());

        log.info(getSelf().path().toStringWithoutAddress() + " " + model);

        serviceActor = getContext().actorOf(Props.create(BodyNodeServiceActor.class), "service");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchAny(this::onReceive)
                .build();
    }

    public void onReceive(Object message) {

        long start = System.currentTimeMillis();

        try {

            if (message instanceof BodySpaceMsg) {

                BodySpaceMsg msg = (BodySpaceMsg) message;
                String operation = msg.getOperation();

                if (operation.equals(HTTPCommand.GET)) {

                    String responseMsg = "{\"body\":{\"name\" : \"" + this.model.getName() + "\", \"status\" : " + this.model.getStatus() + "},\"status\" : 200}";

                    getSender().tell(responseMsg, getSelf());
                }

                monitor.newWriteTime("BodySpaceMsg", System.currentTimeMillis()-start, true);

            } else if (message instanceof BodyNodeMsg) {

                BodyNodeMsg msg = (BodyNodeMsg) message;
                String operation = msg.getOperation();

                if (HTTPCommand.POST.equals(operation)) {

                    createBodyNode(msg);

                } else if (HTTPCommand.GET.equals(operation)) {

                    if (msg.getNt_id() != null && !"".equals(msg.getNt_id())) {
                        String path = getSelf().path() + "/" + msg.getNt_id();

                        getContext().actorSelection(path).tell(message, getSender());

                    } else {
                        getSender().tell("{\"status\" : \"200\",\"info\":\"nt_id is null!\"}", getSelf());
                    }
                }

                monitor.newWriteTime("BodyNodeMsg", System.currentTimeMillis()-start, true);

            } else if (message instanceof ListInfo) {

                serviceActor.tell(message, getSender());

                monitor.newWriteTime("ListInfo", System.currentTimeMillis()-start, true);

            } else if (message instanceof BodyNodeCreateMsg) {

                BodyNodeCreateMsg msg = (BodyNodeCreateMsg) message;

                shardRegion.tell(msg, getSender());

                /*if (getContext().child(msg.getId()).isEmpty()) {
                    ActorRef bodyNode;

                    if (msg.getSpaceName().equals(ActorNames.NT_BODYSPACE)) {
                        bodyNode = getContext().actorOf(Props.create(NTBodyNode.class), msg.getId());
                    } else if (msg.getSpaceName().equals(ActorNames.COOKIE_BODYSPACE)) {
                        bodyNode = getContext().actorOf(Props.create(CookieBodyNode.class), msg.getId());
                    } else {
//                        shardRegion.tell(bodyNodeModel, getSelf());
                        bodyNode = getContext().actorOf(Props.create(BodyNode.class), msg.getId());
                    }
                    bodyNode.tell(msg, getSender());

                } else {
                    getContext().actorSelection(getSelf().path() + "/" + msg.getId()).tell(msg, getSender());
                }*/

                monitor.newWriteTime("BodyNodeCreateMsg", System.currentTimeMillis()-start, true);

            } else if (message instanceof CreateNodeFromDB) {

                shardRegion.tell(message, getSender());
                /*if (getContext().child(((CreateNodeFromDB) message).getNt_id()).isEmpty()){
                    getContext().actorOf(Props.create(NTBodyNode.class), ((CreateNodeFromDB) message).getNt_id()).tell(message, getSender());
                } else {
                    getContext().actorSelection(((CreateNodeFromDB) message).getNt_id()).tell(message, getSender());
                }*/

                monitor.newWriteTime("CreateNodeFromDB", System.currentTimeMillis()-start, true);
            }

        } catch (Exception e) {

            log.error("exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
//            getSender().tell("error!", getSelf());
        }
    }

    /**
     * 创建主体节点
     *
     * @param message
     */
    private void createBodyNode(BodyNodeMsg message) {

        if (getContext().child(message.getNt_id()).isEmpty()) {

            BodyNodeModel bodyNodeModel = createModel(message);

            Iterator<String> iterator = getSelf().path().getElements().iterator();

            iterator.next();
            String site_id = iterator.next();

            iterator.next();
            String spaceName = iterator.next();

            String label = ":Body:" + spaceName.toUpperCase();

            HashMap<String, Object> map = new HashMap();

            map.put("site_id", site_id);

            map.put("nt_id", bodyNodeModel.getNt_id());

            BodyNodeModel neo4jModel = Neo4jDataAccess.getBodyNodeModel(label, map);

            ActorRef bodyNode = null;

            if (neo4jModel == null) {

//                shardRegion.tell(bodyNodeModel, getSelf());
                bodyNode = getContext().actorOf(Props.create(BodyNode.class, bodyNodeModel), message.getNt_id());

                getSender().tell(createPVResultMsg(bodyNodeModel, message.getMsgId()), getSelf());

            }

            //注册actor，分页查询使用
            serviceActor.tell(bodyNode, getSender());
        } else {

            log.info("id为" + message.getId() + "的" + model.getName() + "主体节点已存在");

            message.setOperation(HTTPCommand.GET);
            String path = getSelf().path() + "/" + message.getNt_id();
            getContext().actorSelection(path).tell(message, getSender());
        }
    }

    private BodyNodeModel createModel(BodyNodeMsg message) {

        BodyNodeModel result = new BodyNodeModel();

        result.setBodySpace(message.getSpaceName());

        result.setId(message.getId());

        result.setNt_id(message.getNt_id());

        result.setCreateTime(message.getCreateTime());

//        result.setRelatedNtSet(message.getRelatedNtSet());

        return result;
    }

    private BodyNodeModel mergeModel(BodyNodeModel pvModel, BodyNodeModel neo4jModel) {

        BodyNodeModel newModel = new BodyNodeModel();

        newModel.setBodySpace(pvModel.getBodySpace());

        newModel.setNt_id(pvModel.getNt_id());

        newModel.setId(pvModel.getId());

        newModel.setCreateTime(neo4jModel.getCreateTime());

        newModel.setLastVisitTime(pvModel.getCreateTime());

        Set<String> newSet = new HashSet<>();
//
//        Set<String> pvSet = pvModel.getRelatedNtSet();
//
//        Set<String> neo4jSet = neo4jModel.getRelatedNtSet();
//
//        newSet.addAll(pvSet);
//        newSet.addAll(neo4jSet);
//
//        newModel.setRelatedNtSet(neo4jSet);

        return newModel;
    }

    private PVResultMsg createPVResultMsg(BodyNodeModel newModel, Set<String> newSet, String msgId) {

        PVResultMsg msg = new PVResultMsg();

        msg.setMsgId(msgId);

        if (newSet == null || newSet.isEmpty()) {

            msg.setData_status(PVDataStatus.NO_CHANGE);

            return msg;
        } else {

            msg.setData_status(PVDataStatus.RELATION_CHANGE);
        }

        Iterator<String> iterator = getSelf().path().getElements().iterator();

        iterator.next();
        String site_id = iterator.next();

        iterator.next();
        String spaceName = iterator.next();

        msg.setSite_id(site_id);

        msg.setSpaceName(spaceName);

        BodyNodeModel nodeModel = new BodyNodeModel();

        nodeModel.setBodySpace(spaceName);

        nodeModel.setNt_id(newModel.getNt_id());

        nodeModel.setId(newModel.getId());

//        nodeModel.setRelatedNtSet(newSet);

        msg.setBodyNode(nodeModel);

        return msg;

    }

    private PVResultMsg createPVResultMsg(BodyNodeModel newModel, String msgId) {

        PVResultMsg msg = new PVResultMsg();

        msg.setMsgId(msgId);

        msg.setData_status(PVDataStatus.NODE_ADD);

        Iterator<String> iterator = getSelf().path().getElements().iterator();

        iterator.next();
        String site_id = iterator.next();

        iterator.next();
        String spaceName = iterator.next();

        msg.setSite_id(site_id);

        msg.setSpaceName(spaceName);

        BodyNodeModel nodeModel = new BodyNodeModel();

        nodeModel.setBodySpace(spaceName);

        nodeModel.setNt_id(newModel.getNt_id());

        nodeModel.setId(newModel.getId());

//        nodeModel.setRelatedNtSet(newModel.getRelatedNtSet());

        msg.setBodyNode(nodeModel);

        return msg;
    }

    private Set<String> mergeSet(Set<String> pvSet, Set<String> neo4jSet) {

        Set<String> result = new HashSet<>();

        if (neo4jSet == null || neo4jSet.isEmpty()) {
            return result;

        } else {
            for (String nt_id : pvSet) {

                if (!neo4jSet.contains(nt_id)) {

                    result.add(nt_id);
                }
            }
        }
        return result;
    }

}
