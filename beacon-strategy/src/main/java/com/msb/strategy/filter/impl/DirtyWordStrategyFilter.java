package com.msb.strategy.filter.impl;

import com.msb.common.constant.CacheConstant;
import com.msb.common.model.StandardSubmit;
import com.msb.strategy.client.BeaconCacheClient;
import com.msb.strategy.filter.StrategyFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 基于IK分词器进行敏感词校验
 */
@Service(value = "dirtyword")
@Slf4j
public class DirtyWordStrategyFilter implements StrategyFilter {

    @Autowired
    private BeaconCacheClient beaconCacheClient;

    @Override
    public void strategy(StandardSubmit submit) {

        log.info("【策略模块-敏感词校验】 校验ing......");
        //1.获取短信内容
        String text = submit.getText();

        //2.对短信内容分词，将分词结果存储到set中
        Set<String> contents=new HashSet<>();
        StringReader stringReader = new StringReader(text);
        //使用ik分词器
        IKSegmenter ikSegmenter = new IKSegmenter(stringReader,false);
        Lexeme lex=null;
        while (true){
            try {
                if((lex=ikSegmenter.next())==null){
                    break;
                }
            } catch (IOException e) {
                log.info("【策略模块-敏感词校验】 IK分词器在处理短信内容时出现异常,e={}",e.getMessage());
            }
            contents.add(lex.getLexemeText());
        }

        //3.调用cache模块的增，交集，删方法获得交集结果
        //这里第一个key要保证全局唯一
        Set<Object> dirtyWords = beaconCacheClient.sinterStr(UUID.randomUUID().toString(),
                CacheConstant.DIRTY_WORD, contents.toArray(new String[]{}));

        //4.判断交集是否为空
        if(dirtyWords!=null && dirtyWords.size()>0){
            //5.如果有敏感词，抛出异常或其他操作
            log.info("【策略模块-敏感词校验】 短信内容包含敏感词,dirtyWords={}",dirtyWords);
            //TODO 还需做其他处理
        }

    }
}
