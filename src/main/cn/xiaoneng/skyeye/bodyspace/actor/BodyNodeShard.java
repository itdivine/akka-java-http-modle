package cn.xiaoneng.skyeye.bodyspace.actor;

import akka.cluster.sharding.ShardRegion;
import cn.xiaoneng.skyeye.bodyspace.message.BodyNodeCreateMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.xiaoneng.skyeye.access.Message.EVSProtocal.Create;
import static cn.xiaoneng.skyeye.access.Message.EVSProtocal.Get;

public class BodyNodeShard implements ShardRegion.MessageExtractor {

    protected final Logger log = LoggerFactory.getLogger(BodyNodeShard.class);

    private final int numberOfShards = 100;

    @Override
    public String entityId(Object message) {
        if (message instanceof BodyNodeCreateMsg) {
            return String.valueOf(((BodyNodeCreateMsg) message).getId());
        }
//        else if (message instanceof Get) {
//            return ((Get) message).siteId;
//        }
        return null;
    }

    @Override
    public Object entityMessage(Object message) {
        if (message instanceof BodyNodeCreateMsg)
            return message;
        else
            return message;
    }

    @Override
    public String shardId(Object message) {
        if (message instanceof BodyNodeCreateMsg) {
            String id = ((BodyNodeCreateMsg) message).getId();
            return getShardId(id);
        }
//        else if (message instanceof Get) {
//            String siteId = ((Get) message).siteId;
//            return getShardId(siteId);
//        }
        else if(message instanceof ShardRegion.StartEntity) {
            String id = ((ShardRegion.StartEntity) message).entityId();
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
