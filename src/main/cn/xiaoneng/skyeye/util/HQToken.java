package cn.xiaoneng.skyeye.util;


public class HQToken {
	
	private static final String key= "eduxiaoneng";
	

	public static String getToken(String uid) {
		
		return MD5.encode(key + Statics.getToday() + uid).toUpperCase();
	}
	
	//token= md5(secret+��ǰʱ��+starttime+endtime�� #2015-01-01
	public static String getSuccessOrderToken(String statttime, String endtime) {
		
		return MD5.encode(key + Statics.getToday() + statttime + endtime).toUpperCase();
	}
	
	public static void main(String[] args) {
		//System.out.println(getToken("7550793"));
		
		long endtime = System.currentTimeMillis()/1000;
		long starttime = endtime - 60;
		
		System.out.println(starttime);
		System.out.println(endtime);
		
		System.out.println(getSuccessOrderToken(starttime+"", endtime+""));
	}
}