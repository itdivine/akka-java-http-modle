package cn.xiaoneng.skyeye.access.example;

import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.RemoteAddress;
import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import cn.xiaoneng.skyeye.enterprise.actor.EVS;
import com.alibaba.fastjson.JSON;

import java.net.InetAddress;

//import akka.http.javadsl.marshallers.jackson.Jackson;


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

        return extractRequest(request ->        // Get HttpRequest
                extractClientIP(remoteAddr ->   // Get IP
                        route(
                                pathEndOrSingleSlash(() -> // Listens to the top `/`
                                        complete("Server up and running") // Completes with some text
                                ),
                                path("hello", () -> // Listens to paths that are exactly `/hello`
                                        helloRoutes(request, remoteAddr)
                                )
                        )
                )
        );
    }

    // Get HttpRequest
    private Route request() {
        return extractRequest(request ->
                complete("Request method is " + request.method().name() +
                        " and content-type is " + request.entity().getContentType())
        );
    }

    private Route helloRoutes(HttpRequest request, RemoteAddress remoteAddr) {
        return route(
//            get(() -> parameter("id", id -> complete("get id=" + id))),
                get(() -> parameterMap(params -> {
                    String id = params.get("id");
                    return complete("get id=" + id + " \n uri=" + request.getUri().getPathString() + " \n Client's IP is " + remoteAddr.getAddress().map(InetAddress::getHostAddress)
                            .orElseGet(() -> "unknown"));
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
