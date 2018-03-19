package cn.xiaoneng.skyeye.monitor;

import cn.xiaoneng.skyeye.util.Statics;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * 监控类：
 * 可以给服务的每个关键节点配置此类，来实时监控方法的执行性能参数
 *
 * 统计30秒内，每个方法的最大处理时长、平均处理时长、调用次数；
 * 统计30秒内，所有读或写方法的最大处理时长、平均处理时长、调用次数；
 * 统计从服务启动开始，所有方法的总调用次数
 *
 * @author xy
 * @version 创建时间：2014-8-18 下午5:39:32
 */
public class Monitor {

	private Node _name; //监控节点名字，如：永久库、统计库、Servlet、redis等

	protected final static Logger log = LoggerFactory.getLogger(Monitor.class);

	//监控所有读方法、写方法： 定时（30秒内）清空，实时计算
	private long writeMaxTime = 0;   //最大处理时长
	private long writeTotalTime = 0; //总处理时长
	private long writeTotalCount = 0;     //总处理个数
	private long writeTotalFailCount = 0; //总处理失败个数

	private long readMaxTime = 0;
	private long readTotalTime = 0;
	private long readTotalCount = 0;
	private long readTotalFailCount = 0;

	//监控单个读、写入方法： 定时（30秒内）清空，实时计算
	private Map<String,Long> _writeMaxTime_realTime = new HashMap<String,Long>();   //最大处理时长
	private Map<String,Long> _writeTotalTime_realTime = new HashMap<String,Long>(); //总处理时长
	private Map<String,Long> _writeCount_realTime = new HashMap<String,Long>();     //总处理个数
	private Map<String,Long> _writeInvalidCount_realTime = new HashMap<String,Long>(); //总无效处理个数

	private Map<String,Long> _readMaxTime_realTime = new HashMap<String,Long>();
	private Map<String,Long> _readTotalTime_realTime = new HashMap<String,Long>();
	private Map<String,Long> _readCount_realTime = new HashMap<String,Long>();
	private Map<String,Long> _readInvalidCount_realTime = new HashMap<String,Long>(); //总无效处理个数


	//监控单个读、写入方法：从服务启动开始，累计计算
	private Map<String,Long> _writeCount_sum = new HashMap<String,Long>(); //总处理个数
	private Map<String,Long> _readCount_sum = new HashMap<String,Long>();  //总处理个数

	private Map<String,Long> _writeInvalidCount_sum = new HashMap<String,Long>();  //总处理失败个数
	private Map<String,Long> _readInvalidCount_sum = new HashMap<String,Long>();  //总处理失败个数


	public Node getMonitorName() {
		return _name;
	}

	public Monitor(Node name) {
		_name = name;
	}

	private String getReadTime()
	{
		StringBuffer result = new StringBuffer();
		try{
			result.append("read:" + readMaxTime + "/" + Statics.division(readTotalTime,readTotalCount)
					+ "/" + readTotalFailCount + "/" + readTotalCount + "/" + getMapSum(_readInvalidCount_sum) + "/" + getMapSum(_readCount_sum));
		}
		catch(Exception e)
		{

		}

		readMaxTime = 0;
		readTotalTime = 0;
		readTotalCount = 0;
		readTotalFailCount = 0;

		return result.toString();
	}

	private String getWriteTime()
	{
		StringBuffer result = new StringBuffer();
		try{
			result.append("write:" + writeMaxTime + "/" + Statics.division(writeTotalTime,writeTotalCount)
					+ "/" + writeTotalFailCount + "/" + writeTotalCount + "/" + getMapSum(_writeInvalidCount_sum) + "/" + getMapSum(_writeCount_sum));

			result.append(getDBFunc());
		}
		catch(Exception e)
		{

		}
		finally
		{
			writeMaxTime = 0;
			writeTotalTime = 0;
			writeTotalCount = 0;
			writeTotalFailCount = 0;
		}

		return result.toString();
	}

