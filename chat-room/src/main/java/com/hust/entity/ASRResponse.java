package com.hust.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class ASRResponse {

    @JSONField(name="task_id")
    private String taskId;

    private String result;

    private Integer status; // 20000000：成功； 40000001：失败

    private String message;
}
