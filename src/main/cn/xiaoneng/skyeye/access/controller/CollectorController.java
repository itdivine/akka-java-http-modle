package cn.xiaoneng.skyeye.access.controller;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import static cn.xiaoneng.skyeye.access.Message.CollectorProtocal.*;
import cn.xiaoneng.skyeye.access.remote.Message;
import cn.xiaoneng.skyeye.collector.model.CollectorModel;
import cn.xiaoneng.skyeye.enterprise.bean.EVSInfo;
import cn.xiaoneng.skyeye.util.ActorNames;
import cn.xiaoneng.skyeye.util.base.BaseMessage;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by XuYang on 2018/3/19.
 *
 * 1.查询信息
 * 2.修改信息
 */
public class CollectorController extends BaseController {

    protected final static Logger log = LoggerFactory.getLogger(CollectorController.class.getName());

    public Route route() {

        return
                extractUri(uri ->
                        path(PathMatchers.segment("enterprises").slash(PathMatchers.segment()).slash(ActorNames.COLLECTOR), siteId ->
                                route(
                                        get(() -> {
                                            String topic = siteId + PathMatchers.slash() + ActorNames.COLLECTOR;
                                            log.debug("topic = " + topic);
                                            return complete(get(topic, new Get(siteId)));
                                        }),

                                        put(() -> entity(Unmarshaller.entityToString(), data -> {
                                            String topic = siteId + PathMatchers.slash() + ActorNames.COLLECTOR;
                                            CollectorModel model = JSON.parseObject(data, CollectorModel.class);
                                            return complete(update(topic, new Update(model)));
                                        }))

                                        .orElse(complete("请求资源不存在"))
                                )
                        )
                );
    }

    /**
     * 使用ShardRegion发送给Actor
     */
    private HttpResponse get(String uri, BaseMessage cmd) {
        Message message = new Message(uri, cmd);
        Object obj = messageDispatcher.publishMsg(message);
        if(obj != null) {
            Result result = (Result)obj;
            return response(result.code, result.info==null ? null : JSON.toJSONString(result.info));
        } else {
            return badResponse();
        }
    }

    private HttpResponse update(String uri, BaseMessage cmd) {
        Message message = new Message(uri, cmd);
        Object obj = messageDispatcher.publishMsg(message);
        if(obj != null) {
            Result result = (Result)obj;
            return response(result.code, result.info==null ? null : JSON.toJSONString(result.info));
        } else {
            return badResponse();
        }
    }
}
