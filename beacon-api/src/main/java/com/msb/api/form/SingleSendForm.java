package com.msb.api.form;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SingleSendForm {

    /**
     * 客户的api
     */
    @NotBlank(message = "apikey不允许为空！")
    private String apikey;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不允许为空！")
    private String mobile;

    /**
     * 发送的短信内容：【签名】+模板
     */
    @NotBlank(message = "短信内容不允许为空！")
    private String text;

    /**
     * 用户业务内的uid
     */
    private String uid;

    /**
     * 0-验证码类，1-通知类，2-营销类
     */
    @Range(min=0,max=2,message = "短信类型只能是0-2的整数！")
    @NotNull(message = "短信类型不允许为空！")
    //String类型用NotBlank，非string类型用NotNull
    private Integer state;
}
