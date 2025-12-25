package com.msb.api.filter;

import com.msb.common.model.StandardSubmit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 校验策略的上下文对象
 */
@Component
@RefreshScope //动态更新nacos配置文件
public class CheckFilterContext {

    //Spring的ioc会把对象全部放在map集合
    //基于spring4.x提供的泛型注入，基于map只拿到需要的类型对象即可
    @Autowired
    private Map<String,CheckFilter> checkFiltersMap;

    //校验顺序在nacos注册中心中指定
    @Value("${filters:apikey,ip,sign,template}")
    private String filters;

    /**
     * 这个check方法用于管理校验链的顺序
     */
    public void check(StandardSubmit submit){
        //1.把获取到的filters根据逗号切分
        String[] filterArr = filters.split(",");
        //2.遍历数组集合
        for (String filter : filterArr) {
            //从checkFilterMap中拿到spring容器管理的对象
            CheckFilter checkFilter = checkFiltersMap.get(filter);
            checkFilter.check(submit);
        }
    }
}
