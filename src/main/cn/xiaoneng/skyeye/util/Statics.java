package cn.xiaoneng.skyeye.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Statics {

	private static Logger log =Logger.getLogger(Statics.class.getName());
	private static final String NTALKER_UID = "_ISME9754_";

	/**
	 * 获取Actor路径中的企业ID
	 * @param it  getSelf().path().elements().iterator())
	 * @return siteId
	 */
	public static String getSiteId(scala.collection.Iterator<String> it) {

		it.next();
		it.next();
		String siteId = it.next();
		return siteId;
	}

	public static String decode(String param, String code) {

		String re = null;
		try {
			param = param.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
			re = URLDecoder.decode(param, "gb2312");
		} catch (UnsupportedEncodingException e) {
//			log.warning("Exception :" + e + " " + param + "/" + code);
		}
		return re;
	}

	public static String regex_match(String content,String regex){

		try {
			if(content==null || content.isEmpty() || regex==null || regex.isEmpty())
				return content;

			Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(content);
			if(matcher.find()){
				return matcher.group();
			}

		} catch (Exception e) {
//			log.warning("Exception :" + e + " " + content + "/" + regex);
		}
		return "";
	}

	public static boolean isregex_match(String content,String regex){

		try {
			if(content==null || content.isEmpty() || regex==null || regex.isEmpty())
				return false;

			Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(content);
			if(matcher.find()){
				return true;
			}

		} catch (Exception e) {
//			log.warning("Exception :" + content + "/" + regex + " " + e);
		}
		return false;
	}

	public static String getBrowserVersion(String content,String regex){

		String version = "";

		try {
			String browser = regex_match(content,regex);
			if (!browser.isEmpty() && browser.contains("/"))
			{
				int index = browser.indexOf("/") + 1;
				version = browser.substring(index);
			}
			else
				version = browser;

		} catch (Exception e) {
			log.warning("Exception :" + e);

		}
		return version;
	}

	@SuppressWarnings("deprecation")
	public static long getTodayStartTime() {

		Date date = null;

		try {
			date = new Date();
			date.setHours(00);
			date.setMinutes(00);
			date.setSeconds(00);

			return date.getTime();

		} catch (Exception e) {
			log.warning(e.toString());
		}

		return 0L;
	}

	//2012-04-10 17:01:21
	public static String formatTime(long time, String format){

		String timeStr = String.valueOf(time);
		if(timeStr.length() > 13)
		{
			log.warning("time longer than 13 " + time);
			timeStr = timeStr.substring(0, 13);
			time = Long.parseLong(timeStr);
		}

		Date date = new Date();
		date.setTime(time);  //
		SimpleDateFormat df = new SimpleDateFormat(format);
		return df.format(date);
	}

	public static long parseTime(String param, String format) throws ParseException
	{
		SimpleDateFormat df = new SimpleDateFormat(format);
		return df.parse(param).getTime();
	}

	public static String checkEmpty(String param) {

		return param==null?"":param;

	}

	public static String checkEmptyAndLength(String param,int maxlen) {

		if(param==null)
			return "";
		if(param.length()>maxlen)
			param = param.substring(0,maxlen-1);

		return param;
	}



	public static long checkLength(long param,int maxlen) {

		String p = param + "";

		int n = p.length() - maxlen;

		if(n <= 0)
			return param;

		int d = (int)Math.pow(10,n);

		param = param/d;

		return param;
	}

	/**
	 *
	 * 048.255.255.255
	 * @return
	 */

	public static boolean isIP(String ip) {

		Pattern pattern = Pattern.compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");
		Matcher matcher = pattern.matcher(ip);
		if (matcher.find())
			return true;

		return false;
	}

	//
	public static long getCalendarTime(int amount) {

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, amount);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal.getTimeInMillis();
	}

	public static long division(long divisor, long dividend)
	{
		if(dividend == 0)
			return 0;
		else
			return divisor/dividend;
	}

	public static boolean isNullOrEmpty(String param) {

		if(param==null || param.isEmpty())
			return true;

		return false;
	}


	/**
	 * Check a String is json format
	 * @param param
	 * @return true|false
	 */
	public static boolean isJson(String param){

		if(param==null || param.equals(""))
			return false;

		try {
			char strr = param.charAt(0);
			char strrr = param.charAt(param.length()-1);

			if(strr == '{'&&strrr == '}')
				return true;
			else
				return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 *
	 * @param param1
	 * @param param2
	 * @return  param1
	 */
	public static boolean compareString(String param1, String param2) {

		try {

			int i1 = param1.length();
			int i2 = param2.length();

			if(i1 > i2)
				return true;
			else if(i1 < i2)
				return false;
			else if(i1==i2)
			{
				if(!param1.matches("\\d*") || !param2.matches("\\d*"))
					return false;

				if(i1>18)
				{
					param1 = param1.substring(0, 18);
					param2 = param2.substring(0, 18);
				}

				long l1 = Long.parseLong(param1);
				long l2 = Long.parseLong(param2);

				if(l1>l2)
					return true;
				else
					return false;
			}

		} catch (Exception e) {
			log.warning("compareString Exception :" + e.getMessage() + " " + param1 + "/" + param2);
		}

		return false;
	}

	public static boolean isNumber(String param) {

		if(isNullOrEmpty(param))
			return false;

		try {

			if(param.matches("\\d*"))
				return true;

		} catch (Exception e) {
			log.warning("isNumber Exception :" + e.getMessage() + " " + param);
		}

		return false;
	}
	/**
	 *
	 * @return
	 */
	public static long getDayDif(long time , long time1) {

		return (long)(time - time1)/(24*60*60*1000);
	}

	public static String getToday() {

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(new Date());
	}

	/**
	 * 判断是否是平台的商家siteId <br>
	 * 条件：比较下划线'_'的前字符串是否相同
	 *
	 * @param platformSiteId : nt_1000
	 * @param dbSiteId :  nt_1001 | kf_9762
	 * @return  nt_1001返回true   kf_9762返回false
	 */
	public static boolean checkIsSamePlatform(String platformSiteId, String dbSiteId) {

		if(Statics.isNullOrEmpty(platformSiteId) || Statics.isNullOrEmpty(dbSiteId))
			return false;

		String downline = "_";

		if(!platformSiteId.contains(downline) || !dbSiteId.contains(downline))
		{
			return false;
		}

		try {
			int indexP = platformSiteId.indexOf(downline);
			String platformHead = platformSiteId.substring(0,indexP);

			int indexD = dbSiteId.indexOf(downline);
			String dbHead = dbSiteId.substring(0,indexD);

			if(platformHead != null && dbHead!=null && platformHead.equals(dbHead))
				return true;

		} catch (Exception e) {
			log.warning("checkIsSamePlatform Exception " + e.toString());
		}

		return false;
	}



	/**
	 * 从HTTP请求中获取一个参数的值
	 * 如：https://www.baidu.com/link?wd=&eqid=ab25673f00006c8000000002576d0145
	 * 获取eqid的值
	 * @param url
	 * @param paramName
	 * @return paramValue
	 */
	public static String getParamFromUrl(String url, String paramName) {

		if(isNullOrEmpty(url) || isNullOrEmpty(paramName))
			return null;

		if(!url.contains(paramName))
			return null;

		try {

			String[] arr = url.split("&");
			for(String entry: arr) {
				String[] kv = entry.split("=");
				if(kv[0].equals(paramName) && kv.length>1)
					return kv[1];
			}

		} catch (Exception e) {
			log.warning("Exception " + e.toString() + " " + url + " " + paramName);
		}
		return null;
	}

	public static String apiKeyError() {
		return "{\"status\":400,\"body\":\"Invalid API key\"}";
	}

	/**
	 * 判断两个日期是否是同一天
	 *
	 * @param date1 date1
	 * @param date2 date2
	 * @return
	 */
	public static boolean isSameDate(Date date1, Date date2) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);

		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);

		boolean isSameYear = cal1.get(Calendar.YEAR) == cal2
				.get(Calendar.YEAR);
		boolean isSameMonth = isSameYear
				&& cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
		boolean isSameDate = isSameMonth
				&& cal1.get(Calendar.DAY_OF_MONTH) == cal2
				.get(Calendar.DAY_OF_MONTH);

		return isSameDate;
	}

	public static boolean is24Hours(long time, long time1) {
		long dif = Math.abs(time - time1);
		if(dif < 24*60*60*1000)
			return true;
		return false;
	}

	public static boolean is3Day(long time, long time1) {
		long dif = Math.abs(time - time1);
		if(dif < 3*24*60*60*1000)
			return true;
		return false;
	}

	public static void main(String[] args) {
//		String url ="https://www.baidu.com/link?wd=&eqid=ab25673f00006c8000000002576d0145";
//		String p = "eqid";
//		System.out.println(getParamFromUrl(url, p));

//		String url = "http://www.baidu.com/link?url=ZvKNZeSh-Kh6MD90P1J52DC4cX82YHUUD24VNn0fM-qAxWxErVtDyMXwlC4O1CNY&ie=utf-8&f=8&tn=baidu&wd=%E5%B0%8F%E8%83%BD%E7%A7%91%E6%8A%80&rqlang=cn&inputT=1028";
//		String regex = "(&|\\?)(wd|word)=([^&]*)";
//		System.out.println(regex_match(url, regex));

		//1.获取3天前的时间戳
		System.out.println(isSameDate(new Date(1514361476603L), new Date(1514361476604L)));
		System.out.println(1514361476603L-1513672113356L);
	}
}
