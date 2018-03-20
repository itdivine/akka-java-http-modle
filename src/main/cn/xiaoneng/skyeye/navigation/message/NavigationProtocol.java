package cn.xiaoneng.skyeye.navigation.message;

import java.io.Serializable;

/**
 * Created by XY on 2017/6/19.
 */
public class NavigationProtocol {

    /**
     * 查询导航空间列表
     */
    public static final class NavSpaceListGet implements Serializable {
        private static final long serialVersionUID = 1L;

        public final String siteid;
        public final int page;
        public final int per_page;

        public NavSpaceListGet(String siteid, int page, int per_page) {
            this.siteid = siteid;
            this.page = page;
            this.per_page = per_page;
        }

        @Override
        public String toString() {
            return "NavSpaceListGet{" +
                    "siteid='" + siteid + '\'' +
                    ", page=" + page +
                    ", per_page=" + per_page +
                    '}';
        }
    }


}
