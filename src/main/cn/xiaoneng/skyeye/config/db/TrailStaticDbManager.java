package cn.xiaoneng.skyeye.config.db;//package cn.xiaoneng.skyeye.config.db;
//
//
//import org.apache.commons.pool.impl.GenericObjectPool;
//import org.apache.commons.dbcp.ConnectionFactory;
//import org.apache.commons.dbcp.PoolingDriver;
//import org.apache.commons.dbcp.PoolableConnectionFactory;
//import org.apache.commons.dbcp.DriverManagerConnectionFactory;
//
//import java.sql.*;
//import java.util.concurrent.locks.ReentrantReadWriteLock;
//
//
//
//public class TrailStaticDbManager {
//    private static TrailStaticDbManager _instance;
//    private boolean _hasInit = false;
//    private String poolConStr = "jdbc:apache:commons:dbcp:tatawe2";
//
//    private static ReentrantReadWriteLock _locksingle = new ReentrantReadWriteLock();
//
//    private TrailStaticDbManager()
//    {
//
//    }
//
//    public static TrailStaticDbManager getInstance()
//    {
//        if(_instance ==null)
//        {
//            try{
//                _locksingle.writeLock().lock();
//
//                if(_instance==null)
//                {
//                    _instance = new TrailStaticDbManager();
//                }
//            }
//            catch(Exception e)
//            {
//
//            }
//            finally
//            {
//                _locksingle.writeLock().unlock();
//            }
//        }
//        return _instance;
//    }
//    public Connection getConnection()throws Exception
//    {
//        return DriverManager.getConnection(poolConStr);
//    }
//
//    public void init(String driverName,String url,int maxActive,int minIdle) throws Exception
//    {
//        init(driverName,url,maxActive,minIdle,10000);
//    }
//
//    public void init(String driverName,String url,int maxActive,int minIdle,int maxwait) throws Exception
//    {
//        if(_hasInit)
//            return;
//
//        _hasInit = true;
//
//        Class.forName(driverName);
//        //
//        // First, we'll need a ObjectPool that serves as the
//        // actual pool of connections.
//        //
//        // We'll use a GenericObjectPool instance, although
//        // any ObjectPool implementation will suffice.
//        //
//        GenericObjectPool connectionPool = new GenericObjectPool(null);
//        connectionPool.setMinIdle(minIdle);  //池个数上升: 如果池中空闲连接个数少于minIdle，则服务器需要新连接时，池需要到db中创建新连接
//        connectionPool.setMaxActive(maxActive);
//        connectionPool.setMaxIdle(minIdle);  //池个数下降: 在睡眠池中，最大保持的连接个数
//        connectionPool.setMaxWait(maxwait); //若在对象池空时获取链接，则最大等待多少毫秒
//
//
//        //
//        // Next, we'll createEVSWithResponse a ConnectionFactory that the
//        // pool will use to createEVSWithResponse Connections.
//        // We'll use the DriverManagerConnectionFactory,
//        // using the connect string passed in the command line
//        // arguments.
//        //
//        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url,null);
//
//        //
//        // Now we'll createEVSWithResponse the PoolableConnectionFactory, which wraps
//        // the "real" Connections created by the ConnectionFactory with
//        // the classes that implement the pooling functionality.
//        //
//        @SuppressWarnings("unused")
//        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);
//
//        //
//        // Finally, we createEVSWithResponse the PoolingDriver itself...
//        //
//        Class.forName("org.apache.commons.dbcp.PoolingDriver");
//        PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
//
//        //
//        // ...and register our pool with it.
//        //
//        driver.registerPool("tatawe2",connectionPool);
//        //
//        // Now we can just use the connect string "jdbc:apache:commons:dbcp:ephchat"
//        // to access our pool of Connections.
//        //
//        poolConStr = "jdbc:apache:commons:dbcp:tatawe2";
//
//    }
//
//}