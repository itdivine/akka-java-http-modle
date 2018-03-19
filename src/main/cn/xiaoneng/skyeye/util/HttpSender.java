package cn.xiaoneng.skyeye.util;

import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by liangyongheng on 2017/4/24 14:48.
 */
public class HttpSender {

    protected final static org.slf4j.Logger log = LoggerFactory.getLogger(HttpSender.class);

    public static String getInfos(String query){

        String infoStr = null;
        InputStream fis = null;
        HttpURLConnection httpConn = null;

        long startt = System.currentTimeMillis();

        try {
            URL url = new URL(query);

            log.debug("getInfos: " + query);

            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestProperty("User-agent", "MSIE8.0");
            httpConn.setRequestMethod("GET");
            httpConn.setReadTimeout(5000);
            httpConn.setConnectTimeout(5000);
            httpConn.setDoOutput(true);

            if (httpConn.getResponseCode() == 200)
            {
                fis = httpConn.getInputStream();

                infoStr = Stream2String(fis, "utf-8");//通过http获得的没有经过校验的串

                log.debug("getInfos success." + infoStr);
            }

        } catch (Exception e) {
            log.error("Exception :" + e.toString() + " url= " + query);
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.warn(er[i].toString());
            }
        } finally {
            if (httpConn != null)
                httpConn.disconnect();
            long stopt = System.currentTimeMillis();
            long span = stopt - startt;
            if(span > 5000)
                log.warn("HttpSender send http spans " + span + "ms  url= " + query);
        }
        return infoStr;
    }

    private static String Stream2String(InputStream in, String encoding) {

        if (in == null)
            return null;

        StringBuffer out = new StringBuffer();

        try {

            char[] b = new char[1024];
            InputStreamReader inread = new InputStreamReader(in, encoding);

            for (int n; (n = inread.read(b)) != -1;) {
                String line = new String(b, 0, n);
                out.append(line);
            }

        } catch (Exception e) {

            log.error("Exception: " + e.toString());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.warn(er[i].toString());
            }
        }

        return out.toString();
    }

}
