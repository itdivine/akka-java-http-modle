package cn.xiaoneng.skyeye.access.controller;

import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.scaladsl.model.StatusCodes;
import cn.xiaoneng.skyeye.access.remote.MessageDispatcher;

/**
 * Created by XY on 2017/8/30.
 */
public class BaseRouter extends AllDirectives {

    public MessageDispatcher messageDispatcher = MessageDispatcher.getInstance();

    public HttpResponse badResponse(String reason) {
        String body = "{\"message\":\"" + reason + "\"}";
        return HttpResponse.create().withStatus(StatusCodes.BadRequest()).withEntity(ContentTypes.APPLICATION_JSON, body);
    }

    public HttpResponse badResponse() {
        return badResponse("");
    }

    public HttpResponse successResponse(int code, String body) {

        return HttpResponse.create().withStatus(StatusCodes.custom(code, "", null)).withEntity(ContentTypes.APPLICATION_JSON, body);
    }


//    public Route route = null;

//    public Route getRoute(){
//        return null;
//    }


}
