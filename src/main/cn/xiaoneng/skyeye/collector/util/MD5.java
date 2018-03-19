package cn.xiaoneng.skyeye.collector.util;


import java.security.MessageDigest;


public class MD5 {
	public static String encode(String text) {
		String hs = "";
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(text.getBytes());
			byte[] ctext = md.digest();
			String stmp = "";
			for (int n = 0; n < ctext.length; n++) {
				stmp = (Integer.toHexString(ctext[n] & 0xff));
				if (stmp.length() == 1)
					hs = hs + "0" + stmp;
				else
					hs = hs + stmp;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hs;
	}
	public static void main(String[] args) {
		long time = System.currentTimeMillis()/1000;
		System.out.println(time);
		System.out.println(encode("ntalker_123" + "1376982696"));
		
	}
}
