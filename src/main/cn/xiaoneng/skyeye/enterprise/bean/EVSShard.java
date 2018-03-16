package cn.xiaoneng.skyeye.enterprise.bean;

import akka.cluster.sharding.ShardRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.xiaoneng.skyeye.enterprise.message.EVSProtocal.*;

public class EVSShard implements ShardRegion.MessageExtractor {

    protected final Logger log = LoggerFactory.getLogger(EVSShard.class);

    private final int numberOfShards = 100;

    @Override
    public String entityId(Object message) {
        if (message instanceof Create) {
            return String.valueOf(((Create) message).evsInfo.getSiteId());
        } else if (message instanceof Get) {
            return ((Get) message).siteId;
        }
        return null;
    }

    @Override
    public Object entityMessage(Object message) {
        if (message instanceof Create)
            return message;
        else
            return message;
    }

    @Override
    public String shardId(Object message) {
        if (message instanceof Create) {
            String siteId = ((Create) message).evsInfo.getSiteId();
            return getShardId(siteId);
        } else if (message instanceof Get) {
            String siteId = ((Get) message).siteId;
            return getShardId(siteId);
        } else if(message instanceof ShardRegion.StartEntity) {
            String siteId = ((ShardRegion.StartEntity) message).entityId();
            return getShardId(siteId);
        } else {
            return null;
        }
    }

    private String getShardId(String siteId) {
        int shardId = Math.abs(siteId.hashCode() % numberOfShards);
        log.info("getShardId: " + shardId);
        return String.valueOf(shardId);
    }
}
