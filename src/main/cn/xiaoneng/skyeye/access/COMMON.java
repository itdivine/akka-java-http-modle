package cn.xiaoneng.skyeye.access;

import com.typesafe.config.Config;


public class COMMON {

	//Status value list:  -1释放 0关闭 1开启
	public static final int ON = 1;
	public static final int OFF = 0;
	public static final int CLEAR = -1;

	//KAFKA
	public static int KAFKA_ServiceSize = 10;
	public static String KAFKA_BROKERS = "192.168.30.210:9092";
	public static String KAFKA_TOPIC = "input_hxz_20170314";

	//AKKA
	public static String appUrl = "0.0.0.0";
	public static int appPort = 8080;
	public static String systemName = "NSkyEye";
	public static String clusterAddr = "akka.tcp://NSkyEye@127.0.0.1:2551";
	public static String appConfigFileName = "App2";
	public static String httpConfigFileName = "httpModel";

	//Neo4j
	public static int neo4j_maxSession = 200;
	public static String neo4j_userName = "neo4j";
	public static String neo4j_password = "xuyang";
	public static String neo4j_url = "bolt://192.168.30.230";

	//Actor Count
	public static int collectorHandlerCount = 100;

	public static String kpi_url = "";

	public static void read(Config prop) {
		appUrl = prop.getString("appUrl");
		appPort = Integer.parseInt(prop.getString("appPort"));

		systemName = prop.getString("systemName");
		clusterAddr = prop.getString("clusterAddr");

		appConfigFileName = prop.getString("appConfigFileName");
		httpConfigFileName = prop.getString("httpConfigFileName");

		neo4j_maxSession = Integer.parseInt(prop.getString("neo4j_maxSession"));
		neo4j_userName = prop.getString("neo4j_userName");
		neo4j_password = prop.getString("neo4j_password");
		neo4j_url = prop.getString("neo4j_url");

		KAFKA_BROKERS = prop.getString("KAFKA_BROKERS");
		KAFKA_TOPIC = prop.getString("KAFKA_TOPIC");
		KAFKA_ServiceSize = Integer.parseInt(prop.getString("KAFKA_ServiceSize"));

		kpi_url = prop.getString("kpi_url");

	}






}
