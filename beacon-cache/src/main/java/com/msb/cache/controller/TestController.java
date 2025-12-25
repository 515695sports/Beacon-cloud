package com.msb.cache.controller;

import com.msb.framework.redis.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {

    @Autowired
    private RedisClient redisClient;

    //写测试 hash结构
    @PostMapping("/test/set/{key}")
    public String set(@PathVariable String key, @RequestBody Map map){
        redisClient.hSet(key,map);
        return "ok";
    }

    //读测试
    @GetMapping("/test/get/{key}")
    public Map get(@PathVariable String key){
        Map<String, Object> result = redisClient.hGetAll(key);
        return result;
    }

    //管道测试
    @PostMapping("/test/pipeline")
    public String pipeline(){
        Map<String,Object> maps=new HashMap<>();
        maps.put("1888888","北京 北京,移动");
        maps.put("1888889","北京 北京,电信");
        redisClient.pipelined(operations->{
            for (Map.Entry<String, Object> entry : maps.entrySet()) {
                operations.opsForValue().set(entry.getKey(),entry.getValue());
            }
        });
        return "ok!";
    }
}
