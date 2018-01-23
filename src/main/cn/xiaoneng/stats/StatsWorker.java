package cn.xiaoneng.stats;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.HashMap;
import java.util.Map;

public class StatsWorker extends AbstractActor {

  Map<String, Integer> cache = new HashMap<String, Integer>();

    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public StatsWorker() {
        log.info("StatsWorker: " + getSelf().path());
    }

  @Override
  public Receive createReceive() {

    return receiveBuilder()
      .match(String.class, word -> {
        Integer length = cache.get(word);
        if (length == null) {
          length = word.length();
          cache.put(word, length);
            log.info(word + "  " + getSelf().path());
        }
          log.info("cache  " + cache);

        sender().tell(length, self());
      })
      .build();
  }
}
