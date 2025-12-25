package com.msb.strategy.util;

import cn.hutool.dfa.WordTree;
import com.msb.common.constant.CacheConstant;
import com.msb.strategy.client.BeaconCacheClient;

import java.util.List;
import java.util.Set;

/**
 * 利用Hutool提供的DFA工具实现敏感词校验
 * 这个工具可以忽略特殊符号->dirtyWords=[刀架保安, 代%孕]
 */
public class HutoolDFAUtil {

    private static WordTree wordTree=new WordTree();

    /**
     * 初始化敏感词树
     */
    static {
        //获取spring容器中的cacheCient
        BeaconCacheClient cacheClient = (BeaconCacheClient)SpringUtil.getBeanByClass(BeaconCacheClient.class);
        //调用cacheClient获取敏感词库
        Set<String> dirtyWords = cacheClient.smember(CacheConstant.DIRTY_WORD);
        //调用WordTree的add方法构建敏感词树
        wordTree.addWords(dirtyWords);
    }

    public static List<String> getDirtyWord(String text){
        return wordTree.matchAll(text);
    }
}
