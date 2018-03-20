package cn.xiaoneng.skyeye.kafka.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatOrder implements Serializable {

    public String nt;
    public String cs_template_id; //模板id（接待组id）
    public List<Map<String,String>> rs_list = new ArrayList(); //责任客服list（rs_cs_id 客服id,rs_cs 客服名称）
    public String vs_startpage_url; //咨询发起页
    public long conversation_time; //咨询发生的时间
    public int keylevel; //咨询页面
    public String keyname;

    //订单来访数据
    public long order_time; //订单发生的时间
    public int order_type; //1:当天订单 2:24小时订单  3:三天内订单
    public String order_id;
    public String order_price;
    public long order_level; //6 订单填写完成  7 订单支付
    public String ip;
    public String country;
    public String province;
    public String city;
    public String keyword;
    public String source;
    public String ref;
    public String tml;





}
