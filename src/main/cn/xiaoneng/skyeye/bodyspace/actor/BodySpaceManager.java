package cn.xiaoneng.skyeye.bodyspace.actor;

import akka.actor.*;
import akka.routing.RoundRobinPool;
import cn.xiaoneng.skyeye.bodyspace.message.BodySpaceFieldMsg;
import cn.xiaoneng.skyeye.bodyspace.message.BodySpaceMsg;
import cn.xiaoneng.skyeye.bodyspace.model.BodySpaceModel;
import cn.xiaoneng.skyeye.bodyspace.service.BodySpaceServiceActor;
import cn.xiaoneng.skyeye.bodyspace.service.NTMessageRouter;
import cn.xiaoneng.skyeye.bodyspace.service.PVMsgExcutor;
import cn.xiaoneng.skyeye.monitor.Monitor;
import cn.xiaoneng.skyeye.monitor.MonitorCenter;
import cn.xiaoneng.skyeye.monitor.Node;
import cn.xiaoneng.skyeye.track.message.GetUserTrackMessage;
import cn.xiaoneng.skyeye.util.ActorNames;
import cn.xiaoneng.skyeye.util.HTTPCommand;
import cn.xiaoneng.skyeye.util.PVMessage;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * 主体空间管理器
 * Created by liangyongheng on 2016/7/25 16:16 .
 */
public class BodySpaceManager extends AbstractActor {

    protected final static Logger log = LoggerFactory.getLogger(BodySpaceManager.class);
    private static Monitor monitor = MonitorCenter.getMonitor(Node.BodySpaceManager);


    private ActorRef serviceRouter = null;
    private ActorRef pvMsgExcutor = null;

    private ActorRef ntExcutor = null;

    public static String[] bodySpaceNames = {"dvid", ActorNames.IMEI_BODYSPACE, ActorNames.WX_BODYSPACE, ActorNames.COOKIE_BODYSPACE, ActorNames.LOGIN_BODYSPACE,
            ActorNames.QQ_BODYSPACE, ActorNames.EMAIL_BODYSPACE, ActorNames.PHONE_BODYSPACE, ActorNames.NT_BODYSPACE};

    @Override
    public void preStart() throws Exception {

        initBodySpace();
        getServiceInstance();
        ntExcutor = this.context().actorOf(Props.create(NTMessageRouter.class), "ntrouter");

        super.preStart();

        log.debug("BodySpaceManager init success, path = " + getSelf().path());
    }

    /**
     * 初始化预制主体空间
     */
    private void initBodySpace() {

        Set<String>  fieldSet = new HashSet<>();

        for (String spaceName : bodySpaceNames) {

            BodySpaceModel model = new BodySpaceModel(null, spaceName, 0);

            ActorRef bodySpace = getContext().actorOf(Props.create(BodySpace.class, model), spaceName);

            fieldSet.add(spaceName);

            log.debug("init " + spaceName + "BodySpace success! path: " + bodySpace.path());
        }
        BodySpaceFieldMsg fieldMsg = new BodySpaceFieldMsg(fieldSet);

        //通知采集器更新字段列表
        this.getContext().system().eventStream().publish(fieldMsg);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchAny(this::onReceive)
                .build();
    }

    public void onReceive(Object message) {

        long start = System.currentTimeMillis();

        try {
            log.debug("receive message " + message);

            if (message instanceof PVMessage) {

                getPVMsgExcutorInstance().tell(message, getSender());
                monitor.newWriteTime("PVMessage", System.currentTimeMillis()-start, true);

            } else if (message instanceof GetUserTrackMessage) {
                ntExcutor.tell(message, getSender());

            } else if (message instanceof BodySpaceMsg) {
                serviceRouter.tell(message, getSender());
            }
        } catch (Exception e) {

            log.error("exception" + e.getMessage());
            getSender().tell("{\"status\":415,\"body\":\"\"}", getSelf());
        }

    }

    /**
     * 创建业务处理路由*
     *
     * @return
     */
    private ActorRef getServiceInstance() {

        if (serviceRouter == null) {

            serviceRouter = getContext().actorOf(new RoundRobinPool(5).props(Props.create(BodySpaceServiceActor.class)), "router");
        }
        return serviceRouter;
    }

    private ActorRef getPVMsgExcutorInstance() {

        if (pvMsgExcutor == null) {

            pvMsgExcutor = getContext().actorOf(new RoundRobinPool(5).props(Props.create(PVMsgExcutor.class)), "excutor");
        }
        return pvMsgExcutor;
    }
}
