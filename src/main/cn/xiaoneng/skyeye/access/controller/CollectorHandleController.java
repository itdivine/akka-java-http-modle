package cn.xiaoneng.skyeye.access.controller;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import cn.xiaoneng.skyeye.access.Message.CollectorProtocal;
import cn.xiaoneng.skyeye.access.remote.Message;
import cn.xiaoneng.skyeye.util.ActorNames;
import cn.xiaoneng.skyeye.util.BaseMessage;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

public class CollectorHandleController extends BaseController {

    protected final static Logger log = LoggerFactory.getLogger(CollectorHandleController.class.getName());

    public Route route() {

        return extractRequest(request ->
                extractClientIP(remoteAddr ->
                        path(PathMatchers.segment("enterprises").slash(PathMatchers.segment()).slash(ActorNames.COLLECTOR).slash(ActorNames.COLLECTOR_Handler), siteId ->
                                route(
                                        post(() -> entity(Unmarshaller.entityToString(), data -> {
                                            String ip = remoteAddr.getAddress().map(InetAddress::getHostAddress).orElseGet(() -> "unknow");
                                            String topic = siteId + ActorNames.SLASH + ActorNames.COLLECTOR_Handler;
                                            String userAgent = request.getHeader("User-Agent").get().toString();
                                            log.debug("topic = " + topic + " ip = " + ip + "data = " + data);
                                            return complete(post(topic, new CollectorProtocal.Report(data, ip, userAgent)));
                                        }))

                                        .orElse(complete("请求资源不存在"))
                                )
                        )
                )
        );
    }

    private HttpResponse post(String uri, BaseMessage cmd) {
        Message message = new Message(uri, cmd);
        CollectorProtocal.ReportResult result = (CollectorProtocal.ReportResult)messageDispatcher.publishMsg(message);
        return response(result.code, result.info ==null ? null : JSON.toJSONString(result.info));
//        return HttpResponse.create().withStatus(StatusCodes.get(200)).withEntity(ContentTypes.APPLICATION_JSON, uri);
    }

    /*public static void main(String[] args) throws Exception {
        final CollectorHandleController myServer = new CollectorHandleController();
        // This will start the server until the return key is pressed
        myServer.startServer("localhost", 8080);
    }*/
}