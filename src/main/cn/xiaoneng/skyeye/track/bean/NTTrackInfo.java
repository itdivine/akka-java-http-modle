package cn.xiaoneng.skyeye.track.bean;

import akka.actor.ActorRef;
import cn.xiaoneng.skyeye.util.ActorNames;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xuyang on 2016/8/15.
 */
public class NTTrackInfo implements Serializable {

    protected final static Logger log = LoggerFactory.getLogger(NTTrackInfo.class);

    /**
     * 名字 = NT主体空间名字 （唯一）
     */
    private String name = ActorNames.NT_BODYSPACE;

    /**
     * 状态 = 导航空间状态  [-1释放 0关闭 1开启]
     */
    private int status = 1;


    /**
     * 标识每个人来访了几次
     * 每次来访创建了几个访问关系
     * key: nt_id
     * key: sid
     * value: Records
     */
    private Map<String, LinkedHashMap<String, List<ActorRef>>> nt2sid2Record = new HashMap<>();

    /**
     * 标识每个人来访了几次
     * key: nt_id
     * value: sids
     */
//    private Map<String, List<String>> nt2sid = new HashMap<>();

    /**
     * 标识每次来访创建了几个访问关系
     * key: sid
     * value: Records
     */
//    private Map<String, List<ActorRef>> sid2Record = new HashMap<>();


    public NTTrackInfo(String name, int status) {
        this.name = name;
        this.status = status;
    }

    public String toJSONString() {

        return JSONObject.toJSONString(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Map<String, LinkedHashMap<String, List<ActorRef>>> getNt2sid() {
        return nt2sid2Record;
    }
}
