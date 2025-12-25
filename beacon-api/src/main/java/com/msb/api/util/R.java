package com.msb.api.util;

import com.msb.api.vo.ResultVO;
import com.msb.common.enums.ExceptionEnums;
import com.msb.common.exception.ApiException;

/**
 * ResultVO的工具类
 */
public class R {
    public static ResultVO ok(){
        ResultVO resultVO = new ResultVO();
        resultVO.setCode(0);
        resultVO.setMsg("接收成功！");
        return resultVO;
    }

    public static ResultVO error(Integer code,String msg) {
        ResultVO resultVO = new ResultVO();
        resultVO.setCode(code);
        resultVO.setMsg(msg);
        return resultVO;
    }

    public static ResultVO error(ApiException ex) {
        ResultVO resultVO = new ResultVO();
        resultVO.setCode(ex.getCode());
        resultVO.setMsg(ex.getMessage());
        return resultVO;
    }
}
