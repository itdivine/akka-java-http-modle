package cn.xiaoneng.nskyeye.access;

import com.typesafe.config.Config;
import akka.actor.Address;

/**
 * Created by XuYang on 2017/8/27.
 */
public class AccessConfig {

    // actorSystem的名称
    public String systemName;
    // akka配置文件对象
    public Config config;
    // 主机对外暴露ip地址
    public String host;
    // 主机对外暴露端口号
    public int port;
    // 总线master节点地址
    public Address masterAddress;
    // 远程访问actor的默认过期时间
    public Long timeout;

    public AccessConfig(String systemName, Config config, String host, int port, Address masterAddress, Long timeout) {
        this.systemName = systemName;
        this.config = config;
        this.host = host;
        this.port = port;
        this.masterAddress = masterAddress;
        this.timeout = timeout;
    }
}
