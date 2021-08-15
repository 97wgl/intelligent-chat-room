package com.hust.service;

import com.hust.config.ClassFirstConfig;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.assistant.v2.Assistant;
import com.ibm.watson.assistant.v2.model.*;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class WatsonService {
    
    private static IamAuthenticator AUTHENTICATOR = new IamAuthenticator(ClassFirstConfig.API_KEY);
    public static Assistant ASSISTANT = new Assistant(ClassFirstConfig.VERSION_DATE, AUTHENTICATOR);

    static {
        ASSISTANT.setServiceUrl(ClassFirstConfig.SERVICE_URL);
    }

    public static MessageResponse requestOfText(String request, SessionResponse session) {

        try {
            MessageInput input = new MessageInput.Builder()
                    .messageType("text")
                    .text(request)
                    .build();

            MessageOptions options = new MessageOptions.Builder(ClassFirstConfig.ASSISTANT_ID, session == null ? UUID.randomUUID().toString() : session.getSessionId())
                    .input(input)
                    .build();

            return ASSISTANT.message(options).execute().getResult();
        } catch (Exception e) {
            log.error("[WatsonService#requestOfText] 获取Watson响应异常", e);
            return null;
        }
    }

}
