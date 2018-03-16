package cn.xiaoneng.skyeye.collector.model;


/**
 * 子页面 对应表t2d_subpage_config
 */
public class SubPage implements Comparable<SubPage> {

    public String siteid;
    public String urlreg;
    public String keyname;
    public int pageid;
    public int pagelevel;
    public int fatherid;


    /**
     * 按照fatherid从大到小排序  <br>
     * 当fatherid相同时，pageid按照从大到小排序
     */
    public int compareTo(SubPage o) {

        SubPage op = (SubPage) o;
        if (pageid < op.pageid)
            return 1;
        else if (pageid > op.pageid)
            return -1;
        else
            return 0;
    }

}
