package cn.xiaoneng.skyeye.monitor;

import org.slf4j.LoggerFactory;

import java.util.TimerTask;


/**
 * input: 定时收集服务各个环节的性能参数
 * output: 1、日志   2、http监控接口
 *
 * @author xy
 *
 */
public class TaskAlarmLog extends TimerTask {

	protected final static org.slf4j.Logger log = LoggerFactory.getLogger(TaskAlarmLog.class);

	@Override
	public void run() {

		try {

			log.info("******* ONLINE_TaskAlarmLog start *******");

			MonitorCenter.getInstance().getMonitorsToString();

			log.info("******* ONLINE_TaskAlarmLog end *******");


		} catch (Exception e) {
			log.warn("TaskAlarmLog Exception:"+e.toString());
			StackTraceElement[] er = e.getStackTrace();
			for (int i = 0; i < er.length; i++) {
				log.warn(er[i].toString());
			}
		}
	}
}