	private Object getDBFunc() {


		StringBuffer result = new StringBuffer();

		try{
			for(Map.Entry<String,Long> entry:_writeMaxTime_realTime.entrySet())
			{
				long count = 0;
				long avg = 0;
				long invalidCount = 0;

				String method = entry.getKey();
				long max = entry.getValue();

				if (_writeCount_realTime.containsKey(method))
					count = _writeCount_realTime.get(method);

				if (_writeTotalTime_realTime.containsKey(method))
					avg = Statics.division(_writeTotalTime_realTime.get(method),
							count);

				if (_writeInvalidCount_realTime.containsKey(method))
					invalidCount = _writeInvalidCount_realTime.get(method);

				result.append(" " + method + ":" + max + "/" + avg + "/" + invalidCount + "/" + count + " ");

				_writeMaxTime_realTime.put(method, 0L);
				_writeTotalTime_realTime.put(method, 0L);
				_writeCount_realTime.put(method, 0L);
				_writeInvalidCount_realTime.put(method, 0L);
			}

			for(Map.Entry<String,Long> entry:_readMaxTime_realTime.entrySet())
			{
				long count = 0;
				long avg = 0;
				double hit = 0;
				long invalidCount = 0;

				String method = entry.getKey();
				long max = entry.getValue();

				if (_readCount_realTime.containsKey(method))
					count = _readCount_realTime.get(method);
				if (_readTotalTime_realTime.containsKey(method))
					avg = Statics.division(_readTotalTime_realTime.get(method), count);
				if (_readInvalidCount_realTime.containsKey(method))
					invalidCount = _readInvalidCount_realTime.get(method);

				//读取缓存命中率HIT = 命中数/查询数
				if(count != 0)
					hit = 1 - (double)invalidCount/(double)count;

				result.append(" " + method + ":" + max + "/" + avg + "/" + invalidCount + "/" + count + " " + hit + " ");

				_readMaxTime_realTime.put(method, 0L);
				_readTotalTime_realTime.put(method, 0L);
				_readCount_realTime.put(method, 0L);
				_readInvalidCount_realTime.put(method, 0L);
			}
		}
		catch(Exception e)
		{
			log.warn("Exception " + e.toString());
		}

		return result.toString();
	}

	/**
	 * Map中所有value求和
	 * @param map
	 * @return
	 */
	private long getMapSum(Map<String,Long> map) {

		long re = 0;
		Collection<Long> coll = map.values();
		for(Long l : coll)
		{
			re += l;
		}
		return re;
	}


	private JSONObject getReadTimeToJson()
	{
		JSONObject obj = new JSONObject();

		try {

			obj.put("read_max_time", readMaxTime);
			obj.put("read_avg_time", Statics.division(readTotalTime, readTotalCount));
			obj.put("read_total_count", readTotalCount);
			obj.put("read_total_fail_count", readTotalFailCount);

			JSONArray arr = new JSONArray();

			for(Map.Entry<String,Long> entry:_readMaxTime_realTime.entrySet())
			{
				long count = 0;
				long avg = 0;
				long invalidCount = 0;

				String method = entry.getKey();
				long max = entry.getValue();

				if (_readCount_realTime.containsKey(method))
					count = _readCount_realTime.get(method);
				if (_readTotalTime_realTime.containsKey(method))
					avg = Statics.division(_readTotalTime_realTime.get(method), count);
				if (_readInvalidCount_realTime.containsKey(method))
					invalidCount = _readInvalidCount_realTime.get(method);

				JSONObject methodJson = new JSONObject();
				methodJson.put("read_max_time", max);
				methodJson.put("read_avg_time", avg);
				methodJson.put("read_count", count);
				methodJson.put("read_invalid_count", invalidCount);

				//读取缓存命中率HIT = 命中数/查询数
				if(count != 0)
					methodJson.put("hit", 1 - (double)invalidCount/(double)count);

				JSONObject theMethodJson = new JSONObject();
				theMethodJson.put(method, methodJson);
				arr.add(theMethodJson);

				_readMaxTime_realTime.put(method, 0L);
				_readTotalTime_realTime.put(method, 0L);
				_readCount_realTime.put(method, 0L);
				_readInvalidCount_realTime.put(method, 0L);
			}

			obj.put("methods", arr);

		} catch (Exception e) {
			log.warn("Exception :" + e);
			StackTraceElement[] er = e.getStackTrace();
			for (int i = 0; i < er.length; i++) {
				log.info(er[i].toString());
			}
		}

		readMaxTime = 0;
		readTotalTime = 0;
		readTotalCount = 0;

		return obj;
	}

	private JSONObject getWriteTimeToJson()
	{
		JSONObject obj = new JSONObject();

		try {

			obj.put("write_max_time", writeMaxTime);
			obj.put("write_avg_time", Statics.division(writeTotalTime, writeTotalCount));
			obj.put("write_total_count", writeTotalCount);
			obj.put("write_total_fail_count", writeTotalFailCount);

			JSONArray arr = new JSONArray();

			for(Map.Entry<String,Long> entry:_writeMaxTime_realTime.entrySet())
			{
				long count = 0;
				long avg = 0;
				long invalidCount = 0;

				String method = entry.getKey();
				long max = entry.getValue();

				if(_writeCount_realTime.containsKey(method))
					count = _writeCount_realTime.get(method);

				if(_writeTotalTime_realTime.containsKey(method))
					avg = Statics.division(_writeTotalTime_realTime.get(method), count);

				if(_writeInvalidCount_realTime.containsKey(method))
					invalidCount = _writeInvalidCount_realTime.get(method);

				JSONObject theMethodJson = new JSONObject();
				JSONObject methodJson = new JSONObject();
				methodJson.put("write_max_time", max);
				methodJson.put("write_avg_time", avg);
				methodJson.put("write_count", count);
				methodJson.put("write_invalid_count", invalidCount);

				theMethodJson.put(method, methodJson);
				arr.add(theMethodJson);

				_writeMaxTime_realTime.put(method, 0L);
				_writeTotalTime_realTime.put(method, 0L);
				_writeCount_realTime.put(method, 0L);
				_writeInvalidCount_realTime.put(method, 0L);
			}

			obj.put("methods", arr);

		} catch (Exception e) {
			log.warn("Exception :" + e);
			StackTraceElement[] er = e.getStackTrace();
			for (int i = 0; i < er.length; i++) {
				log.info(er[i].toString());
			}
		}

		writeMaxTime = 0;
		writeTotalTime = 0;
		writeTotalCount = 0;

		return obj;

	}

