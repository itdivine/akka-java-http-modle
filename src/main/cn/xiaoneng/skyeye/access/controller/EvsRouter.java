package cn.xiaoneng.skyeye.access.controller;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import cn.xiaoneng.skyeye.access.remote.Message;
import cn.xiaoneng.skyeye.enterprise.actor.EVS;
import cn.xiaoneng.skyeye.enterprise.bean.EVSInfo;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

import static cn.xiaoneng.skyeye.access.Message.EVSProtocol.EVSListGet;

/**
 * Created by XuYang on 2017/9/20.
 *
 * 1.查询企业信息
 * 2.修改企业信息
 * 3.删除企业信息
 */
public class EvsRouter extends BaseRouter {

    protected final static Logger log = LoggerFactory.getLogger(EvsRouter.class);

    public Route route() {

        return
                extractUri(uri ->
                        path(PathMatchers.segment("enterprises").slash(PathMatchers.segment()), siteId ->
                                route(
                                        get(() -> {
                                            String actorPath = "/user" + uri.getPathString();
                                            log.debug("actorPath = " + actorPath);
                                            return complete(getEVS(actorPath, new EVS.Get(siteId)));
                                        }),

                                        post(() -> entity(Unmarshaller.entityToString(), data -> {
                                            EVSInfo evs = JSON.parseObject(data, EVSInfo.class);
                                            String actorPath = "/user" + uri.getPathString();
                                            return complete(createEVS(actorPath, new EVS.Create(evs)));
                                        }))

                                        .orElse(complete("请求资源不存在"))
                                ))
                );
    }

    /**
     * 查询企业信息
     *
     * @param uri
     * @param cmd
     * @return
     */
    private HttpResponse getEVS(String uri, EVS.Get cmd) {

        Message message = new Message(uri, cmd);
        Object object = messageDispatcher.publishMsg(message);
//        Object object = messageDispatcher.sendMsg(message);

        return response(object);
    }


    private HttpResponse createEVS(String uri, EVS.Create cmd) {

        Message message = new Message(uri, cmd);
        EVS.Result result = (EVS.Result)messageDispatcher.publishMsg(message);
//        EVS.Result result = (EVS.Result)messageDispatcher.sendMsg(message);

        return response(result.code, result.evsInfo==null ? null : JSON.toJSONString(result.evsInfo));
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