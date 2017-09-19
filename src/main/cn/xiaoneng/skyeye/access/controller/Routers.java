package cn.xiaoneng.skyeye.access.controller;

import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import cn.xiaoneng.skyeye.access.example.routes.BaseRoutes;
import cn.xiaoneng.skyeye.access.remote.MessageDispatcher;

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
        return route(evsManagerRouter.route, BaseRoutes.baseRoutes());
    }
}
