package cn.xiaoneng.skyeye.track.service;

import akka.actor.*;
import akka.cluster.Cluster;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.*;
import akka.http.javadsl.model.headers.RawHeader;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import cn.xiaoneng.skyeye.bodyspace.model.BodyNodeModel;
import cn.xiaoneng.skyeye.track.message.GetUserTrackMessage;
import cn.xiaoneng.skyeye.track.message.KPIMessage;
import cn.xiaoneng.skyeye.util.COMMON;
import cn.xiaoneng.skyeye.util.HttpSender;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by Administrator on 2017/2/23.
 */
public class HttpHandler extends AbstractActor {

    protected final static Logger log = LoggerFactory.getLogger(HttpHandler.class);

//    private final String token = "";


    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .matchAny(this::onReceive)
                .build();
    }
    public void onReceive(Object message) {

        if(message instanceof GetUserTrackMessage) {
            handle((GetUserTrackMessage)message);
        } else if(message instanceof String) {
            handle(null);
        } else {
            unhandled(message);
        }
    }

    private void handle(GetUserTrackMessage message) {

        String nt_id = message.getBodyMap().get(BodyNodeModel.NT_ID);
        String url = getUrl(nt_id);
        if(url == null)
            getSender().tell(new KPIMessage(message.getMsgId()), getSelf());
        else {

            Map<String,String> headers = new HashMap<String,String>();

//            headers.put("token", token);
            String content = HttpSender.getInfos(url,headers);
            if(content != null) {
                JSONObject json = JSON.parseObject(content);
                Map<String,Object> map = new HashMap<>();
                map.put("page_load", json.getInteger("nskyeye:page_load:"+nt_id));
                map.put("chat_open", json.getInteger("nskyeye:chat_open:"+nt_id));
                map.put("order_create", json.getInteger("nskyeye:order_create:"+nt_id));
                map.put("staytime_avg", json.getLong("nskyeye:staytime_avg:"+nt_id));

                getSender().tell(new KPIMessage(message.getMsgId(), map), getSelf());

            } else {
                getSender().tell(new KPIMessage(message.getMsgId()), getSelf());
            }
        }
    }

    private String getUrl(String nt_id) {

        if(COMMON.kpi_url == null)
            return null;

        return COMMON.kpi_url + "nskyeye:page_load:"+nt_id+",nskyeye:staytime_avg:"+nt_id+",nskyeye:chat_open:"+nt_id+",nskyeye:order_create:"+nt_id+"";
    }


    private String getMessage(){
        String url = "http://kpi-dev.ntalker.com/api/counter/queries/nskyeye:page_load:54ad6f8c-e282-41f7-9956-ad42585769f6,nskyeye:staytime_avg:54ad6f8c-e282-41f7-9956-ad42585769f6,nskyeye:chat_open:54ad6f8c-e282-41f7-9956-ad42585769f6,nskyeye:order_create:54ad6f8c-e282-41f7-9956-ad42585769f6";
        String content = "";
//        Authorization authorization = Authorization.basic("token", token);
        Set<HttpHeader> headers = new HashSet<>();
//        headers.add(RawHeader.createEVS("token", token));

        final Materializer materializer = ActorMaterializer.create(getContext().system());
        Http.get(getContext().system())
                .singleRequest(HttpRequest.create().withUri(url).withMethod(HttpMethods.GET).addHeaders(headers), materializer)
                .whenComplete((response, t)->{
                    System.out.println("=========="+response.status().intValue());
                    System.out.println("=========="+response.entity().toString());
                    if(response.status().intValue() == 200) {
//                        content = getContent(response.entity().toString());
                    }

                });

        return content;
    }

    /*
     HttpEntity.Strict(application/json,{
      "nskyeye:page_load:54ad6f8c-e282-41f7-9956-ad42585769f6": 6,
      "nskyeye:staytime_avg:54ad6f8c-e282-41f7-9956-ad42585769f6": 550,
      "nskyeye:chat_open:54ad6f8c-e282-41f7-9956-ad42585769f6": 0,
      "nskyeye:order_create:54ad6f8c-e282-41f7-9956-ad42585769f6": 0
    })
 */
    private String getContent(String entity) {
        String en = entity.substring(entity.indexOf("("));
        String[] arr = en.split(",");
        return arr[1];
    }
}
