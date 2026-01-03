package com.msb.cache.controller;

import com.msb.framework.redis.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@Slf4j
public class CacheController {

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private RedisTemplate redisTemplate;

    //存储hash结构
    @PostMapping(value = "/cache/hmset/{key}")
    public void hmset(@PathVariable(value = "key") String key, @RequestBody Map<String,Object> map){
        log.info("【缓存模块】 hmset方法，存储key={}，存储value={}",key,map);
        redisClient.hSet(key,map);
    }

    //存储string结构
    @PostMapping(value = "/cache/set/{key}")
    public void set(@PathVariable(value = "key") String key, @RequestParam(value = "value") Object value){
        log.info("【缓存模块】 set方法，存储key={}，存储value={}",key,value);
        redisClient.set(key,value);
    }

    //存储set结构（每个元素是一个map集合）
    @PostMapping(value = "/cache/sadd/{key}")
    public void sadd(@PathVariable(value = "key") String key,@RequestBody Map<String,Object>...maps){
        log.info("【缓存模块】 sadd方法，存储key={}，存储value={}",key,maps);
        redisClient.sAdd(key,maps);
    }

    //存储set结构（每个元素是string）
    @PostMapping(value = "/cache/saddstr/{key}")
    public void saddStr(@PathVariable(value = "key") String key,@RequestBody String...value){
        log.info("【缓存模块】 saddStr方法，存储key={}，存储value={}",key,value);
        redisClient.sAdd(key,value);
    }

    /**
     * 对string集合进行存，交集，删
     * @param key 要存入的集合key
     * @param sinterKey 做交集的key
     * @param value 要存入的集合
     * @return 交集结果
     */
    @PostMapping(value = "/cache/sinterstr/{key}/{sinterKey}")
    public Set<Object> sinterStr(@PathVariable(value = "key") String key,@PathVariable(value = "sinterKey")String sinterKey,@RequestBody String...value){
        log.info("【缓存模块】 sinterStr的交集方法，存储key={}，sinterKey={},存储value={}",key,sinterKey,value);
        //1.存储数据到set集合
        redisClient.sAdd(key,value);
        //2.把key和sinterKey做交集拿到结果
        Set<Object> result = redisClient.sIntersect(key, sinterKey);
        //3.删除key
        redisClient.delete(key);
        //4.返回交集结果
        return result;
    }

    //获取hash结构的数据
    @GetMapping("/cache/hgetall/{key}")
    public Map hGetAll(@PathVariable(value = "key") String key){
        log.info("【缓存模块】 hGetAll方法，获取key={}的数据",key);
        Map<String, Object> map = redisClient.hGetAll(key);
        log.info("【缓存模块】 hGetAll方法，获取key={}的数据,value={}",key,map);
        return map;
    }

    //根据key和field获取某个value的值
    @GetMapping("/cache/hget/{key}/{field}")
    public Object hget(@PathVariable(value = "key")String key,@PathVariable(value = "field") String field){
        log.info("【缓存模块】 hget方法，获取key={},field={}的数据",key,field);
        Object value = redisClient.hGet(key, field);
        log.info("【缓存模块】 hget方法，获取key={}的数据,field={}的数据:value={}",key,field,value);
        return value;
    }

    //查询set结构key对应的所有数据
    @GetMapping("/cache/smember/{key}")
    public Set smember(@PathVariable(value = "key")String key){
        log.info("【缓存模块】 smember方法，获取key={} 的所有数据",key);
        Set<Object> values = redisClient.sMembers(key);
        log.info("【缓存模块】 smember方法，获取key={} 的所有数据，value={}",key,values);
        return values;
    }

    //pipeline存储多个string类型
    @PostMapping("/cache/pipeline/string")
    public void pipelineString(@RequestBody Map<String,String> map){
        log.info("【缓存模块】 string方法，获取到存储的数据,map的长度为{}",map.size());
        redisClient.pipelined(operation->{
            for (Map.Entry<String, String> entry : map.entrySet()) {
                operation.opsForValue().set(entry.getKey(), entry.getValue());
            }
        });
    }

    //查询string结构
    @GetMapping(value = "/cache/get/{key}")
    public Object get(@PathVariable(value = "key") String key){
        log.info("【缓存模块】 get方法，查询key={}",key);
        Object value = redisClient.get(key);
        log.info("【缓存模块】 get方法，查询key={}对应的value={}",key,value);
        return value;
    }

    /**
     * zset添加一个元素的到key集合的方法
     * @param key 集合名
     * @param score 权重-long
     * @param member 元素-Object
     * @return boolean
     */
    @PostMapping("/cache/zaddLong/{key}/{score}/{member}")
    public boolean zaddLong(@PathVariable(value = "key") String key,
                     @PathVariable(value = "score") Long score,
                     @PathVariable(value = "member") Object member){
        log.info("【缓存模块】 zaddLong方法，存储key={},score={},member={}",key,score,member);
        boolean result = redisClient.zAdd(key, member, score);
        return result;
    }

    /**
     * zset获取key集合权重在[start,end]之前的元素个数
     * @param key 集合名
     * @param start 开始权重
     * @param end 结束权重
     * @return 区间内的元素条数
     */
    @GetMapping("/cache/zrangebyscorecount/{key}/{start}/{end}")
    public int zRangeByScoreCount(@PathVariable(value = "key") String key,
                         @PathVariable(value = "start") Long start,
                         @PathVariable(value = "end") Long end){
        log.info("【缓存模块】 zRangeByScoreCount方法，查询key={},start={},end={}",key,start,end);
        //使用Double.parseDouble()把long转换成double可以避免精度丢失
        Set<Object> values = redisClient.zRangeByScore(key, Double.parseDouble(start + ""), Double.parseDouble(end + ""));
        if(values!=null){
            return values.size();
        }
        return 0;
    }

    //根据zset的key和member删除这个元素
    @DeleteMapping("/cache/zremove/{key}/{member}")
    public void zRemove(@PathVariable(value = "key") String key,@PathVariable(value = "member")String member){
        log.info("【缓存模块】 zRemove方法，删除key={},member={}",key,member);
        redisClient.zRemove(key,member);
    }

    /**
     * hash结构field自增(减)函数
     * @param key key
     * @param field 字段
     * @param delta 自增值，正值自增，负值自减
     */
    @PostMapping("/cache/hincrby/{key}/{field}/{delta}")
    public Long hIncrBy(@PathVariable(value = "key") String key,
                        @PathVariable(value = "field")String field,
                        @PathVariable(value = "delta")Long delta){
        log.info("【缓存模块】 hIncrBy方法，自增key={},feild={},number={}",key,field,delta);
        Long result = redisClient.hIncrementBy(key, field, delta);
        log.info("【缓存模块】 hIncrBy方法，自增key={},feild={},number={},剩余余额为{}",key,field,delta,result);
        return result;
    }

    @PostMapping("/cache/keys/{pattern}")
    public Set<String> keys(@PathVariable String pattern){
        log.info("【缓存模块】 keys方法，根据pattern查询key的信息,pattern={}",pattern);
        Set<String> keys = redisTemplate.keys(pattern);
        log.info("【缓存模块】 keys方法，根据pattern查询key的信息,pattern={},查询出全部的key信息,keys={}",pattern,keys);
        return keys;
    }
}
