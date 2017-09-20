package cn.xiaoneng.skyeye.access.controller;

import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import cn.xiaoneng.skyeye.access.example.routes.BaseRoutes;

/**
 * Created by XY on 2017/8/28.
 */
public class Routers extends AllDirectives {

    public Route createRoute() {
        EvsManagerRouter evsManagerRouter = new EvsManagerRouter();
        EvsRouter evsRouter = new EvsRouter();
        return route(evsManagerRouter.route(), evsRouter.route(), BaseRoutes.baseRoutes());
    }
}
