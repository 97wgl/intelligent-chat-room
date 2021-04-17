package com.hust.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.aliyuncs.exceptions.ClientException;
import com.hust.config.AlibabaSpeechConfig;
import com.hust.entity.ASRResponse;
import com.hust.util.comonent.HttpUtil;

import java.util.HashMap;

public class SpeechRecognizerRestfulService {
    private static String accessToken;
    private static String appkey;

    private static final String format = "pcm";
    private static final int sampleRate = 16000;
    private static final boolean enablePunctuationPrediction = true;
    private static final boolean enableInverseTextNormalization = true;
    private static final boolean enableVoiceDetection = false;

    static {
        accessToken = AlibabaSpeechConfig.TOKEN;
        appkey = AlibabaSpeechConfig.APP_KEY;
    }

    public SpeechRecognizerRestfulService(String appKey, String token) {
        appkey = appKey;
        accessToken = token;
    }

    public static String process(String fileName) {

        /**
         * 设置HTTP RESTful POST请求：
         * 1.使用HTTP协议。
         * 2.语音识别服务域名：nls-gateway.cn-shanghai.aliyuncs.com。
         * 3.语音识别接口请求路径：/stream/v1/asr。
         * 4.设置必选请求参数：appkey、format、sample_rate。
         * 5.设置可选请求参数：enable_punctuation_prediction、enable_inverse_text_normalization、enable_voice_detection。
         */
        String request = "http://nls-gateway.cn-shanghai.aliyuncs.com/stream/v1/asr";
        request = request + "?appkey=" + appkey;
        request = request + "&format=" + format;
        request = request + "&sample_rate=" + sampleRate;
        if (enablePunctuationPrediction) {
            request = request + "&enable_punctuation_prediction=" + true;
        }
        if (enableInverseTextNormalization) {
            request = request + "&enable_inverse_text_normalization=" + true;
        }
        if (enableVoiceDetection) {
            request = request + "&enable_voice_detection=" + true;
        }

        // System.out.println("Request: " + request);

        /**
         * 设置HTTP头部字段：
         * 1.鉴权参数。
         * 2.Content-Type：application/octet-stream。
         */
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("X-NLS-Token", accessToken);
        headers.put("Content-Type", "application/octet-stream");

        /**
         * 发送HTTP POST请求，返回服务端的响应。
         */
        String response = HttpUtil.sendPostFile(request, headers, fileName);

        if (response != null) {
            ASRResponse asrResponse = JSONObject.parseObject(response, ASRResponse.class);
            System.out.println("Response: " + response);
            if (asrResponse.getStatus().equals(40000001)) {
                // TODO 需要重新获取token
                try {
                    accessToken = SpeechTokenService.getToken();
                } catch (ClientException e) {
                    e.printStackTrace();
                }
                return process(fileName);
            }
            String result = JSONPath.read(response, "result").toString();
            System.out.println("识别结果：" + result);
            return result;
        } else {
            System.err.println("识别失败!");
            return null;
        }

    }

    public static void main(String[] args) {

        String fileName = "E:\\大数据实验室\\项目\\intelligent-chat-room\\chat-room\\97507510-5aab-45c0-b812-3d5719af152b.wav";
        System.out.println(process(fileName));;
    }
}
