package cn.xiaoneng.skyeye.access.controller;

import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import cn.xiaoneng.skyeye.access.example.routes.BaseRoutes;

/**
 * Created by XY on 2017/8/28.
 */
public class Routers extends AllDirectives {

    public Route createRoute() {
        EvsManagerControl evsManagerRouter = new EvsManagerControl();
        EvsControl evsControl = new EvsControl();
        return route(evsManagerRouter.route(), evsControl.route(), BaseRoutes.baseRoutes());
    }
}