	/**
	 * 保存方法消耗时长
	 *
	 * @param method  方法名
	 * @param time    消耗时长
	 * @param isSuccess  调用是否成功
	 */
	public void newWriteTime(String method, long time, boolean isSuccess) {

		if (time > writeMaxTime)
			writeMaxTime = time;
		writeTotalTime += time;
		writeTotalCount++;

		if (_writeCount_sum.get(method) == null)
			_writeCount_sum.put(method, 0L);
		if (_writeMaxTime_realTime.get(method) == null)
			_writeMaxTime_realTime.put(method, 0L);
		if (_writeTotalTime_realTime.get(method) == null)
			_writeTotalTime_realTime.put(method, 0L);
		if (_writeCount_realTime.get(method) == null)
			_writeCount_realTime.put(method, 0L);

		if (!isSuccess)
		{
			if(_writeInvalidCount_realTime.get(method) == null)
				_writeInvalidCount_realTime.put(method, 0L);
			if(_writeInvalidCount_sum.get(method) == null)
				_writeInvalidCount_sum.put(method, 0L);

			_writeInvalidCount_realTime.put(method,_writeInvalidCount_realTime.get(method)+1);
			_writeInvalidCount_sum.put(method,_writeInvalidCount_sum.get(method)+1);
			writeTotalFailCount++;
		}

		if (_writeMaxTime_realTime.containsKey(method) && time > _writeMaxTime_realTime.get(method))
			_writeMaxTime_realTime.put(method, time);

		_writeTotalTime_realTime.put(method, _writeTotalTime_realTime.get(method) + time);
		_writeCount_realTime.put(method, _writeCount_realTime.get(method) + 1);
		_writeCount_sum.put(method, _writeCount_sum.get(method) + 1);
	}

	public void newReadTime(String method ,long time, boolean isSuccess)
	{
		if(time>readMaxTime)
			readMaxTime = time;
		readTotalTime += time;
		readTotalCount++;

		if (_readCount_sum.get(method) == null)
			_readCount_sum.put(method, 0L);
		if(_readMaxTime_realTime.get(method)==null)
			_readMaxTime_realTime.put(method,(Long)(0L));
		if(_readTotalTime_realTime.get(method)==null)
			_readTotalTime_realTime.put(method,(Long)(0L));
		if(_readCount_realTime.get(method)==null)
			_readCount_realTime.put(method,(Long)(0L));

		if (!isSuccess)
		{
			if(_readInvalidCount_realTime.get(method) == null)
				_readInvalidCount_realTime.put(method, 0L);
			if(_readInvalidCount_sum.get(method) == null)
				_readInvalidCount_sum.put(method, 0L);

			_readInvalidCount_realTime.put(method,_readInvalidCount_realTime.get(method)+1);
			_readInvalidCount_sum.put(method,_readInvalidCount_realTime.get(method)+1);
			readTotalFailCount++;
		}

		if(_readMaxTime_realTime.containsKey(method) && time>_readMaxTime_realTime.get(method))
			_readMaxTime_realTime.put(method, time);

		_readTotalTime_realTime.put(method,_readTotalTime_realTime.get(method)+time);
		_readCount_realTime.put(method,_readCount_realTime.get(method)+1);
		_readCount_sum.put(method, _readCount_sum.get(method) + 1);
	}

	/**
	 * 返回给前端监控接口
	 * @return JSON
	 */
	public JSONObject toJson() {

		JSONObject obj = new JSONObject();

		try {

			obj.put("write", getWriteTimeToJson());
			obj.put("read", getReadTimeToJson());

		} catch (Exception e) {
			log.warn("Exception :" + e);
			StackTraceElement[] er = e.getStackTrace();
			for (int i = 0; i < er.length; i++) {
				log.info(er[i].toString());
			}
		}

		return obj;
	}

	public long getAvgTime() {
		return Statics.division(writeTotalTime+readTotalTime,writeTotalCount+readTotalCount);
	}

	public String toString() {

		return " max/avg/fail/count/invalidcount/totalcount " + getWriteTime() + " " + getReadTime();
	}

	public static void main(String[] args) {


		long invalidCount = 10;
		long count = 100;

		System.out.println((double)invalidCount/(double)count);
	}

}
