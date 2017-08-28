package remote;

import akka.actor.AbstractActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class ClusterListener extends AbstractActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    Cluster cluster = Cluster.get(getContext().system());
    private AccessConfig config;

    public ClusterListener(AccessConfig config) {
        this.config = config;
    }

    @Override
    public void preStart() {
        // 使用cluster将此actor注册以用来监听节点
        cluster.subscribe(self(), ClusterEvent.initialStateAsEvents(),
                MemberEvent.class, UnreachableMember.class);

        // 将节点加入到主节点
        cluster.join(config.masterAddress);
        log.debug("cluster has joined master[{}]", config.masterAddress);
    }

    //re-subscribe when restart
    @Override
    public void postStop() {
        cluster.unsubscribe(self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(MemberUp.class, mUp -> {
                    log.info("Member is Up: {}", mUp.member());
                })
                .match(UnreachableMember.class, mUnreachable -> {
                    log.info("Member detected as unreachable: {}", mUnreachable.member());
                })
                .match(MemberRemoved.class, mRemoved -> {
                    log.info("Member is Removed: {}", mRemoved.member());
                })
                .match(MemberEvent.class, otherEvent -> {
                    log.info("otherEvent: {}", otherEvent);
                })
                .build();
    }
}
