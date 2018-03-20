package cn.xiaoneng.skyeye.navigation.message;//package cn.xiaoneng.skyeye.navigation.message;
//
//import akka.actor.ActorRef;
//import cn.xiaoneng.skyeye.navigation.bean.RawNavigation;
//import cn.xiaoneng.skyeye.util.BaseMessage;
//import cn.xiaoneng.skyeye.util.Operation;
//
//import java.io.Serializable;
//
//
///**
// * Created by Administrator on 2016/7/30.
// */
//public class NavCommandMessage extends BaseMessage implements Serializable {
//
//    private RawNavigation rawNavigation;
//    private ActorRef callback;
//
//    public RawNavigation getRawNavigation() {
//        return rawNavigation;
//    }
//
//    public NavCommandMessage(String operation, long timeToLive, RawNavigation rawNavigation, ActorRef callback) {
//        super(null, operation, timeToLive);
//        this.rawNavigation = rawNavigation;
//        this.callback = callback;
//    }
//
//    @Override
//    public String toString() {
//        return "EVSCommandMessage{" +
//                "rawNavigation=" + rawNavigation +
//                " super=" + super.toString() +
//                '}';
//    }
//
//    public ActorRef getNavNodes() {
//        return callback;
//    }
//
//    public void setNavNodes(ActorRef callback) {
//        this.callback = callback;
//    }
//
//    public static void main(String[] args) {
////        NavCommandMessage getNav = new NavCommandMessage(Operation.GET,10, null,null);
////        System.out.println(getNav);
//
//    }
//
//}
