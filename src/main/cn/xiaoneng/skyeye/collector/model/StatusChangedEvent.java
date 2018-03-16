package cn.xiaoneng.skyeye.collector.model;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

/**
 * Created by liangyongheng on 2016/8/6 21:20.
 */
public class StatusChangedEvent implements Serializable{

    private final JSONObject json;

    public StatusChangedEvent(JSONObject json) {

        this.json = json;
    }

    public JSONObject getJson() {
        return json;
    }
}
