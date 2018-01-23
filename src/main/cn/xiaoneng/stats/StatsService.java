package cn.xiaoneng.stats;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.ConsistentHashingRouter.ConsistentHashableEnvelope;
import akka.routing.FromConfig;

public class StatsService extends AbstractActor {

    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  // This router is used both with lookup and deploy of routees. If you
  // have a router with only lookup of routees you can use Props.empty()
  // instead of Props.create(StatsWorker.class).
  ActorRef workerRouter = getContext().actorOf(
      FromConfig.getInstance().props(Props.create(StatsWorker.class)),
      "workerRouter");

    public StatsService() {
        log.info("StatsService: " + getSelf().path());
        log.info("workerRouter: " + workerRouter.path());
    }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
      .match(StatsMessages.StatsJob.class, job -> !job.getText().isEmpty(), job -> {
        String[] words = job.getText().split(" ");
        ActorRef replyTo = sender();
      })
      .build();
  }
}
