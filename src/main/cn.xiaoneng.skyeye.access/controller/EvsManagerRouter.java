package controller;

import akka.http.javadsl.server.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.pathEndOrSingleSlash;

/**
 * Created by XuYang on 2017/8/27.
 */
public class EvsManagerRouter {

    protected final static Logger log = LoggerFactory.getLogger(EvsManagerRouter.class);

    /**
     * This route is the one that listens to the top level '/'. It can be a static method
     */
    public static Route baseRoutes() {
        return pathEndOrSingleSlash(() -> // Listens to the top `/`
                complete("Server up and running")); // Completes with some text
    }


}
