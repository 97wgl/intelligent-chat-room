package com.hust.common;

import com.ibm.watson.assistant.v2.model.SessionResponse;

public class SessionResponseCounterPair {
    SessionResponse sessionResponse;
    Integer dialogCounter;
    Integer repeatResponseCounter;

    public SessionResponseCounterPair() {
    }

    public SessionResponseCounterPair(SessionResponse sessionResponse, Integer dialogCounter, Integer repeatResponseCounter) {
        this.sessionResponse = sessionResponse;
        this.dialogCounter = dialogCounter;
        this.repeatResponseCounter = repeatResponseCounter;
    }

    public SessionResponse getSessionResponse() {
        return sessionResponse;
    }

    public void setSessionResponse(SessionResponse sessionResponse) {
        this.sessionResponse = sessionResponse;
    }

    public Integer getDialogCounter() {
        return dialogCounter;
    }

    public void setDialogCounter(Integer dialogCounter) {
        this.dialogCounter = dialogCounter;
    }

    public Integer getRepeatResponseCounter() {
        return repeatResponseCounter;
    }

    public void setRepeatResponseCounter(Integer repeatResponseCounter) {
        this.repeatResponseCounter = repeatResponseCounter;
    }
}
