import akka.http.javadsl.server.Route;
import controller.EvsManagerRouter;
import example.routes.BaseRoutes;
import akka.http.javadsl.server.AllDirectives;

/**
 * Created by XY on 2017/8/28.
 */
public class Routers extends AllDirectives {

    public Route createRoute() {
        return route(EvsManagerRouter.baseRoutes(), BaseRoutes.baseRoutes());
    }
}
