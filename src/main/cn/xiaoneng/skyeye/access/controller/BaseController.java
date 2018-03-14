package cn.xiaoneng.skyeye.access.controller;

import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.model.StatusCode;
import akka.http.javadsl.model.StatusCodes;
import cn.xiaoneng.skyeye.access.code.CustomStateCode;
import cn.xiaoneng.skyeye.access.remote.MessageDispatcher;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by XY on 2017/8/30.
 */
public class BaseController extends AllDirectives {

    public MessageDispatcher messageDispatcher = MessageDispatcher.getInstance();

    public HttpResponse response(StatusCode code, String message) {

        if(message == null)
            return customResponse(code);
        else
            return successResponse(code, message);
    }

    public HttpResponse badResponse() {
        return HttpResponse.create().withStatus(StatusCodes.BAD_REQUEST);
    }

    /**
     * 响应实体消息、请求资源
     * @param code  http原生状态码
     * @param message 请求资源
     * @return
     */
        private HttpResponse successResponse(StatusCode code, String message) {
        return HttpResponse.create().withStatus(StatusCodes.get(code.intValue())).withEntity(ContentTypes.APPLICATION_JSON, message);
    }

    /**
     * 响应自定义状态码
     * @param code 自定义状态码
     * @return
     */
    private HttpResponse customResponse(StatusCode code) {
        return HttpResponse.create().withEntity(ContentTypes.APPLICATION_JSON, getCodeEntry(code));
    }

    private String getCodeEntry(StatusCode code) {
        Map<String, Object> map = new HashMap<>();
        map.put("errcode", code.intValue());
        map.put("errmsg", code.defaultMessage());
        return JSON.toJSONString(map);  //{"errcode":46001,"errmsg":"evs is not exsit"}
    }
}