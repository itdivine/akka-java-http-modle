package cn.xiaoneng.skyeye.temple;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import cn.xiaoneng.skyeye.enterprise.message.IsRegistMessage;
import cn.xiaoneng.skyeye.util.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 查询所有子Actor列表
 *
 * 接收者列表
 * 聚合器
 *
 * Created by Administrator on 2016/8/1.
 */
public class ListProcessor extends AbstractActor {

    private ActorRef mediator;

    protected final Logger log = LoggerFactory.getLogger(getSelf().path().toStringWithoutAddress());

    // 接收者列表(按时间顺序： 近到远)
    private LinkedHashMap<String,IsRegistMessage> registors = new LinkedHashMap<String, IsRegistMessage>();

    /**
     * key：请求消息ID
     * value：聚合所有返回的消息
     */
    private Map<String, AggregationFulfillment> fulfillmentEVSMap = new HashMap<String, AggregationFulfillment>();

    @Override
    public void preStart() throws Exception {
        log.info("ListProcessor init " + getSelf().path());

        mediator = DistributedPubSub.get(this.getContext().system()).mediator();
        mediator.tell(new DistributedPubSubMediator.Subscribe(getSelf().path().toStringWithoutAddress(), ActorNames.NSkyEye, getSelf()), getSelf());

        log.info("ListProcessor init success, path = " + getSelf().path());


        super.preStart();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchAny(msg -> onReceive(msg))
                .build();
    }

    public void onReceive(Object message) {

        try {
            if(message instanceof String) {
                processHTTPCommand((String)message);
            }
            // 接收者注册
            else if(message instanceof IsRegistMessage) {
                IsRegistMessage registor = (IsRegistMessage)message;
                if(registor.isRegist())
                    registors.put(registor.getPath(),registor);
                else
                    registors.remove(registor.getPath());
            }

            // 请求列表信息
            else if(message instanceof ListMessage) {

                ListMessage listMessage = (ListMessage)message;

                // 1.获取当前页的接收者列表
                List<ActorRef> receiverList = getReceiverList(listMessage.getPage(), listMessage.getPer_page());

                if(receiverList == null || receiverList.size() == 0) {
                    getSender().tell("{\"code\":200,\"body\":\"\"}", getSelf());
                    return;
                }

                // 2.创建一条聚合器记录
                AggregationFulfillment evsFulfillment = new AggregationFulfillment(listMessage.getMsgId(), receiverList.size(), new ArrayList<BaseMessage>(), getSender());
                fulfillmentEVSMap.put(listMessage.getMsgId(), evsFulfillment);

                // 3.遍历发送消息
                CommandMessage getMessage = new CommandMessage(Operation.GET, 10, listMessage.getMsgId());
                dispatchTo(getMessage,receiverList);

            }

            // 接收返回的企业信息，并聚合
            else if(message instanceof DocumentMessage) {

                DocumentMessage docMsg = (DocumentMessage)message;

                AggregationFulfillment previousFulfillment = fulfillmentEVSMap.get(docMsg.getMsgId());
                previousFulfillment.messages.add(docMsg);

                if(previousFulfillment.messages.size() >= previousFulfillment.count) {

                    JSONArray array = previousFulfillment.getJsonEVSInfos();
                    JSONObject obj = new JSONObject();
                    obj.put("status",200);
                    obj.put("body",array);

                    previousFulfillment.callback.tell(obj.toString(), getSelf());
                    fulfillmentEVSMap.remove(docMsg.getMsgId());
                }
            }


        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        }
    }

    private void processHTTPCommand(String message) {

        try {
            JSONObject messageJson = JSON.parseObject(message);
            String method = messageJson.getString("method");
            JSONObject params = messageJson.getJSONObject("params");


            if (Statics.isNullOrEmpty(method)) {
                log.info("method is null, message= " + message);
                getSender().tell("{\"status\":415,\"body\":\"\"}", getSelf());
                return;
            }

            if(method.equals(HTTPCommand.GET)) {

                // 查询导航节点列表
                list(messageJson);
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage() + "  message= " + message);
        }
    }

    /**
     *  查询导航列表
     */
    private void list(JSONObject messageJson) {

        try {
            int page = 0;
            int per_page = 0;

            JSONObject bodyJson = messageJson.getJSONObject("body");

            if(bodyJson != null) {
                page = bodyJson.getInteger("page");
                per_page = bodyJson.getInteger("per_page");
            }

            if(page == 0) {
                page = 1;
            }

            if(per_page == 0) {
                per_page = 30;
            }

            ListMessage listMessage = new ListMessage(page, per_page, 10);


            // 1.获取当前页的接收者列表
            List<ActorRef> receiverList = getReceiverList(listMessage.getPage(), listMessage.getPer_page());

            if(receiverList == null || receiverList.size() == 0) {
                getSender().tell("{\"status\":200,\"body\":\"\"}", getSelf());
                return;
        }

            // 2.创建一条聚合器记录
            AggregationFulfillment evsFulfillment = new AggregationFulfillment(listMessage.getMsgId(), receiverList.size(), new ArrayList<BaseMessage>(), getSender());
            fulfillmentEVSMap.put(listMessage.getMsgId(), evsFulfillment);

            // 3.遍历发送消息
            CommandMessage getMessage = new CommandMessage(Operation.GET, 10, listMessage.getMsgId());
            dispatchTo(getMessage,receiverList);

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            // TODO HTTP 4xx
        }
    }

    private void dispatchTo(CommandMessage getMessage, List<ActorRef> evsList) {

        try {
            for(ActorRef evsRef:evsList) {
                evsRef.tell(getMessage, getSelf());
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        }

    }

    private List<ActorRef> getReceiverList(int page, int per_page) {

        List<ActorRef> list = new ArrayList<ActorRef>();

        try {
            Collection<IsRegistMessage> collection = registors.values();

            Object[] values = collection.toArray();

            for(int i=(page-1)*per_page;i<registors.size();i++) {
                list.add(((IsRegistMessage)values[i]).getEvsRef());

                if(list.size() >= per_page)
                    break;
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        }

        return list;
    }


    /**
     * 消息聚合
     */
    class AggregationFulfillment {

        // 请求和聚合的消息具有相同的唯一标识
        private String msgId;

        // 聚合的消息数量
        private int count;

        // 聚合的消息
        private List<BaseMessage> messages;

        // 消息返回给谁
        private ActorRef callback;

        /**
         * @param baseMessages 必须重写toJson()方法
         */
        public AggregationFulfillment(String msgId, int count, List<BaseMessage> baseMessages, ActorRef callback) {
            this.msgId = msgId;
            this.callback = callback;
            this.count = count;
            this.messages = baseMessages;
        }

        public JSONArray getJsonEVSInfos() {

            JSONArray array = new JSONArray();

            if(messages == null)
                return array;

            try {
                for (BaseMessage message: messages) {
                    array.add(message.toJson());
                }

            } catch (Exception e) {
                log.error("Exception " + e.getMessage());
            }

            return array;
        }
    }
}
