package com.msb.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;
import java.util.Set;

@FeignClient(value = "beacon-cache")
public interface BeaconCacheClient {

    //获取key对应的Hash结构的数据
    @GetMapping("/cache/hgetall/{key}")
    Map hGetAll(@PathVariable(value = "key") String key);

    //根据key和field获取value的值
    @GetMapping("/cache/hget/{key}/{field}")
    Object hget(@PathVariable(value = "key")String key,@PathVariable(value = "field") String field);

    //方法重载，和上面路径一样，但是返回值直接写String
    @GetMapping("/cache/hget/{key}/{field}")
    String hgetString(@PathVariable(value = "key")String key,@PathVariable(value = "field") String field);

    //查询set结构key对应的所有数据
    @GetMapping("/cache/smember/{key}")
    Set<Map> smember(@PathVariable(value = "key")String key);
}

