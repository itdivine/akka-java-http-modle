package cn.xiaoneng.skyeye.navigation.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import cn.xiaoneng.skyeye.bodyspace.message.PVDataStatus;
import cn.xiaoneng.skyeye.db.Neo4jDataAccess;
import cn.xiaoneng.skyeye.monitor.Monitor;
import cn.xiaoneng.skyeye.monitor.MonitorCenter;
import cn.xiaoneng.skyeye.monitor.Node;
import cn.xiaoneng.skyeye.navigation.bean.NavNodeInfo;
import cn.xiaoneng.skyeye.navigation.message.BackNavNodeMsg;
import cn.xiaoneng.skyeye.navigation.message.CreateNavNodeMsg;
import cn.xiaoneng.skyeye.navigation.message.GetNavNodeInfo;
import cn.xiaoneng.skyeye.navigation.message.ReturnNavNodeInfo;
import cn.xiaoneng.skyeye.util.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Created by xuyang on 2016/8/3.
 */
public class NavigationNode extends AbstractActor {

    protected final Logger log = LoggerFactory.getLogger(getSelf().path().toStringWithoutAddress());
    private static Monitor monitor = MonitorCenter.getMonitor(Node.NavigationNode);

    private ActorRef mediator;
    private NavNodeInfo _navNodeInfo;
    private String _navSpaceName;

    public NavigationNode(String navSpaceName) {
        _navSpaceName = navSpaceName;
    }

    public void set_navNodeInfo(NavNodeInfo _navNodeInfo) {
        this._navNodeInfo = _navNodeInfo;
    }

    public NavNodeInfo get_navNodeInfo() {
        return _navNodeInfo;
    }

