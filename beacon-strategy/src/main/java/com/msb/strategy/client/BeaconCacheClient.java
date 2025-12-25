package com.msb.strategy.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@FeignClient(value = "beacon-cache")
public interface BeaconCacheClient {

    //根据key和field获取某个value的值(返回String)
    @GetMapping("/cache/hget/{key}/{field}")
    String hget(@PathVariable(value = "key")String key, @PathVariable(value = "field") String field);

    //根据key和field获取某个value的值(返回Integer)
    @GetMapping("/cache/hget/{key}/{field}")
    Integer hgetInteger(@PathVariable(value = "key")String key, @PathVariable(value = "field") String field);

    //获取hash结构的数据
    @GetMapping("/cache/hgetall/{key}")
    public Map hGetAll(@PathVariable(value = "key") String key);

    //查询string结构
    @GetMapping(value = "/cache/get/{key}")
    String get(@PathVariable(value = "key") String key);

    /**
     * 对string集合进行存，交集，删
     * @param key 要存入的集合key
     * @param sinterKey 做交集的key
     * @param value 要存入的集合
     * @return 交集结果
     */
    @PostMapping(value = "/cache/sinterstr/{key}/{sinterKey}")
    Set<Object> sinterStr(@PathVariable(value = "key") String key, @PathVariable(value = "sinterKey")String sinterKey, @RequestBody String...value);

    //查询set结构key对应的所有数据
    @GetMapping("/cache/smember/{key}")
    Set<String> smember(@PathVariable(value = "key")String key);

    //查询set结构key对应的所有数据
    @GetMapping("/cache/smember/{key}")
    Set<Map> smemberMap(@PathVariable(value = "key")String key);

    /**
     * zset添加一个元素的到key集合的方法
     * @param key 集合名
     * @param score 权重-long
     * @param member 元素-Object
     * @return boolean
     */
    @PostMapping("/cache/zaddLong/{key}/{score}/{member}")
    boolean zaddLong(@PathVariable(value = "key") String key,
                            @PathVariable(value = "score") Long score,
                            @PathVariable(value = "member") Object member);

    /**
     * zset获取key集合权重在[start,end]之前的元素个数
     * @param key 集合名
     * @param start 开始权重
     * @param end 结束权重
     * @return 区间内的元素条数
     */
    @GetMapping("/cache/zrangebyscorecount/{key}/{start}/{end}")
    int zRangeByScoreCount(@PathVariable(value = "key") String key,
                                  @PathVariable(value = "start") Long start,
                                  @PathVariable(value = "end") Long end);

    //根据zset的key和member删除这个元素
    @DeleteMapping("/cache/zremove/{key}/{member}")
    void zRemove(@PathVariable(value = "key") String key,@PathVariable(value = "member")String member);

    /**
     * hash结构field自增(减)函数
     * @param key key
     * @param field 字段
     * @param delta 自增值，正值自增，负值自减
     * @return 余额
     */
    @PostMapping("/cache/hincrby/{key}/{field}/{delta}")
    Long hIncrBy(@PathVariable(value = "key") String key,
                        @PathVariable(value = "field")String field,
                        @PathVariable(value = "delta")Long delta);

}
