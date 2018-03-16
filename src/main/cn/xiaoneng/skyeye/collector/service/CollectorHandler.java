package cn.xiaoneng.skyeye.collector.service;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import cn.xiaoneng.UserAgent;
import cn.xiaoneng.skyeye.collector.config.ItemConfigKeyAction;
import cn.xiaoneng.skyeye.collector.config.SourcePage;
import cn.xiaoneng.skyeye.collector.http.HttpSender;
import cn.xiaoneng.skyeye.collector.util.CollectorStatus;
import cn.xiaoneng.skyeye.collector.util.JSonToPVMsgConverter;
import cn.xiaoneng.skyeye.config.container.*;
import cn.xiaoneng.skyeye.config.ip.CityInfo;
import cn.xiaoneng.skyeye.config.ip.IpLocation;
import cn.xiaoneng.skyeye.config.ip.LocationCityLoading;
import cn.xiaoneng.skyeye.config.message.ContainerStatusChangedMsg;
import cn.xiaoneng.skyeye.config.model.Brand;
import cn.xiaoneng.skyeye.config.model.Catagory;
import cn.xiaoneng.skyeye.db.Neo4jDataAccess;
import cn.xiaoneng.skyeye.monitor.Monitor;
import cn.xiaoneng.skyeye.monitor.MonitorCenter;
import cn.xiaoneng.skyeye.monitor.Node;
import cn.xiaoneng.skyeye.util.ActorNames;
import cn.xiaoneng.skyeye.util.FieldConstants;
import cn.xiaoneng.skyeye.util.PVMessage;
import cn.xiaoneng.skyeye.util.Statics;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * 处理采集字段
 * Created by liangyongheng on 2016/8/23 16:30.
 */
public class CollectorHandler extends UntypedActor {

    private static Monitor monitor = MonitorCenter.getMonitor(Node.CollectorHandler);
    protected final static Logger log = LoggerFactory.getLogger(CollectorHandler.class);

    //关键页面配置
    private static KeyPageContainer keyPageContainer;

    private static ConfigSubPage subPage;

    //来源配置
    private static ConfigSourcemap sourcemap;

    private static BrowserContainer browserContainer;

    private static LanguageConfig languageConfig;

    private static BrandContainer brandContainer;

    private static CatagoryContainer catagoryContainer;

    private static HttpConfigContainer httpConfigContainer;

    private static SourceTypeContainer sourceTypeContainer;

    private int status;

    public CollectorHandler(int status) {
        this.status = status;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        this.getContext().system().eventStream().subscribe(getSelf(), Container.class);

        getContext().actorSelection("../../../../configs/*").tell(new ContainerStatusChangedMsg(), getSelf());

        log.debug("CollectorHandler init success! path:" + getSelf().path());
    }

    @Override
    public void onReceive(Object message) {

        try {

            if (message instanceof Container) {

                if (message instanceof KeyPageContainer) {
                    keyPageContainer = (KeyPageContainer) message;

                } else if (message instanceof ConfigSubPage) {
                    subPage = (ConfigSubPage) message;

                } else if (message instanceof ConfigSourcemap) {
                    sourcemap = (ConfigSourcemap) message;

                } else if (message instanceof BrowserContainer) {
                    browserContainer = (BrowserContainer) message;

                } else if (message instanceof LanguageConfig) {
                    languageConfig = (LanguageConfig) message;

                } else if (message instanceof BrandContainer) {
                    brandContainer = (BrandContainer) message;

                } else if (message instanceof CatagoryContainer) {
                    catagoryContainer = (CatagoryContainer) message;

                } else if (message instanceof HttpConfigContainer) {
                    httpConfigContainer = (HttpConfigContainer) message;

                } else if (message instanceof SourceTypeContainer) {
                    sourceTypeContainer = (SourceTypeContainer) message;
                }

            } else if (message instanceof JSONArray) {
                handle((JSONArray) message);

            } else if (message instanceof String) {
                preHandle((String) message);
            }

        } catch (Exception e) {
            log.error("Exception: " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.warn(er[i].toString());
            }
        }
    }

