package cn.xiaoneng.skyeye.collector.actor;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.PathMatchers;
import static cn.xiaoneng.skyeye.access.Message.CollectorProtocal.*;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.RecoveryCompleted;
import akka.persistence.SaveSnapshotSuccess;
import akka.persistence.SnapshotOffer;
import akka.routing.FromConfig;
import akka.routing.SmallestMailboxPool;
import cn.xiaoneng.skyeye.access.Message.CollectorProtocal;
import cn.xiaoneng.skyeye.collector.model.CollectorModel;
//import cn.xiaoneng.skyeye.collector.service.CollectorHandler;
import cn.xiaoneng.skyeye.collector.service.CollectorCopy;
import cn.xiaoneng.skyeye.collector.service.CollectorHandler;
import cn.xiaoneng.skyeye.util.ActorNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 采集器
 * <p>
 * Created by xy on 2016/8/5 16:19.
 */
public class Collector extends AbstractPersistentActor {

    protected final Logger log = LoggerFactory.getLogger(getSelf().path().toStringWithoutAddress());

    private CollectorModel model;
    private ActorRef handlerRoute;

//    private static Monitor monitor = MonitorCenter.getMonitor(Node.Collector);

//    public Collector(){}
    public Collector(String siteId, int status) {

        model = new CollectorModel(siteId, status);
        saveSnapshot(model);
//        this.model.siteId = getContext().getParent().path().name();
        handlerRoute = getContext().actorOf(new SmallestMailboxPool(3).props(Props.create(CollectorHandler.class, model)), ActorNames.COLLECTOR_Handler);
//        getContext().actorOf(FromConfig.getInstance().props(Props.create(CollectorHandler.class, model)), ActorNames.COLLECTOR_Handler);
        getContext().actorOf(Props.create(CollectorCopy.class), ActorNames.CollectorCopy);
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        String topic = model.getSiteId() + ActorNames.SLASH + ActorNames.COLLECTOR; // kf_1003/collector
        ActorRef mediator = DistributedPubSub.get(this.getContext().system()).mediator();
        mediator.tell(new DistributedPubSubMediator.Subscribe(topic, ActorNames.NSkyEye, getSelf()), getSelf());

        // akka://NSkyEye/system/sharding/EVS/18/kf_1003/collector
        log.info("collector init success! path = " + getSelf().path() + " topic = " + topic);
    }

    @Override
    public String persistenceId() {
        return this.getSelf().path().toStringWithoutAddress();
    }

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
                .match(SnapshotOffer.class, s -> this.model = (CollectorModel)s.snapshot())
                .match(RecoveryCompleted.class, msg -> log.info("RecoveryCompleted: " + model))
                .matchAny(msg -> log.info("unhandled: " + msg))
                .build();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CollectorProtocal.Report.class, this::report)
                .match(Get.class, msg -> get())
                .match(Update.class, this::update)
                .match(DistributedPubSubMediator.SubscribeAck.class, msg -> log.info("Subscribe Success"))
                .match(SaveSnapshotSuccess.class, msg -> log.info("SaveSnapshotSuccess: " + model))
                .matchAny(msg -> log.info("unhandled: " + msg))
                .build();
    }

    private void report(CollectorProtocal.Report report) {
        handlerRoute.forward(report, getContext());
    }

    private void update(Update msg) {
        log.debug("update: " + msg);
        this.model = msg.model;
        saveSnapshot(model);
        getSender().tell(new Result(StatusCodes.OK, model), getSelf());
    }

    private void get() {
        getSender().tell(new Result(StatusCodes.OK, model), getSelf());
    }
}