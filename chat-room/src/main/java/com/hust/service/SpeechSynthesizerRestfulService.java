package com.hust.service;

import com.alibaba.fastjson.JSONObject;
import com.hust.config.AlibabaSpeechConfig;
import okhttp3.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;

public class SpeechSynthesizerRestfulService {
    private static String accessToken;
    private static String appkey;
    private static final String format = "wav";
    private static final int sampleRate = 16000;
    private static final String voiceType = "Annie"; // 虚拟老师用的"Annie"，虚拟学伴是儿童音“Aiwei”

    static {
        appkey = AlibabaSpeechConfig.APP_KEY;
        accessToken = AlibabaSpeechConfig.TOKEN;
    }

    public SpeechSynthesizerRestfulService(String appKey, String token) {
        appkey = appKey;
        accessToken = token;
    }

    /**
     * HTTPS GET 请求
     */
    private static void processGETRequest(String text, String audioSaveFile, String format, int sampleRate, String voice) {
        /**
         * 设置HTTPS GET请求
         * 1.使用HTTPS协议
         * 2.语音识别服务域名：nls-gateway.cn-shanghai.aliyuncs.com
         * 3.语音识别接口请求路径：/stream/v1/tts
         * 4.设置必须请求参数：appkey、token、text、format、sample_rate
         * 5.设置可选请求参数：voice、volume、speech_rate、pitch_rate
         */
        // System.out.println(text);
        String url = "https://nls-gateway.cn-shanghai.aliyuncs.com/stream/v1/tts";
        url = url + "?appkey=" + appkey;
        url = url + "&token=" + accessToken;
        url = url + "&text=" + text;
        url = url + "&format=" + format;
        url = url + "&voice=" + voice;
        url = url + "&sample_rate=" + String.valueOf(sampleRate);
        // voice 发音人，可选，默认是xiaoyun
        // url = url + "&voice=" + "xiaoyun";
        // volume 音量，范围是0~100，可选，默认50
        // url = url + "&volume=" + String.valueOf(50);
        // speech_rate 语速，范围是-500~500，可选，默认是0
        // url = url + "&speech_rate=" + String.valueOf(0);
        // pitch_rate 语调，范围是-500~500，可选，默认是0
        // url = url + "&pitch_rate=" + String.valueOf(0);
        // System.out.println("URL: " + url);
        /**
         * 发送HTTPS GET请求，处理服务端的响应
         */
        Request request = new Request.Builder().url(url).get().build();
        try {
            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            // System.out.println("total latency :" + (System.currentTimeMillis() - start) + " ms");
            // System.out.println(response.headers().toString());
            String contentType = response.header("Content-Type");
            if ("audio/mpeg".equals(contentType)) {
                File f = new File(audioSaveFile);
                FileOutputStream fout = new FileOutputStream(f);
                fout.write(response.body().bytes());
                fout.close();
                // System.out.println("The GET request succeed!");
            }
            else {
                // ContentType 为 null 或者为 "application/json"
                String errorMessage = response.body().string();
                accessToken = SpeechTokenService.getToken();
                processGETRequest(text, audioSaveFile, format, sampleRate, voice);
                // System.out.println("The GET request failed: " + errorMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * HTTPS POST 请求
     */
    private static void processPOSTRequest(String text, String audioSaveFile, String format, int sampleRate, String voice) {
        /**
         * 设置HTTPS POST请求
         * 1.使用HTTPS协议
         * 2.语音合成服务域名：nls-gateway.cn-shanghai.aliyuncs.com
         * 3.语音合成接口请求路径：/stream/v1/tts
         * 4.设置必须请求参数：appkey、token、text、format、sample_rate
         * 5.设置可选请求参数：voice、volume、speech_rate、pitch_rate
         */
        String url = "https://nls-gateway.cn-shanghai.aliyuncs.com/stream/v1/tts";
        JSONObject taskObject = new JSONObject();
        taskObject.put("appkey", appkey);
        taskObject.put("token", accessToken);
        taskObject.put("text", text);
        taskObject.put("format", format);
        taskObject.put("voice", voice);
        taskObject.put("sample_rate", sampleRate);
        // voice 发音人，可选，默认是xiaoyun
        // taskObject.put("voice", "xiaoyun");
        // volume 音量，范围是0~100，可选，默认50
        // taskObject.put("volume", 50);
        // speech_rate 语速，范围是-500~500，可选，默认是0
        // taskObject.put("speech_rate", 0);
        // pitch_rate 语调，范围是-500~500，可选，默认是0
        // taskObject.put("pitch_rate", 0);
        String bodyContent = taskObject.toJSONString();
        System.out.println("POST Body Content: " + bodyContent);
        RequestBody reqBody = RequestBody.create(MediaType.parse("application/json"), bodyContent);
        Request request = new Request.Builder()
            .url(url)
            .header("Content-Type", "application/json")
            .post(reqBody)
            .build();
        try {
            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            String contentType = response.header("Content-Type");
            if ("audio/mpeg".equals(contentType)) {
                File f = new File(audioSaveFile);
                FileOutputStream fout = new FileOutputStream(f);
                fout.write(response.body().bytes());
                fout.close();
                System.out.println("The POST request succeed!");
            }
            else {
                // ContentType 为 null 或者为 "application/json"
                String errorMessage = response.body().string();
                System.out.println("The POST request failed: " + errorMessage);
            }
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String textUrlEncode(String text) {
        // 采用RFC 3986规范进行urlencode编码
        String textUrlEncode = text;
        try {
            textUrlEncode = URLEncoder.encode(textUrlEncode, "UTF-8")
                    .replace("+", "%20")
                    .replace("*", "%2A")
                    .replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return textUrlEncode;
    }

    public static void request(String text, String fileName) {
        processGETRequest(textUrlEncode(text), fileName, format, sampleRate, voiceType);
    }

    public static void request(String text, String fileName, String type) {
        processGETRequest(textUrlEncode(text), fileName, format, sampleRate, type);
    }

    public static void main(String[] args) {
        String text = "你好";
        request(text,LocalDate.now() + "_tts_" + System.currentTimeMillis() + ".wav", "sitong");
    }
}