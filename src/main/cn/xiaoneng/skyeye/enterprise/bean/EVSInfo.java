package cn.xiaoneng.skyeye.enterprise.bean;

import cn.xiaoneng.skyeye.util.Statics;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * 企业信息
 * 企业过期后，在最后期限前充值，可以继续使用。
 * 超过最后期限，则企业被彻底释放。
 * <p>
 * <p>
 * Created by Administrator on 2016/7/27.
 */
public class EVSInfo implements Serializable {

    private String siteId;
    private String name;
    private String creatorId;
    private long creattime;
    private long expriedtime; // 过期时间
    private long deadline;    // 最后期限
    private int status;       // 状态：-1释放 0关闭 1开启

    @JSONField(serialize = false)
    private int count = 10; //扩展字段数量

    private Map<String, String> external = new TreeMap<String, String>(); // 10个扩展字段
    private Quota quota;    // 限额

    public EVSInfo() {
//        init();
        for (int i = 1; i < count + 1; i++) {
            external.put("ES" + i, "");
        }
        creatorId = "";
        creattime = System.currentTimeMillis();
        deadline = 0;
        status = 0;
        quota = new Quota();
    }

    public EVSInfo(EVSInfo evsInfo) {
        siteId = evsInfo.getSiteId();
        name = evsInfo.getName();
        creatorId = evsInfo.getCreatorId();
        creattime = System.currentTimeMillis();
        expriedtime = evsInfo.getExpriedtime();
        deadline = evsInfo.getDeadline();
        status = evsInfo.getStatus();
        quota = evsInfo.getQuota();
        external = evsInfo.external;
        if (external == null || external.isEmpty()) {
            for (int i = 1; i < count + 1; i++) {
                external.put("ES" + i, "");
            }
        }
    }

    /**
     * 更新企业信息
     *
     * @param evsInfo
     */
    public void update(EVSInfo evsInfo) {

        String name = evsInfo.getName();
        if (!Statics.isNullOrEmpty(name)) {
            this.name = name;
        }
        long deadline = evsInfo.getDeadline();
        if (deadline > 0) {
            this.deadline = deadline;
        }
        long expriedtime = evsInfo.getExpriedtime();
        if (expriedtime > 0) {
            this.expriedtime = expriedtime;
        }
        int status = evsInfo.getStatus();
        if (status >= 0) {
            this.status = status;
        }

        quota.update(evsInfo.getQuota());

        external = evsInfo.external;
    }

    public EVSInfo clone() {
        EVSInfo info = new EVSInfo();
        BeanUtils.copyProperties(this, info);
        return info;
    }


    public class Quota implements Serializable {

        private int pv_count_quota_perday;
        private int concurrent_count_quota;
        private int backtrace_days; //回溯周期
        private int baidu_keyword_onoff = 0; //百度关键词开关  1:开通  0:关闭
        private long baidu_keyword_count = 0; //百度关键词数量

        public Quota() {
        }

        public Quota(Quota quota) {
            pv_count_quota_perday = quota.getPv_count_quota_perday();
            concurrent_count_quota = quota.getConcurrent_count_quota();
            backtrace_days = quota.backtrace_days;
            baidu_keyword_onoff = quota.baidu_keyword_onoff;
            baidu_keyword_count = quota.baidu_keyword_count;
        }

        public void update(Quota quota) {

            int pv_count_quota_perday = quota.getPv_count_quota_perday();
            if (pv_count_quota_perday >= 0) {
                this.pv_count_quota_perday = pv_count_quota_perday;
            }

            int concurrent_count_quota = quota.getConcurrent_count_quota();
            if (concurrent_count_quota >= 0) {
                this.concurrent_count_quota = concurrent_count_quota;
            }

            int backtrace_days = quota.backtrace_days;
            if (backtrace_days >= 0) {
                backtrace_days = quota.backtrace_days;
            }

            baidu_keyword_onoff = quota.baidu_keyword_onoff;
            baidu_keyword_count = quota.baidu_keyword_count;
        }


        public void setPv_count_quota_perday(int pv_count_quota_perday) {
            this.pv_count_quota_perday = pv_count_quota_perday;
        }

        public void setConcurrent_count_quota(int concurrent_count_quota) {
            this.concurrent_count_quota = concurrent_count_quota;
        }

        public int getPv_count_quota_perday() {
            return pv_count_quota_perday;
        }

        public int getConcurrent_count_quota() {
            return concurrent_count_quota;
        }

        public int getBacktrace_days() {
            return backtrace_days;
        }

