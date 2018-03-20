package cn.xiaoneng.skyeye.access.Message;

import akka.http.javadsl.model.StatusCode;
import cn.xiaoneng.skyeye.collector.model.CollectorModel;
import cn.xiaoneng.skyeye.util.BaseMessage;

public class CollectorProtocal {

    /**
     * 上报采集数据
     */
    public static final class Report extends BaseMessage {
        public final String data;
        public final String ip;
        public final String userAgent;
        public final long time = System.currentTimeMillis();
        public Report(String data, String ip, String userAgent) {
            this.ip = ip;
            this.data = data;
            this.userAgent = userAgent;
        }
        @Override
        public String toString() {
            return "Report{" +
                    "data='" + data + '\'' +
                    ", ip='" + ip + '\'' +
                    ", userAgent='" + userAgent + '\'' +
                    ", time=" + time +
                    '}';
        }
    }

    /**
     * 采集器信息
     */
    public static final class ReportResult extends BaseMessage {
        public final StatusCode code;
        public final String info;
        public ReportResult(StatusCode code, String info) {
            this.code = code;
            this.info = info;
        }
    }

    /**
     * 更新采集器状态
     */
    public static final class Update extends BaseMessage {
        public final CollectorModel model;
        public Update(CollectorModel model) {
            this.model = model;
        }
    }
    /**
     * 查询采集器状态
     */
    public static final class Get extends BaseMessage {
        public final String siteId;
        public Get(String siteId) {
            this.siteId = siteId;
        }
    }

    /**
     * 采集器信息
     */
    public static final class Result extends BaseMessage {
        public final StatusCode code;
        public final CollectorModel info;
        public Result(StatusCode code, CollectorModel info) {
            this.code = code;
            this.info = info;
        }
    }
}
