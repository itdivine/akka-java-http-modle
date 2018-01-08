package cn.xiaoneng.skyeye.access;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.actor.AddressFromURIString;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;
import akka.cluster.singleton.ClusterSingletonProxy;
import akka.cluster.singleton.ClusterSingletonProxySettings;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import cn.xiaoneng.skyeye.access.controller.Routers;
import cn.xiaoneng.skyeye.access.remote.MessageDispatcher;
import cn.xiaoneng.skyeye.enterprise.actor.EVSManager;
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

//        ServerSettings serverSettings = HttpCodeRegister.addCustomCode(system);

        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        //In order to access all directives we need an instance where the routes are define.
//        HttpServer app = new HttpServer();

        MessageDispatcher.getInstance().init(system, config);

        Routers routers = new Routers();

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = routers.createRoute().flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
                ConnectHttp.toHost(config.host, config.port),materializer);

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
//        ActorSystem system = ActorSystem.createEVS("routes");
        ActorSystem system = ActorSystem.create(config.getString("systemName"), config);

        //system.actorOf(Props.create(EVSManager.class), "enterprises");
        ClusterSingletonManagerSettings settings = ClusterSingletonManagerSettings.create(system);
        system.actorOf(ClusterSingletonManager.props(
                Props.create(EVSManager.class), PoisonPill.getInstance(), settings),
                "enterprises");

        // Is an actor on each node that keeps track of where current single master exists
        // The ClusterSingletonProxy receives text from users and delegates to the current StatsService,the single master.
        // It listens to cluster events to lookup the StatsService on the oldest node
        ClusterSingletonProxySettings proxySettings = ClusterSingletonProxySettings.create(system);
        system.actorOf(ClusterSingletonProxy.props("/user/enterprises", proxySettings), "enterprisesProxy");

        //方案一：HTTP MODLE：新的ActorSystem
//            ActorSystem system1 = ActorSystem.createEVS(COMMON.systemName, accessConfig.config());
//            new AccessServer(system1, accessConfig).run();

        //方案二：HTTP MODLE：同一个ActorSystem
        new AccessServer(system, accessConfig).start();

    }


}
