package cn.xiaoneng.skyeye.access.controller;

import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

/**
 * Created by XY on 2017/8/28.
 */
public class Routers extends AllDirectives {

    public Route createRoute() {
        EvsManagerController evsManagerRouter = new EvsManagerController();
        EvsController evsController = new EvsController();
        return route(evsManagerRouter.route(), evsController.route(), RootController.baseRoutes());
    }
}
