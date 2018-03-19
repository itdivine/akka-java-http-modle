package cn.xiaoneng.skyeye.config.db;

import cn.xiaoneng.skyeye.collector.config.ItemConfigKeyAction;
import cn.xiaoneng.skyeye.collector.model.Source;
import cn.xiaoneng.skyeye.collector.model.SubPage;
import cn.xiaoneng.skyeye.config.container.CustomAttrs;
import cn.xiaoneng.skyeye.config.model.Brand;
import cn.xiaoneng.skyeye.config.model.HttpConfig;
import cn.xiaoneng.skyeye.config.model.SourceTypeModel;
import cn.xiaoneng.skyeye.enterprise.bean.EVSInfo;
import cn.xiaoneng.skyeye.util.BeanUtil;
import cn.xiaoneng.skyeye.util.Statics;
//import cn.xiaoneng.storage.consumer.service.dao.common.MysqlAPIImpl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.rowset.CachedRowSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by liangyongheng on 2016/11/4 17:03.
 */
public class MySqlDataAccess {

    protected final static Logger log = LoggerFactory.getLogger(MySqlDataAccess.class);

    public static Set<EVSInfo> getAllEVS() {

        Set<EVSInfo> evsInfos = new HashSet<>();
        long startnow = System.currentTimeMillis();
        String sql = "SELECT * FROM nskyeye.trail_enterprise where isdel=0";

        try {

            CachedRowSet rowSet = query(sql, new HashMap<Integer, Object>());

            EVSInfo evsInfo = null;
            while (rowSet.next()) {

                evsInfo = new EVSInfo();
                evsInfos.add(evsInfo);

                evsInfo.setSiteId(rowSet.getString("siteid"));
                evsInfo.setName(rowSet.getString("name"));
                evsInfo.setStatus(rowSet.getInt("status"));
                evsInfo.setExpriedtime(rowSet.getLong("expriedtime"));
                evsInfo.setDeadline(rowSet.getLong("deadline"));
                evsInfo.setCreatorId(rowSet.getString("creatorid"));
                evsInfo.getQuota().setBaidu_keyword_count(rowSet.getLong("baidu_keyword_count"));
                evsInfo.getQuota().setBaidu_keyword_onoff(rowSet.getInt("baidu_keyword_onoff"));
                evsInfo.getQuota().setPv_count_quota_perday(rowSet.getInt("pv_count_quota_perday"));
                evsInfo.getQuota().setConcurrent_count_quota(rowSet.getInt("concurrent_count_quota"));

                Map<String, String> externalMap = evsInfo.getExternal();
                for (Map.Entry<String, String> entry : externalMap.entrySet()) {
                    String key = entry.getKey();
                    externalMap.put(key, rowSet.getString(key));
                }

                log.debug(evsInfo.toString());
            }

        } catch (Exception e) {
            log.error("Exception " + e.toString() + sql);

        } finally {

            long stopnow = new Date().getTime();
            long timespend = stopnow - startnow;
            if (timespend > 1000) {
                log.warn("DB ACCESS END time=" + timespend);
            }
        }
        return evsInfos;

    }

    public static EVSInfo getEVS(String siteId) {

        EVSInfo evsInfo = null;
        long startnow = System.currentTimeMillis();
        String sql = "SELECT * FROM trail_enterprise where siteid=?";

        try {

            HashMap<Integer, Object> map = new HashMap<Integer, Object>();
            map.put(1, siteId);

            CachedRowSet rowSet = query(sql, map);

            if (rowSet.next()) {

                evsInfo = new EVSInfo();
                evsInfo.setSiteId(siteId);
                evsInfo.setName(rowSet.getString("name"));
                evsInfo.setStatus(rowSet.getInt("status"));
                evsInfo.setExpriedtime(rowSet.getLong("expriedtime"));
                evsInfo.setDeadline(rowSet.getLong("deadline"));
                evsInfo.setCreatorId(rowSet.getString("creatorid"));
                evsInfo.getQuota().setBaidu_keyword_count(rowSet.getLong("baidu_keyword_count"));
                evsInfo.getQuota().setBaidu_keyword_onoff(rowSet.getInt("baidu_keyword_onoff"));
                evsInfo.getQuota().setPv_count_quota_perday(rowSet.getInt("pv_count_quota_perday"));
                evsInfo.getQuota().setConcurrent_count_quota(rowSet.getInt("concurrent_count_quota"));

                Map<String, String> externalMap = evsInfo.getExternal();
                for (Map.Entry<String, String> entry : externalMap.entrySet()) {
                    String key = entry.getKey();
                    externalMap.put(key, rowSet.getString(key));
                }

                log.info(evsInfo.toString());
            }

        } catch (Exception e) {
            log.error("Exception " + e.toString() + sql);

        } finally {

            long stopnow = new Date().getTime();
            long timespend = stopnow - startnow;
            if (timespend > 1000) {
                log.warn("DB ACCESS END time=" + timespend);
            }
        }
        return evsInfo;

    }

