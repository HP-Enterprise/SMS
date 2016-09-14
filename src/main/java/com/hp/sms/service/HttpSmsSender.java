package com.hp.sms.service;

/**
 * Created by jackl on 2016/9/14.
 */
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
/**
 * 简单短信网关
 */
@Service
public class HttpSmsSender {

    @Value("${com.hp.sms.url}")
    private String _smsUrl;
    private static int RESULT_SUCCESS=1;
    private static int RESULT_FAILURE=0;

    public int Send(String sim, String content){
        String ADD_URL=_smsUrl;
        try {
            //建立连接
            URL url = new URL(ADD_URL);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            connection.connect();
            //POST请求
            OutputStreamWriter  out = new  OutputStreamWriter(connection.getOutputStream(), "UTF-8");

            HashMap<String,String> m=new HashMap<String,String>();
            m.put("phone", sim);
            m.put("message", content);
            Gson gson=new Gson();
            String jsonstr=gson.toJson(m);
            System.out.println(jsonstr);
            out.append(jsonstr);
            out.flush();
            out.close();
            //读取响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String lines;
            StringBuffer sb = new StringBuffer("");
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes(), "utf-8");
                sb.append(lines);
            }
            System.out.println(sb);
            //{"status":"success"}
            //{"status":"failure"}
            reader.close();
            // 断开连接
            connection.disconnect();
            if (sb.equals(""))
                return RESULT_FAILURE;
            else{
                Gson gs=new Gson();
                Map<String,String> resultMap = gs.fromJson(sb.toString(), new TypeToken<Map<String, String>>(){}.getType());
                String status=resultMap.get("status");
                System.out.println("status:"+status);
                if (status!=null) {
                    if (status.equals("success"))
                        return RESULT_SUCCESS;
                }
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return RESULT_FAILURE;//success返回1 faild返回0
    }

}
