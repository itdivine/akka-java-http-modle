package cn.xiaoneng.skyeye.config.container;

import java.io.Serializable;

/**
 * Created by liangyongheng on 2017/3/1 17:30.
 */
public class CustomAttrs implements Serializable {

    public static String tableName = "trail_custom_attr";

    private int id;

    private String siteid;

    private int type;

    private int length;

    private String ename;

    private String cname;

    private int isdel;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSiteid() {
        return siteid;
    }

    public void setSiteid(String siteid) {
        this.siteid = siteid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getEname() {
        return ename;
    }

    public void setEname(String ename) {
        this.ename = ename;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public int getDr() {
        return isdel;
    }

    public void setDr(int isdel) {
        this.isdel = isdel;
    }


}
