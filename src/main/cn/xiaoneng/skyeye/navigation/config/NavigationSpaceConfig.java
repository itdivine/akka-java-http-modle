package cn.xiaoneng.skyeye.navigation.config;

import cn.xiaoneng.skyeye.navigation.bean.NavigationSpaceInfo;
import cn.xiaoneng.skyeye.util.ActorNames;
import cn.xiaoneng.skyeye.util.FieldConstants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by xuyang on 2016/8/8.
 */
public class NavigationSpaceConfig {

    private static Map<String, NavigationSpaceInfo> item = new HashMap<String, NavigationSpaceInfo>();

    //全部轨迹标签中展示的来访信息只包含：终端、来源、IP导航空间
    private static Set<String> spaceNames = new HashSet<>();

    private static NavigationSpaceConfig _instance;

    private NavigationSpaceConfig() {
        init();
    }

    public static NavigationSpaceConfig getInstance() {

        if (_instance == null) {
            _instance = new NavigationSpaceConfig();
        }

        return _instance;
    }

    public static void addItem(NavigationSpaceInfo info) {
        item.put(info.getName(),info);
    }

    /**
     * 加载每个导航空间预制采集的字段
     */
    public Map<String, NavigationSpaceInfo> getItem() {
        return item;
    }

    public Set<String> getSpaceNames() {
        return spaceNames;
    }

    /**
     * 获取导航空间的索引参数名
     * @param navSpaceName
     * @return
     */
    public String getNavID(String navSpaceName) {
        NavigationSpaceInfo info = item.get(navSpaceName);
        if(info == null) {
            return null;
        }
        return info.getIndexParam();
    }

//    public static String ChatOrder = "ChatOrder";

