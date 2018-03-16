package cn.xiaoneng.skyeye.collector.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;


public class SiteDomainContainer {
	
	private static Logger log =Logger.getLogger(SiteDomainContainer.class.getName());
	private static SiteDomainContainer _instance = null;
	
	Map<String,Set<String>> domainMap = new HashMap<String,Set<String>>();
	Map<String,Long> updatetimeMap = new HashMap<String,Long>();
	
	private static ReentrantReadWriteLock _lockinstance = new ReentrantReadWriteLock();
	
	private SiteDomainContainer(){}
	
	public static SiteDomainContainer getInstance() 
	{
		try { 
			_lockinstance.writeLock().lock();
			
			if(_instance==null)
				_instance = new SiteDomainContainer();
			
		} catch (Exception e) {
			log.warning("New MainProcess Exception " + e.toString());
		}finally {
			_lockinstance.writeLock().unlock();
		}
		
		return _instance;
	}
	
	public Set<String> getConfigSiteDomain(String siteid) {
		return domainMap.get(siteid);
	}

	public void updateConfig(Map<String, Set<String>> configs) {
		if(configs != null && configs.size() > 0)
			domainMap = configs;
	}

}
