package com.msb.webmaster.controller;

import com.msb.common.constant.WebMasterConstant;
import com.msb.common.enums.ExceptionEnums;
import com.msb.common.util.R;
import com.msb.common.vo.ResultVO;
import com.msb.webmaster.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 *认证，注册...等基于用户的操作接口
 */
@RestController
@RequestMapping("/sys")
@Slf4j
public class SmsUserController {
    @PostMapping("/login")
    public ResultVO login(@RequestBody @Valid UserDTO userDTO, BindingResult result){
        //1.请求参数非空校验
        if(result.hasErrors()){
            //参数不合法
            log.info("【认证操作】 参数不合法,userDTO={}",userDTO);
            return R.error(ExceptionEnums.PARAMETER_ERROR);
        }
        //2.基于验证码校验请求是否合理
        String correctCaptcha = (String)SecurityUtils.getSubject().getSession().getAttribute(WebMasterConstant.KAPTCHA);
        if(!userDTO.getCaptcha().equals(correctCaptcha)){
            log.info("【认证操作】 验证码有误,captcha={},",userDTO.getCaptcha());
            return R.error(ExceptionEnums.KAPTCHA_ERROR);
        }
        //3.基于用户名和密码做Shiro的认证操作
        UsernamePasswordToken usernamePasswordToken=new UsernamePasswordToken(userDTO.getUsername(),userDTO.getPassword());
        usernamePasswordToken.setRememberMe(userDTO.getRememberMe());
        try {
            SecurityUtils.getSubject().login(usernamePasswordToken);
        } catch (AuthenticationException e) {
            //4.根据Shiro的认证，返回响应信息
            log.info("【认证操作】 用户名或密码错误,ex={}",e.getMessage());
            return R.error(ExceptionEnums.AUTHEN_ERROR);
        }
        //5.认证通过
        return R.ok();
    }
}
