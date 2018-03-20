package cn.xiaoneng.skyeye.bodyspace.message;

/**
 * Created by liangyongheng on 2016/9/23 11:10.
 */
public class PVDataStatus {

    /**
     * 节点关系发生变化(关系写库)
     */
    public static final int RELATION_CHANGE = 0;

    /**
     * 添加节点(节点和关系都需写库)
     */
    public static final int NODE_ADD = 1;

    /**
     * 节点和关系都没发生变化(无需写库)
     */
    public static final int NO_CHANGE = 2;
}