    private void preHandle(String message) {

        long start = System.currentTimeMillis();
        try {

            JSONObject jsonMsg = JSON.parseObject((String) message);

            String bodyMsg = jsonMsg.getString("body");
            String ip = jsonMsg.getString("ip");
            String userAgent = jsonMsg.getString(FieldConstants.USERAGENT);

            log.info("RawMsg: " + jsonMsg);

            long time = Long.parseLong(jsonMsg.getString("time") + "");
            monitor.newWriteTime("difTime", System.currentTimeMillis() - time, true);

            JSONObject json = null;
            JSONArray array = null;

            if (bodyMsg.startsWith("{")) {

                json = JSONObject.parseObject(bodyMsg);
            } else {

                array = JSONArray.parseArray(bodyMsg);
            }

            //单个pv上报
            if (json != null && json.size() >= 3) {

                if (status == CollectorStatus.OFF) {
                    log.info("采集器已关闭，不允许收集数据");
                    //直接返回结果
                    getSender().tell("{\"body\" : \"上报失败\",\"status\" : 502}", getSelf());
                } else {

                    getSender().tell("{\"body\" : \"上报成功\",\"status\" : 200}", getSelf());

                    JSONObject ipJson = new JSONObject();
                    ipJson.put("ip", ip.trim());
                    if (!json.getJSONObject("navigation").containsKey("IP")) {

                        json.getJSONObject("navigation").put("IP", ipJson);
                    }

                    if (json.getJSONObject("navigation").containsKey("Terminal")) {

                        json.getJSONObject("navigation").getJSONObject("Terminal").put(FieldConstants.USERAGENT, userAgent);
                    }

                    array = new JSONArray();
                    array.add(json);

                    handle(array);

                }

            } else if (json == null && array != null) {

                //多个pv上报
                if (status == CollectorStatus.OFF) {
                    log.info("采集器已关闭，不允许收集数据");
                    //直接返回结果
                    getSender().tell("{\"body\" : \"上报失败\",\"status\" : 200}", getSelf());
                } else {

                    getSender().tell("{\"body\" : \"上报成功\",\"status\" : 200}", getSelf());

                    for (Object o : array) {

                        JSONObject pv = (JSONObject) o;
                        JSONObject ipJson = new JSONObject();
                        ipJson.put("ip", ip.trim());

                        if (!pv.getJSONObject("navigation").containsKey("IP")) {

                            pv.getJSONObject("navigation").put("IP", ipJson);
                        }

                        if (!json.getJSONObject("navigation").containsKey("Terminal")) {

                            json.getJSONObject("navigation").getJSONObject("Terminal").put(FieldConstants.USERAGENT, userAgent);
                        }
                    }

                    handle(array);
                }

            } else {

                getSender().tell("{\"body\" : \"请求错误\",\"status\" : 200}", getSelf());
            }

        } catch (Exception e) {

            getSender().tell("{\"body\" : \"服务器错误\",\"status\" : 404}", getSelf());
            log.error("exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.warn(er[i].toString());
            }

        } finally {
            monitor.newWriteTime("handleTrail", System.currentTimeMillis() - start, true);
        }
    }

