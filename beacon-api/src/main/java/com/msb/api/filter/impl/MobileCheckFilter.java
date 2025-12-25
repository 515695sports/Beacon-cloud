package com.msb.api.filter.impl;

import com.msb.api.filter.CheckFilter;
import com.msb.api.util.PhoneFormatCheckUtil;
import com.msb.common.enums.ExceptionEnums;
import com.msb.common.exception.ApiException;
import com.msb.common.model.StandardSubmit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 校验客户的手机号是否合法
 */
@Service(value = "mobile")
@Slf4j
public class MobileCheckFilter implements CheckFilter {
    @Override
    public void check(StandardSubmit submit) {
        log.info("【接口模块-校验手机号】 校验ing......");
        String mobile = submit.getMobile();
        if(!StringUtils.isEmpty(mobile) && PhoneFormatCheckUtil.isChinaPhone(mobile)){
            //手机号正确
            log.info("【接口模块-校验手机号】 手机号格式合法,mobile={}",mobile);
            return;
        }
        //不合法
        log.info("【接口模块-校验手机号】 手机号格式不正确,mobile={}",mobile);
        throw new ApiException(ExceptionEnums.ERROR_MOBILE);
    }
}
