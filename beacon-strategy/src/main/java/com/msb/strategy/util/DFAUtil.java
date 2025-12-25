package com.msb.strategy.util;

import com.msb.common.constant.CacheConstant;
import com.msb.strategy.client.BeaconCacheClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 有穷自动机算法匹配敏感词
 */

public class DFAUtil {
    //敏感词树
    private static Map dfaMap=new HashMap();
    private static final String IS_END="isEnd";
    private static final String NOT_END="0";
    private static final String ALREADY_END="1";

    //父类静态代码块 → 子类静态代码块 → 父类实例代码块 → 父类构造器 → 子类实例代码块 → 子类构造器
    //静态代码块在第一次加载类时执行，实例代码块每次创建对象时执行
    //初始化敏感词树
    static {
        //获取spring容器中的beaconCacheClient
        BeaconCacheClient cacheClient = (BeaconCacheClient) SpringUtil.getBeanByClass(BeaconCacheClient.class);
        //获取存储在redis的全部敏感词
        Set<String> dirtyWords = cacheClient.smember(CacheConstant.DIRTY_WORD);
        //调用createTree构建敏感词树
        creatTree(dirtyWords);
    }


    /*public static void main(String[] args) {
        //敏感词库
        Set<String> dirtyWords=new HashSet<>();
        dirtyWords.add("三胖");
        dirtyWords.add("山炮");
        dirtyWords.add("三胖啊啊");
        dirtyWords.add("瘦啊");

        //创建敏感词树
        creatTree(dirtyWords);
        for (Object o : dfaMap.entrySet()) {
            System.out.println(o);
        }
        //测试敏感词校验
        String text="你三瘦啊山炮";
        Set<String> words = getDirtyWords(text);
        System.out.println(words);
    }*/

    /**
     * 构建敏感词树
     * @param dirtyWords 敏感词库
     */
    public static void creatTree(Set<String> dirtyWords){
        //声明一个HashMap作为临时存储
        Map nowMap;

        //遍历敏感词库
        for (String dirtyWord : dirtyWords) {
            nowMap=dfaMap;
            //获取敏感词的每个字
            for (int i = 0; i < dirtyWord.length(); i++) {
                String word = String.valueOf(dirtyWord.charAt(i));
                Map map=(Map)nowMap.get(word);
                //当前字符之前不存在,存入当前字符和空value->map
                if(map==null){
                    map=new HashMap();
                    nowMap.put(word,map);
                }
                nowMap=map;
                //如果这个word对应的isEnd存在且为1，不用管
                if(nowMap.containsKey(IS_END) && nowMap.get(IS_END).equals(ALREADY_END)){
                    continue;
                }
                //isEnd不存在或isEnd=0
                if(i==dirtyWord.length()-1){
                    //如果该word是单词里的最后一个字，isEnd设为1
                    map.put(IS_END,ALREADY_END);
                }else {
                    map.putIfAbsent(IS_END,NOT_END); //如果不存在设为0
                }
            }
        }
    }

    /**
     * 基于敏感词树，获取文本中的敏感词并返回
     * @param text 待校验文本
     * @return 校验到的敏感词
     */
    public static Set<String> getDirtyWords(String text){//你三瘦啊是个大山炮
        //作为返回结果存储敏感词
        Set<String> dirtyWords=new HashSet<>();
        //遍历文本内容每个字
        for (int i = 0; i < text.length(); i++) {
            int nextLen=0;
            int dirtyLen=0;
            Map nowMap=dfaMap;
            //外层复制索引向后移动，匹配最外层的key
            //内层负责在外层匹配上之后，继续向后移动匹配内层的key
            for (int j = i; j < text.length(); j++) {
                String word = String.valueOf(text.charAt(j));
                nowMap=(Map)nowMap.get(word);
                if(nowMap==null){ //没匹配上最外层
                    break;
                }else { //匹配上最外层
                    dirtyLen++;
                    if(ALREADY_END.equals(nowMap.get(IS_END))){ //内层匹配到了末尾
                        nextLen=dirtyLen;
                    }
                }
            }
            if(nextLen>0){ //匹配到了一个敏感词
                dirtyWords.add(text.substring(i,i+nextLen));
                //移动外层索引:这里-1的原因是外层for循环会i++
                i=i+nextLen-1;
            }
        }
        return dirtyWords;
    }
}