    /**
     * 预创建的导航空间信息
      */
    private static void init() {

        // 透明参数："address","nearby","speed"
        Set<String> lbsParams = new HashSet<>();
        lbsParams.add("lng");
        lbsParams.add("lat");
        lbsParams.add("country");
        lbsParams.add("province");
        lbsParams.add("city");
        item.put(ActorNames.LBS, new NavigationSpaceInfo(ActorNames.LBS, 1, lbsParams, null));
        spaceNames.add(ActorNames.LBS);

        // 咨询导航空间
        Set<String> chatParams = new HashSet<>();
        chatParams.add(FieldConstants.CONVERID);
        chatParams.add(FieldConstants.START_TIME);
        chatParams.add(FieldConstants.END_TIME);
        chatParams.add(FieldConstants.SUPPLIERS);
//        chatParams.add(FieldConstants.SUPPLIERID);
//        chatParams.add(FieldConstants.SUPPLIERNAME);
//        chatParams.add(FieldConstants.GROUPID);
        chatParams.add(FieldConstants.TEMPLATEID);
        chatParams.add(FieldConstants.STARTPAGEURL);
        chatParams.add(FieldConstants.STARTPAGETITLE);
        chatParams.add(FieldConstants.EVALUATION);
        chatParams.add(FieldConstants.SUMMARY);
        item.put(ActorNames.Chat, new NavigationSpaceInfo(ActorNames.Chat, 1, chatParams, FieldConstants.CONVERID));

//        // 咨询订单导航空间
//        Set<String> chatOrderParams = new HashSet<>();
//        chatParams.add(FieldConstants.CONVERID);
//        chatParams.add(FieldConstants.ORDERID);
//        chatParams.add(FieldConstants.KEYLEVEL);
//        item.put(ChatOrder, new NavigationSpaceInfo(ChatOrder, 1, chatOrderParams, null));

        //  透明参数 user-agent
        Set<String> terminalParams = new HashSet<>();
        terminalParams.add(FieldConstants.APPNAME);
        terminalParams.add(FieldConstants.DEVICE);
        terminalParams.add(FieldConstants.FLASH);
        terminalParams.add(FieldConstants.LANGUAGE);
        terminalParams.add(FieldConstants.SCREENSIZE);
        terminalParams.add(FieldConstants.BROWSER);
        terminalParams.add(FieldConstants.SYSTEM);
        item.put(ActorNames.Terminal, new NavigationSpaceInfo(ActorNames.Terminal, 1, terminalParams, null));
        spaceNames.add(ActorNames.Terminal);


        //  透明参数 ref
        Set<String> sourceParams = new HashSet<>();
        sourceParams.add(FieldConstants.REFFER);
        sourceParams.add(FieldConstants.SOURCE);
        sourceParams.add(FieldConstants.KEYWORD);
        sourceParams.add(FieldConstants.DOMAIN);
        item.put(ActorNames.Source, new NavigationSpaceInfo(ActorNames.Source, 1, sourceParams, null));
        spaceNames.add(ActorNames.Source);

        // 透明参数 ntalkerparam
        Set<String> webParams = new HashSet<>();
        webParams.add(FieldConstants.PAGEID);
        webParams.add(FieldConstants.URL);
        webParams.add(FieldConstants.TITLE);
        webParams.add(FieldConstants.KEYLEVEL);
        item.put(ActorNames.Web, new NavigationSpaceInfo(ActorNames.Web, 1, webParams, FieldConstants.PAGEID));


        Set<String> ipParams = new HashSet<>();
        ipParams.add(FieldConstants.IP);
        ipParams.add(FieldConstants.COUNTRY);
        ipParams.add(FieldConstants.PROVINCE);
        ipParams.add(FieldConstants.CITY);
        item.put(ActorNames.IP, new NavigationSpaceInfo(ActorNames.IP, 1, ipParams, FieldConstants.IP));
        spaceNames.add(ActorNames.IP);

        Set<String> productParams = new HashSet<>();
        productParams.add(FieldConstants.PRODUCTID);
        productParams.add(FieldConstants.PRODUCTNAME);
        productParams.add(FieldConstants.MARKETPRICE);
        productParams.add(FieldConstants.SITEPRICE);
        productParams.add(FieldConstants.IMAGEURL);
        productParams.add(FieldConstants.CATEGORYNAME);
        productParams.add(FieldConstants.BRANDNAME);
        item.put(ActorNames.Product, new NavigationSpaceInfo(ActorNames.Product, 1, productParams, FieldConstants.PRODUCTID));


        Set<String> orderParams = new HashSet<>();
        orderParams.add(FieldConstants.ORDERID);
        orderParams.add(FieldConstants.ORDERPRICE);
        item.put(ActorNames.Order, new NavigationSpaceInfo(ActorNames.Order, 1, orderParams, FieldConstants.ORDERID));


        Set<String> eventParams = new HashSet<>();
        eventParams.add(FieldConstants.PAGEID);
        eventParams.add(FieldConstants.EVENTID);
        eventParams.add(FieldConstants.EVENTHASH);  // hashtable:url中#后面的部分
        eventParams.add(FieldConstants.EVENTPOSITION);       // 页面标签位置
        eventParams.add(FieldConstants.EVENTPOSITIONINDEX);  // 页面标签位置索引
        eventParams.add(FieldConstants.EVENTVALUE);      // 事件值(加入购物车、提交订单)
        eventParams.add(FieldConstants.EVENTTYPE);       // 事件类型(click)
        item.put(ActorNames.Event, new NavigationSpaceInfo(ActorNames.Event, 1, eventParams, FieldConstants.EVENTID));

        // 透明参数 value time
//        String[] EventParams = new String[]{"node_id","node_name","event_source","event_lab"};
//        NavigationSpaceInfo Event = new NavigationSpaceInfo(ActorNames.Event, 1, EventParams, null);
//        item.put(ActorNames.Event,Event);
//
//        String[] SearchParams = new String[]{"keyword","scope"};
//        NavigationSpaceInfo Search = new NavigationSpaceInfo(ActorNames.Search, 1, SearchParams, null);
//        item.put(ActorNames.Search,Search);
//
//        String[] CustomParams = new String[]{};
//        NavigationSpaceInfo Custom = new NavigationSpaceInfo(ActorNames.Custom, 1, CustomParams, null);
//        item.put(ActorNames.Custom,Custom);

    }

    public NavigationSpaceInfo getNavigationSpaceInfo(String navSpaceName) {
        return item.get(navSpaceName);
    }

    public static void main(String[] args) {

        Map<String, NavigationSpaceInfo> item = NavigationSpaceConfig.getInstance().getItem();
        for (Map.Entry<String, NavigationSpaceInfo> entry : item.entrySet()) {

            System.out.println(entry.getKey());
        }
    }
}
