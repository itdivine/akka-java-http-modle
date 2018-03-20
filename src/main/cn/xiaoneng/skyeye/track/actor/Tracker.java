package cn.xiaoneng.skyeye.track.actor;

import akka.actor.AbstractActor;
 import cn.xiaoneng.skyeye.monitor.Monitor;
 import cn.xiaoneng.skyeye.monitor.MonitorCenter;
 import cn.xiaoneng.skyeye.monitor.Node;
import cn.xiaoneng.skyeye.track.bean.RecordInfo;
import cn.xiaoneng.skyeye.track.bean.TrackInfo;
import cn.xiaoneng.skyeye.track.message.TrackMessage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 跟踪器
 * 目的：根据导航节点id 查询 主体节点id
 * 创建时机：
 *      创建企业时 预创建跟踪器
 *      创建导航空间时 创建跟踪器
 *
 * Created by xuyang on 2016/8/8.
 */
public class Tracker extends AbstractActor {

    private TrackInfo trackInfo;

    protected final Logger log = LoggerFactory.getLogger(getSelf().path().toStringWithoutAddress());

    private static Monitor monitor = MonitorCenter.getMonitor(Node.Tracker);

    /**
     * key: 导航节点id
     */
    private Map<String, RecordInfo> ralationMap = new HashMap<>();

    public Tracker(TrackInfo trackInfo) {
        this.trackInfo = trackInfo;
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
            log.debug("Receive: " + message);

            if(message instanceof RecordInfo) {
                cacheRecordInfo((RecordInfo) message);

            } else if (message instanceof UpdateTrackMessage) {
                log.debug("Receive message: " + (UpdateTrackMessage)message + " " + getSelf().path().toStringWithoutAddress());
                updateTrack((UpdateTrackMessage)message);

            } else if (message instanceof GetTrackMessage) {
                log.debug("Receive message: " + (GetTrackMessage)message + " " + getSelf().path().toStringWithoutAddress());
                getTrack((GetTrackMessage)message);


            } else {
                unhandled(message);
            }


        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        }
    }



    private void getTrack(GetTrackMessage message) {
        getSender().tell(new TrackMessageResult(true, trackInfo), getSelf());
    }

    /**
     * 更新跟踪状态
     * @param message
     */
    private void updateTrack(UpdateTrackMessage message) {
        trackInfo.setStatus(message.status);
        getSender().tell(new TrackMessageResult(true, trackInfo), getSelf());
    }

    /**
     * 缓存Record
     * @param message
     */
    private void cacheRecordInfo(RecordInfo message) {







    }


}
