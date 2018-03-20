package cn.xiaoneng.skyeye.bodyspace.service;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ReceiveTimeout;
import cn.xiaoneng.skyeye.bodyspace.message.BodyNodeMsg;
import cn.xiaoneng.skyeye.bodyspace.message.BodyNodeMsgMap;
import cn.xiaoneng.skyeye.bodyspace.message.CreateNodeFromDB;
import cn.xiaoneng.skyeye.bodyspace.message.NTCommand;
import cn.xiaoneng.skyeye.bodyspace.model.BodyNodeModel;
import cn.xiaoneng.skyeye.monitor.Monitor;
import cn.xiaoneng.skyeye.monitor.MonitorCenter;
import cn.xiaoneng.skyeye.monitor.Node;
import cn.xiaoneng.skyeye.track.message.GetUserTrackMessage;
import cn.xiaoneng.skyeye.util.ActorNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.util.Map;
import java.util.Set;

/**
 * Created by liangyongheng on 2016/8/10 20:57.
 */
public class NTMessageRouter extends AbstractActor {

    private ActorRef sender;

    private int dispatherCount = 0;

    private BodyNodeMsgMap resultMsg = new BodyNodeMsgMap();

    private String msgId = null;

    protected final static Logger log = LoggerFactory.getLogger(NTMessageRouter.class);

    private static Monitor monitor = MonitorCenter.getMonitor(Node.NTMessageRouter);

    @Override
    public void preStart() throws Exception {

//        getContext().setReceiveTimeout(Duration.createEVS(10, TimeUnit.SECONDS));
        super.preStart();
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .matchAny(this::onReceive)
                .build();
    }
    public void onReceive(Object message) {

        long start = System.currentTimeMillis();

        if (message instanceof GetUserTrackMessage) {

            log.debug("receive GetUserTrackMessage " + (GetUserTrackMessage)message);

            Map<String, String> idMap = ((GetUserTrackMessage) message).getBodyMap();
//
//            sender = getSender();
//            msgId = ((GetUserTrackMessage) message).getMsgId();
//
//            String key = null;
//            String value = null;
//            for (Map.Entry<String, String> entry : idMap.entrySet()) {
//
//                key = entry.getKey();
//                value = entry.getValue();
//            }
//            if (key.equals("nt")) {
//
//                String path = "../*/" + value;
//                NTCommand command = new NTCommand(value, false);
//
//                getContext().actorSelection(path).tell(command, getSelf());
//
////                Patterns.ask()
//            } else {
//                String path = "../" + key + "*";
//
//                IdCommand command = new IdCommand(value,false);
//
//                getContext().actorSelection(path).tell(command,getSelf());
//
//            }
            String nt_id = idMap.get(BodyNodeModel.NT_ID);

            // TODO: 2016/10/27 根据nt查询数据库，获取账号信息

            String path = "../" + ActorNames.NT_BODYSPACE + "/" + nt_id;


            // 内存中查找导航节点
            Option<ActorRef> navOption = getContext().child(path);
            if(navOption.isEmpty()) {
                getContext().actorSelection("../nt").tell(new CreateNodeFromDB(nt_id, (GetUserTrackMessage) message), getSender());
                log.debug("tell CreateNodeFromDB " + message);
            } else {
                navOption.get().tell(message, getSender());
                log.debug("tell GetUserTrackMessage " + message);
            }



//            NTMessage ntMessage = new NTMessage(nt_id, null);
//            BodyNodeMsgMap result = new BodyNodeMsgMap();

//            result.setMsgId(((GetUserTrackMessage) message).getMsgId());

//            ((GetUserTrackMessage) message).getCallback().tell(result, getSelf());

            monitor.newWriteTime("GetUserTrackMessage", System.currentTimeMillis()-start, true);

        } else if (message instanceof BodyNodeMsg) {

            log.debug("receive BodyNodeMsg " + (BodyNodeMsg)message);

            BodyNodeMsg msg = (BodyNodeMsg) message;

            resultMsg.getNodeMap().put(msg.getSpaceName(), msg);

            if (!msg.isRelatedNode()) {

                Set<String> ntSet = msg.getRelatedNtSet();

                dispatherCount = ntSet.size();

                dispatcherMsg(ntSet);

            } else {

                if (dispatherCount == resultMsg.getNodeMap().size() - 1) {
                    resultMsg.setMsgId(msgId);

                    sender.tell(resultMsg, getSelf());

                    resultMsg.getNodeMap().clear();

                    sender = null;

                } else {
                    resultMsg.getNodeMap().put(msg.getSpaceName(), msg);

                }
            }

            monitor.newWriteTime("BodyNodeMsg", System.currentTimeMillis()-start, true);

        } else if (message instanceof ReceiveTimeout) {

            log.debug("receive ReceiveTimeout ");

            if (sender != null) {
                resultMsg.setMsgId(msgId);

                sender.tell(resultMsg, getSelf());

                sender = null;
            }

            monitor.newWriteTime("ReceiveTimeout", System.currentTimeMillis()-start, true);
        }

    }

    private void dispatcherMsg(Set<String> ntSet) {

        for (String nt_id : ntSet) {

            NTCommand command = new NTCommand(nt_id, true);
            String path = "../*/" + nt_id;
            getContext().actorSelection(path).tell(command, getSelf());
        }

    }
}
