package com.msb.api.filter.impl;

import com.msb.api.client.BeaconCacheClient;
import com.msb.api.filter.CheckFilter;
import com.msb.common.constant.CacheConstant;
import com.msb.common.enums.ExceptionEnums;
import com.msb.common.exception.ApiException;
import com.msb.common.model.StandardSubmit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 校验客户的ip地址是否合法
 */
@Service(value = "ip")
@Slf4j
public class IPCheckFilter implements CheckFilter {

    @Autowired
    private BeaconCacheClient beaconCacheClient;

    private final String IP_ADDRESS="ipAddress";

    @Override
    public void check(StandardSubmit submit) {
        log.info("【接口模块-校验ip】 校验ing......");
        //1.根据客户apikey和ipAddress查询客户的ip白名单
        //!!可能有坑！！
        String ip = beaconCacheClient.hgetString(CacheConstant.CLIENT_BUSINESS + submit.getApikey(), IP_ADDRESS);
        submit.setIp(ip);

        //2.ip白名单为null直接放行
        //3.判断ip白名单不为null，判断当前客户端请求的ip是否在ip白名单内
        if(StringUtils.isEmpty(ip) || ip.contains(submit.getRealIP())){
            log.info("【接口模块-校验ip】 客户请求ip合法");
            return;
        }

        //4.realIp不在白名单内，抛出异常
        log.info("【接口模块-校验ip】 客户请求ip不在白名单内");
        throw new ApiException(ExceptionEnums.IP_NOT_WHITE);
    }
}