    /**
     * 保存企业信息
     *
     * @return
     */
    public static boolean setEVS(EVSInfo evsInfo) {

        long startnow = System.currentTimeMillis();

        try {

            EVSInfo info = getEVS(evsInfo.getSiteId());
            if (info != null) {
                return true;
            }

            HashMap<Integer, Object> map = new HashMap<Integer, Object>();
            map.put(1, evsInfo.getSiteId());
            map.put(2, evsInfo.getName());
            map.put(3, evsInfo.getStatus());
            map.put(4, evsInfo.getCreattime());
            map.put(5, evsInfo.getExpriedtime());
            map.put(6, evsInfo.getDeadline());
            map.put(7, evsInfo.getCreatorId());
            map.put(8, evsInfo.getQuota().getPv_count_quota_perday());
            map.put(9, evsInfo.getQuota().getConcurrent_count_quota());
            map.put(10, evsInfo.getQuota().getBaidu_keyword_onoff());
            map.put(11, evsInfo.getQuota().getBaidu_keyword_count());

            int index = 10;
            Map<String, String> externalMap = evsInfo.getExternal();
            for (Map.Entry<String, String> entry : externalMap.entrySet()) {
                String ext = "";
                if (entry.getValue() != null) {
                    ext = entry.getValue();
                }
                map.put(++index, ext);
            }

            String sql = "insert into trail_enterprise"
                    + "(siteid,name,status,createtime,validTime,deadline,"
                    + "creatorid,pv_count_quota_perday,concurrent_count_quota,quotaInfo,ES1,ES2,ES3,ES4,ES5,ES6,ES7,ES8,ES9,ES10,baidu_keyword_onoff,baidu_keyword_count)"
                    + " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";


            /* 本地测试
            Connection connection = TrailStaticDbManager.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1,evsInfo.getSiteId());
            ps.setString(2,evsInfo.getName());
            ps.setInt(3,evsInfo.getStatus());
            ps.setLong(4,evsInfo.getCreattime());
            ps.setLong(5,evsInfo.getExpriedtime());
            ps.setLong(6,evsInfo.getDeadline());
            ps.setString(7,evsInfo.getCreatorId());
            ps.setInt(8,evsInfo.getQuota().getPv_count_quota_perday());
            ps.setInt(9,evsInfo.getQuota().getConcurrent_count_quota());
            int index = 9;
            Map<String, String> externalMap = evsInfo.getExternal();
            for(Map.Entry<String,String> entry : externalMap.entrySet()) {
                ps.setString(++index,entry.getValue());
            }
            int status = ps.executeUpdate();*/

            int status = insert(sql, map);

            log.debug("sql: " + sql + " " + evsInfo.toString());

            if (status == 1) {
                return true;
            }

        } catch (Exception e) {
            log.error("Exception " + e.toString());
            e.printStackTrace();

        } finally {

            long stopnow = System.currentTimeMillis();
            long timespend = stopnow - startnow;
            if (timespend > 1000) {
                log.warn("DB ACCESS END time=" + timespend);
            }
        }

        return false;

    }

