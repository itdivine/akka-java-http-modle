package cn.xiaoneng.skyeye.temple;

import cn.xiaoneng.skyeye.util.BaseMessage;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/8/1.
 */
public class ListMessage extends BaseMessage implements Serializable {

    private int page = 1;
    private int per_page = 30;

    public ListMessage(int page, int per_page, long timeToLive) {
        super(null, null, timeToLive);
        this.page = page;
        this.per_page = per_page;
    }

    public int getPage() {
        return page;
    }

    public int getPer_page() {
        return per_page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setPer_page(int per_page) {
        this.per_page = per_page;
    }
}
