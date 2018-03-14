package cn.xiaoneng.skyeye;

import com.typesafe.config.Config;
import akka.actor.Address;

/**
 * Created by XuYang on 2017/8/27.
 */
public class AppConfig {

    // actorSystem的名称
    public String systemName;
    // 主机对外暴露ip地址
    public String host;
    // 主机对外暴露端口号
    public int port;
    // 总线master节点地址
    public Address masterAddress;
    // 远程访问actor的默认过期时间
    public Long timeout;

    public AppConfig(String systemName, String host, int port, Address masterAddress, Long timeout) {
        this.systemName = systemName;
        this.host = host;
        this.port = port;
        this.masterAddress = masterAddress;
        this.timeout = timeout;
    }
}
