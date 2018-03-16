package cn.xiaoneng.skyeye.enterprise.message;

import akka.http.javadsl.model.StatusCode;
import cn.xiaoneng.skyeye.util.base.BaseMessage;
import cn.xiaoneng.skyeye.enterprise.bean.EVSInfo;

import java.util.Set;

public class EVSProtocal {

    public static final class Create extends BaseMessage {
        public final EVSInfo evsInfo;
        public Create(EVSInfo evsInfo) {
            this.evsInfo = evsInfo;
        }
    }

    public static final class Update extends BaseMessage {
        public final EVSInfo evsInfo;
        public Update(EVSInfo evsInfo) {
            this.evsInfo = evsInfo;
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
    /**
     * 查询导航空间列表
     */
    public static final class EVSListGet extends BaseMessage {
        public final int page;
        public final int per_page;
        public EVSListGet(int page, int per_page) {
            this.page = page;
            this.per_page = per_page;
        }
        @Override
        public String toString() {
            return "EVSListGet{" + "page=" + page + ", per_page=" + per_page + '}';
        }
    }

    public static final class Result extends BaseMessage {
        public final StatusCode code;
        public final EVSInfo evsInfo;
        public Result(StatusCode code, EVSInfo evsInfo) {
            this.code = code;
            this.evsInfo = evsInfo;
        }
    }

    public static final class EVSListResult extends BaseMessage {
        public final StatusCode code;
        public final Set<String> siteIds;
        public EVSListResult(StatusCode code, Set<String> siteIds) {
            this.code = code;
            this.siteIds = siteIds;
        }
    }
}
