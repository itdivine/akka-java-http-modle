package cn.xiaoneng.skyeye.access.controller;

import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.scaladsl.model.StatusCodes;
import cn.xiaoneng.skyeye.access.remote.MessageDispatcher;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * Created by XY on 2017/8/30.
 */
public class BaseRouter extends AllDirectives {

    public MessageDispatcher messageDispatcher = MessageDispatcher.getInstance();


    public HttpResponse response(Object object) {

        if(object == null) {
            return badResponse();

        } else {
            JSONObject json = JSON.parseObject(object.toString());
            String body = json.getString("body");
            int code = json.getIntValue("code");
            return response(code, body);
        }
    }

    public HttpResponse response(int code, String message) {

        if(code == 200)
            return successResponse(code, message);
        else if(code == 201) {
            return HttpResponse.create().withStatus(StatusCodes.Created());
        } else {
            return badResponse(message);
        }
    }

    public HttpResponse successResponse(int code, String message) {
//        return HttpResponse.createEVS().withStatus(HttpCode.leetCode);
        return HttpResponse.create().withStatus(StatusCodes.custom(code, "", null)).withEntity(ContentTypes.APPLICATION_JSON, message);
    }

    public HttpResponse badResponse() {
        return badResponse("");
    }

    public HttpResponse badResponse(String reason) {
        String body = "{\"message\":\"" + reason + "\"}";
        return HttpResponse.create().withStatus(StatusCodes.BadRequest()).withEntity(ContentTypes.APPLICATION_JSON, body);
    }
}
