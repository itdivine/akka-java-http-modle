package cn.xiaoneng.skyeye.config.model;

public class KafKaConfig {

	public String siteId;

	public String host;
	public String topic;

	public int level; //大于等于此值的Action,才发送KAFKA

	/**
	 * 0:电商统计 | 1:工单系统
	 */
	public int type;


	@Override
	public String toString() {
		return siteId + " host=" + host + " topic=" + topic;
	}
}