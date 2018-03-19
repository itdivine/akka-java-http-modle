package cn.xiaoneng.skyeye.collector.config;

import cn.xiaoneng.skyeye.collector.actor.Collector;
import cn.xiaoneng.skyeye.collector.model.Source;
import cn.xiaoneng.skyeye.config.container.ConfigSourcemap;
import cn.xiaoneng.skyeye.config.container.SourceTypeContainer;
import cn.xiaoneng.skyeye.util.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Set;

public class SourcePage {

    protected final static Logger log = LoggerFactory.getLogger(SourcePage.class);

    //传入参数
    public String _ref = "";   //www.baidu.com/s?wd=%E9%92%BB%E7%9F%B3%E5%B0%8F%E9%B8%9F&eqid=ab25673f00006c8000000002576d0145
    public String _url = "";   //http://www.zbird.com
    private String _siteid;

    //返回结果
    public String _keyword = "";
    public String _source = "";       //baidu
    public String _sourcedomain = ""; //www.baidu.com
    public String _eqidValue = "";  //eqid==ab25673f00006c8000000002576d0145

    public String sourceTypeName = ""; //来源分类

    public static final String eqid = "eqid";

    /**
     * type：
     * ORGANIC=1,解析ref,是搜索引擎(自然流量)
     * PAY=2,解析着落页url,是付费流量
     */
    public static final int TYPE_URL = 2;
    public static final int TYPE_REF = 1;


    public SourcePage(String siteid, String ref, String url) {

        _url = url;
        _siteid = siteid;
        _ref = ref;
    }

    public void setSourcePage(ConfigSourcemap sourcemap, SourceTypeContainer sourceTypeContainer) {

        Source source = null;

        try {

            if (!Statics.isNullOrEmpty(_ref))
                _sourcedomain = Statics.regex_match(_ref, COMMON.DOMAIN_NAME_REGEX);

            //取得关于url、ref的正则配置信息
            Map<Integer, Set<Source>> querys = sourcemap.getConfigSourcemap(_siteid);

            //1.第一优先级: 解析url中Ntalker参数
            _source = Statics.regex_match(_url, "nsource=([^&]*)");
            if (!Statics.isNullOrEmpty(_source)) {
                String sourcedomain = Statics.regex_match(_url, "nvalues=([^&]*)");
                if (!Statics.isNullOrEmpty(sourcedomain))
                    _sourcedomain = sourcedomain;
                return;
            }

            //2.第二优先级: 解析url
            log.info("_source=" + _source + " type=" + TYPE_URL);
            if (Statics.isNullOrEmpty(_source) && querys.containsKey(TYPE_URL))
                source = getSourceByUrl(querys.get(TYPE_URL));


            //3.第三优先级: 解析ref
            if (source == null && querys.containsKey(TYPE_REF))
                source = getsourceByRef(querys.get(TYPE_REF));

            //4.取关键词
            if (source != null) {
                _sourcedomain = source.domain;
                _source = source.ename;
                _keyword = getKeyWord(source);
                if (_keyword == null)
                    _keyword = "";

                if (sourceTypeContainer != null) {

                    String typeName = sourceTypeContainer.getTypeNameByid(_siteid, source.source_type_id);

                    sourceTypeName = typeName;
                }

                log.info("source=" + source.ename);
            }

            if (_source != null && _source.contains("baidu") && _ref != null && _ref.contains(eqid) && Statics.isNullOrEmpty(_keyword)) {
                _eqidValue = Statics.getParamFromUrl(_ref, eqid);
                log.info("eqid=" + _eqidValue + " ref=" + _ref + " siteId=" + _siteid);

                getBaiduKeyword();
            }


            if (Statics.isNullOrEmpty(_source)) {
                _source = COMMON.INPUT;
                log.info("SourcePage :  input " + _ref + " " + _url);
            }

            //5.判断input link
            if (_source.equals(COMMON.INPUT))
                setIsLink();

        } catch (Exception e) {
            log.info("Exception :" + e.toString());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }
    }

