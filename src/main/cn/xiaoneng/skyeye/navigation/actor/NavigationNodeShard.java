package cn.xiaoneng.skyeye.navigation.actor;

import akka.cluster.sharding.ShardRegion;
import cn.xiaoneng.skyeye.navigation.bean.NavNodeInfo;
import cn.xiaoneng.skyeye.navigation.message.CreateNavNodeMsg;
import cn.xiaoneng.skyeye.navigation.message.GetNavNodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NavigationNodeShard implements ShardRegion.MessageExtractor {

    protected final Logger log = LoggerFactory.getLogger(NavigationNodeShard.class);

    private final int numberOfShards = 100;

    @Override
    public String entityId(Object message) {
        if (message instanceof NavNodeInfo) {
            return String.valueOf(((NavNodeInfo) message).getId());
        }
        else if (message instanceof CreateNavNodeMsg) {
            return ((CreateNavNodeMsg) message).getNavNodeInfo().getId();
        }
        else if (message instanceof GetNavNodeInfo) {
            return ((GetNavNodeInfo) message).getNavId();
        }
        return null;
    }

    @Override
    public Object entityMessage(Object message) {
        return message;
    }

    @Override
    public String shardId(Object message) {
        if (message instanceof NavNodeInfo) {
            String id = ((NavNodeInfo) message).getId();
            return getShardId(id);
        }
        else if (message instanceof CreateNavNodeMsg) {
            String nodeId = ((CreateNavNodeMsg) message).getNavNodeInfo().getId();
            return getShardId(nodeId);
        }
        else if(message instanceof ShardRegion.StartEntity) {
            String id = ((ShardRegion.StartEntity) message).entityId();
            return getShardId(id);
        }
        else if (message instanceof GetNavNodeInfo) {
            String id =  ((GetNavNodeInfo) message).getNavId();
            return getShardId(id);
        } else {
            return null;
        }
    }

    private String getShardId(String id) {
        int shardId = Math.abs(id.hashCode() % numberOfShards);
        log.info("getShardId: " + shardId);
        return String.valueOf(shardId);
    }
}