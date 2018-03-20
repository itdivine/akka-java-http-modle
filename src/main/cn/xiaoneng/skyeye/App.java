package cn.xiaoneng.skyeye;

import akka.NotUsed;
import akka.actor.*;
import akka.cluster.seed.ZookeeperClusterSeed;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;
import akka.cluster.singleton.ClusterSingletonProxy;
import akka.cluster.singleton.ClusterSingletonProxySettings;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.japi.Option;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import cn.xiaoneng.kafka.NTKafkaProducer;
import cn.xiaoneng.skyeye.enterprise.actor.EVS;
import cn.xiaoneng.skyeye.enterprise.actor.EVSShard;
import cn.xiaoneng.skyeye.util.ActorNames;
import cn.xiaoneng.skyeye.util.COMMON;
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

    public static NTKafkaProducer ntKafkaProducer;

    public App(ActorSystem system) {
        this.system = system;
    }

    public void start(AppConfig appConfig) {

        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        MessageDispatcher.getInstance().init(system);
        Routers routers = new Routers();

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = routers.createRoute().flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
                ConnectHttp.toHost(appConfig.host, appConfig.port),materializer);

    }

    public static void main(String[] args) {

        startup();
    }

    public static void startup() {

        Config config = ConfigFactory.load();
        COMMON.read(config);

        //初始化KAFKA生产者:计算引擎
        ntKafkaProducer = new NTKafkaProducer(COMMON.KAFKA_BROKERS, COMMON.KAFKA_TOPIC, COMMON.systemName);

        ActorSystem system = ActorSystem.create(COMMON.systemName, config);

        HttpCodeRegister.addCustomCode(system);

        new ZookeeperClusterSeed((ExtendedActorSystem) system).join();

//        system.actorOf(Props.create(EVSManager.class),"enterprises");
        createSingletionEVSManager(system);
        createShard(system);


        //启动http服务
        AppConfig appConfig = new AppConfig(COMMON.systemName, COMMON.HOST, COMMON.PORT, COMMON.address, 6000L);
        new App(system).start(appConfig);
    }

    private static void createSingletionEVSManager(ActorSystem system) {

        ClusterSingletonManagerSettings settings = ClusterSingletonManagerSettings.create(system);
        system.actorOf(ClusterSingletonManager.props(
                Props.create(EVSManager.class), PoisonPill.getInstance(), settings),"enterprises");

        ClusterSingletonProxySettings proxySettings = ClusterSingletonProxySettings.create(system);
        system.actorOf(ClusterSingletonProxy.props("/user/enterprises", proxySettings), "enterprisesProxy");
    }

    private static void createShard(ActorSystem system) {

        Option<String> roleOption = Option.none();
        ClusterShardingSettings settings = ClusterShardingSettings.create(system);
        ClusterSharding.get(system)
                .start(
                        ActorNames.EVS,
                        Props.create(EVS.class),
                        settings,
                        new EVSShard());
    }
}
