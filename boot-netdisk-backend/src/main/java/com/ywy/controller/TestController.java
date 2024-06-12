package com.ywy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class TestController {
    @Resource
    private JavaMailSender javaMailSender;

    @RequestMapping("/test")
    public String test() {
        return "test";
    }

    @RequestMapping("/emailTest")
        public void sendTestEmail() {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("yukehan8@gmail.com"); //設置收件人信箱
            message.setSubject("Test Email"); //設置信箱主題
            message.setText("This is a test email."); //設置信箱內容
            javaMailSender.send(message); //發送郵件
        }

}

