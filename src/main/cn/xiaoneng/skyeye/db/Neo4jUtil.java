package cn.xiaoneng.skyeye.db;

import org.neo4j.driver.v1.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.neo4j.driver.v1.Values.parameters;

/**
 * Document: https://neo4j.com/docs/developer-manual/current/drivers/
 *
 *
 * Created by xuyang on 2016/8/26.
 */
public class Neo4jUtil {

    private static Driver driver;

    private static Neo4jUtil _instance;

    private static ReentrantReadWriteLock _lock = new ReentrantReadWriteLock();

    protected final static Logger log = LoggerFactory.getLogger(Neo4jUtil.class);

    private Neo4jUtil(){}

    public static Neo4jUtil getInstance(){

        if(_instance == null) {
            try {
                _lock.writeLock().lock();
                if(_instance == null)
                {
                    _instance = new Neo4jUtil();
                }
            } catch (Exception e) {
                log.warn("Exception " + e.toString());
            }finally {
                _lock.writeLock().unlock();
            }
        }

        return _instance;
    }


    /**
     * 初始化Neo4j，创建Driver
     *
     * @param url  bolt://localhost
     * @param maxSession  100
     * @param userName  neo4j
     * @param password  xuyang
     */
    public void init(String url, int maxSession, String userName, String password) {

        try {
//        boolean hasPassword = password != null && !password.isEmpty();
//        AuthToken token = hasPassword ? AuthTokens.basic(userName, password) : AuthTokens.none();
//        Config config = Config.build().withMaxSessions( maxSession ).withEncryptionLevel(Config.EncryptionLevel.REQUIRED).toConfig();
//        driver = GraphDatabase.driver(url, token, config);

            Config noSSL = Config.build().withEncryptionLevel(Config.EncryptionLevel.NONE).toConfig();
            driver = GraphDatabase.driver(url,AuthTokens.basic(userName,password),noSSL);


        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        }
    }


    /**
     * 1.创建Driver
     *
     * MaxSessions: There is no limit to how many sessions that can be created,
     *              but a maximum limits how many sessions will be buffered after they are returned to the session pool.
     *
     * EncryptionLevel: Configure driver to require TLS encryption
     *
     * @return Driver
     */
    public Driver getDriver() {
        return driver;
    }


    /**
     * 2.创建Session
     *
     * The driver has a session pool which can be configured.
     * Sessions are returned to the pool when they are closed.
     * It is important to close sessions, to allow them to return to the pool and be reused.
     *
     * @return Session
     */
    public Session getSession() {
        return driver.session();
    }

    public static void main(String[] args) {

        // 初始化图数据库
        //Neo4jUtil.getInstance().init(config.getString("neo4j_url"), config.getInt("neo4j_maxSession"), config.getString("neo4j_userName"), config.getString("neo4j_password"));

        String url = "bolt://192.168.30.215:10015";
        String userName = "neo4j";
        String password = "111111";

//        String url = "bolt://192.168.30.230";
//        String userName = "neo4j";
//        String password = "xuyang";

        Driver driver = GraphDatabase.driver(url, AuthTokens.basic( userName, password ) );
        Session session = driver.session();

        session.run( "CREATE (a:Person {name: {name}, title: {title}})",
                parameters( "name", "Arthur", "title", "King" ) );

        StatementResult result = session.run( "MATCH (a:Person) WHERE a.name = {name} " +
                        "RETURN a.name AS name, a.title AS title",
                parameters( "name", "Arthur" ) );

        while ( result.hasNext() )
        {
            Record record = result.next();
            System.out.println( record.get( "title" ).asString() + " " + record.get( "name" ).asString() );
        }

        session.close();
        driver.close();


    }


}
