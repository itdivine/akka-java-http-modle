package cn.xiaoneng.skyeye.bodyspace.message;

import cn.xiaoneng.skyeye.util.BaseMessage;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by liangyongheng on 2016/10/26 10:36.
 */
public class BodySpaceFieldMsg extends BaseMessage {

    private Set<String> spaceFieldSet = new HashSet<>();

    public BodySpaceFieldMsg(Set<String> fieldSet) {

        this.spaceFieldSet = fieldSet;
    }

    public Set<String> getSpaceFieldSet() {
        return spaceFieldSet;
    }

    public void setSpaceFieldSet(Set<String> spaceFieldSet) {
        this.spaceFieldSet = spaceFieldSet;
    }
}
