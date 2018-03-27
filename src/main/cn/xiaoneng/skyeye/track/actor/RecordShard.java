//package cn.xiaoneng.skyeye.track.actor;
//
//import akka.cluster.sharding.ShardRegion;
//import cn.xiaoneng.skyeye.bodyspace.message.BodyNodeCreateMsg;
//import cn.xiaoneng.skyeye.track.bean.RecordInfo;
//import cn.xiaoneng.skyeye.track.message.GetUserTrackMessage;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class RecordShard implements ShardRegion.MessageExtractor {
//
//    protected final Logger log = LoggerFactory.getLogger(RecordShard.class);
//
//    private final int numberOfShards = 100;
//
//    @Override
//    public String entityId(Object message) {
//        if (message instanceof RecordInfo) {
//            return String.valueOf(((BodyNodeCreateMsg) message).getId());
//        }
//        else if (message instanceof GetUserTrackMessage) {
//            return ((GetUserTrackMessage) message).getId();
//        }
//        return null;
//    }
//
//    @Override
//    public Object entityMessage(Object message) {
//        return message;
//    }
//
//    @Override
//    public String shardId(Object message) {
//        if (message instanceof BodyNodeCreateMsg) {
//            String id = ((BodyNodeCreateMsg) message).getId();
//            return getShardId(id);
//        }
//        else if (message instanceof GetUserTrackMessage) {
//            String id = ((GetUserTrackMessage) message).getId();
//            return getShardId(id);
//        }
//        else if(message instanceof ShardRegion.StartEntity) {
//            String id = ((ShardRegion.StartEntity) message).entityId();
//            return getShardId(id);
//        } else {
//            return null;
//        }
//    }
//
//    private String getShardId(String id) {
//        int shardId = Math.abs(id.hashCode() % numberOfShards);
//        log.info("getShardId: " + shardId);
//        return String.valueOf(shardId);
//    }
//}