        public void setBacktrace_days(int backtrace_days) {
            this.backtrace_days = backtrace_days;
        }

        public int getBaidu_keyword_onoff() {
            return baidu_keyword_onoff;
        }

        public void setBaidu_keyword_onoff(int baidu_keyword_onoff) {
            this.baidu_keyword_onoff = baidu_keyword_onoff;
        }

        public long getBaidu_keyword_count() {
            return baidu_keyword_count;
        }

        public void setBaidu_keyword_count(long baidu_keyword_count) {
            this.baidu_keyword_count = baidu_keyword_count;
        }

        @Override
        public String toString() {
            return "Quota{" +
                    "pv_count_quota_perday=" + pv_count_quota_perday +
                    ", concurrent_count_quota=" + concurrent_count_quota +
                    ", backtrace_days=" + backtrace_days +
                    ", baidu_keyword_onoff=" + baidu_keyword_onoff +
                    ", baidu_keyword_count=" + baidu_keyword_count +
                    '}';
        }
    }

    public static void main(String[] args) {

        try {
            long time = System.currentTimeMillis();
            System.out.println(new Date(time));

            String json = "{\n" +
                    "    \"creatorId\":\"创建者ID\",\n" +
                    "    \"siteId\":\"kf_1000\",\n" +
                    "    \"name\":\"星巴克\",\n" +
                    "    \"expriedtime\":0,\n" +
                    "    \"deadline\":0,\n" +
                    "    \"status\":0,\n" +
                    "    \"quota\":{\n" +
                    "        \"pv_count_quota_perday\":10,\n" +
                    "        \"concurrent_count_quota\":20\n" +
                    "    },\n" +
                    "    \"external\":{\n" +
                    "            \"ES1\":\"123\",\n" +
                    "            \"ES3\":\"\",\n" +
                    "            \"ES2\":\"\",\n" +
                    "            \"ES5\":\"\",\n" +
                    "            \"ES4\":\"\",\n" +
                    "            \"ES7\":\"\",\n" +
                    "            \"ES6\":\"\",\n" +
                    "            \"ES9\":\"\",\n" +
                    "            \"ES8\":\"\",\n" +
                    "            \"ES10\":\"\"\n" +
                    "    }\n" +
                    "}";
            System.out.println(JSON.parseObject(json).getString("siteId"));

            EVSInfo evsInfo1 = JSON.parseObject(json, EVSInfo.class);

            String test = "EVSInfo";


//            EVSInfo evsInfo2 = JSON.parseObject(json, (Class<EVSInfo>)Class.forName("cn.xiaoneng.skyeye.enterprise.bean.EVSInfo"));
//            System.out.println(evsInfo2);

//            System.out.println(getBean(json, "cn.xiaoneng.skyeye.enterprise.bean.EVSInfo"));

//            System.out.println(JSON.toJSONString(evsInfo1));

//            JSONObject obj = (JSONObject) JSON.toJSON(info);
//            System.out.println(obj);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public static Class<?> getBean(String json, String classPath) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
//
//        //TODO : kafka发送
//        Class<?> clazz = JSON.parseObject(json, (Class<T>)Class.forName(classPath));
//        return clazz;
//    }

    @Override
    public String toString() {
        return "EVSInfo{" +
                "siteId='" + siteId + '\'' +
                ", name='" + name + '\'' +
                ", creatorId='" + creatorId + '\'' +
                ", creattime=" + creattime +
                ", expriedtime=" + expriedtime +
                ", deadline=" + deadline +
                ", status=" + status +
                ", quota=" + quota +
                ", count=" + count +
                ", external=" + external +
                '}';
    }

    public String toJSONString() {
        return JSON.toJSONString(this);
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public void setCreattime(long creattime) {
        this.creattime = creattime;
    }

    public void setExpriedtime(long expriedtime) {
        this.expriedtime = expriedtime;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setQuota(Quota quota) {
        this.quota = quota;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public long getDeadline() {
        return deadline;
    }

    public String getSiteId() {
        return siteId;
    }

    public String getName() {
        return name;
    }

    public String getCreatorId() {
        if (creatorId == null) {
            creatorId = "";
        }
        return creatorId;
    }

    public long getCreattime() {
        return creattime;
    }

    public long getExpriedtime() {
        return expriedtime;
    }

    public int getStatus() {
        return status;
    }

    public Quota getQuota() {
        return quota;
    }

    public Map<String, String> getExternal() {
        return external;
    }

    public void setExternal(Map<String, String> external) {
        this.external = external;
    }

}
