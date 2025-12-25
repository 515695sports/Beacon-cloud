package com.msb.strategy.util;

import com.msb.common.model.StandardSubmit;

import java.util.Map;

/**
 * 通道转换留的口子
 */
public class ChannelTransferUtil {
    /**
     * 留的这个口子暂时什么都不做
     * @param submit
     * @param channel 当前选中的通道
     * @return
     */
    public static Map transfer(StandardSubmit submit,Map channel){
        //do nothing
        return channel;
    }
}
