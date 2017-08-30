package cn.xiaoneng.nskyeye.access;

import akka.http.javadsl.server.Route;
import cn.xiaoneng.nskyeye.access.controller.EvsManagerRouter;
import cn.xiaoneng.nskyeye.access.example.routes.BaseRoutes;
import akka.http.javadsl.server.AllDirectives;
import cn.xiaoneng.nskyeye.access.remote.MessageDispatcher;

/**
 * Created by XY on 2017/8/28.
 */
public class Routers extends AllDirectives {

    MessageDispatcher messageDispatcher;
    public Routers(MessageDispatcher messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
    }

    public Route createRoute() {
        EvsManagerRouter evsManagerRouter = new EvsManagerRouter();
        return route(evsManagerRouter.route(messageDispatcher), BaseRoutes.baseRoutes());
    }
}
