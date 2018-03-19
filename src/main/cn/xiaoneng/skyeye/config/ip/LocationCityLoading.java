package cn.xiaoneng.skyeye.config.ip;

import cn.xiaoneng.skyeye.util.Statics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * 可以对中国的省份、城市进行标准化	<br><br>
 * 如：吉林省通化市，标准化为： 吉林 通化	<br>
 */
public class LocationCityLoading {

    private static LocationCityLoading _instare = null;
    private static ReentrantReadWriteLock _lockmainprocess = new ReentrantReadWriteLock();

    /**
     * 中国省份城市配置文件地址
     */
    private static final String SPAMCONFIG = "/localtion_city_china.xml";

    /**
     * 中国省份城市列表 	<br>
     * key: 省份 	<br>
     * value: 城市集合
     */
    private Map<String, Set<String>> _pcMap = new ConcurrentHashMap<String, Set<String>>();//<省,市>

    /**
     * 中国省份和城市的中英文对照表 	<br>
     * key: 中文名	 	<br>
     * value： 英文名
     */
    private Map<String, String> _area_Chin_Eng_Map = new ConcurrentHashMap<String, String>();

    protected final static Logger log = LoggerFactory.getLogger(LocationCityLoading.class);

    public static LocationCityLoading getInstance() {

        if (_instare == null) {
            try {
                _lockmainprocess.writeLock().lock();

                if (_instare == null) {
                    _instare = new LocationCityLoading();
                } else {
                    log.info("Singleton is not single!");
                }

            } catch (Exception e) {
                log.warn("Exception " + e.toString());
            } finally {
                _lockmainprocess.writeLock().unlock();
            }
        }
        return _instare;
    }

    private LocationCityLoading() {
        parseAddressXml();
    }

    /**
     * 加载省份城市列表
     */
    private void parseAddressXml() {
        try {
            File file = new File(this.getClass().getResource(SPAMCONFIG).getPath());
            if (!file.isFile()) {
                log.error("Config File is Error  path=" + SPAMCONFIG);
                return;
            }

            DocumentBuilder db = getBuilder();
            Document doc = db.parse(file);

            // 列表查看XML元素
            NodeList np = doc.getElementsByTagName("province");

            Element e1 = null;
            Element e2 = null;
            String province_cn = null;
            String province_en = null;
            String city_cn = null;
            String city_en = null;

            int len = np.getLength();
            for (int i = 0; i < len; i++) {
                e1 = (Element) np.item(i);
                province_cn = (e1.getAttribute("name"));
                province_en = (e1.getAttribute("en"));
                log.debug("province city: " + province_cn + ":" + province_en);
                _area_Chin_Eng_Map.put(province_cn, province_en);

                NodeList ncity = e1.getElementsByTagName("city");
                Set<String> citys = new HashSet<String>();
                int len2 = ncity.getLength();
                for (int i2 = 0; i2 < len2; i2++) {
                    e2 = (Element) ncity.item(i2);
                    city_cn = (e2.getAttribute("name"));
                    city_en = (e2.getAttribute("en"));
                    _area_Chin_Eng_Map.put(city_cn, city_en);
                    citys.add(city_cn);
                }
                _pcMap.put(province_cn, citys);
            }

        } catch (Exception e) {
            log.warn("Exception " + e.toString());
        }
    }

    private DocumentBuilder getBuilder() {
        try {
            DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
            return domfac.newDocumentBuilder();

        } catch (Exception e) {
            log.warn("Exception " + e.toString());
        }

        return null;
    }

    public CityInfo getStandardLocation(String province, String city, String address, String language) {
        if (Statics.isNullOrEmpty(province) || Statics.isNullOrEmpty(city))
            return null;

        CityInfo cityInfo = null;
        String configProvince = null;
        Set<String> citys = null;

        try {

            if (_pcMap.size() == 0) {
                cityInfo = new CityInfo();
                cityInfo._country = "中国";
                cityInfo._province = province;
                cityInfo._city = city;
                cityInfo._address = address;
            }

            for (Entry<String, Set<String>> entry : _pcMap.entrySet()) {
                configProvince = entry.getKey();
                citys = entry.getValue();

                if (!province.contains(configProvince))
                    continue;

                cityInfo = new CityInfo();
                cityInfo._country = "中国";
                cityInfo._province = configProvince;
                cityInfo._address = address;

                for (String configCity : citys) {
                    if (city.contains(configCity)) {
                        cityInfo._city = configCity;
                        break;
                    }
                }
                break;
            }

            parseLanguage(cityInfo, language);

        } catch (Exception e) {
            log.warn("Exception " + e);
        }
        return cityInfo;
    }

    private void parseLanguage(CityInfo cityInfo, String language) {

        if (cityInfo == null)
            return;

        try {
            if (language.equalsIgnoreCase("en")) {
                cityInfo._country = "China";
                cityInfo._province = _area_Chin_Eng_Map.get(cityInfo._province);
                cityInfo._city = _area_Chin_Eng_Map.get(cityInfo._city);
            }

        } catch (Exception e) {
            log.warn("Exception " + e);
        }
    }

    public CityInfo getLocalArea(String location, String address, String language) {
        CityInfo cinfo = null;
        String configProvince = null;
        Set<String> citys = null;

        log.debug("language " + language);

        if (_pcMap == null || _pcMap.size() == 0) {
            cinfo = new CityInfo();
//			cinfo._country = "中国";
            cinfo._province = location;
            cinfo._address = address;
        }

        try {
            for (Entry<String, Set<String>> entry : _pcMap.entrySet()) {
                configProvince = entry.getKey();
                citys = entry.getValue();

                if (!location.contains(configProvince))
                    continue;

                cinfo = new CityInfo();
                cinfo._country = "中国";
                cinfo._province = configProvince;
                cinfo._address = address;

                for (String city : citys) {
                    if (location.contains(city)) {
                        cinfo._city = city;
                        break;
                    }
                }
                break;
            }

            if (cinfo != null && language.equalsIgnoreCase("en")) {
                cinfo._country = "China";
                cinfo._province = _area_Chin_Eng_Map.get(cinfo._province);
                cinfo._city = _area_Chin_Eng_Map.get(cinfo._city);
            }

        } catch (Exception e) {
            log.warn("Exception " + e);
        }
        return cinfo;
    }

    public static void main(String[] args) {
        LocationCityLoading.getInstance();
    }
}