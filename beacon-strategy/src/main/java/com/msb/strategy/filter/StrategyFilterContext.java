package com.msb.strategy.filter;

import com.msb.common.constant.ApiConstant;
import com.msb.common.constant.CacheConstant;
import com.msb.common.model.StandardSubmit;
import com.msb.strategy.client.BeaconCacheClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 控制整个策略模块校验链的执行
 */
@Component
@Slf4j
public class StrategyFilterContext {

    //泛型注入
    @Autowired
    private Map<String,StrategyFilter> strategyFilterMap;
    //注入cacheClient
    @Autowired
    private BeaconCacheClient beaconCacheClient;

    private final String CLIENT_FILTERS="clientFilters";

    //当前方法用于管理校验链的顺序
    public void strategy(StandardSubmit submit){

        //1.基于redis获取客户的校验信息
        String filters = (String) beaconCacheClient.hget(CacheConstant.CLIENT_BUSINESS + submit.getApikey(), CLIENT_FILTERS);

        //2.健壮性校验后，基于,分割，遍历即可
        String[] filterArray;
        if(filters!=null && (filterArray=filters.split(",")).length>0){
            //filterArray不为null且有数据
            for (String strategy : filterArray) {
                //3.遍历时从strategyFilterMap中获取到要执行的校验信息，执行
                StrategyFilter strategyFilter = strategyFilterMap.get(strategy);
                if(strategyFilter!=null){
                    strategyFilter.strategy(submit); //这里的错误向上抛到listener
                }
            }
        }

    }
}
