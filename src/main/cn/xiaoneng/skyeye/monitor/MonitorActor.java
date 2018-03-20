package cn.xiaoneng.skyeye.monitor;


import akka.actor.AbstractActor;

/**
 * Created by Administrator on 2017/3/22.
 */
public class MonitorActor extends AbstractActor {


    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .matchAny(this::onReceive)
                .build();
    }
    public void onReceive(Object message) {

//        try {
//
//            log.info("Receive message: " + message);
//
//            if (message instanceof String) {
//                processHTTPCommand((String) message);
//
//            } else if (message instanceof CommandMessage) {
//                processCommand((CommandMessage) message);
//
//            } else if (message instanceof Set) {
//                initEVSFromDB((Set<EVSInfo>)message);
//
//            } else {
//                unhandled(message);
////                getSender().tell("{\"status\":415,\"body\":\"\"}", getSelf());
//            }
//
//        } catch (Exception e) {
//            log.error("Exception " + e.getMessage());
//            getSender().tell("{\"status\":415,\"body\":\"\"}", getSelf());
//        }

    }
}
