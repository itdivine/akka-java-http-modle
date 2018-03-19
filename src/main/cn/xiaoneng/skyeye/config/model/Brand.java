package cn.xiaoneng.skyeye.config.model;

import cn.xiaoneng.skyeye.config.container.BrandContainer;

import java.util.Map;
import java.util.logging.Logger;


/**
 * @author xy
 * @version 创建时间：2014-4-4 上午10:53:10
 */
public class Brand {

    private static Logger log = Logger.getLogger(Brand.class.getName());

    private String id;
    private String name = "";

    private String siteid = "";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSiteid() {
        return siteid;
    }

    public void setSiteid(String siteid) {
        this.siteid = siteid;
    }

    /**
     * 从pageid1;pageid2解析到id1名字;id2名字
     * web端格式：首页;全部分类;女装/女士精品;半身裙;商品详情
     *
     * @param str
     * @return
     */
    public static String paraseBrank(String siteid, String str,BrandContainer container) {
        StringBuffer sbf = new StringBuffer();
        try {
            if (str == null || str.length() <= 0) {
                return str;
            }

            Map<String, Brand> items = container.getBranks(siteid);
            if (items == null || items.size() <= 0) {
                return str;
            }
            String[] strarray = str.split(";");
            if (strarray == null || strarray.length <= 0) {
                return str;
            }
            int loop = 0;
            int count = strarray.length;

            for (String id : strarray) {
                if (id == null || id.length() <= 0) {
                    continue;
                }
                loop++;

                Brand cat = items.get(id);
                if (cat == null) {
                    if (loop >= count) {
                        sbf.append(id);
                    } else {
                        sbf.append(id).append(";");
                    }
                    continue;
                }
                if (loop >= count) {
                    sbf.append(cat.getName());
                } else {
                    sbf.append(cat.getName()).append(";");
                }
            }

        } catch (Exception e) {
            log.warning("Exception " + e.toString() + siteid + " /" + str);
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }
        return sbf.toString();
    }
}
