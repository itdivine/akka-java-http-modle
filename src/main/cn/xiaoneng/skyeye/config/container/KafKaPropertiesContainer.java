package cn.xiaoneng.skyeye.config.container;

import cn.xiaoneng.skyeye.config.model.KafKaConfig;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xuyang
 * @version 创建时间：2015-2-9 下午3:35:37
 */
public class KafKaPropertiesContainer {


	public static Map<String, Set<KafKaConfig>> _configs = new ConcurrentHashMap<String, Set<KafKaConfig>>();

	public static void update(Map<String, Set<KafKaConfig>> configs) {
		if(configs != null && configs.size() > 0)
			_configs = configs;
	}

	public static Set<KafKaConfig> getKafKaProperties(String siteId) {
		return _configs.get(siteId);
	}
}
