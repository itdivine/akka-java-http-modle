package cn.xiaoneng.skyeye.collector.model;

public class Source {


    public String siteid = "";  //空表示全网站通用；否则为某一网站专用
    public String ename = ""; //来源英文名字
    public String cname = ""; //来源的中文名字
    public String domain = ""; //完整域名
    public String ref_word_rex = ""; //信息中关键词的正则
    public String encode = ""; //中关键词的编码
    public String url_reg = ""; //着落页来源的正则
    public int pk_config_source; //当前节点id
    public int source_type_id;  //父节点id
    public int type; //值为1时，表示通过ref解析来源、关键词；值为2时，表示先通过ref解析来源、关键词，然后通过着落页进一步解析来源。type=2的值域会覆盖type=1.

    public String sourceexplain;

    public String source_logo = "";

    public String wap_logo = "";

    @Override
    public String toString() {
        return "Source{" +
                "siteid='" + siteid + '\'' +
                ", ename='" + ename + '\'' +
                ", cname='" + cname + '\'' +
                ", domain='" + domain + '\'' +
                ", ref_word_rex='" + ref_word_rex + '\'' +
                ", encode='" + encode + '\'' +
                ", url_reg='" + url_reg + '\'' +
                ", pk_config_source=" + pk_config_source +
                ", source_type_id=" + source_type_id +
                ", type=" + type +
                ", sourceexplain='" + sourceexplain + '\'' +
                ", source_logo='" + source_logo + '\'' +
                ", wap_logo='" + wap_logo + '\'' +
                '}';
    }
}