    private void getBaiduKeyword() {

        if (Statics.isNullOrEmpty(COMMON.referer_url)) {
            log.info("RefererUrl is null,  RefererUrl=" + COMMON.referer_url);
            return;
        }

        //当前企业是否开通百度关键词   --- 企业配额信息中
//        if (Collector.getLastKeyWordCount(_siteid) <= 0) {
//            return;
//        }

        try {

            String url = COMMON.referer_url.replace("[eqid]", _eqidValue);
            String info = HttpSender.getInfos(url);

            if (Statics.isNullOrEmpty(info))
                return;
            JSONObject obj = JSON.parseObject(info);
            if (obj.containsKey("success") && obj.getBoolean("success")) {
//                Collector.upadteLastKeyWordCount(_siteid);
                String refererKeyword = obj.getString("wd");
                if (Statics.isNullOrEmpty(refererKeyword)) {
                    return;
                }

                _keyword = URLDecoder.decode(refererKeyword, "utf8");

                if (_keyword == null)
                    _keyword = "";

                if (_keyword.length() > 50)
                    _keyword = _keyword.substring(0, 50);


//                if(!Statics.isNullOrEmpty(_ra.keyword) && timeout>0 && !Statics.isNullOrEmpty(_ra.eqid))
//                    JedisUtil.getInstance().setKeyword(_ra.eqid, _ra.keyword, timeout);
            }


        } catch (Exception e) {
            log.error("Exception :" + e.toString());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.error(er[i].toString());
            }
        }
    }


    private void setIsLink() {

        if (Statics.isNullOrEmpty(_sourcedomain))
            return;

        try {
            //非搜索引擎，是友情链接
            String CurTopDomainName = Statics.regex_match(_url, COMMON.DOMAIN_NAME_REGEX);

            if (_sourcedomain.indexOf(CurTopDomainName) < 0) {
                _source = COMMON.LINK;

                //判断是否本网站的子域名(DB中配置)，是则修改成input
                Set<String> domainSet = SiteDomainContainer.getInstance().getConfigSiteDomain(_siteid);
                if (domainSet == null || domainSet.size() == 0)
                    return;

                if (domainSet.contains(_sourcedomain))
                    _source = COMMON.INPUT;
            }

        } catch (Exception e) {
            log.info("Exception: " + e.toString());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }
    }

    private Source getSourceByUrl(Set<Source> sources) {

        if (sources == null || sources.size() == 0)
            return null;

        try {
            for (Source source : sources) {
                if (Statics.isregex_match(_url, source.url_reg)) {
                    return source;
                }
            }

        } catch (Exception e) {
            log.info("Exception: " + e.toString());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }
        return null;
    }

    private String getKeyWord(Source source) {

        if (Statics.isNullOrEmpty(_ref))
            return null;

        try {
            String keyName = source.ref_word_rex;
            String code = source.encode;
            if (Statics.isNullOrEmpty(code))
                code = "utf8";

            String keywordValue = Statics.regex_match(_ref, keyName + "=([^&]*)");
            if (keywordValue.isEmpty())
                return null;

            int index = keywordValue.indexOf("=");
            if (index < 0)
                return null;

            String keyword = keywordValue.substring(index + 1);

            if (!keyword.isEmpty() && keyword.length() > 0)  //搜索到关键词
            {
                if (_ref.toLowerCase().contains("gb2312") || _ref.toLowerCase().contains("gbk")) {
                    keyword = URLDecoder.decode(keyword, "gbk");
                } else if (_ref.toLowerCase().contains("utf-8")) {
                    keyword = URLDecoder.decode(keyword, "utf-8");
                } else if (_ref.toLowerCase().contains("utf8")) {
                    if (source.ename.equals("google")) //谷歌搜索结果需要两次解码后转换为gbk编码
                    {
                        keyword = URLDecoder.decode(keyword);
                        keyword = URLDecoder.decode(keyword);
                        keyword = EncodingUtil.EnocodeSwitch("utf-8", "gbk", keyword);
                    } else {
                        keyword = URLDecoder.decode(keyword, "utf8");
                    }
                } else {
                    String thekeyword = URLDecoder.decode(keyword, code);

                    if (MessyCodeCheck.isMessyCode(thekeyword) && source.ename.equals("baidu") && _ref.contains("word="))
                        keyword = URLDecoder.decode(keyword, "utf-8");
                    else
                        keyword = thekeyword;
                }

                return keyword;   //new String(keyword.getBytes("ISO-8859-1"),"UTF-8");
            }

        } catch (Exception e) {
            log.info("Exception: " + e.toString() + " Source=" + source.toString());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }
        return null;
    }

    private Source getsourceByRef(Set<Source> sources) {

        if (sources == null || sources.size() == 0)
            return null;

        try {
            for (Source source : sources) {
                if (_sourcedomain.contains(source.domain)) {
                    return source;
                }
            }

        } catch (Exception e) {
            log.info("Exception :" + e.toString());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }
        return null;
    }

    public static void main(String[] args) {

        String param = "%BA%E9%B1%A4%7C%BA%E9%B1%A4%D1%A7%D0%A3%7C%D0%A1%D3%EF%D6%D6%C5%E0%D1%B5%7C%B5%C2%D3%EF%D1%A7%CF%B0%7C%BA%E9%B1%A4%B5%C2%D3%EF%C5%E0%D1%B5%B0%E0%7C%BA%A3%B5%ED%C7%F8%B5%C2%D3%EF%C5%E0%D1%B5%D1%A7%D0%A3%7C%B5%C2%B9%FA%B0%C2%B5%D8%";

        try {
            param = param.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
            System.out.println(URLDecoder.decode(param, "gb2312"));

            System.out.println(Statics.decode(param, "gb2312"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
