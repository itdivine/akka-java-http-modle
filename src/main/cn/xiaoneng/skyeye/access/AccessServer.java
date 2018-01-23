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
import cn.xiaoneng.stats.StatsService;
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

        MessageDispatcher.getInstance().init(system, config);
        Routers routers = new Routers();

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = routers.createRoute().flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
                ConnectHttp.toHost(config.host, config.port),materializer);

    }

    public static void main(String[] args) {

        if (args.length == 0) {
            startupDemo(new String[] { "2551", "2552"}); //, "2552", "0", "2552"
        } else {
            startup(args);
        }
    }

    public static void startup(String[] ports) {

            for (String port : ports) {

                // 启动轨迹云服务
                //Config config = ConfigFactory.load().getConfig("App2");
                Config config = ConfigFactory
                        .parseString("akka.remote.netty.tcp.port=" + port)
                        .withFallback(ConfigFactory.load());

                //初始化配置信息
                //COMMON.read(config);

                //启动http服务
    //            AccessConfig accessConfig = new AccessConfig(COMMON.systemName, ConfigFactory.load().getConfig(config.getString("httpConfigFileName")),
    //                    config.getString("appUrl"), config.getInt("appPort"), AddressFromURIString.parse(config.getString("clusterAddr")), 6000L);

                ActorSystem system = ActorSystem.create("NSkyEye", config);

                ClusterSingletonManagerSettings settings = ClusterSingletonManagerSettings.create(system);
                system.actorOf(ClusterSingletonManager.props(
                        Props.create(EVSManager.class), PoisonPill.getInstance(), settings),
                        "enterprises");

                ClusterSingletonProxySettings proxySettings = ClusterSingletonProxySettings.create(system);
                system.actorOf(ClusterSingletonProxy.props("/user/enterprises", proxySettings), "enterprisesProxy");


                //HTTP MODLE：同一个ActorSystem
                //new AccessServer(system, accessConfig).start();
            }
    }

    public static void startupDemo(String[] ports) {
        for (String port : ports) {
            // Override the configuration of the port
            Config config = ConfigFactory
                    .parseString("akka.remote.netty.tcp.port=" + port)
                    .withFallback(ConfigFactory.load());

            ActorSystem system = ActorSystem.create(config.getString("systemName"), config);

            ClusterSingletonManagerSettings settings = ClusterSingletonManagerSettings.create(system);
            system.actorOf(ClusterSingletonManager.props(
                    Props.create(StatsService.class), PoisonPill.getInstance(), settings),
                    "statsService");

            // Is an actor on each node that keeps track of where current single master exists
            // The ClusterSingletonProxy receives text from users and delegates to the current StatsService,the single master.
            // It listens to cluster events to lookup the StatsService on the oldest node
            ClusterSingletonProxySettings proxySettings = ClusterSingletonProxySettings.create(system);
            system.actorOf(ClusterSingletonProxy.props("/user/statsService", proxySettings), "statsServiceProxy");
        }
    }


}