    /**
     * 修改企业信息
     *
     * @return
     */
    public static boolean updateEVS(EVSInfo evsInfo) {

        long startnow = System.currentTimeMillis();

        try {
            HashMap<Integer, Object> map = new HashMap<Integer, Object>();
            map.put(1, evsInfo.getName());
            map.put(2, evsInfo.getStatus());
            map.put(3, evsInfo.getCreattime());
            map.put(4, evsInfo.getExpriedtime());
            map.put(5, evsInfo.getDeadline());
            map.put(6, evsInfo.getCreatorId());
            map.put(7, evsInfo.getQuota().getPv_count_quota_perday());
            map.put(8, evsInfo.getQuota().getConcurrent_count_quota());
            map.put(9, evsInfo.getQuota().getBaidu_keyword_onoff());
            map.put(10, evsInfo.getQuota().getBaidu_keyword_count());

            int index = 8;
            Map<String, String> externalMap = evsInfo.getExternal();
            for (Map.Entry<String, String> entry : externalMap.entrySet()) {
                map.put(++index, entry.getValue());
            }
            map.put(++index, evsInfo.getSiteId());

            String sql = "UPDATE trail_enterprise SET name=?,status=?,createtime=?,expriedtime=?,deadline=?, "
                    + "creatorid=?,pv_count_quota_perday=?,concurrent_count_quota=?,ES1=?,ES2=?,ES3=?,ES4=?,ES5=?," +
                    "ES6=?,ES7=?,ES8=?,ES9=?,ES10=?,baidu_keyword_onoff=?,baidu_keyword_count=? where siteid=?";

             /* 本地测试
            Connection connection = TrailStaticDbManager.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1,evsInfo.getName());
            ps.setInt(2,evsInfo.getStatus());
            ps.setLong(3,evsInfo.getCreattime());
            ps.setLong(4,evsInfo.getExpriedtime());
            ps.setLong(5,evsInfo.getDeadline());
            ps.setString(6,evsInfo.getCreatorId());
            ps.setInt(7,evsInfo.getQuota().getPv_count_quota_perday());
            ps.setInt(8,evsInfo.getQuota().getConcurrent_count_quota());
            int index = 8;
            Map<String, String> externalMap = evsInfo.getExternal();
            for(Map.Entry<String,String> entry : externalMap.entrySet()) {
                ps.setString(++index,entry.getValue());
            }
            ps.setString(++index,evsInfo.getSiteId());
            int status = ps.executeUpdate();*/

            int status = insert(sql, map);

            if (status == 1) {
                return true;
            }

        } catch (Exception e) {
            log.error("Exception " + e.toString());

        } finally {

            long stopnow = System.currentTimeMillis();
            long timespend = stopnow - startnow;
            if (timespend > 1000) {
                log.warn("DB ACCESS END time=" + timespend);
            }
        }

        return false;

    }

    /**
     * 删除企业信息
     *
     * @return
     */
    public static boolean deleteEVS(String siteId) {

        long startnow = System.currentTimeMillis();

        try {
            HashMap<Integer, Object> map = new HashMap<Integer, Object>();
            map.put(1, siteId);

            String sql = "UPDATE trail_enterprise SET isdel=1 where siteid=?";

            int status = insert(sql, map);

            if (status == 1) {
                return true;
            }

        } catch (Exception e) {
            log.error("Exception " + e.toString());

        } finally {

            long stopnow = System.currentTimeMillis();
            long timespend = stopnow - startnow;
            if (timespend > 1000) {
                log.warn("DB ACCESS END time=" + timespend);
            }
        }

        return false;

    }

