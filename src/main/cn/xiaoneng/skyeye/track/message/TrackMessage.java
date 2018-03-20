package cn.xiaoneng.skyeye.track.message;

import cn.xiaoneng.skyeye.track.bean.TrackInfo;

import java.io.Serializable;

/**
 * Created by XY on 2017/5/17.
 */
public class TrackMessage {


    public static final class CreateTrackMessage implements Serializable {
        private static final long serialVersionUID = 1L;

        public final TrackInfo info;

        public CreateTrackMessage(TrackInfo info) {
            this.info = info;
        }

        @Override
        public String toString() {
            return "CreateTrackMessage{" + "info=" + info + '}';
        }
    }

    public static final class UpdateTrackMessage implements Serializable {
        private static final long serialVersionUID = 1L;

        public final int status;

        public UpdateTrackMessage(int status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return "UpdateTrackMessage{" + "status=" + status + '}';
        }
    }

    public static final class GetTrackMessage implements Serializable {
        private static final long serialVersionUID = 1L;

        public final String siteId;
        public final String name;

        public GetTrackMessage(String siteId, String name) {
            this.siteId = siteId;
            this.name = name;
        }

        @Override
        public String toString() {
            return "GetTrackMessage{" + "siteId='" + siteId + '\'' + ", name='" + name + '\'' + '}';
        }
    }

    public static final class RemoveTrackMessage implements Serializable {
        private static final long serialVersionUID = 1L;

        public final String siteId;
        public final String name;

        public RemoveTrackMessage(String siteId, String name) {
            this.siteId = siteId;
            this.name = name;
        }

        @Override
        public String toString() {
            return "RemoveTrackMessage{" + "siteId='" + siteId + '\'' + ", name='" + name + '\'' + '}';
        }
    }

    public static final class TrackMessageResult implements Serializable {
        private static final long serialVersionUID = 1L;

        public final boolean result;
        public final TrackInfo info;

        public TrackMessageResult(boolean result, TrackInfo info) {

            this.result = result;
            this.info = info;
        }

        @Override
        public String toString() {
            return "TrackMessageResult{" + "info=" + info + '}';
        }
    }

}
