package cn.xiaoneng.skyeye.access.controller;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import cn.xiaoneng.skyeye.access.example.bean.EVS;
import cn.xiaoneng.skyeye.access.remote.Message;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.xiaoneng.skyeye.access.Message.EVSProtocol.*;

/**
 * Created by XuYang on 2017/8/27.
 * 1.查询企业列表
 * 2.创建企业
 */
public class EvsManagerRouter extends BaseRouter {

    protected final static Logger log = LoggerFactory.getLogger(EvsManagerRouter.class);

    public Route route = path("enterprises", () ->

            extractRequest(request ->

                    route(
                            get(() -> parameterMap(params -> {

                                int page = Integer.parseInt(params.getOrDefault("page", "0"));
                                int per_page = Integer.parseInt(params.getOrDefault("per_page", "5"));
                                String uri = request.getUri().getPathString();
                                return complete(getEVSList(uri, new EVSListGet(page, per_page)));

                            })),

                            post(() -> entity(Unmarshaller.entityToString(), data -> {
                                EVS evs = JSON.parseObject(data, EVS.class);
                                return complete("post " + evs.toString());
                            }))

                            .orElse(complete("Unsupported operations"))
                    )
            )
    );

    /**
     * 分页查询企业列表
     *
     * @param uri
     * @param msg
     * @return
     */
    private HttpResponse getEVSList(String uri, EVSListGet msg) {

        Message message = new Message(uri, msg);
//        Object object = messageDispatcher.publishMsg(message);
        Object object = messageDispatcher.sendMsg(message);

        if(object == null) {
            return badResponse();

        } else {
            JSONObject json = JSON.parseObject(object.toString());
            String body = json.getString("body");
            int code = json.getIntValue("code");
            return successResponse(code, body);
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