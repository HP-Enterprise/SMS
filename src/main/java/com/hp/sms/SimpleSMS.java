package com.hp.sms;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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
@Component
public class SimpleSMS implements ISms{

    @Value("${com.hp.sms.ip}")
    private String _smsIP;

    private Logger _logger;

    private static int RESULT_SUCCESS=1;
    private static int RESULT_FAILURE=0;


    public SimpleSMS(){
        this._logger = LoggerFactory.getLogger(SimpleSMS.class);
    }

    public int Send(String sim, String content){
        String ADD_URL="http://"+ _smsIP +"/message/messageSend";
        OutputStreamWriter out = null;
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
            out = new  OutputStreamWriter(connection.getOutputStream(), "UTF-8");

            HashMap<String,String> m=new HashMap<String,String>();
            m.put("sim", sim);
            m.put("content", content);
            Gson gson=new Gson();
            String jsonstr=gson.toJson(m);
            this._logger.info(jsonstr);
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
            this._logger.info(sb.toString());
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
                this._logger.info("status:"+status);
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
        } finally {
            if(out!=null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return RESULT_FAILURE;//success返回1 faild返回0
    }

}
