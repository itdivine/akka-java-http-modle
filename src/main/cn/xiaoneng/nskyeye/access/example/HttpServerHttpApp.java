package cn.xiaoneng.nskyeye.access.example;

import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.Route;
//import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import com.alibaba.fastjson.JSON;
import cn.xiaoneng.nskyeye.access.example.bean.EVS;


/**
 * Server will be started calling `WebServerHttpApp.startServer("localhost", 8080)`
 * and it will be shutdown after pressing return.
 */
class HttpServerHttpApp extends HttpApp {

//    protected final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    /**
     * Routes that this WebServer must handle are defined here
     * Please note this method was named `route` in versions prior to 10.0.7
     */
    @Override
    protected Route routes() {
        return route(
                pathEndOrSingleSlash(() -> // Listens to the top `/`
                        complete("Server up and running") // Completes with some text
                ),
        path("hello", () -> // Listens to paths that are exactly `/hello`
                helloRoutes()
        ));
    }

    private Route helloRoutes() {
        return route(
//            get(() -> parameter("id", id -> complete("get id=" + id))),
                get(() -> parameterMap(params -> {
                    String id = params.get("id");
                    return complete("get id=" + id);
                })),
                post(() -> entity(Unmarshaller.entityToString(), data -> {
                    EVS evs = JSON.parseObject(data, EVS.class);
//                    System.out.println(evs.toString());
                    return complete("post " + evs.toString());
                        }))
                .orElse(complete("Received something else"))
        );
    }

    public static void main(String[] args) throws Exception {
        final HttpServerHttpApp myServer = new HttpServerHttpApp();
        // This will start the server until the return key is pressed
        myServer.startServer("localhost", 8080);
    }
}
