//package cn.xiaoneng.skyeye.access.code;
//
//import akka.actor.ActorSystem;
//import akka.http.javadsl.settings.ParserSettings;
//import akka.http.javadsl.settings.ServerSettings;
//
///**
// * 添加全局返回码
// *
// * Created by XuYang on 2017/9/19.
// */
//public class HttpCodeRegister {
//
//    public static ServerSettings addCustomCode(ActorSystem system) {
//        // Add custom method to parser settings:
//        ParserSettings parserSettings = ParserSettings.create(system).withCustomStatusCodes(HttpCode.leetCode);
//        ServerSettings serverSettings = ServerSettings.create(system).withParserSettings(parserSettings);
//        return serverSettings;
//    }
//
//
//
//
//
//
//
//
//
//}
