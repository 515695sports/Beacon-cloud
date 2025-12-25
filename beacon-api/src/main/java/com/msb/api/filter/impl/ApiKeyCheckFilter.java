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

import java.util.Map;

/**
 * 校验客户的apiKey是否合法
 * 默认校验顺序：apikey,ip,sign,template,mobile,fee
 */
@Service(value = "apikey")
@Slf4j
public class ApiKeyCheckFilter implements CheckFilter {

    //注入openfeign-client，用于调用beacon-cache
    @Autowired
    private BeaconCacheClient beaconCacheClient;

    @Override
    public void check(StandardSubmit submit) {
        log.info("【接口模块-校验apikey】 校验ing......");
        //1.基于BeaconCacheClient查询客户信息
        Map clientBusiness = beaconCacheClient.hGetAll(CacheConstant.CLIENT_BUSINESS + submit.getApikey());

        //2.如果为null，抛异常
        if(clientBusiness==null || clientBusiness.size()==0){
            log.info("【接口模块-校验apikey】 非法的apikey = {}",submit.getApikey());
            throw new ApiException(ExceptionEnums.ILLEGAL_APIKEY);
        }

        //3.不为null,封装数据
        //clientId
        submit.setClientId(Long.parseLong(clientBusiness.get("id")+"")); //Object转Long
        log.info("【接口模块-校验apikey】 查询到客户信息 clientBusiness = {}",clientBusiness);

    }
}
