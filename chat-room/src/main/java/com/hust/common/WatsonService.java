package com.hust.common;

import com.hust.config.ClassFirstConfig;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.assistant.v2.Assistant;
import com.ibm.watson.assistant.v2.model.*;

public class WatsonService {
    
    private final static IamAuthenticator AUTHENTICATOR = new IamAuthenticator(ClassFirstConfig.API_KEY);
    public final static Assistant ASSISTANT = new Assistant(ClassFirstConfig.VERSION_DATE, AUTHENTICATOR);

    static {
        ASSISTANT.setServiceUrl(ClassFirstConfig.SERVICE_URL);
    }

    public static String requestOfText(String request, SessionResponse session) {

        MessageInput input = new MessageInput.Builder()
                .messageType("text")
                .text(request)
                .build();

        MessageOptions options = new MessageOptions.Builder(ClassFirstConfig.ASSISTANT_ID, session == null ? "dfsafdgjahuigdhs" : session.getSessionId())
                .input(input)
                .build();

        StringBuilder res = new StringBuilder();
        for (RuntimeResponseGeneric s : ASSISTANT.message(options).execute().getResult().getOutput().getGeneric()) {
            res.append(s.text()).append("\n");
        }
        return res.toString();
    }

}