    public static Map<String, Map<Integer, Set<Source>>> getConfigSourceMap() {

        long startnow = System.currentTimeMillis();

        String sql = "SELECT * FROM trail_config_source where isdel = 0";

        try {
            CachedRowSet rowSet = query(sql, new HashMap<Integer, Object>());

            Set<Source> sources = null;
            Map<Integer, Set<Source>> allquerys = null;
            Map<String, Map<Integer, Set<Source>>> siteId2source = new ConcurrentHashMap<>();
            while (rowSet.next()) {
                String ename = rowSet.getString("ename");
                if (Statics.isNullOrEmpty(ename))
                    continue;

                Source source = new Source();
                source.ename = ename;
                source.siteid = rowSet.getString("siteid");
                source.cname = rowSet.getString("cname");
                source.domain = rowSet.getString("domain");
                source.ref_word_rex = rowSet.getString("ref_word_rex");
                source.encode = rowSet.getString("encode");
                source.url_reg = rowSet.getString("url_reg");
                source.pk_config_source = rowSet.getInt("pk_config_source");
                source.sourceexplain = rowSet.getString("sourceexplain");
                source.source_type_id = rowSet.getInt("source_type_id");
                source.source_logo = rowSet.getString("source_logo");
                source.wap_logo = rowSet.getString("wap_logo");

                //regulation值：空type=1 |  非空type=2
                source.type = Statics.isNullOrEmpty(source.url_reg) ? 1 : 2;

                if(siteId2source.containsKey(source.siteid)) {
                    allquerys = siteId2source.get(source.siteid);
                } else {
                    allquerys = new ConcurrentHashMap<>();
                    siteId2source.put(source.siteid, allquerys);
                }

                if (allquerys.containsKey(source.type))
                    sources = allquerys.get(source.type);
                else
                    sources = null;

                if (sources == null) {
                    sources = new HashSet<Source>();
                    allquerys.put(source.type, sources);
                }

                sources.add(source);

                log.debug("trail_config_source siteid=" + source.siteid + " ename=" + source.ename);
            }

            return siteId2source;

        } catch (Exception e) {
            log.error("Exception sql:  SELECT * FROM trail_config_source");
            log.error("SQL: " + sql + "  Exception: " + e.toString());

        } finally {

            long stopnow = new Date().getTime();
            long timespend = stopnow - startnow;
            if (timespend > 1000) {
                log.warn("DB ACCESS END time=" + timespend);
            }
        }
        return null;

    }

    public static List<ItemConfigKeyAction> getConfigKeyList(String siteid) {

        if (siteid == null) {
            return null;
        }

        String preSql = "select * from trail_config_keypage where siteid = ? and isdel = ?";

        HashMap<Integer, Object> parmaterMap = new HashMap<>();
        parmaterMap.put(1, siteid);
        parmaterMap.put(2, 0);

        List<ItemConfigKeyAction> result = new ArrayList<>();

        try {
            CachedRowSet rowSet = query(preSql, parmaterMap);

            while (rowSet.next()) {
                ItemConfigKeyAction item = new ItemConfigKeyAction();

                String urlreg = rowSet.getString("urlreg");
                String keyname = rowSet.getString("keyname");
                item.keylevel = rowSet.getInt("pagelevel");

                if (keyname == null || keyname.length() <= 0 || item.keylevel < 0)
                    continue;

                if (urlreg == null || urlreg.length() <= 0)
                    continue;

                item.siteid = rowSet.getString("siteid");
                item.urlreg = urlreg;
                item.keyname = keyname;
                item.keyid = rowSet.getInt("keyid");
                item.orderidRegex = rowSet.getString("orderidconfig");
                item.pk_keypage = rowSet.getInt("pk_keypage");

                if (item.orderidRegex == null)
                    item.orderidRegex = "";

                result.add(item);
            }
        } catch (Exception e) {
            log.warn("Exception " + e.toString());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.warn(er[i].toString());
            }
        }

