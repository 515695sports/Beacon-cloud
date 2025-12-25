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
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Set;

/**
 * 校验客户的短信签名是否合法
 */
@Service(value = "sign")
@Slf4j
public class SignCheckFilter implements CheckFilter {

    @Autowired
    private BeaconCacheClient beaconCacheClient;
    //截取签名的开始索引
    private final int SIGN_START_INDEX =1;
    //客户存储签名信息的字段
    private final String CLIENT_SIGN_INFO="signInfo";
    //签名的id
    private final String SIGN_ID="id";

    @Override
    public void check(StandardSubmit submit) {
        log.info("【接口模块-校验签名】 校验ing......");
        //1.判断短信内容是否携带了【】
        String text = submit.getText();
        if (!text.startsWith(ApiConstant.SIGN_PREFIX) || !text.contains(ApiConstant.SIGN_SUFFIX)) {
            log.info("【接口模块-校验签名】 无可用签名 text={}",text);
            throw new ApiException(ExceptionEnums.UNKNOW_SIGN);
        }

        //2.携带了签名，把短信内容中的签名截取出来
        String sign = text.substring(SIGN_START_INDEX, text.indexOf(ApiConstant.SIGN_SUFFIX));
        if(StringUtils.isEmpty(sign)){
            log.info("【接口模块-校验签名】 无可用签名 text={}",text);
            throw new ApiException(ExceptionEnums.UNKNOW_SIGN);
        }

        //3.从redis中查询出客户绑定的签名
        Set<Map> set = beaconCacheClient.smember(CacheConstant.CLIENT_SIGN + submit.getClientId());
        if(set==null || set.size()==0){
            log.info("【接口模块-校验签名】 无可用签名 text={}",text);
            throw new ApiException(ExceptionEnums.UNKNOW_SIGN);
        }

        //4.判断短信中的签名和客户绑定的签名是否一致
        for (Map map: set) {
            if(sign.equals(map.get(CLIENT_SIGN_INFO))){
                //签名校验已通过
                log.info("【接口模块-校验签名】 找到匹配的签名 sign={}",sign);
                //封装StandardSubmit
                submit.setSign(sign); //封装匹配的那个签名
                submit.setSignId(Long.parseLong(map.get(SIGN_ID)+"")); //封装匹配的签名id
                return;
            }
        }

        //5.签名不匹配
        log.info("【接口模块-校验签名】 无可用签名 text={}",text);
        throw new ApiException(ExceptionEnums.UNKNOW_SIGN);
    }
}
