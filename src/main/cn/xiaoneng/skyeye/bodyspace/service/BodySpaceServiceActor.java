package cn.xiaoneng.skyeye.bodyspace.service;

import akka.actor.AbstractActor;
import cn.xiaoneng.skyeye.bodyspace.actor.BodySpaceManager;
import cn.xiaoneng.skyeye.bodyspace.message.BodySpaceMsg;
import cn.xiaoneng.skyeye.util.HTTPCommand;
import cn.xiaoneng.skyeye.util.Operation;


/**
 * Created by liangyongheng on 2016/7/25.
 */
public class BodySpaceServiceActor extends AbstractActor {

    @Override
    public void preStart() throws Exception {
        super.preStart();
//        System.out.println(this.getSelf().path());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchAny(this::onReceive)
                .build();
    }

    public void onReceive(Object message) {
        BodySpaceMsg msg = (BodySpaceMsg) message;
        String operation = msg.getOperation();
        if (operation.equals(HTTPCommand.GET)) {
            queryBodySpace(msg.getSpaceName(), msg);
        } else if (operation.equals(Operation.LIST)) {
            queryBodySpaces();
        }

    }

    public void queryBodySpace(String name, BodySpaceMsg msg) {
        String path = "../../" + name;
        if (check(path)) {
            getContext().actorSelection(path).tell(msg, getSender());

        } else {
            System.out.println("该主体空间不存在！");
        }


    }

    public boolean check(String path) {
        boolean result = true;
        // TODO: 2016/8/1
        return result;
    }

    private void queryBodySpaces() {
        for (String bodySpaceName : BodySpaceManager.bodySpaceNames) {
            BodySpaceMsg msg = new BodySpaceMsg();
            msg.setOperation(HTTPCommand.GET);
            String path = "../../" + bodySpaceName;
            getContext().actorSelection(path).tell(msg, getSender());
        }
    }
}