    @Override
    public void preStart() throws Exception {
        log.info("NavigationNode init " + getSelf().path().toStringWithoutAddress());

        mediator = DistributedPubSub.get(this.getContext().system()).mediator();
        mediator.tell(new DistributedPubSubMediator.Subscribe(getSelf().path().toStringWithoutAddress(), ActorNames.NSkyEye, getSelf()), getSelf());

        log.info("NavigationNode init success, path = " + getSelf().path());
        super.preStart();

        ActorRef ref = getContext().parent();
        log.debug(ref.toString());
        log.debug(ref.path()+"");
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
            log.debug("Receive message: " + message);

            if(message instanceof String) {

                processHTTPCommand((String)message);

            } else if(message instanceof CommandMessage) {

                processNavCommand((CommandMessage) message);

            } else if(message instanceof GetNavNodeInfo) {

                getSender().tell(new ReturnNavNodeInfo(((GetNavNodeInfo) message).getMsgId(), (NavNodeInfo)SerializationUtils.clone(_navNodeInfo)), getSelf());
                monitor.newWriteTime("GetNavNodeInfo", System.currentTimeMillis()-start, true);

            } else if(message instanceof CreateNavNodeMsg) {

                createNavNode((CreateNavNodeMsg)message);
                monitor.newWriteTime("CreateNavNodeMsg", System.currentTimeMillis()-start, true);

            }


        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        }
    }

    private void createNavNode(CreateNavNodeMsg message) {

        ActorRef actorRef = null;
        int data_status = PVDataStatus.NO_CHANGE;

        try {

            NavNodeInfo httpNodeInfo = message.getNavNodeInfo();

            if(_navNodeInfo == null) {

                // DB中查找导航节点
                NavNodeInfo dbNodeInfo = getNodeFromDB(httpNodeInfo.getId(), httpNodeInfo.getSpaceName());

                // httpNodeInfo有值，内存是空，DB是空，则创建内存和db
                if(dbNodeInfo == null) {
                    _navNodeInfo = httpNodeInfo;
                    data_status = PVDataStatus.NODE_ADD;
                } else {
                    // httpNodeInfo有值，内存是空，DB有值，则比较http和db节点的信息
                    if(dbNodeInfo.compare(httpNodeInfo)) {
                        // 一样则不需要更新db
                        _navNodeInfo = dbNodeInfo;
                    }
                    else {
                        // 不一样，则更新内存、更新db
                        _navNodeInfo = httpNodeInfo;
                        data_status = PVDataStatus.NODE_ADD;
                    }
                }

            } else {

                // 比较http和内存信息
                if(!_navNodeInfo.compare(httpNodeInfo)) {
                    // 不一样，则更新内存、更新db
                    _navNodeInfo = httpNodeInfo;
                    data_status = PVDataStatus.NODE_ADD;
                }
            }

            // Write node to Neo4j
            if(data_status== PVDataStatus.NODE_ADD) {
                Neo4jDataAccess.setNavigationNode(_navNodeInfo.getSpaceName(), _navNodeInfo.toNeo4jMap());
            }

            getSender().tell(new BackNavNodeMsg(message.getMsgId(), _navNodeInfo, data_status, actorRef, 10), getSelf());

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
            getSender().tell(new BackNavNodeMsg(message.getMsgId(), _navNodeInfo, data_status, actorRef, 10), getSelf());
        }
    }

    private NavNodeInfo getNodeFromDB(String id, String spaceName) {
        HashMap<String,Object> map = new HashMap();
        map.put("id", id);
        return Neo4jDataAccess.getNavigationNode(":Navigation:" + spaceName, map);
    }

    private void processHTTPCommand(String message) {

        long start = System.currentTimeMillis();

        try {
            JSONObject messageJson = JSON.parseObject(message);

            String method = messageJson.getString("method");

            if (Statics.isNullOrEmpty(method)) {
                log.info("method is null, message= " + message);
                getSender().tell("{\"status\":415,\"body\":\"\"}", getSelf());
                return;
            }

            switch (method) {

                // 获取导航节点信息
                case Operation.GET:
                    getSender().tell("{\"status\":200,\"body\":" + _navNodeInfo.toJSONString() + "}", getSelf());
                    monitor.newWriteTime("HTTP.GET", System.currentTimeMillis()-start, true);
                    break;

                // 删除节点
                case Operation.DELETE:
                    getContext().parent().tell(new CommandMessage(Operation.DELETE, 10, null, getSender()), getSelf());
                    monitor.newWriteTime("HTTP.DELETE", System.currentTimeMillis()-start, true);
                    break;

                // 更新节点
                case Operation.UPDATE:
                    JSONObject bodyJson = messageJson.getJSONObject("body");
                    NavNodeInfo info = JSON.parseObject(bodyJson.toString(), NavNodeInfo.class);
                    _navNodeInfo.update(info);
                    getSender().tell("{\"status\":200,\"body\":" + _navNodeInfo.toJSONString() + "}", getSelf());
                    monitor.newWriteTime("HTTP.UPDATE", System.currentTimeMillis()-start, true);
                    break;


                default:
                    getSender().tell("{\"status\":400,\"body\":\"\"}", getSelf());
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage() + "  message= " + message);
        }
    }

    private void processNavCommand(CommandMessage message) {

        long start = System.currentTimeMillis();

        try {
            String operation = message.getOperation();
            if(Statics.isNullOrEmpty(operation)) {
                message.getCallback().tell("{\"status\":415}", getSelf());
                return;
            }

            switch (operation) {

                // 验证创建
                case Operation.CREATE:
                    message.getCallback().tell("{\"status\":200,\"body\":" + _navNodeInfo.toJSONString() + "}", getSelf());
                    monitor.newWriteTime("CREATE", System.currentTimeMillis()-start, true);
                    break;

                // 列表聚合查询信息
                case Operation.GET:
                    DocumentMessage documentMessage = new DocumentMessage(null, 10, _navNodeInfo.toJSONString(), ((CommandMessage)message).getMsgId());
                    getSender().tell(documentMessage, getSelf());
                    monitor.newWriteTime("GET", System.currentTimeMillis()-start, true);
                    break;

                default:
                    log.info("Invalid Message Operation: " + operation);
                    message.getCallback().tell("{\"status\":415}", getSelf());
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            message.getCallback().tell("{\"status\":400}", getSelf());
        }
    }
}
