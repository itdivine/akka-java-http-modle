package cn.xiaoneng.nskyeye.access.controller;

import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import cn.xiaoneng.nskyeye.access.remote.Message;
import cn.xiaoneng.nskyeye.access.remote.MessageDispatcher;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by XuYang on 2017/8/27.
 * 1.查询企业列表
 * 2.创建企业
 */
public class EvsManagerRouter extends AllDirectives {

    protected final static Logger log = LoggerFactory.getLogger(EvsManagerRouter.class);


    public Route route(MessageDispatcher messageDispatcher) {

        return
                extractUri(uri ->
                        path("enterprises", () ->
                                route(
                                        get(() -> parameterMap(params -> {
                                            String page = params.get("page");
                                            String per_page = params.get("per_page");
                                            String actorPath = "/user" + uri.getPathString();
                                            log.info("actorPath = " + actorPath);
                                            Object obj = messageDispatcher.publishMsg(new Message(actorPath, page));
                                            return complete("response " + obj); //getEVSList()
                                        })),
                                        post(() -> entity(Unmarshaller.entityToString(), data -> {
                                            cn.xiaoneng.nskyeye.access.example.bean.EVS evs = JSON.parseObject(data, cn.xiaoneng.nskyeye.access.example.bean.EVS.class);
                                            return complete("post " + evs.toString());
                                        }))
                                                .orElse(complete("Received something else"))
                                ))
                );
    }


}


//                get(() -> complete("enterprises get"))
//                        helloRoutes()