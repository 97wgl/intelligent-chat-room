package com.hust.common;

import com.ibm.watson.assistant.v2.model.SessionResponse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SessionResponseCounterPair {
    SessionResponse sessionResponse;
    Integer dialogCounter;
    Integer repeatResponseCounter;
    Set<Integer> assistantReplySet;
    /**
     * 记录每一轮对话，学生是否响应
     */
    Map<Integer, Boolean> stuRepliedMap;

    public SessionResponseCounterPair() {
    }

    public SessionResponseCounterPair(SessionResponse sessionResponse, Integer dialogCounter, Integer repeatResponseCounter) {
        this.sessionResponse = sessionResponse;
        this.dialogCounter = dialogCounter;
        this.repeatResponseCounter = repeatResponseCounter;
        assistantReplySet = new HashSet<>();
        stuRepliedMap = new HashMap<>();
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

    public Set<Integer> getAssistantReplySet() {
        return assistantReplySet;
    }

    public void setAssistantReplySet(Set<Integer> assistantReplySet) {
        this.assistantReplySet = assistantReplySet;
    }

    public Map<Integer, Boolean> getStuRepliedMap() {
        return stuRepliedMap;
    }

    public void setStuRepliedMap(Map<Integer, Boolean> stuRepliedMap) {
        this.stuRepliedMap = stuRepliedMap;
    }
}
