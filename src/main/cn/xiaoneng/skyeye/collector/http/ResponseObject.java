package cn.xiaoneng.skyeye.collector.http;

import java.util.logging.Logger;

/**
 * Created by liangyongheng on 2016/9/1 11:35.
 */
public abstract class ResponseObject {

    protected String _siteId;
    protected String _responseInfo;
    protected static Logger log = Logger.getLogger(ResponseObject.class.getName());


    /**
     * 内存中是否已经有相关信息
     * @return
     */
    public abstract boolean isRedisContain();

    public abstract String getUrl();

    public abstract String getInfo(String url);

    public abstract void cache2Redis();

//    public abstract void merge2RedisUser(RawAction ra);
}
