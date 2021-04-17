package com.hust.controller;


import com.alibaba.fastjson.JSONObject;
import com.hust.entity.ApiConfig;
import com.hust.service.ApiConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author WGL
 * @since 2021-04-15
 */
@Controller
@RequestMapping("/api-config")
public class ApiConfigController {

    @Autowired
    ApiConfigService apiConfigService;

    @ResponseBody
    @GetMapping("/list")
    public String list() {
        List<ApiConfig> list = apiConfigService.list();
        if (list == null || list.size() == 0) {
            return null;
        }
        return JSONObject.toJSONString(list.get(0));
    }
}