        return result;
    }

    public static HashMap<String, List<ItemConfigKeyAction>> getConfigKeyAction() {
        String sql = null;

        long startnow = new Date().getTime();
        HashMap<String, List<ItemConfigKeyAction>> allquerys = new HashMap<String, List<ItemConfigKeyAction>>();

        try {

            sql = "SELECT * FROM trail_config_keypage  where isdel = ?";
            HashMap<Integer, Object> parmaterMap = new HashMap<>();
            parmaterMap.put(1, 0);

            CachedRowSet rowSet = query(sql, parmaterMap);

            List<ItemConfigKeyAction> actions = null;
            ItemConfigKeyAction item = null;

            while (rowSet.next()) {
                item = new ItemConfigKeyAction();

                String urlreg = rowSet.getString("urlreg");
                String keyname = rowSet.getString("keyname");
                item.keylevel = rowSet.getInt("pagelevel");

                if (keyname == null || keyname.length() <= 0 || item.keylevel < 0)
                    continue;

                if (urlreg == null || urlreg.length() <= 0)
                    continue;

                item.siteid = rowSet.getString("siteid");
                item.urlreg = urlreg;
                item.keyname = keyname;
                item.keyid = rowSet.getInt("keyid");
//                item.issave = rowSet.getInt("issave");
                item.orderidRegex = rowSet.getString("orderidconfig");
                item.pk_keypage = rowSet.getInt("pk_keypage");


                if (item.orderidRegex == null)
                    item.orderidRegex = "";

                actions = allquerys.get(item.siteid);
                if (actions == null) {
                    actions = new ArrayList<>();
                    allquerys.put(item.siteid, actions);
                }

                actions.add(item);

                log.debug("trail_config_keypage site=" + item.siteid + " pagelevel=" + item.keylevel + " keyname=" + keyname + " urlreg=" + urlreg);
            }

        } catch (Exception e) {

            log.info("Exception sql:  SELECT * FROM trail_config_keypage where isdel = 0 ");
            log.info("Exception " + e.toString());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        } finally {

            long stopnow = new Date().getTime();
            long timespend = stopnow - startnow;
            if (timespend > 1000) {
                log.warn("DB ACCESS END time=" + timespend);
            }
        }

        return allquerys;
    }

    public static HashMap<String, List<SubPage>> getConfigSubPage() {


        long startnow = new Date().getTime();
        HashMap<String, List<SubPage>> _allquerys = new HashMap<String, List<SubPage>>();

        try {

            String sql = "SELECT * FROM nskyeye.trail_config_subpage where isdel = 0";
            CachedRowSet rowSet = query(sql, new HashMap<Integer, Object>());

            List<SubPage> subPageList = null;
            SubPage subpage = null;

            while (rowSet.next()) {
                subpage = new SubPage();

                subpage.siteid = rowSet.getString("siteid");
                subpage.urlreg = rowSet.getString("urlreg");
                subpage.keyname = rowSet.getString("keyname");
                subpage.pageid = rowSet.getInt("pageid");
                subpage.pagelevel = rowSet.getInt("pagelevel");
                subpage.fatherid = rowSet.getInt("fatherid");

                subPageList = _allquerys.get(subpage.siteid);
                if (subPageList == null) {
                    subPageList = new ArrayList<SubPage>();
                    _allquerys.put(subpage.siteid, subPageList);
                }
                subPageList.add(subpage);

                log.info("trail_config_subpage site=" + subpage.siteid + " pagelevel=" + subpage.pagelevel + " fid=" + subpage.fatherid);
            }

        } catch (Exception e) {
            log.error("Exception sql:  SELECT * FROM trail_config_subpage");
            log.error("Exception " + e.toString());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        } finally {

            long stopnow = new Date().getTime();
            long timespend = stopnow - startnow;
            if (timespend > 1000) {
                log.warn("DB ACCESS END time=" + timespend);
            }
        }

        return _allquerys;
    }

    public static Map<String, HttpConfig> getHttpConfigs() {

        long startnow = new Date().getTime();
        HttpConfig httpConfig = null;
        Map<String, HttpConfig> r = new HashMap<>();
        try {
            String sql = "SELECT siteid,producturl,userurl,orderurl FROM trail_config_http where isdel = 0";

            CachedRowSet rowSet = query(sql, new HashMap<Integer, Object>());

            while (rowSet.next()) {
                httpConfig = new HttpConfig();

                httpConfig.siteid = rowSet.getString("siteid");
                httpConfig.productUrl = rowSet.getString("producturl");
                httpConfig.userUrl = rowSet.getString("userurl");
                httpConfig.orderUrl = rowSet.getString("orderurl");

                if (httpConfig.productUrl == null && httpConfig.userUrl == null && httpConfig.orderUrl == null)
                    continue;

                if (r == null)
                    r = new HashMap<String, HttpConfig>();

                r.put(httpConfig.siteid, httpConfig);
            }

        } catch (Exception e) {
            log.error("Exception " + "SELECT siteid,producturl,userurl,orderurl FROM trail_config_http where isdel = 0" + " / " + e.toString());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }

        } finally {

            long stopnow = new Date().getTime();
            long timespend = stopnow - startnow;
            if (timespend > 1000) {
                log.warn("DB ACCESS END time=" + timespend);
            }
        }
        return r;
    }

    public static Map<String, String> getConfigBrowsermap() {

        long startnow = new Date().getTime();

        try {

            String sql = "SELECT * FROM trail_config_browser where isdel = 0";

            CachedRowSet rowSet = query(sql, new HashMap<Integer, Object>());

            Map<String, String> allquerys = null;
            while (rowSet.next()) {
                String name = rowSet.getString("name");
                String reg = rowSet.getString("reg");

                if (Statics.isNullOrEmpty(name) || Statics.isNullOrEmpty(reg))
                    continue;

                if (allquerys == null)
                    allquerys = new LinkedHashMap<>();

                log.info("trail_browser_reg " + name + " / " + reg);
                allquerys.put(reg, name);
            }

            return allquerys;

        } catch (Exception e) {
            log.error("Exception sql:  SELECT * FROM trail_config_browser where isdel = 0");
            log.error("Exception " + e.toString());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }

        } finally {

            long stopnow = new Date().getTime();
            long timespend = stopnow - startnow;
            if (timespend > 1000) {
                log.warn("DB ACCESS END time=" + timespend);
            }
        }
        return null;
    }

    public static HashMap<String, Map<String, Brand>> getConfigBrand() {

        boolean isSuccess = true;

        long startnow = new Date().getTime();
        HashMap<String, Map<String, Brand>> _allquerys = new HashMap<>();

        try {

            String sql = "SELECT * FROM trail_config_brand where isdel = ?";

            HashMap<Integer, Object> map = new HashMap<Integer, Object>();

            map.put(1, 0);

            CachedRowSet rowSet = query(sql, map);

            Map<String, Brand> configs = null;
            Brand brand = null;
            while (rowSet.next()) {
                String id = rowSet.getInt("pk_brand") + "";
                String name = rowSet.getString("name");
                String website = rowSet.getString("siteid");

                brand = new Brand();
                brand.setId(id);
                brand.setName(name);

                configs = _allquerys.get(website);
                if (configs == null) {
                    configs = new HashMap<String, Brand>();
                    _allquerys.put(website, configs);
                }

                configs.put(id, brand);

                log.info("getConfigBrand " + id + " / " + name + " / " + website);
            }

        } catch (Exception e) {
            isSuccess = false;
            log.error("Exception " + e.toString());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }

        } finally {

            long stopnow = new Date().getTime();
            long timespend = stopnow - startnow;
            if (timespend > 1000) {
                log.warn("DB ACCESS END time=" + timespend);
            }
        }
        return _allquerys;
    }

    public static List<SourceTypeModel> querySourceTypeListBySiteid(String siteid) throws Exception {

        String querySql = BeanUtil.getQuerySql(SourceTypeModel.class.getName());
        HashMap<Integer, Object> parMap = new HashMap<>();

        if (siteid != null && !"".equals(siteid)) {

            querySql = querySql + " and siteid = ?";

            parMap.put(1, 0);
            parMap.put(2, siteid);

        } else {

            parMap.put(1, 0);
        }

        List<SourceTypeModel> sourceTypeModels = new ArrayList<>();

        CachedRowSet rowSet = query(querySql, parMap);
        while (rowSet.next()) {

            SourceTypeModel model = new SourceTypeModel();
            model.setSource_type_id(rowSet.getInt("source_type_id"));
            model.setSiteid(rowSet.getString("siteid"));
            model.setPid(rowSet.getInt("pid"));
            model.setTypename(rowSet.getString("typename"));

            sourceTypeModels.add(model);
        }
        return sourceTypeModels;
    }


    public static String querySourceTypesBySiteid(String siteid) throws Exception {

        String querySql = BeanUtil.getQuerySql(SourceTypeModel.class.getName());
        querySql = querySql + " and siteid = ?";

        HashMap<Integer, Object> parMap = new HashMap<>();
        parMap.put(1, 0);
        parMap.put(2, siteid);

        List<SourceTypeModel> sourceTypeModels = new ArrayList<>();

        CachedRowSet rowSet = query(querySql, parMap);
        while (rowSet.next()) {

            SourceTypeModel model = new SourceTypeModel();
            model.setSource_type_id(rowSet.getInt("source_type_id"));
            model.setSiteid(rowSet.getString("siteid"));
            model.setPid(rowSet.getInt("pid"));
            model.setTypename(rowSet.getString("typename"));
            model.setTypeexplain(rowSet.getString("typeexplain"));

            sourceTypeModels.add(model);
        }

        return "{\"data\" : " + buildSourceTypeJsonTree(sourceTypeModels) + "}";
    }

    /**
     * 构造前端需要的json树格式
     *
     * @param sourceTypeModels
     * @return
     */
    public static String buildSourceTypeJsonTree(List<SourceTypeModel> sourceTypeModels) {

        Map<Integer, SourceTypeModel> idMap = new HashMap<>();
        Map<Integer, List<Integer>> pid2idsMap = new HashMap<>();

        for (SourceTypeModel model : sourceTypeModels) {

            idMap.put(model.getSource_type_id(), model);

            List<Integer> list = pid2idsMap.containsKey(model.getPid()) ? pid2idsMap.get(model.getPid()) : new ArrayList<>();

            list.add(model.getSource_type_id());
            pid2idsMap.put(model.getPid(), list);

        }
        return build(0, idMap, pid2idsMap, new JSONArray()).toJSONString();
    }

    private static JSONArray build(int pid, Map<Integer, SourceTypeModel> idMap, Map<Integer, List<Integer>> pid2idsMap, JSONArray array) {

        if (pid2idsMap.containsKey(pid)) {

            for (int id : pid2idsMap.get(pid)) {

                SourceTypeModel model = idMap.get(id);
                JSONObject object = JSON.parseObject(JSON.toJSONString(model));

                object.put("children", build(id, idMap, pid2idsMap, new JSONArray()));

                array.add(object);
            }
        }
        return array;
    }

    /*public static Source insertSource(String preSql, HashMap<Integer, Object> parametersMap) throws Exception {
        int id = new StorageInterface().RDBMSInsert(preSql, parametersMap);
        String querySql = "select * from trail_config_source where pk_config_source = " + id;

        CachedRowSet rowSet = query(querySql, new HashMap<>());

        Source source = new Source();
        while (rowSet.next()) {
            source.ename = rowSet.getString("ename");
            source.siteid = rowSet.getString("siteid");
            source.cname = rowSet.getString("cname");
            source.domain = rowSet.getString("domain");
            source.ref_word_rex = rowSet.getString("ref_word_rex");
            source.encode = rowSet.getString("encode");
            source.url_reg = rowSet.getString("url_reg");
            source.pk_config_source = rowSet.getInt("pk_config_source");
            source.sourceexplain = rowSet.getString("sourceexplain");
            source.wap_logo = rowSet.getString("wap_logo");
            source.source_logo = rowSet.getString("source_logo");
            break;
        }
        return source;
    }*/

    /*public static ItemConfigKeyAction insertkeyPage(String preSql, HashMap<Integer, Object> parametersMap) {

        int id = new StorageInterface().RDBMSInsert(preSql, parametersMap);
        String querySql = "select * from trail_config_keypage where pk_keypage = ?";

        HashMap<Integer, Object> map = new HashMap<>();
        map.put(1, id);

        ItemConfigKeyAction item = new ItemConfigKeyAction();
        try {
            CachedRowSet rowSet = query(querySql, map);

            while (rowSet.next()) {

                String urlreg = rowSet.getString("urlreg");
                String keyname = rowSet.getString("keyname");
                item.keylevel = rowSet.getInt("pagelevel");

                if (keyname == null || keyname.length() <= 0 || item.keylevel < 0)
                    continue;

                if (urlreg == null || urlreg.length() <= 0)
                    continue;

                item.siteid = rowSet.getString("siteid");
                item.urlreg = urlreg;
                item.keyname = keyname;
                item.keyid = rowSet.getInt("keyid");
                item.orderidRegex = rowSet.getString("orderidconfig");
                item.pk_keypage = rowSet.getInt("pk_keypage");

                if (item.orderidRegex == null)
                    item.orderidRegex = "";

            }
        } catch (Exception e) {
            log.info("select * from trail_config_keypage orderby pagelevel desc where siteid = ? and isdel = ?");
            log.info("Exception " + e.toString());
        }
        return item;

    }*/

    /*public static SourceTypeModel insertSourceType(String preSql, HashMap<Integer, Object> parametersMap) throws Exception {
        int id = new StorageInterface().RDBMSInsert(preSql, parametersMap);
        String querySql = "select * from trail_config_sourcetype where source_type_id = " + id;

        CachedRowSet rowSet = query(querySql, new HashMap<>());

        SourceTypeModel model = new SourceTypeModel();
        while (rowSet.next()) {

            model.setSource_type_id(rowSet.getInt("source_type_id"));
            model.setSiteid(rowSet.getString("siteid"));
            model.setPid(rowSet.getInt("pid"));
            model.setTypename(rowSet.getString("typename"));
            model.setTypeexplain(rowSet.getString("typeexplain"));

            break;

        }
        return model;
    }*/

    public static List<CustomAttrs> queryCustomAttrs(String sql, HashMap<Integer, Object> parMap) throws Exception {

        List<CustomAttrs> result = new ArrayList<>();

        CachedRowSet rowSet = query(sql, parMap);

        while (rowSet.next()) {
            CustomAttrs attrs = new CustomAttrs();

            attrs.setCname(rowSet.getString("cname"));
            attrs.setEname(rowSet.getString("ename"));
            attrs.setId(rowSet.getInt("id"));

            result.add(attrs);
        }


        return result;
    }

    public static int insert(String preSql, HashMap<Integer, Object> parametersMap) {
//        int result = new MysqlAPIImpl().RDBMSInsert(preSql, parametersMap);
//        return result;
        return 0;
    }

    public static CachedRowSet query(String preSql, HashMap<Integer, Object> parametersMap) {
//        return new MysqlAPIImpl().RDBMSSearch(preSql, parametersMap);
        return null;
    }

    public static int update(String preSql, HashMap<Integer, Object> parametersMap) {
//        return new MysqlAPIImpl().RDBMSUpdate(preSql, parametersMap);
        return 0;
    }


    public static void main(String[] args) {

        MySqlDataAccess.getConfigSourceMap();

        SourceTypeModel model1 = new SourceTypeModel();
        model1.setSource_type_id(1);
        model1.setPid(0);

        SourceTypeModel model2 = new SourceTypeModel();
        model2.setSource_type_id(2);
        model2.setPid(1);
        SourceTypeModel model3 = new SourceTypeModel();
        model3.setSource_type_id(3);
        model3.setPid(1);
        SourceTypeModel model4 = new SourceTypeModel();
        model4.setSource_type_id(4);
        model4.setPid(2);

        SourceTypeModel model5 = new SourceTypeModel();
        model5.setSource_type_id(5);
        model5.setPid(0);

        List<SourceTypeModel> list = new ArrayList<>();

        list.add(model1);
        list.add(model2);
        list.add(model3);
        list.add(model4);
        list.add(model5);

        System.out.println(buildSourceTypeJsonTree(list));

    }

    /*public static void insertDefaultSourceDatas(List<String> sqlList, HashMap<Integer, Object> params) {

        for (String sql : sqlList) {

            insert(sql, params);
        }
    }*/

}
