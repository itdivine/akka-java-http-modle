package cn.xiaoneng.skyeye.access;

import akka.http.javadsl.server.Route;
import cn.xiaoneng.skyeye.access.controller.EvsManagerControl;
import cn.xiaoneng.skyeye.access.example.routes.BaseRoutes;
import akka.http.javadsl.server.AllDirectives;

/**
 * Created by XY on 2017/8/28.
 */
public class Routers extends AllDirectives {

    public Route createRoute() {
        EvsManagerControl evsManagerRouter = new EvsManagerControl();
        return route(evsManagerRouter.route(), BaseRoutes.baseRoutes());
    }
}
