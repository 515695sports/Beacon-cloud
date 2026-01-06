package com.msb.webmaster.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.msb.common.constant.WebMasterConstant;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Controller
public class KaptchaController {

    private final String JPG="jpg";

    @Autowired
    private DefaultKaptcha defaultKaptcha;

    @GetMapping("/captcha.jpg")
    public void captcha(HttpServletResponse resp) {
        //1.验证码图片不需要存储或缓存
        resp.setHeader("Cache-Control","no-store,no-cache");
        //2.设置响应头信息
        resp.setContentType("image/jpg");
        //3.生成验证码文字
        String text = defaultKaptcha.createText();
        //认证需要验证验证码，这里需要基于Shiro对验证码文字做持久化
        SecurityUtils.getSubject().getSession().setAttribute(WebMasterConstant.KAPTCHA,text);
        //4.基于文字生成对应的图片
        BufferedImage image = defaultKaptcha.createImage(text);
        //5.写回验证码图片信息
        try {
            ServletOutputStream outputStream = resp.getOutputStream();
            //指定写出的图像，图像格式以及输出目标
            ImageIO.write(image,JPG,outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
