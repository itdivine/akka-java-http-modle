package cn.xiaoneng.skyeye.access.Message;

import akka.http.javadsl.model.StatusCode;
import cn.xiaoneng.skyeye.util.BaseMessage;

public class TrackProtocal {

    /**
     * 查询用户轨迹
     */
    public static final class Get extends BaseMessage {

        public final String siteId;
        public final int page;
        public final int per_page;
        public final String nt_id;
        public final String nav;
        public final String start_page;

        public Get(String siteId, String nt_id, String nav, String start_page, int page, int per_page) {
            this.siteId = siteId;
            this.nt_id = nt_id;
            this.nav = nav;
            this.start_page = start_page;
            this.page = page;
            this.per_page = per_page;
        }
    }

    /**
     * 采集器信息
     */
    public static final class Result extends BaseMessage {
        public final StatusCode code;
        public final String info;
        public Result(StatusCode code, String info) {
            this.code = code;
            this.info = info;
        }
    }
}
