package cn.xiaoneng.skyeye.access;

import akka.NotUsed;
import akka.actor.*;
import akka.cluster.seed.ZookeeperClusterSeed;
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
import cn.xiaoneng.skyeye.access.code.HttpCodeRegister;
import cn.xiaoneng.skyeye.access.controller.Routers;
import cn.xiaoneng.skyeye.access.remote.MessageDispatcher;
import cn.xiaoneng.skyeye.enterprise.actor.EVSManager;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.concurrent.CompletionStage;

/**
 * Created by XY on 2017/8/28.
 */
public class App {

    private ActorSystem system;

    public App(ActorSystem system) {
        this.system = system;
    }

    public void start(AppConfig appConfig) {

        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        MessageDispatcher.getInstance().init(system, appConfig.masterAddress);
        Routers routers = new Routers();

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = routers.createRoute().flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
                ConnectHttp.toHost(appConfig.host, appConfig.port),materializer);

    }

    public static void main(String[] args) {

        startup();
    }

    /*public static void startup(String[] ports) {
        for (String PORT : ports) {
            Config config = ConfigFactory
                    .parseString("akka.remote.netty.tcp.PORT=" + PORT)
                    .withFallback(ConfigFactory.load());

            ActorSystem system = ActorSystem.create("NSkyEye", config);
            //system.actorOf(Props.create(EVSManager.class),"enterprises");

            ClusterSingletonManagerSettings settings = ClusterSingletonManagerSettings.create(system);
            system.actorOf(ClusterSingletonManager.props(
                    Props.create(EVSManager.class), PoisonPill.getInstance(), settings),
                    "enterprises");

            // Is an actor on each node that keeps track of where current single master exists
            // The ClusterSingletonProxy receives text from users and delegates to the current StatsService,the single master.
            // It listens to cluster events to lookup the StatsService on the oldest node
            ClusterSingletonProxySettings proxySettings = ClusterSingletonProxySettings.create(system);
            system.actorOf(ClusterSingletonProxy.props("/user/enterprises", proxySettings), "enterprisesProxy");

            //启动http服务  同一个ActorSystem
            COMMON.read(config);
            AppConfig accessConfig = new AppConfig(COMMON.systemName, config,
                    config.getString("HOST"), config.getInt("PORT"), AddressFromURIString.parse(config.getString("address")), 6000L);
            accessConfig.PORT = Integer.parseInt(PORT) + 5529; //重置HTTP端口号
            new App(system, accessConfig).start();

        }
    }*/


    public static void startup() {

        Config config = ConfigFactory.load();

        COMMON.read(config);

        ActorSystem system = ActorSystem.create(COMMON.systemName, config);
        HttpCodeRegister.addCustomCode(system);
        new ZookeeperClusterSeed((ExtendedActorSystem) system).join();

        system.actorOf(Props.create(EVSManager.class),"enterprises");
//        createSingletionEVSManager(system);

        //启动http服务
        AppConfig appConfig = new AppConfig(COMMON.systemName,
                COMMON.HOST, COMMON.PORT, COMMON.address, 6000L);

        new App(system).start(appConfig);

    }

    private static void createSingletionEVSManager(ActorSystem system) {

        ClusterSingletonManagerSettings settings = ClusterSingletonManagerSettings.create(system);
        system.actorOf(ClusterSingletonManager.props(
                Props.create(EVSManager.class), PoisonPill.getInstance(), settings),"enterprises");

        ClusterSingletonProxySettings proxySettings = ClusterSingletonProxySettings.create(system);
        system.actorOf(ClusterSingletonProxy.props("/user/enterprises", proxySettings), "enterprisesProxy");
    }
}
