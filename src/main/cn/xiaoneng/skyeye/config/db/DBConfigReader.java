package cn.xiaoneng.skyeye.config.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by liangyongheng on 2016/11/4 17:54.
 */

public class DBConfigReader {

    protected final static Logger log = LoggerFactory.getLogger(DBConfigReader.class);

    /**
     * 读取mysql配置信息
     *
     * @return
     */
    public static String[] readMySqlConfig() {

        String[] message = new String[7];
        Properties prop = new Properties();

        try {

            ClassLoader classLoader = DBConfigReader.class.getClassLoader();

            classLoader.getResourceAsStream("db.properties");
//            File file = new File("./db.properties");
//            file.length();

            InputStream in = new BufferedInputStream(classLoader.getResourceAsStream("./db.properties"));

            prop.load(in);
            message[0] = prop.getProperty("mysql_url");
            message[1] = prop.getProperty("mysql_user");
            message[2] = prop.getProperty("mysql_password");
            message[3] = prop.getProperty("mysql_driver");

            message[4] = prop.getProperty("dbUrl1");
            message[5] = prop.getProperty("dbMaxActive1");
            message[6] = prop.getProperty("dbMinIdle1");

            System.out.println(message[0]);

        } catch (Exception e) {

            log.error(e.getMessage());
        }

        return message;

    }

    public static void main(String[] args) {

        new DBConfigReader().readMySqlConfig();
    }
}
