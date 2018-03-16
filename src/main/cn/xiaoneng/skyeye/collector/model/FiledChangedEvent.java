package cn.xiaoneng.skyeye.collector.model;

import cn.xiaoneng.skyeye.collector.message.FieldMessage;

import java.io.Serializable;

/**
 * Created by liangyongheng on 2016/8/6 21:29.
 */
public class FiledChangedEvent implements Serializable{

    private final FieldMessage message;

    public FiledChangedEvent(FieldMessage message) {

       this.message = message;
    }

    public FieldMessage getMessage() {
        return message;
    }
}
