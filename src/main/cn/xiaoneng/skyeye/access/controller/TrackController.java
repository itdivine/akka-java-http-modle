package cn.xiaoneng.skyeye.access.controller;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import static cn.xiaoneng.skyeye.access.Message.TrackProtocal.*;
import cn.xiaoneng.skyeye.access.remote.Message;
import cn.xiaoneng.skyeye.collector.model.CollectorModel;
import cn.xiaoneng.skyeye.util.ActorNames;
import cn.xiaoneng.skyeye.util.BaseMessage;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by XuYang on 2018/3/19.
 *
 * 1.查询跟踪器信息
 * 2.上报访客轨迹
 */
public class TrackController extends BaseController {

    protected final static Logger log = LoggerFactory.getLogger(TrackController.class.getName());
    private final String per_page_count = "1"; //每页默认个数

    public Route route() {

        return
                extractUri(uri ->
                        path(PathMatchers.segment("enterprises").slash(PathMatchers.segment()).slash(ActorNames.TrackerManager).slash(ActorNames.NT_BODYSPACE), siteId ->
                                route(
                                        get(() -> parameterMap(params -> {
                                            int page = Integer.parseInt(params.getOrDefault("page", "0"));
                                            int per_page = Integer.parseInt(params.getOrDefault("per_page", per_page_count));
                                            String nt_id = String.valueOf(params.getOrDefault("nt_id", null));
                                            String nav = String.valueOf(params.getOrDefault("nav", "all"));
                                            String start_page = String.valueOf(params.getOrDefault("start_page", null));
                                            String topic = siteId + ActorNames.SLASH + ActorNames.TrackerManager + ActorNames.SLASH + ActorNames.NT_BODYSPACE;
                                            log.info("topic = " + topic);
                                            return complete(get(topic, new Get(siteId, nt_id, nav, start_page, page, per_page)));
                                        }))
//kf_1001/tracks/nt
//                                        put(() -> entity(Unmarshaller.entityToString(), data -> {
//                                            String topic = siteId + ActorNames.SLASH + ActorNames.COLLECTOR;
//                                            CollectorModel model = JSON.parseObject(data, CollectorModel.class);
//                                            return complete(update(topic, new Update(model)));
//                                        }))

                                                .orElse(complete("请求资源不存在"))
                                )
                        )
                );
    }

    private HttpResponse get(String uri, BaseMessage cmd) {
        Message message = new Message(uri, cmd);
        Object obj = messageDispatcher.publishMsg(message);
        if(obj != null) {
            Result result = (Result)obj;
            return response(result.code, result.info==null ? null : (result.info));
        } else {
            return badResponse();
        }
    }

    /*private HttpResponse update(String uri, BaseMessage cmd) {
        Message message = new Message(uri, cmd);
        Object obj = messageDispatcher.publishMsg(message);
        if(obj != null) {
            Result result = (Result)obj;
            return response(result.code, result.info==null ? null : JSON.toJSONString(result.info));
        } else {
            return badResponse();
        }
    }*/
}
