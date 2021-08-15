package com.hust.service;

import com.hust.config.AssistantConfig;
import com.hust.util.StringUtil;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.assistant.v2.Assistant;
import com.ibm.watson.assistant.v2.model.*;

import java.util.UUID;

public class WatsonAssistantService {

    private static IamAuthenticator AUTHENTICATOR = new IamAuthenticator(AssistantConfig.API_KEY);
    public static Assistant ASSISTANT = new Assistant(AssistantConfig.VERSION_DATE, AUTHENTICATOR);

    static {
        ASSISTANT.setServiceUrl(AssistantConfig.SERVICE_URL);
    }

    public static MessageResponse requestOfText(String request, SessionResponse session) {

        request = StringUtil.removeSpecialChar(request);
        MessageInput input = new MessageInput.Builder()
                .messageType("text")
                .text(request)
                .build();

        MessageOptions options = new MessageOptions.Builder(AssistantConfig.ASSISTANT_ID, session == null ? UUID.randomUUID().toString() : session.getSessionId())
                .input(input)
                .build();

        return ASSISTANT.message(options).execute().getResult();
    }

}
