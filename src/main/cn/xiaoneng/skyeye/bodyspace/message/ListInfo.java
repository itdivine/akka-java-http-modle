package cn.xiaoneng.skyeye.bodyspace.message;

import java.io.Serializable;

/**
 * 请求列表
 * Created by liangyongheng on 2016/8/2 15:04.
 */
public class ListInfo implements Serializable{
    private int pageSize;

    private int per_page;

    private String msgid;

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPer_page() {
        return per_page;
    }

    public void setPer_page(int per_page) {
        this.per_page = per_page;
    }

    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }
}
