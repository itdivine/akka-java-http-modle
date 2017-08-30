package cn.xiaoneng.nskyeye.access.controller;

import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.Unmarshaller;
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

    public Route route() {

        HttpRequest.GET("uri");

        return path("enterprises", () ->
                route(
                        get(() -> parameterMap(params -> {
                            String page = params.get("page");
                            String per_page = params.get("per_page");
                            //request.getUri();
                            return complete("get id=" + page); //getEVSList()
                        })),
                        post(() -> entity(Unmarshaller.entityToString(), data -> {
                            cn.xiaoneng.nskyeye.access.example.bean.EVS evs = JSON.parseObject(data, cn.xiaoneng.nskyeye.access.example.bean.EVS.class);
                            return complete("post " + evs.toString());
                        }))
                                .orElse(complete("Received something else"))
                )
        );
    }


}


//                get(() -> complete("enterprises get"))
//                        helloRoutes()