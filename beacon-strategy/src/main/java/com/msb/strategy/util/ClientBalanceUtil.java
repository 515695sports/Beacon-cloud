package com.msb.strategy.util;

public class ClientBalanceUtil {

    /**
     * 后期如果要给客户指定欠费的额度等级，再去重写方法
     * @param clientId 客户id
     * @return
     */
    public static Long getClientAmountLimit(Long clientId){
        //200r
        return -10000L;
    }
}
