package com.msb.strategy.filter.impl;

import com.msb.common.constant.CacheConstant;
import com.msb.common.model.StandardSubmit;
import com.msb.strategy.client.BeaconCacheClient;
import com.msb.strategy.filter.StrategyFilter;
import com.msb.strategy.util.DFAUtil;
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
 * 基于DFA算法（有穷自动机）进行敏感词校验
 */
@Service(value = "dfaDirtyWord")
@Slf4j
public class DirtyWordDFAStrategyFilter implements StrategyFilter {

    @Override
    public void strategy(StandardSubmit submit) {

        log.info("【策略模块-敏感词校验】 校验ing......");
        //1.获取短信内容
        String text = submit.getText();

        //2.调用DFAUtil查看敏感词
        Set<String> dirtyWords = DFAUtil.getDirtyWords(text);

        //3.判断是否有敏感词
        if(dirtyWords!=null && dirtyWords.size()>0){
            //4.如果有敏感词，抛出异常或其他操作
            log.info("【策略模块-敏感词校验】 短信内容包含敏感词,dirtyWords={}",dirtyWords);
            //TODO 还需做其他处理
        }

    }
}
