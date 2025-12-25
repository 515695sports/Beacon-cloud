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

/**
 * 校验客户的剩余金额是否充足
 */
@Service(value = "fee")
@Slf4j
public class FeeCheckFilter implements CheckFilter {

    @Autowired
    private BeaconCacheClient beaconCacheClient;

    //短信文字<=70按照一条计算
    private final int MAX_LENGTH=70;
    //短信文字>70,超出部分按照67字/条计算
    private final int LOOP_LENGTH=67;
    //用户余额
    private final String BALANCE="balance";

    @Override
    public void check(StandardSubmit submit) {
        log.info("【接口模块-校验剩余金额】 校验ing......");
        //1.获取短息长度
        int length = submit.getText().length();
        //2.判断短信字数,<=70字算一条,>70字每67字一条，算出当前短信费用
        if(length<=MAX_LENGTH){
            //1条
            submit.setFee(ApiConstant.SINGLE_FEE);
        }else {
            //多条
            int strip=length % LOOP_LENGTH==0 ? length/LOOP_LENGTH : length/LOOP_LENGTH+1;
            submit.setFee(ApiConstant.SINGLE_FEE*strip);
        }
        //3.从redis中查询用户剩余的金额
        Long balance = ((Integer)beaconCacheClient.hget(CacheConstant.CLIENT_BALANCE+submit.getClientId(),BALANCE)).longValue();

        //4.判断金额是否充足
        if(balance>=submit.getFee()){
            log.info("【接口模块-校验剩余金额】 用户金额充足！");
            return;
        }

        //5.金额不足
        log.info("【接口模块-校验剩余金额】 用户余额不足！");
        throw new ApiException(ExceptionEnums.BALANCE_NOT_ENOUGH);
    }
}
