package cn.xiaoneng.skyeye.collector.config;//package cn.xiaoneng.skyeye.collector.config;
//
//import cn.xiaoneng.skyeye.config.container.ConfigSubPage;
//import cn.xiaoneng.skyeye.config.container.KeyPageContainer;
//import cn.xiaoneng.skyeye.util.Statics;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.List;
//
//
//public class ItemConfigKeyAction implements Serializable {
//
//    private static final long serialVersionUID = 1L;
//    public String urlreg = "";    //依据URL正则判断
//    public String keyname = "";
//    public int keylevel = -1;
//    public int keyid = -1;  //t2d_keypage_config.id
//    public int subpageid = -1;  //子页面ID t2d_subpage_config.id
//    public String siteid = "";
//    public int issave;
//    public String orderidRegex = "";  //订单号的正则
//    public int pk_keypage;
//
//    //取值范围:list”, ”item”、“cart“、“order“、“pay“、“paysucess“、”register”、 ”registersucess”，
//    //分别对应商品列表、商品详情、购物车页、 订单填写页、订单提交成功页、支付成功、开始注册、注册成功
//    public String pagetype = "";
//
//    protected final static Logger log = LoggerFactory.getLogger(ItemConfigKeyAction.class);
//
//    public static ItemConfigKeyAction getKeyItem(String siteid, String actionurl, KeyPageContainer container, ConfigSubPage subPage) {
//
//        if (siteid == null || actionurl == null)
//            return null;
//
//        try {
//            //生成动作名称信息
//            List<ItemConfigKeyAction> ica = container.getConfigKeyAction(siteid);
//            if (ica == null || ica.size() <= 0)
//                return null;
//
//            for (int i = 0; i < ica.size(); i++) {
//                ItemConfigKeyAction item = ica.get(i);
//                if (item == null)
//                    continue;
//
//                if (Statics.isregex_match(actionurl, item.urlreg)) {
////                    item.subpageid = subPage.getSubKeyId(siteid, actionurl, item.keylevel);
//
//                    log.warn("keyaction info " + item.keyname + " " + item.keylevel + " " + item.subpageid);
//
//                    return item;
//                }
//            }
//        } catch (Exception e) {
//            log.warn("Exception :" + e.toString());
//            StackTraceElement[] er = e.getStackTrace();
//            for (int i = 0; i < er.length; i++) {
//                log.info(er[i].toString());
//            }
//        }
//        return null;
//    }
//
//
//    public static ItemConfigKeyAction getKeyItem(String siteid, int level) {
//
//        if (siteid == null)
//            return null;
//
//        try {
//            //生成动作名称信息
//            List<ItemConfigKeyAction> ica = KeyPageContainer.getInstance().getConfigKeyAction(siteid);
//            if (ica == null || ica.size() <= 0)
//                return null;
//
//            for (int i = 0; i < ica.size(); i++) {
//                ItemConfigKeyAction item = ica.get(i);
//                if (item == null)
//                    continue;
//
//                if (item.keylevel == level) {
//                    log.warn("keyaction info " + item.keyname + " " + item.keylevel);
//
//                    return item;
//                }
//            }
//        } catch (Exception e) {
//            log.warn("Exception :" + e.toString());
//            StackTraceElement[] er = e.getStackTrace();
//            for (int i = 0; i < er.length; i++) {
//                log.info(er[i].toString());
//            }
//        }
//        return null;
//    }
//
//    public static void main(String[] args) {
//
//        String siteid = "kf_9800";
//        String actionurl = "http://www.vip.com/detail-7268-997701.html";
//
//        try {
//            List<String> ica = new ArrayList<String>();
//            ica.add("checkout\\.vip\\.com\\/te2\\/order\\.php");
//            ica.add("checkout\\.vip\\.com\\/te2\\/\\? ");
//            ica.add("cart\\.vip\\.com");
//            ica.add("detail-.*\\.html");
//            ica.add("www.vip.com(/)?$|www.vip.com/?");
//            ica.add("/register");
//            ica.add("www\\.vip\\.com(/)?$|www\\.vip\\.com/\\?");
//
//            for (int i = 0; i < ica.size(); i++) {
//                if (Statics.isregex_match(actionurl, ica.get(i))) {
//                    System.out.println(ica.get(i));
//                }
//            }
//
//            String s = "detail.*\\.html";
//            if (Statics.isregex_match(actionurl, s))
//                System.out.println(true);
//        } catch (Exception e) {
//            log.warn("Exception :" + e.toString());
//            StackTraceElement[] er = e.getStackTrace();
//            for (int i = 0; i < er.length; i++) {
//                log.info(er[i].toString());
//            }
//        }
//    }
//
//}