    /**
     * 处理上报轨迹
     *
     * @param jsonArray
     */
    public void handle(JSONArray jsonArray) {

        long start = System.currentTimeMillis();
        JSONObject pvJson = null;
        String siteid = null;
        String url = null;
        ItemConfigKeyAction keyItem = null;

        try {

            for (int i = 0; i < jsonArray.size(); i++) {

                pvJson = jsonArray.getJSONObject(i);
                if (pvJson.containsKey("other")) {
                    pvJson.getJSONObject("other").put("time", System.currentTimeMillis());
                    siteid = pvJson.getJSONObject("other").getString(FieldConstants.SITEID);
                }

                //解析导航空间字段
                if (pvJson.containsKey(PVMessage.NAVIGATION)) {
                    JSONObject navigationJson = pvJson.getJSONObject(PVMessage.NAVIGATION);

                    if (navigationJson.containsKey("Web") && keyPageContainer != null) {

                        String  keyLevel = navigationJson.getJSONObject("Web").getString(FieldConstants.KEYLEVEL);
                        if(keyLevel != null) {
                            // 移动端只有keylevel没有url
                            int level = Integer.parseInt(keyLevel);
                            keyItem = ItemConfigKeyAction.getKeyItem(siteid, level);
                        } else{
                            // web端根据siteid和url解析keyItem(页面级别、名称)
                            url = navigationJson.getJSONObject("Web").getString(FieldConstants.URL);
                            keyItem = ItemConfigKeyAction.getKeyItem(siteid, url, keyPageContainer, subPage);
                        }

                        if (keyItem != null) {
                            navigationJson.getJSONObject("Web").put(FieldConstants.KEYLEVEL, keyItem.keylevel);
                            navigationJson.getJSONObject("Web").put(FieldConstants.KEYNAME, keyItem.keyname);
                        }
                    }

                    //根据siteid,ref,url解析keyword,eqid,domain,source
                    if (navigationJson.containsKey("Source")) {

                        String ref = navigationJson.getJSONObject("Source").getString(FieldConstants.REFFER);
                        String source = navigationJson.getJSONObject("Source").getString(FieldConstants.SOURCE);
                        if(Statics.isNullOrEmpty(source)) {

                            if (sourcemap != null) {
                                SourcePage sp = new SourcePage(siteid, ref, url);
                                sp.setSourcePage(sourcemap, sourceTypeContainer);

                                navigationJson.getJSONObject("Source").put(FieldConstants.KEYWORD, sp._keyword);
                                //navigationJson.getJSONObject("Source").put(FieldConstants.EQID, sp._eqidValue);
                                navigationJson.getJSONObject("Source").put(FieldConstants.DOMAIN, sp._sourcedomain);
                                navigationJson.getJSONObject("Source").put(FieldConstants.SOURCE, sp._source);
                                navigationJson.getJSONObject("Source").put(FieldConstants.SOURCETYPENAME, sp.sourceTypeName);
                            }
                        } else {
                            //微信、二维码
                        }

                    } else {

                        if (sourcemap != null && url != null) {
                            SourcePage sp = new SourcePage(siteid, null, url);
                            sp.setSourcePage(sourcemap, sourceTypeContainer);

                            navigationJson.put("Source", new JSONObject());

                            navigationJson.getJSONObject("Source").put(FieldConstants.KEYWORD, sp._keyword);
//                            navigationJson.getJSONObject("Source").put(FieldConstants.EQID, sp._eqidValue);
                            navigationJson.getJSONObject("Source").put(FieldConstants.DOMAIN, sp._sourcedomain);
                            navigationJson.getJSONObject("Source").put(FieldConstants.SOURCE, sp._source);
                            navigationJson.getJSONObject("Source").put(FieldConstants.SOURCETYPENAME, sp.sourceTypeName);

                        }

                    }

                    //根据userAgent解析system,browser
                    if (navigationJson.containsKey("Terminal")) {

                        if (browserContainer != null) {
                            String useragent = navigationJson.getJSONObject("Terminal").getString(FieldConstants.USERAGENT);

                            UserAgent ua = new UserAgent(useragent);
                            String browser = ua.getBrowser().getName();
//                            String browserName = ua.getBrowser().getGroup().toString();
                            String system = ua.getOperatingSystem().getName();

                            navigationJson.getJSONObject("Terminal").put(FieldConstants.SYSTEM, system);
                            navigationJson.getJSONObject("Terminal").put(FieldConstants.BROWSER, browser);

                            //处理language字段
                            String language = navigationJson.getJSONObject("Terminal").getString(FieldConstants.LANGUAGE);
                            if (language == null)
                                language = "";
                            else {
                                language = language.toLowerCase();
                                language = language.replace(" ", "");
                            }

                            if (language.length() > 16)
                                language = language.substring(0, 16);

                            navigationJson.getJSONObject("Terminal").put(FieldConstants.LANGUAGE, language);

                            //处理screensize字段
                            String screensize = navigationJson.getJSONObject("Terminal").getString(FieldConstants.SCREENSIZE);
                            if (screensize != null) {
                                screensize = screensize.toLowerCase();
                                screensize = screensize.replace(" ", "");
                            }

                            if (screensize != null && screensize.length() > 20)
                                screensize = screensize.substring(0, 20);
                            navigationJson.getJSONObject("Terminal").put(FieldConstants.SCREENSIZE, screensize);

                            //处理device字段
                            String device = navigationJson.getJSONObject("Terminal").getString(FieldConstants.DEVICE);
                            if (device != null && device.length() > 32)
                                device = device.substring(0, 32);
                            navigationJson.getJSONObject("Terminal").put(FieldConstants.DEVICE, device);

                            //处理flash字段
                            String flash = navigationJson.getJSONObject("Terminal").getString(FieldConstants.FLASH);
                            if (flash == null)
                                flash = "";
                            if (flash.length() > 20)
                                flash = flash.substring(0, 20);
                            navigationJson.getJSONObject("Terminal").put(FieldConstants.FLASH, flash);
                        }
                    }

                    if (navigationJson.containsKey("IP")) {
                        JSONObject ipJson = navigationJson.getJSONObject("IP");
                        String ip = ipJson.getString(FieldConstants.IP).trim();
                        ipJson.put(FieldConstants.IP, ip);

                        dealIPFields(ipJson, siteid, ip);
                    }

                    log.info(navigationJson.toJSONString());

                    format(navigationJson);
                }
            }

        } catch (Exception e) {

            log.error(e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.warn(er[i].toString());
            }

        } finally {
            log.debug("collector handle message finished...");
            ActorSelection copy = getContext().actorSelection("../../../" + ActorNames.COLLECTOR + "/copy");

            ActorSelection manager = getContext().actorSelection("../../../" + ActorNames.TrackerManager + "/" + ActorNames.TrackReportPVProcessor);

            for (int i = 0; i < jsonArray.size(); i++) {

                PVMessage pvMessage = JSonToPVMsgConverter.convert(jsonArray.get(i).toString());
                if(!pvMessage.getOtherInfo().containsKey("sid")) {
                    String nt = pvJson.getJSONObject("body").getString(FieldConstants.NTID);
                    String sid = getLastSidFromDB(nt);
                    if(sid == null)
                        sid = ""+System.currentTimeMillis();
                    pvMessage.getOtherInfo().put("sid", sid);
                }
                copy.tell(pvMessage, getSender());
                manager.tell(pvMessage, getSender());
            }

            long end = System.currentTimeMillis();
            monitor.newWriteTime("handleTrail", end - start, true);

        }
    }


