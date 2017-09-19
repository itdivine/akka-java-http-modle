package cn.xiaoneng.skyeye.util;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

/**
 * Created by xuyang on 2016/8/5.
 */
public class DocumentMessage extends BaseMessage implements Serializable {

    /**
     * 这里不用类，使用对象的JSON
     * 优点：DocumentMessage可以通用
     */
    private String document;


    public DocumentMessage(String operation, long timeToLive, String document, String msgId) {
        super(msgId, operation, timeToLive);
        this.document = document;
    }

    public String getDocument() {
        return document;
    }

    public JSONObject toJson() {
        return JSON.parseObject(document);
    }

}
