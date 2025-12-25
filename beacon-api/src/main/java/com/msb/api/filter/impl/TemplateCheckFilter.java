package com.msb.api.filter.impl;

import com.msb.api.client.BeaconCacheClient;
import com.msb.api.filter.CheckFilter;
import com.msb.common.constant.ApiConstant;
import com.msb.common.constant.CacheConstant;
import com.msb.common.enums.ExceptionEnums;
import com.msb.common.exception.ApiException;
import com.msb.common.model.StandardSubmit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

/**
 * 校验客户的短信模板是否合法
 */
@Service(value = "template")
@Slf4j
public class TemplateCheckFilter implements CheckFilter {

    @Autowired
    private BeaconCacheClient beaconCacheClient;
    //具体模板信息
    private final String TEMPLATE_TEXT="templateText";
    //模板占位符
    private final String TEMPLATE_PLACEHOLDER="#";

    @Override
    public void check(StandardSubmit submit) {
        log.info("【接口模块-校验模板】 校验ing......");
        //1.从submit获取短信内容，签名内容和签名id
        String text = submit.getText();
        String sign = submit.getSign();
        Long signId = submit.getSignId();
        //2.从短信内容中去掉签名，获取模板 ->您的验证码是234578，如非本人操作，请忽略这条短信。
        text=text.replace(ApiConstant.SIGN_PREFIX+sign+ApiConstant.SIGN_SUFFIX,"");
        //3.从redis中获取签名id绑定的所用模板
        Set<Map> templates = beaconCacheClient.smember(CacheConstant.CLIENT_TEMPLATE + signId);
        //4.遍历签名绑定的所有模板
        if(templates!=null && templates.size()>0){
            for (Map template : templates) {
                String templateText = (String) template.get(TEMPLATE_TEXT);
                //4.1模板内容和具体短信内容匹配
                if(text.equals(templateText)){
                    //模板不含变量
                    log.info("【接口模块-校验模板】 校验模板通过,templateText={}",templateText);
                    return;
                }
                //4.2判断模板内容是否只包含一个变量，如果是，直接让具体短信内容匹配前缀和后缀
                //您的验证码是#code#，如非本人操作，请忽略这条短信。
                if(templateText!=null && templateText.contains(TEMPLATE_PLACEHOLDER)
                        && templateText.length()-templateText.replaceAll(TEMPLATE_PLACEHOLDER,"").length()==2){
                    //模板不为空且只含一个变量
                    //获取模板撇去变量后的前后缀
                    String templateTextPrefix = templateText.substring(0, templateText.indexOf(TEMPLATE_PLACEHOLDER));
                    String templateTextSuffix = templateText.substring(templateText.lastIndexOf(TEMPLATE_PLACEHOLDER) + 1);
                    //判断短信具体内容是否匹配前后缀
                    if(text.startsWith(templateTextPrefix) && text.endsWith(templateTextSuffix)){
                        //短信内容匹配短信模板
                        log.info("【接口模块-校验模板】 校验模板通过,templateText={}",templateText);
                        return;
                    }
                }
            }
        }
        //5.校验失败
        log.info("【接口模块-校验模板】 无可用模板，text={}",text);
        throw new ApiException(ExceptionEnums.UNKNOW_TEMPLATE);
    }
}
