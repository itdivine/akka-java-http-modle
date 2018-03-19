package cn.xiaoneng.skyeye.monitor;


import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * 监控中心
 *   目前监控环节:
 *
 * @author xy
 * @version 创建时间：2014-8-19 下午2:50:33
 */
public class MonitorCenter {

	protected final static Logger log = LoggerFactory.getLogger(MonitorCenter.class);

	private static MonitorCenter _instance = null;
	private static ReentrantReadWriteLock _lockInstance = new ReentrantReadWriteLock();

	/**
	 * 节点名(redis) - Monitor
	 */
	private static Map<Node, Monitor> _monitorMap = new ConcurrentHashMap<Node, Monitor>();



	private MonitorCenter(){}

	public static MonitorCenter getInstance() {

		try {

			_lockInstance.writeLock().lock();

			if(_instance == null)
			{
				_instance = new MonitorCenter();
			}

		} catch (Exception e) {
			log.warn("New MonitorManager Exception " + e.toString());
		}finally {
			_lockInstance.writeLock().unlock();
		}
		return _instance;
	}

	public static Monitor getMonitor(Node name) {

		if(!_monitorMap.containsKey(name))
		{
			Monitor monitor = new Monitor(name);
			_monitorMap.put(name, monitor);
		}

		return _monitorMap.get(name);
	}


	/**
	 * 获取完整监控信息
	 *
	 * @return 完整监控信息(format: JSON)
	 */
	public String getMonitorsToJson() {

		Node name = null;
		Monitor monitor = null;
		JSONObject obj = null;

		try {
			//1、所有监控器
			for(Entry<Node, Monitor> entry : _monitorMap.entrySet())
			{
				name = entry.getKey();
				monitor = entry.getValue();

				if(obj == null)
					obj = new JSONObject();

				obj.put("ONLINE" + name, monitor.toJson());
			}

		} catch (Exception e) {
			log.warn("Exception :" + e);
			StackTraceElement[] er = e.getStackTrace();
			for (int i = 0; i < er.length; i++) {
				log.warn(er[i].toString());
			}
		}

		return obj.toString();
	}


	/**
	 * 获取完整监控信息
	 *
	 * @return 完整监控信息(format: String)
	 */
	public void getMonitorsToString() {

		Node name = null;
		Monitor monitor = null;

		try {

			//1、所有监控器
			for(Entry<Node, Monitor> entry : _monitorMap.entrySet())
			{
				name = entry.getKey();
				monitor = entry.getValue();
				log.info("ONLINE_" + name + monitor.toString());
			}

		} catch (Exception e) {
			log.warn("Exception :" + e);
			StackTraceElement[] er = e.getStackTrace();
			for (int i = 0; i < er.length; i++) {
				log.warn(er[i].toString());
			}
		}
	}
}
