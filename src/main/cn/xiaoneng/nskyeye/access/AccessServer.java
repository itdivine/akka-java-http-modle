package cn.xiaoneng.nskyeye.access;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.actor.AddressFromURIString;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;


import java.util.concurrent.CompletionStage;

/**
 * Created by XY on 2017/8/28.
 */
public class AccessServer {

    private ActorSystem system;
    private AccessConfig config;

    public AccessServer(ActorSystem system, AccessConfig config) {
        this.system = system;
        this.config = config;
    }

    public void start() {

        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        //In order to access all directives we need an instance where the routes are define.
//        HttpServer app = new HttpServer();
        Routers routers = new Routers();

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = routers.createRoute().flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
                ConnectHttp.toHost("localhost", 8080), materializer);

//        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
//        System.in.read(); // let it run until user presses return
//
//        binding
//                .thenCompose(ServerBinding::unbind) // trigger unbinding from the port
//                .thenAccept(unbound -> system.terminate()); // and shutdown when done
    }

    public static void main(String[] args) {

        // 启动轨迹云服务
        Config config = ConfigFactory.load().getConfig("App2");

        //初始化配置信息
        COMMON.read(config);

        //启动http服务
        AccessConfig accessConfig = new AccessConfig(COMMON.systemName, ConfigFactory.load().getConfig(config.getString("httpConfigFileName")),
                config.getString("appUrl"), config.getInt("appPort"), AddressFromURIString.parse(config.getString("clusterAddr")), 6000L);

        // boot up server using the route as defined below
        ActorSystem system = ActorSystem.create("routes");

        //方案一：HTTP MODLE：新的ActorSystem
//            ActorSystem system1 = ActorSystem.create(COMMON.systemName, accessConfig.config());
//            new AccessServer(system1, accessConfig).run();

        //方案二：HTTP MODLE：同一个ActorSystem
        new AccessServer(system, accessConfig).start();

    }


}