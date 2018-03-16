package cn.xiaoneng.skyeye.access.controller;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import cn.xiaoneng.skyeye.access.remote.Message;
import cn.xiaoneng.skyeye.enterprise.actor.EVS;
import cn.xiaoneng.skyeye.enterprise.bean.EVSInfo;
import cn.xiaoneng.skyeye.util.ActorNames;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static cn.xiaoneng.skyeye.enterprise.message.EVSProtocal.*;

/**
 * Created by XuYang on 2017/9/20.
 *
 * 1.查询企业信息
 * 2.修改企业信息
 * 3.删除企业信息
 */
public class EvsController extends BaseController {

    protected final static Logger log = LoggerFactory.getLogger(EvsController.class);

    private final String path = "/system/sharding/EVS/";

    public Route route() {

        return
                extractUri(uri ->
                        path(PathMatchers.segment("enterprises").slash(PathMatchers.segment()), siteId ->
                                route(
                                        get(() -> {
                                            //String actorPath = path + siteId + "/" + siteId;
                                            //log.debug("actorPath = " + siteId);
                                            return complete(getEVS(EvsManagerController.enterprisesProxyPath, new Get(siteId)));
                                        }),

                                        put(() -> entity(Unmarshaller.entityToString(), data -> {
                                            EVSInfo evs = JSON.parseObject(data, EVSInfo.class);
                                            return complete(updateEVS(siteId, new Update(evs)));
                                        })),

                                        delete(() -> {
                                            return complete(deleteEVS(siteId, new Delete(siteId)));
//                                            return complete(deleteEVS(EvsManagerController.enterprisesProxyPath, new EVS.Delete(siteId)));
                                        })

                                        .orElse(complete("请求资源不存在"))
                                ))
                );
    }


    /**
     * 使用ShardRegion发送给Actor
     */
    private HttpResponse getEVS(String uri, Get cmd) {
        Message message = new Message(uri, cmd);
        Object obj = messageDispatcher.sendShardMsg(message, ActorNames.EVS);
        if(obj != null) {
            Result result = (Result)obj;
            return response(result.code, result.evsInfo==null ? null : JSON.toJSONString(result.evsInfo));
        } else {
            return badResponse();
        }
    }

    private HttpResponse updateEVS(String uri, Update cmd) {
        Message message = new Message(uri, cmd);
        Object obj = messageDispatcher.sendShardMsg(message, ActorNames.EVS);
        if(obj != null) {
            Result result = (Result)obj;
            return response(result.code, result.evsInfo==null ? null : JSON.toJSONString(result.evsInfo));
        } else {
            return badResponse();
        }
    }

    private HttpResponse deleteEVS(String uri, Delete cmd) {

        Object obj = null;
        Message message = new Message(uri, cmd);

        try {
            obj = messageDispatcher.sendShardMsg(message, ActorNames.EVS);
        } catch (Exception e) {
            log.error("Exception: " + e.getMessage());
        }

        if(obj != null) {
            Result result = (Result)obj;
            return response(result.code, result.evsInfo==null ? null : JSON.toJSONString(result.evsInfo));
        } else {
            return badResponse();
        }
    }
}


//                get(() -> complete("enterprises get"))
//                        helloRoutes()

//    public Route getRoute() {
//
//        return path("enterprises", () ->
//
//                extractRequest(request ->
//
//                        route(
//
//                                get(() -> parameterMap(params -> {
//                                    int page = Integer.parseInt(params.getOrDefault("page", "0"));
//                                    int per_page = Integer.parseInt(params.getOrDefault("per_page", "5"));
//                                    String uri = request.getUri().getPathString();
//                                    return complete(getEVSList(uri, new EVSListGet(page, per_page)));
//                                })),
//
//                                post(() -> entity(Unmarshaller.entityToString(), data -> {
//                                    cn.xiaoneng.skyeye.access.example.bean.EVS evs = JSON.parseObject(data, cn.xiaoneng.skyeye.access.example.bean.EVS.class);
//                                    return complete("post " + evs.toString());
//                                }))
//                                        .orElse(complete("Received something else"))
//                        )
//                )
//        );
//    }