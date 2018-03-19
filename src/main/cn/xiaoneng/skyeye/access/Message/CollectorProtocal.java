package cn.xiaoneng.skyeye.access.Message;

import akka.http.javadsl.model.StatusCode;
import cn.xiaoneng.skyeye.collector.model.CollectorModel;
import cn.xiaoneng.skyeye.util.base.BaseMessage;

public class CollectorProtocal {

    /*public static final class Create extends BaseMessage {
        public final CollectorModel model;
        public Create(CollectorModel model) {
            this.model = model;
        }
    }*/

    public static final class Update extends BaseMessage {
        public final CollectorModel model;
        public Update(CollectorModel model) {
            this.model = model;
        }
    }

    public static final class Get extends BaseMessage {
        public final String siteId;
        public Get(String siteId) {
            this.siteId = siteId;
        }
    }
    public static final class Delete extends BaseMessage {
        public final String siteId;
        public Delete(String siteId) {
            this.siteId = siteId;
        }
    }

    public static final class Result extends BaseMessage {
        public final StatusCode code;
        public final CollectorModel info;
        public Result(StatusCode code, CollectorModel info) {
            this.code = code;
            this.info = info;
        }
    }
}
