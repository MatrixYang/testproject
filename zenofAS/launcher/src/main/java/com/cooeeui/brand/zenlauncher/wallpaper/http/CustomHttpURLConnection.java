package com.cooeeui.brand.zenlauncher.wallpaper.http;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Steve on 2015/8/19.
 */
public class CustomHttpURLConnection {

    private synchronized HttpURLConnection getHttpURLConnection(String address, String data) {
        HttpURLConnection urlConnection = null;
        try {
            // 根据地址创建URL对象
            URL url = new URL(address);
            // 根据URL对象打开链接
            urlConnection = (HttpURLConnection) url.openConnection();
            // 设置请求的方式
            urlConnection.setRequestMethod("POST");
            // 设置请求的超时时间
            urlConnection.setReadTimeout(5000);
            // 设置连接超时的时间
            urlConnection.setConnectTimeout(5000);
            // 设置请求的头
            urlConnection.setRequestProperty("Connection", "keep-alive");
            // 设置请求的头
            urlConnection.setRequestProperty("Charset", "UTF-8");
            // 设置请求的头
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            // 设置请求的头
            urlConnection
                .setRequestProperty("Content-Length", String.valueOf(data.getBytes().length));
            // 设置请求的头
            urlConnection
                .setRequestProperty("User-Agent",
                                    "Mozilla/5.0(Linux;U;Android 2.2.1;en-us;Nexus One Build.FRG83) AppleWebKit/553.1(KHTML,like Gecko) Version/4.0 Mobile Safari/533.1");
            // 发送POST请求必须设置允许输出
            urlConnection.setDoOutput(true);
            // 发送POST请求必须设置允许输入;setDoInput的默认值就是true
            urlConnection.setDoInput(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return urlConnection;
    }


    public String post(String address, String data) {
        HttpURLConnection urlConnection = null;
        String response = "";
        try {
            urlConnection = getHttpURLConnection(address, data);
            DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());
            out.writeBytes(data);
            out.flush();
            out.close();
            //使用BufferedReader替代BufferedInputStream获取时间从100ms降低到3ms
            if (urlConnection.getResponseCode() == 200) {
                BufferedReader input =
                    new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String s;
                while ((s = input.readLine()) != null) {
                    response += s;
                }
                input.close();
            } else {
                Log.e("WallPaper", "WallPaper connect error");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return response;
    }
}
