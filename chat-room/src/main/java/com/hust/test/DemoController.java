package com.hust.test;

import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.cloud.sdk.core.service.exception.NotFoundException;
import com.ibm.cloud.sdk.core.service.exception.RequestTooLargeException;
import com.ibm.cloud.sdk.core.service.exception.ServiceResponseException;
import com.ibm.watson.assistant.v2.Assistant;
import com.ibm.watson.assistant.v2.model.*;

import java.util.Scanner;

public class DemoController {

    private final static String APIKEY = "WDlok4GE5mvlOC3pQUxcJDQFuftpGvXg2ffOrmf1Qdk6";
    private final static String SERVICE_URL = "https://api.us-south.assistant.watson.cloud.ibm.com/instances/659c9d26-31d9-4ba4-99bb-14ed6b0aa3ed";
    private final static String ASSISTANT_TUTOR_ID = "c330058d-c07b-4801-8f43-b95d1b846910";


    public static void main(String[] args) {
        IamAuthenticator authenticator = new IamAuthenticator(APIKEY);
        Assistant assistant = new Assistant("2020-11-30", authenticator);
        assistant.setServiceUrl(SERVICE_URL);

        try {
            CreateSessionOptions options = new CreateSessionOptions.Builder(ASSISTANT_TUTOR_ID).build();
            // 返回sessionId
            SessionResponse sessionResponse = assistant.createSession(options).execute().getResult();
            MessageOptions options2 = new MessageOptions.Builder(ASSISTANT_TUTOR_ID, sessionResponse.getSessionId())
                    .build();

            MessageResponse response = assistant.message(options2).execute().getResult();

            System.out.println();
            System.out.print("Teacher:");
            for (int i = 0; i < response.getOutput().getGeneric().size(); i++) {
                String type = response.getOutput().getGeneric().get(i).responseType();
                if ("text".equals(type)) {
                    System.out.println(response.getOutput().getGeneric().get(i).text());
                } else if ("image".equals(type)) {
                    System.out.println(response.getOutput().getGeneric().get(i).source());
                }
            }
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println();
                System.out.print("Student:");
                String s = scanner.nextLine();
                MessageInput input = new MessageInput.Builder()
                        .messageType("text")
                        .text(s)
                        .build();

                MessageOptions options3 = new MessageOptions.Builder(ASSISTANT_TUTOR_ID, sessionResponse.getSessionId())
                        .input(input)
                        .build();

                MessageResponse response3 = assistant.message(options3).execute().getResult();

                System.out.println();
                System.out.print("Teacher:");
                for (int i = 0; i < response3.getOutput().getGeneric().size(); i++) {
                    String type = response3.getOutput().getGeneric().get(i).responseType();
                    if ("text".equals(type)) {
                        System.out.println(response3.getOutput().getGeneric().get(i).text());
                    } else if ("image".equals(type)) {
                        System.out.println(response3.getOutput().getGeneric().get(i).source());
                    }
                }
                //System.out.println(response3);
            }
        } catch (NotFoundException e) {
            // Handle Not Found (404) exception
        } catch (RequestTooLargeException e) {
            // Handle Request Too Large (413) exception
        } catch (ServiceResponseException e) {
            // Base class for all exceptions caused by error responses from the service
            System.out.println("Service returned status code "
                    + e.getStatusCode() + ": " + e.getMessage());
        }
    }


}
