package cn.xiaoneng.skyeye.bodyspace.message;

/**
 * Created by liangyongheng on 2016/8/17 16:19.
 */
public class IdCommand {

    private String id;

    private boolean isRelatedNode;

    public IdCommand(String id, boolean isRelatedNode) {

        this.id = id;

        this.isRelatedNode = isRelatedNode;

    }

    public String getId() {
        return id;
    }

    public boolean isRelatedNode() {
        return isRelatedNode;
    }
}
