package com.hust.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/speech")
@CrossOrigin
public class SpeechController {

    @ResponseBody
    @RequestMapping("/test")
    public String test(@RequestParam("name") String name) {
        return "wang " + name;
    }

}
