package cn.xiaoneng.skyeye.config.container;

import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class BrowserContainer implements Container {

    private static BrowserContainer _instance = null;
    protected final static org.slf4j.Logger log = LoggerFactory.getLogger(BrowserContainer.class);
    private Map<String, String> _allquerys = new LinkedHashMap<String, String>();

    //对应trailcenter.trail_browser_reg表，当DB无法访问时，可以弥补数据解析失败
    private static Map<String, String> _browserConfigs = null;

    private static ReentrantReadWriteLock _lockinstance = new ReentrantReadWriteLock();

    public static BrowserContainer getInstance() {
        try {
            _lockinstance.writeLock().lock();

            if (_instance == null)
                _instance = new BrowserContainer();

        } catch (Exception e) {
            log.info("Exception " + e.toString());
        } finally {
            _lockinstance.writeLock().unlock();
        }

        return _instance;
    }

    private BrowserContainer() {
        initBrowserConfig();
        initConfigData();
    }

    private void initBrowserConfig() {
        _browserConfigs = new LinkedHashMap<String, String>();
        _browserConfigs.put("Internet Explorer", "rv:([^\\s|\\)]+)");
        _browserConfigs.put("Lynx", "Lynx/([^\\s]+)/");
        _browserConfigs.put("NetCaptor", "NetCaptor\\s([^\\s|;]+)");
        _browserConfigs.put("Netscape", "Netscape([\\d]*)/([^\\s]+)");
        _browserConfigs.put("Safari", "safari/([^\\s]+)");
        _browserConfigs.put("Google Chrome", "Chrome/([^\\s]+)");
        _browserConfigs.put("FireFox", "FireFox/([^\\s]+)");
        _browserConfigs.put("Internet Explorer", "MSIE\\s([^\\s|;]+)");
        _browserConfigs.put("腾讯TT", "TencentTraveler\\s([^\\s|;]+)");
        _browserConfigs.put("遨游", "Maxthon\\s([^\\s]+)");
        _browserConfigs.put("Opera", "OPR/([^\\s]+)");
        _browserConfigs.put("猎豹", "LBBROWSER");
        _browserConfigs.put("QQ浏览器", "QQBrowser");
        _browserConfigs.put("百度浏览器", "BIDUBrowser");
        _browserConfigs.put("搜狗", "MetaSr");
        _browserConfigs.put("UC", "UCBrowser/([^\\s]+)");
    }


    /**
     * 取得浏览器配置，所有网站共享
     * <p>
     * key - 版本号的正则表达式 <br>
     * value - 浏览器名字
     *
     * @return
     */
    public Map<String, String> getBrowserConfigs() {
        if (_allquerys == null || _allquerys.size() == 0)
            return _browserConfigs;

        return _allquerys;
    }

    public void updateAllquery(Map<String, String> configs) {
        if (configs != null && configs.size() > 0)
            _allquerys = configs;
    }

    public Map<String, String> get_allquerys() {
        return _allquerys;
    }

    private void initConfigData() {

//        Map<String, String> queryData = MySqlDataAccess.getConfigBrowsermap();
//
//        if (queryData != null) {
//            _allquerys.putAll(queryData);
//        }

    }

}

