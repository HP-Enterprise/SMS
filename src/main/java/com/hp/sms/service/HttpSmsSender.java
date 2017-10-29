package com.hp.sms.service;

/**
 * Created by jackl on 2016/9/14.
 */
import com.alibaba.fastjson.JSONObject;
import com.hp.sms.utils.Sha1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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
    @Value("${com.hp.zjy.sms.url}")
    private String _zjySmsUrl;
    @Value("${com.hp.zjy.sms.newUrl}")
    private String _newZjySmsUrl;
    @Value("${com.hp.zjy.sms.cid}")
    private Integer _cid;
    @Value("${com.hp.zjy.sms.cipher}")
    private String _cipher;
    @Value("${com.hp.zjy.sms.username}")
    private String _zjyUsername;
    @Value("${com.hp.zjy.sms.pasword}")
    private String _zjyPassword;
    private static int RESULT_SUCCESS=1;
    private static int RESULT_FAILURE=0;

    private Logger _logger= LoggerFactory.getLogger(HttpSmsSender.class);

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

    /**
     * 中聚元短信接口
     * @param sim
     * @param content
     * @param time 定时时间 (格式201701010101)
     * @return
     */
    public int sendZJY(String sim, String content, String time){
        String _url = _zjySmsUrl + "?name=" + _zjyUsername + "&pwd=" + _zjyPassword + "&dst=" + sim + "&sender=&time=" + time  + "&txt=ccdx&msg=" + content;
        try {
            //建立连接
            URL url = new URL(_url);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type", "application/json;charset=GB2312");
            connection.connect();
            //POST请求
            OutputStreamWriter  out = new  OutputStreamWriter(connection.getOutputStream(), "GB2312");

            out.flush();
            out.close();
            //读取响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String lines;
            StringBuffer sb = new StringBuffer("");
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes(), "GB2312");
                sb.append(lines);
            }
            _logger.info("sim短信发送结果:" + sb);

            reader.close();
            // 断开连接
            connection.disconnect();
            if (sb.equals(""))
                return RESULT_FAILURE;
            else{
                String[] results = sb.toString().split("&");
                String[] nums = results[0].split("=");
                int res = RESULT_FAILURE;
                if(nums != null && nums.length > 0){
                    String num = nums[1];
                    if(Integer.parseInt(num) > 0){
                        res = RESULT_SUCCESS;
                    }
                }
                return res;
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

    /**
     * 新中聚元短信接口
     * @param sim
     * @param content
     * @return
     */
    public int sendAPI(String sim, String content){
        String sign = Sha1.getSha1(sim + content + _cipher);

        try {
            String param = URLEncoder.encode(content, "UTF-8");
            String _url = _newZjySmsUrl + "?cid=" + _cid + "&mobile=" + sim + "&content=" + param + "&sign=" + sign;
            //建立连接
            URL url = new URL(_url);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.connect();
            //POST请求
            OutputStreamWriter  out = new  OutputStreamWriter(connection.getOutputStream(), "GB2312");

            out.flush();
            out.close();
            //读取响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String lines;
            StringBuffer sb = new StringBuffer("");
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes(), "GB2312");
                sb.append(lines);
            }
            _logger.info("sim短信发送结果:" + sb);

            reader.close();
            // 断开连接
            connection.disconnect();
            if (sb.equals(""))
                return RESULT_FAILURE;
            else{
                JSONObject obj = JSONObject.parseObject(sb.toString());
                String status = obj.getString("status");
                int res = RESULT_FAILURE;
                if("1".equals(status)){
                    res = RESULT_SUCCESS;
                }
                return res;
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