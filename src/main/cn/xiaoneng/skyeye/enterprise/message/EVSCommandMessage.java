package cn.xiaoneng.skyeye.enterprise.message;

import cn.xiaoneng.skyeye.enterprise.bean.EVSInfo;
import cn.xiaoneng.skyeye.util.BaseMessage;
import cn.xiaoneng.skyeye.util.Operation;

import java.io.Serializable;


/**
 * Created by Administrator on 2016/7/30.
 */
public class EVSCommandMessage extends BaseMessage implements Serializable {

    private EVSInfo rawEVS;

    public EVSInfo getEVSInfo() {
        return rawEVS;
    }

    public EVSCommandMessage(String operation, long timeToLive, EVSInfo rawEVS) {
        super(null, operation, timeToLive);
        this.rawEVS = rawEVS;
    }

    @Override
    public String toString() {
        return "EVSCommandMessage{" +
                "rawEVS=" + rawEVS +
                " super=" + super.toString() +
                '}';
    }

    public static void main(String[] args) {
        EVSCommandMessage getEVS = new EVSCommandMessage(Operation.GET, 10, null);
        System.out.println(getEVS);

    }

}
