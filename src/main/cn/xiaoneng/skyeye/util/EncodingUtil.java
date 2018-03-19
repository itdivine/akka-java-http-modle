package cn.xiaoneng.skyeye.util;

import java.util.logging.Logger;


public class EncodingUtil {
	
	private static Logger log =Logger.getLogger(EncodingUtil.class.getName());
	
//	private static final String LATIN1 = "ISO-8859-1";
	private static final String GBK = "gbk";
//	private static final String GB2312 = "gb2312";
	private static final String UTF8 = "utf-8";

	
	
	public static String JavaToDB(String str)
	{
		return EnocodeSwitch(UTF8,GBK,str);
		//return EnocodeSwitch(GBK,LATIN1,str);
	}
	
	public static String DBtoJava(String str)
	{
		return EnocodeSwitch(GBK,UTF8,str);
		//return EnocodeSwitch(LATIN1,GBK,str);
	}
	
	
	public static String EnocodeSwitch(String srcencoding,String dstencoding,String str)
	{		
		String v = str;
		try{
			if(str==null || str.length()<=0)
				return str;
			
			v = new String(str.getBytes(srcencoding),dstencoding);		
		}
		catch(Exception e)
		{
			log.warning("EnocodeSwitch Exception " + e.toString());
		}
		
		return v;
	}
	
	public static String changeCharset(String str, String newCharset) {
		try {
			if (str != null) {
				
				byte[] bs = str.getBytes();  // ��Ĭ���ַ���������ַ�����
//				byte[] bs = str.getBytes(oldCharset);  //�þɵ��ַ���������ַ�����������ܻ�����쳣
				
				return new String(bs, newCharset);  // ���µ��ַ����������ַ���
			}

		} catch (Exception e) {
			log.warning("changeCharset Exception " + e.toString());
		}
		return null;
	}

}
