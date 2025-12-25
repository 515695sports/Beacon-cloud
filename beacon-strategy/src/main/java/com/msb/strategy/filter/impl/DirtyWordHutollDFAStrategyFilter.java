package com.msb.strategy.filter.impl;

import com.msb.common.constant.CacheConstant;
import com.msb.common.constant.RabbitMQConstant;
import com.msb.common.constant.SmsConstant;
import com.msb.common.enums.ExceptionEnums;
import com.msb.common.exception.StrategyException;
import com.msb.common.model.StandardReport;
import com.msb.common.model.StandardSubmit;
import com.msb.strategy.client.BeaconCacheClient;
import com.msb.strategy.filter.StrategyFilter;
import com.msb.strategy.util.DFAUtil;
import com.msb.strategy.util.ErrorSendMessageUtil;
import com.msb.strategy.util.HutoolDFAUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * 基于Hutool工具类提供的DFA进行敏感词校验
 */
@Service(value = "hutoolDFADirtyWord")
@Slf4j
public class DirtyWordHutollDFAStrategyFilter implements StrategyFilter {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private BeaconCacheClient beaconCacheClient;
    @Autowired
    private ErrorSendMessageUtil errorSendMessageUtil;

    @Override
    public void strategy(StandardSubmit submit) {

        log.info("【策略模块-敏感词校验】 校验ing......");
        //1.获取短信内容
        String text = submit.getText();

        //2.调用HutoolDFAUtil查看敏感词
        List<String> dirtyWords = HutoolDFAUtil.getDirtyWord(text);

        //3.判断是否有敏感词
        if(dirtyWords!=null && dirtyWords.size()>0){
            //4.如果有敏感词，抛出异常或其他操作
            log.info("【策略模块-敏感词校验】 短信内容包含敏感词,dirtyWords={}",dirtyWords);
            //TODO 还需做其他处理
            //===============================发送写日志=============================
            //封装错误信息和reportState
            submit.setErrorMsg(ExceptionEnums.HAVE_DIRTY_WORD.getMsg()+" dirtyWord= "+ dirtyWords.toString());
            errorSendMessageUtil.sendWriteLog(submit);
            //====================发送状态报告的消息前，需要将report对象数据封装====================
            errorSendMessageUtil.sendPushReport(submit);
            //============================抛出有敏感词异常=====================================
            throw new StrategyException(ExceptionEnums.HAVE_DIRTY_WORD);
        }
        log.info("【策略模块-敏感词校验】 校验通过，没有敏感词信息");

    }

}
