package com.msb.api.controller;

import com.msb.api.filter.CheckFilterContext;
import com.msb.api.form.SingleSendForm;
import com.msb.api.vo.ResultVO;
import com.msb.api.util.R;
import com.msb.common.constant.RabbitMQConstant;
import com.msb.common.enums.ExceptionEnums;
import com.msb.common.model.StandardSubmit;
import com.msb.common.util.SnowFlakeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/sms")
@Slf4j
@RefreshScope
public class SmsController {

    //获取客户端ip的请求头信息，用逗号隔开
    @Value("${headers}")
    private String headers;

    //基于请求头获取信息可能获取到unknow
    private final String UNKNOW="unknow";

    //当前请求头获取ip需要截取到第一个ip
    private final String X_FORWARDED_FOR="x-forwarded-for";

    @Autowired
    private CheckFilterContext checkFilterContext;

    @Autowired
    private SnowFlakeUtil snowFlakeUtil;

    @Autowired
    private RabbitTemplate rabbitTemplate;
/*
* **请求路径：https://sms.beaconcloud.com/v1/sms/single_send
**请求方式：POST
**请求头信息**
| 请求头               | 请求信息                       |
| -------------------- | ------------------------------ |
| Accept（响应的格式） | application/json;charset=utf-8 |
| ContentType          | application/json;charset=utf-8 |

**请求参数：**
| 参数名 | 类型    | 是否必传 | 说明                                     | 示例                             |
| ------ | ------- | -------- | ---------------------------------------- | -------------------------------- |
| apikey | string  | 是       | 由服务方提供，可以在后台首页中查看       | 887559db54d911edba520242ac120002 |
| mobile | string  | 是       | 接收的手机号，仅支持单号码发送           | 18888888888                      |
| text   | string  | 是       | 需要发送的短信内容，需要与签名和模板匹配 | 【烽火云】 您的验证码是 1234     |
| uid    | string  | 否       | 您业务系统内的ID，回调时会携带此参数     | 10086                            |
| state  | integer | 是       | 0-验证码短信 1-通知类短信 2-营销类短信   | 0                                |
* */
    //produces指定服务器返回的Content-Type
    @PostMapping(value = "/single_send",produces = "application/json;charset=utf8")
    public ResultVO singleSend(@RequestBody @Validated SingleSendForm form, BindingResult result, HttpServletRequest request){
        //1.校验参数
        if(result.hasErrors()){
            //哪个字段出了问题，得到对应的message
            String msg = result.getFieldError().getDefaultMessage();
            log.info("【接口模块-单条短信Controller 参数不合法 msg={}】",msg);
            return R.error(ExceptionEnums.PARAMETER_ERROR.getCode(),msg);
        }
        //===================获取真实的ip地址===========================
        String ip=this.getRealIP(request);

        //===================构建StandardSubmit，各种封装校验====================
        StandardSubmit submit = new StandardSubmit();
        submit.setRealIP(ip);
        submit.setReportState(0); //短信发送中
        submit.setApikey(form.getApikey());
        submit.setMobile(form.getMobile());
        submit.setText(form.getText()); //???这个是整个内容还是模板内容
        submit.setState(form.getState());
        submit.setUid(form.getUid());

        //===================调用策略模式的校验链=========================
        checkFilterContext.check(submit);

        //=======基于雪花算法生成短信唯一id,并封装到StandardSubmit,以及设置发送时间==========
        submit.setSequenceId(snowFlakeUtil.nextId());
        submit.setSendTime(LocalDateTime.now());

        //===================发送到MQ，交给策略模块处理====================
        rabbitTemplate.convertAndSend(RabbitMQConstant.SMS_PRE_SEND,submit,new CorrelationData(submit.getSequenceId().toString()));

        //========================返回接收成功============================
        return R.ok();
    }

    /**
     * 获取客户端真实的ip地址，而不是代理的ip地址
     * @param request
     * @return
     */
    private String getRealIP(HttpServletRequest request) {
        //1.声明真实ip地址
        String ip;

        //2.遍历请求头，通过request获取ip地址
        for (String header : headers.split(",")) {
            //健壮性校验
            if(!StringUtils.isEmpty(header)){
                //基于request获取ip地址
                ip = request.getHeader(header);
                //ip不为null，空串或者unknow就可以返回
                if(!StringUtils.isEmpty(ip) && !UNKNOW.equalsIgnoreCase(ip)){
                    //判断请求头是否是x-forwarded-for
                    if(X_FORWARDED_FOR.equalsIgnoreCase(header) && ip.contains(",")){
                        ip=ip.substring(0,ip.indexOf(","));
                    }
                    return ip;
                }
            }
        }
        //如果请求头都没有获取到ip，基于传统方法获取ip
        return request.getRemoteAddr();
    }
}