    /**
     * 获取访客最近一次来访的ID
     */
    private String getLastSidFromDB(String nt_id) {

        try {
            HashMap<String,Object> map = new HashMap();
            map.put("siteId", Statics.getSiteId(getSelf().path().elements().iterator()));
            map.put("id",nt_id);

            List<String> sidList = Neo4jDataAccess.getSidList(map, 0, 1);
            if(sidList == null || sidList.size()<=0)
                return null;

            return sidList.get(0);

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }

        return null;
    }

    /**
     * 统一为数组格式
     *
     * @param navigationJson
     */
    private void format(JSONObject navigationJson) {

        try {
            Set<String> spaceNames = navigationJson.keySet();

            for (String spaceName : spaceNames) {

                Object value = navigationJson.get(spaceName);
                if (value instanceof JSONObject) {

                    JSONObject jsonObject = navigationJson.getJSONObject(spaceName);
                    JSONArray arr = new JSONArray();
                    arr.add(jsonObject);
                    navigationJson.put(spaceName, arr);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.error(er[i].toString());
            }
        }
    }

    private void dealIPFields(JSONObject ipJson, String siteid, String ip) {

        try {
            String country = ipJson.getString(FieldConstants.COUNTRY);
            String province = ipJson.getString(FieldConstants.PROVINCE);
            String city = ipJson.getString(FieldConstants.CITY);
            String address = ipJson.getString(FieldConstants.ADDRESS);

            String language = "cn";
            String unknow = "未知";

            if (languageConfig != null) {
                language = languageConfig.getLanguage(siteid);

                if ("cn".equalsIgnoreCase(language))
                    unknow = "未知";
                else
                    unknow = "unknown";
            }

            CityInfo httpCity = LocationCityLoading.getInstance().getStandardLocation(province, city, address, language);
            if (httpCity != null) {

                ipJson.put(FieldConstants.COUNTRY, httpCity._country);
                ipJson.put(FieldConstants.PROVINCE, httpCity._province);
                ipJson.put(FieldConstants.CITY, httpCity._city);
                ipJson.put(FieldConstants.ADDRESS, httpCity._address);
            } else {
                //IPIP.NET
                if (ip != null) {
                    setIPIP(language, unknow, ip, ipJson);
                }
            }
        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.error(er[i].toString());
            }
        }
    }

