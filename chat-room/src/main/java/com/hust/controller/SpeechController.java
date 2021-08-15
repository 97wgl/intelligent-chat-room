package com.hust.controller;

import com.hust.service.SpeechRecognizerRestfulService;
import com.hust.service.SpeechSynthesizerRestfulService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Objects;

@Controller
@RequestMapping("/speech")
@CrossOrigin
@Slf4j
public class SpeechController {

    @Value("${file.uploadFolder}")
    String uploadFilePath;

    @PostMapping("/asr")
    @ResponseBody
    public String asr(@RequestParam(value = "wavFile") MultipartFile multipartFile) {
        //新的文件名以日期命名
        String fileName = System.currentTimeMillis() + ".wav";
        // 获取项目根路径并转到static/videos
        // String path = Objects.requireNonNull(ClassUtils.getDefaultClassLoader().getResource("")).getPath() + "static/videos/";
        File file = new File(uploadFilePath);
        String filePath = file + "\\" + fileName;
        //文件夹不存在就创建
        if (!file.exists()) {
            file.mkdirs();
        }
        //保存文件
        File tempFile = new File(filePath);
        try {
            log.info("临时文件：" + filePath);
            multipartFile.transferTo(tempFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String text = SpeechRecognizerRestfulService.process(filePath);
        boolean deleted = tempFile.delete();
        if (!deleted) {
            log.error(filePath + "删除失败！");
        } else {
            log.info("临时文件" + filePath + "删除成功！");
        }
        return text;
    }


    @PostMapping("/tts")
    @ResponseBody
    public String tts(@RequestParam(value = "text") String text, @RequestParam(value = "type") String type) {
        String fileName = LocalDate.now() + "_tts_" + System.currentTimeMillis() + ".wav";
        // log.info("语音合成：" + fileName);
        SpeechSynthesizerRestfulService.request(text, uploadFilePath + fileName, type);
        return "/upload/" + fileName;
    }

}
