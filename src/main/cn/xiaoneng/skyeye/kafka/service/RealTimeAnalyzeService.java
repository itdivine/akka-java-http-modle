package cn.xiaoneng.skyeye.kafka.service;

import akka.actor.AbstractActor;
import cn.xiaoneng.skyeye.App;
import cn.xiaoneng.skyeye.kafka.bean.ChatOrder;
import cn.xiaoneng.skyeye.monitor.Monitor;
import cn.xiaoneng.skyeye.monitor.MonitorCenter;
import cn.xiaoneng.skyeye.monitor.Node;
import cn.xiaoneng.skyeye.util.ActorNames;
import cn.xiaoneng.skyeye.util.FieldConstants;
import cn.xiaoneng.skyeye.util.PVMessage;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * Created by xuyang on 2016/11/3.
 */
public class RealTimeAnalyzeService extends AbstractActor {

    protected final static Logger log = LoggerFactory.getLogger(RealTimeAnalyzeService.class);

    private static Monitor monitor = MonitorCenter.getMonitor(Node.RealTimeAnalyzeService);

    //计算引擎需要的数据：PV和订单
    private static final String[] array = new String[]{ActorNames.Web, ActorNames.Event};

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .matchAny(this::onReceive)
                .build();
    }
    public void onReceive(Object message) {

        long start = System.currentTimeMillis();

        try {
            log.info("Receive message: " + message + " " + getSender());

            if(message instanceof PVMessage) {
                processPVMessage((PVMessage) message);
                monitor.newWriteTime("PVMessage", System.currentTimeMillis()-start, true);
            }
            else if(message instanceof ChatOrder) {
                sendChatOrder((ChatOrder)message);
            }
            else {
                log.debug("Error: " + message);
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        }
    }

    private void sendChatOrder(ChatOrder chatOrder) {
        String msg = JSON.toJSONString(chatOrder);
        App.ntKafkaProducer.send(msg);
    }

    protected void processPVMessage(PVMessage message) {

        try {
            JSONArray jsonArray = format(message);
            log.info("Send kafka msg: " + jsonArray.toJSONString());
            for(int i=0;i<jsonArray.size();i++) {
                App.ntKafkaProducer.send(jsonArray.get(i).toString());
            }
        } catch (Exception e) {
            log.warn("Exception :" + e.toString());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }
    }

    private JSONArray format(PVMessage message) {

        JSONArray navArray = null;
        JSONArray jsonArray = new JSONArray();

        try {

            Map<String, Object> otherInfo = message.getOtherInfo();
            Map<String, String> bodyNodeInfo = message.getBodyNodeInfo();
            Map navMap = message.getNavigationInfo();

            JSONObject event_propertie = new JSONObject();
            JSONArray event_properties = new JSONArray();
            event_properties.add(event_propertie);

            event_propertie.putAll(otherInfo);
            event_propertie.putAll(bodyNodeInfo);
//            event_propertie.putAll(navMap);

            Set<String> navNameSet = navMap.keySet();

            for(String navName:navNameSet) {

                navArray = (JSONArray)navMap.get(navName);

                if(navName.equals(ActorNames.Web) || navName.equals(ActorNames.Terminal)
                        || navName.equals(ActorNames.Order)|| navName.equals(ActorNames.IP)|| navName.equals(ActorNames.Source)) {
                    event_propertie.putAll(navArray.getJSONObject(0));
                }
            }

            for(String nav:array) {

                if (!navMap.containsKey(nav))
                    continue;

                JSONObject json = new JSONObject();
                jsonArray.add(json);

                //PV
                if (nav.equals(ActorNames.Web)){
                    json.put("event_name", "page_load");
                }

                //订单
                if (nav.equals(ActorNames.Event)) {
                    json.put("event_name", "order_submit");
                }

                json.put("event_distinctid", String.valueOf((int)(Math.random()*1000000000)));
                json.put("event_siteid", otherInfo.get(FieldConstants.SITEID));
                json.put("event_time", otherInfo.get(FieldConstants.TIME));
                json.put("event_properties", event_properties);
            }

        } catch (Exception e) {
            log.warn("Exception :" + e.toString());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }
        return jsonArray;
    }
}