    private void setIPIP(String language, String unknow, String ip, JSONObject lbsJson) {

        try {
//            String[] arr = IP.getInstance().find(ip);//返回字符串数组["country","province","city"]
            String[] arr = IpLocation.getInstance().findLocation(ip);//返回字符串数组["country","province","city"]
            if (arr != null) {
                if (arr.length >= 3) {
                    lbsJson.put(FieldConstants.COUNTRY, arr[0]);
                    lbsJson.put(FieldConstants.PROVINCE, arr[1]);
                    lbsJson.put(FieldConstants.CITY, arr[2]);

                }
            }
        } catch (Exception e) {
            log.debug("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.debug(er[i].toString());
            }
        }
    }

    private void dealProductFields(String siteid, int pagelevel, JSONObject productJson) {

        String brand = null;
        String brandid = null;
        String catetory = null;
        String catetoryid = null;

        try {

            if (pagelevel == 2) {
                // 1.brandid -> brand 品牌
                if (productJson.containsKey(FieldConstants.BRAND)) {
                    brand = productJson.getString(FieldConstants.BRAND);
                }
                if (productJson.containsKey(FieldConstants.BRANDID)) {
                    brandid = productJson.getString(FieldConstants.BRANDID);
                }

                if (Statics.isNullOrEmpty(brand) && !Statics.isNullOrEmpty(brandid) && brandContainer != null) {
                    String value = Brand.paraseBrank(siteid, brandid, brandContainer);
                    productJson.put(FieldConstants.BRAND, value);
                }

                // 2.catetoryid -> catetory 分类
                if (productJson.containsKey(FieldConstants.CATETORY)) {
                    catetory = productJson.getString(FieldConstants.CATETORY);
                }
                if (productJson.containsKey(FieldConstants.CATEGORYID)) {
                    catetoryid = productJson.getString(FieldConstants.CATEGORYID);
                }

                // 1.最短路径 修改为 最长路径
                if (!Statics.isNullOrEmpty(catetory) && catagoryContainer != null) {
                    String value = Catagory.paraseFullCatagory(siteid, catetory, catagoryContainer);
                    productJson.put(FieldConstants.CATETORY, value);
                }

                // 2.catetoryid -> catetory 暂时不支持通过id解析
                else if (Statics.isNullOrEmpty(catetory) && !Statics.isNullOrEmpty(catetoryid) && catagoryContainer != null) {
                    String value = Catagory.paraseCatagory(siteid, catetoryid, catagoryContainer);
                    productJson.put(FieldConstants.CATETORY, value);

                }

            } else if (pagelevel == 4 || pagelevel == 5) {

                String productId = productJson.getString(FieldConstants.PRODUCTID);
                // TODO: 2016/9/1  添加缓存机制，userId来源？

                if (httpConfigContainer != null) {

                    String url = httpConfigContainer.getProductUrl(siteid, productId, null);

                    String info = HttpSender.getInfos(url, null);

                    productJson.put("item", info);
                }
            }

        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.error(er[i].toString());
            }
        }

    }

}
