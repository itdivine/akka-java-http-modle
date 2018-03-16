package cn.xiaoneng.skyeye.collector.http;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


public class HttpSender {

    private static Logger log = Logger.getLogger(HttpSender.class.getName());

    public static String getInfos(String uri, Map<String,String> headers){

        if(uri==null || uri.isEmpty()) {
            log.info("HttpSender query is Empty");
            return null;
        }

        String infoStr = null;
        InputStream fis = null;
        HttpURLConnection httpConn = null;

        long startt = System.currentTimeMillis();

        try {
            URL url = new URL(uri);

            log.info("getInfos: " + url);

            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestProperty("User-agent", "MSIE8.0");
            httpConn.setRequestMethod("GET");

            if(headers != null) {
                for(Map.Entry<String,String> entry:headers.entrySet()) {
                    httpConn.setRequestProperty(entry.getKey(),entry.getValue());
                }
            }

            httpConn.setReadTimeout(5000);
            httpConn.setConnectTimeout(5000);
            httpConn.setDoOutput(true);

            if (httpConn.getResponseCode() == 200)
            {
                fis = httpConn.getInputStream();

                infoStr = Stream2String(fis, "utf-8");//通过http获得的没有经过校验的串

                log.warning("getInfos success." + infoStr);
            }

        } catch (Exception e) {
            log.warning("Exception :" + e.toString());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        } finally {
            if (httpConn != null)
                httpConn.disconnect();
            long stopt = System.currentTimeMillis();
            long span = stopt - startt;
            if(span > 1000)
                log.warning("HttpSender send http spans " + span + "ms");
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

            log.warning("Exception: " + e.toString());
            StackTraceElement[] er = e.getStackTrace();
            for (int i = 0; i < er.length; i++) {
                log.info(er[i].toString());
            }
        }

        return out.toString();
    }

    public static void main(String[] args) {
        String url = "http://kpi-dev.ntalker.com/api/counter/queries/nskyeye:page_load:54ad6f8c-e282-41f7-9956-ad42585769f6,nskyeye:staytime_avg:54ad6f8c-e282-41f7-9956-ad42585769f6,nskyeye:chat_open:54ad6f8c-e282-41f7-9956-ad42585769f6,nskyeye:order_create:54ad6f8c-e282-41f7-9956-ad42585769f6";
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJKZXJzZXktU2VjdXJpdHktQmFzaWMiLCJzdWIiOiJ7XCJ2YWxpZHRpbWVzXCI6MTU2NzM5MjE2NjY1NyxcInJvbGVzXCI6W1wiQWRtaW5cIixcIkxlYWRlclwiLFwiU2FsZVwiXSxcInNpdGVpZFwiOlwia2ZfODAwMlwiLFwidXNlcmlkXCI6XCJrZl84MDAyXzFcIn0iLCJpYXQiOjE0ODA5OTIxNjYsImV4cCI6MTU2NzM5MjE2Nn0.-uk43M21Ja86b3l8QTDu6KALHSFoua25GexU3o74lZ0";
        String content = "";

        Map<String,String> headers = new HashMap<String,String>();
        headers.put("token", token);
        getInfos(url,headers);
    }

}
