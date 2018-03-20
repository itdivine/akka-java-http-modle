package cn.xiaoneng.skyeye.bodyspace.message;

import java.io.Serializable;

/**
 * Created by liangyongheng on 2016/8/10 21:15.
 */
public class BodyNodeInfo implements Serializable {

    private String key;

    private String value;

    public BodyNodeInfo (String key,String value){

        this.key = key;

        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
