package cn.xiaoneng.skyeye.config.model;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by liangyongheng on 2016/12/13 11:47.
 */
public class SourceTypeModel  {

    private int source_type_id;

//    @JSONField(serialize = false)
//    private String userid;

    private String siteid;

    private String typename;

    private String typeexplain;

//    @JSONField(serialize = false)
//    private String illustration;

    private int pid;

    @JSONField(serialize = false)
    private int isdel;


    public static String tableName = "trail_config_sourcetype";



    public int getSource_type_id() {
        return source_type_id;
    }

    public void setSource_type_id(int source_type_id) {
        this.source_type_id = source_type_id;
    }

//    public String getUserid() {
//        return userid;
//    }
//
//    public void setUserid(String userid) {
//        this.userid = userid;
//    }

    public String getSiteid() {
        return siteid;
    }

    public void setSiteid(String siteid) {
        this.siteid = siteid;
    }

    public String getTypename() {
        return typename;
    }

    public void setTypename(String typename) {
        this.typename = typename;
    }
//
//    public String getIllustration() {
//        return illustration;
//    }
//
//    public void setIllustration(String illustration) {
//        this.illustration = illustration;
//    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getDr() {
        return isdel;
    }

    public void setDr(int isdel) {
        this.isdel = isdel;
    }

    public String getTypeexplain() {
        return typeexplain;
    }

    public void setTypeexplain(String typeexplain) {
        this.typeexplain = typeexplain;
    }
}
