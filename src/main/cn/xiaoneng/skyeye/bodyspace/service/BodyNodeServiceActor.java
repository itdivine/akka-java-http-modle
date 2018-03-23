package cn.xiaoneng.skyeye.bodyspace.service;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import cn.xiaoneng.skyeye.bodyspace.actor.BodySpace;
import cn.xiaoneng.skyeye.bodyspace.message.BodyNodeMsg;
import cn.xiaoneng.skyeye.bodyspace.message.BodyNodesGatherContainer;
import cn.xiaoneng.skyeye.bodyspace.message.ListInfo;
import cn.xiaoneng.skyeye.util.Operation;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 主体节点列表查询聚合器
 * <p>
 * Created by liangyongheng on 2016/8/1 22:33.
 */
public class BodyNodeServiceActor extends AbstractActor {
    protected final Logger log = LoggerFactory.getLogger(getSelf().path().toStringWithoutAddress());
    private List<ActorRef> actorList = new ArrayList<ActorRef>();

    private Map<String, BodyNodesGatherContainer> fulfillmentMap = new HashMap<String, BodyNodesGatherContainer>();

    @Override
    public void preStart() throws Exception {
        super.preStart();
        log.info(getSelf().path().toStringWithoutAddress());
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .matchAny(this::onReceive)
                .build();
    }

    public void onReceive(Object message) {
        if (message instanceof ActorRef) {
            //注册actor
            actorList.add((ActorRef) message);

        } else if (message instanceof ListInfo) {

            //分页查询
            ListInfo nodeInfo = (ListInfo) message;

            List<ActorRef> receivers = getReceivers(nodeInfo.getPageSize(), nodeInfo.getPer_page());

            BodyNodesGatherContainer fillment = new BodyNodesGatherContainer(nodeInfo.getMsgid(), receivers.size(), getSender());
            fulfillmentMap.put(nodeInfo.getMsgid(), fillment);

            dispatherMsg(receivers, nodeInfo.getMsgid());

        } else if (message instanceof JSONObject) {
            //汇总结果
            JSONObject msg = (JSONObject) message;

            BodyNodesGatherContainer fillment = fulfillmentMap.get(msg.getString("msgId"));
            fillment.getInfo().add(msg);

            if (fillment.getInfo().size() >= fillment.getCount()) {

                String bodyJson = fillment.getInfo().toJSONString();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", 200);
                jsonObject.put("body", bodyJson);

                fillment.getSender().tell(jsonObject.toJSONString(), getSelf());
                fulfillmentMap.remove(msg.getString("msgId"));
            }
        }
    }

    private List<ActorRef> getReceivers(int page, int per_page) {
        List<ActorRef> result;

        if (page * per_page > actorList.size()) {
            result = new ArrayList<>(actorList);
        } else {
            result = actorList.subList(actorList.size() - page * per_page, actorList.size());
        }
        return result;
    }

    private void dispatherMsg(List<ActorRef> receivers, String msgId) {

        BodyNodeMsg message = new BodyNodeMsg();
        message.setOperation(Operation.GET);
        message.setMsgId(msgId);

        for (ActorRef receiver : receivers) {

            receiver.tell(message, getSelf());
        }
    }

}

