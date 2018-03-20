package cn.xiaoneng.skyeye.bodyspace.service;

import akka.actor.AbstractActor;
import akka.actor.ReceiveTimeout;
import cn.xiaoneng.skyeye.bodyspace.message.BodyNodeCreateMsg;
import cn.xiaoneng.skyeye.bodyspace.message.BodyNodeFulFillment;
import cn.xiaoneng.skyeye.bodyspace.message.NTRelatedMsg;
import cn.xiaoneng.skyeye.bodyspace.message.PVResultMsg;
import cn.xiaoneng.skyeye.monitor.Monitor;
import cn.xiaoneng.skyeye.monitor.MonitorCenter;
import cn.xiaoneng.skyeye.monitor.Node;
import cn.xiaoneng.skyeye.util.PVMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liangyongheng on 2016/8/3 17:00.
 */
public class PVMsgExcutor extends AbstractActor {

    protected final static Logger log = LoggerFactory.getLogger(PVMsgExcutor.class);
    private static Monitor monitor = MonitorCenter.getMonitor(Node.PVMsgExcutor);


    Map<String, BodyNodeFulFillment> fillmentMap = new HashMap<>();

//    String nt_id = null;
//    String cookieId = null;
//    String loginId = null;


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

        try {

            if (message instanceof PVMessage) {

                send2NT((PVMessage) message);

            } else if (message instanceof PVResultMsg) {

                long start = System.currentTimeMillis();

                PVResultMsg msg = (PVResultMsg) message;

                if ("nt".equals(msg.getSpaceName())) {

                    dispatcherMsg(msg.getMsgId());
                    this.fillmentMap.get(msg.getMsgId()).setNt_id(msg.getBodyNode().getId());

                } else if ("cookie".equals(msg.getSpaceName())) {
                    this.fillmentMap.get(msg.getMsgId()).setCookieId(msg.getBodyNode().getId());

                } else if ("login".equals(msg.getSpaceName())) {
                    this.fillmentMap.get(msg.getMsgId()).setLoginId(msg.getBodyNode().getId());

                } else if ("imei".equals(msg.getSpaceName())) {
                    this.fillmentMap.get(msg.getMsgId()).setImei(msg.getBodyNode().getId());

                } else if ("dvid".equals(msg.getSpaceName())) {
                    this.fillmentMap.get(msg.getMsgId()).setDvid(msg.getBodyNode().getId());
                }

                gatherMsg(msg);

                monitor.newWriteTime("gatherMsg", System.currentTimeMillis() - start, true);

            } else if (message instanceof ReceiveTimeout) {

                //如果超时则将缓存的消息全部发送，并清空缓存
                if (!fillmentMap.isEmpty()) {
                    for (Map.Entry<String, BodyNodeFulFillment> entry : fillmentMap.entrySet()) {

                        BodyNodeFulFillment fillment = entry.getValue();

                        fillment.getSender().tell(fillment.getInfo(), getSelf());
                    }
                    fillmentMap.clear();
                }
            }
        } catch (Exception e) {

            log.error(e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * 消息派发，先通知NT主体空间创建节点，返回成功后再通知其他主体空间创建节点
     *
     * @param message
     */
    private void send2NT(PVMessage message) {

        long start = System.currentTimeMillis();

        try {
            String msgId = message.getMsgId();
            Map<String, String> contentMap = message.getBodyNodeInfo();

            String nt_id = contentMap.get("nt");
            String loginId = contentMap.get("login");
            int dispatcherCount = contentMap.size();

            BodyNodeFulFillment fillment = new BodyNodeFulFillment(msgId, getSender(), message, dispatcherCount);
            fillmentMap.put(msgId, fillment);

            BodyNodeCreateMsg createMsg = new BodyNodeCreateMsg(nt_id, null, loginId, contentMap, "nt", msgId, message.getCreateTime());
            getContext().actorSelection("../../nt").tell(createMsg, getSelf());

        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();

        } finally {
            monitor.newWriteTime("send2NT", System.currentTimeMillis() - start, true);
        }
    }

    /**
     * 消息派发，通知其他     主体空间创建节点
     *
     * @param msgId
     */
    private void dispatcherMsg(String msgId) {

        try {
            BodyNodeFulFillment bodyNodeFulFillment = fillmentMap.get(msgId);
            PVMessage message = bodyNodeFulFillment.getMessage();
            Map<String, String> contentMap = message.getBodyNodeInfo();

            for (Map.Entry<String, String> entry : contentMap.entrySet()) {

                BodyNodeCreateMsg createMsg;
                String nt_id = contentMap.get("nt");
                String loginId = contentMap.get("login");

                if (entry.getKey().equals("nt")) {
                    continue;
                } else {
                    createMsg = new BodyNodeCreateMsg(entry.getValue(), nt_id, entry.getKey(), msgId, message.getCreateTime());
                }

                getContext().actorSelection("../../" + entry.getKey()).tell(createMsg, getSelf());
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 聚集消息，并返回
     *
     * @param message
     */
    private void gatherMsg(PVResultMsg message) {

        String msgId = message.getMsgId();

//        System.out.println("-----------------"+message.getBodyNode().toString());

        BodyNodeFulFillment fillment = fillmentMap.get(msgId);

        fillment.getInfo().add(message);

        if (fillment.getCount() <= fillment.getInfo().size()) {

            String nt_id = fillment.getNt_id();
            String cookieId = fillment.getCookieId();
            String loginId = fillment.getLoginId();

            fillment.getSender().tell(fillment.getInfo(), getSelf());
            fillmentMap.remove(msgId);

            if (nt_id != null && cookieId != null) {

                getContext().actorSelection("../../cookie/" + cookieId).tell(new NTRelatedMsg(nt_id, loginId), getSelf());
            }
        }
    }

//    private Set<String> createNtSet(String selfKey, Map<String, Map<String, String>> bodeMap) {
//
//        Set<String> result = new HashSet<>();
//        for (Map.Entry<String, Map<String, String>> entry : bodeMap.entrySet()) {
//            if (!selfKey.equals(entry.getKey())) {
//
//                result.add(entry.getValue().get("nt_id"));
//            }
//        }
//        return result;
//    }

}
