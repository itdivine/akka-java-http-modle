package cn.xiaoneng.skyeye.bodyspace.actor;

import akka.actor.AbstractActor;
import cn.xiaoneng.skyeye.bodyspace.message.BodyNodeCreateMsg;
import cn.xiaoneng.skyeye.bodyspace.message.NTRelatedMsg;
import cn.xiaoneng.skyeye.bodyspace.model.BodyNodeModel;
import cn.xiaoneng.skyeye.db.Neo4jDataAccess;
import cn.xiaoneng.skyeye.monitor.Monitor;
import cn.xiaoneng.skyeye.monitor.MonitorCenter;
import cn.xiaoneng.skyeye.monitor.Node;
import cn.xiaoneng.skyeye.util.ActorNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Created by liangyongheng on 2016/10/24 11:21.
 */
public class CookieBodyNode extends BodyNode {

    protected final static Logger log = LoggerFactory.getLogger(CookieBodyNode.class);
    private static Monitor monitor = MonitorCenter.getMonitor(Node.CookieBodyNode);

    //TODO 临时解决，需要放入到model中，并让存储更新轨迹bean包
    private String _loginId;


    public CookieBodyNode(BodyNodeModel model) {

        super(model);
    }

    public CookieBodyNode() {

    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .matchAny(this::onReceive)
                .build();
    }

    public void onReceive(Object message) {

        long start = System.currentTimeMillis();

        try {

            log.debug(getSelf().path() + " receive message :" + message.toString());

            if (message instanceof BodyNodeCreateMsg) {

                //cookie登陆新的login账号，则绑定新的login账号，和旧的login账号解绑
//                if(model!=null && !model.getNt_id().equals(((BodyNodeCreateMsg) message).getNt_id())) {
//                    model.setNt_id(((BodyNodeCreateMsg) message).getNt_id());
//                }

                super.onCreateBodyNode((BodyNodeCreateMsg) message);
                _loginId = ((BodyNodeCreateMsg) message).getLoginId();
                monitor.newWriteTime("BodyNodeCreateMsg", System.currentTimeMillis() - start, true);

            } else if (message instanceof NTRelatedMsg) {

                NTRelatedMsg msg = (NTRelatedMsg) message;
                setRelateByNt(msg.getNt_id(), msg.getLoginId());
                monitor.newWriteTime("NTRelatedMsg", System.currentTimeMillis() - start, true);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }

    private void setRelateByNt(String newNt_id, String loginId) {

//        System.out.println("------------------------------------------------loginId : " + loginId);

        if (newNt_id == null || loginId == null) {
            return;
        } else {

            String oldNt_id = this.model.getNt_id();

//            if (newNt_id.equals(oldNt_id)) {
//
//                //通知ntactor创建loginid
//                this.getContext().actorSelection("../../nt/" + oldNt_id).tell(new NTRelatedMsg(null, loginId), getSelf());
//            } else {

            //如果登陆ID相等，创建等价关系
            if(_loginId!=null && _loginId.equals(loginId))
            {
                //通知nt_actor创建nt之间等价关系
                this.getContext().actorSelection("../../nt/" + oldNt_id).tell(new NTRelatedMsg(newNt_id, loginId), getSelf());
                this.getContext().actorSelection("../../nt/" + newNt_id).tell(new NTRelatedMsg(oldNt_id, loginId), getSelf());
            }
            //如果登陆ID不相等，解绑cookie->旧nt的关系，保留cookie<-旧nt的关系，增加cookie和新nt的关系
            {
                removeRelation();

                model.setNt_id(newNt_id);
                saveRelation(ActorNames.COOKIE_BODYSPACE);

            }

//            }
        }
    }

    private void removeRelation() {

        HashMap<String, Object> map = new HashMap<>();

        map.put("id1", model.getId());
        map.put("id2", model.getNt_id());
        map.put("siteId", model.getSiteId());
        Neo4jDataAccess.deleteBodyBondRelation("cookie", map);
//        Neo4jDataAccess_source.deleteBodyBondRelation("cookie", map);

    }

}
