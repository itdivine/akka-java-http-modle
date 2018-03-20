package cn.xiaoneng.skyeye.navigation.message;

import cn.xiaoneng.skyeye.util.BaseMessage;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by liangyongheng on 2016/10/21 19:57.
 */
public class NavSpaceFieldMsg extends BaseMessage {

    private Set<String> fieldList = new HashSet<>();

    private String spaceName;

    public NavSpaceFieldMsg(String spaceName,Set<String> fieldList) {

        this.spaceName = spaceName;

        this.fieldList = fieldList;

    }

    public Set<String> getFieldList() {
        return fieldList;
    }

    public void setFieldList(Set<String> fieldList) {
        this.fieldList = fieldList;
    }

    public String getSpaceName() {
        return spaceName;
    }

    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
    }
}
