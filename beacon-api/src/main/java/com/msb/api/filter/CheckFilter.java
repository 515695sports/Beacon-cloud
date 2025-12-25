package com.msb.api.filter;

import com.msb.common.model.StandardSubmit;

/**
 * 做策略模式的父接口
 */
public interface CheckFilter {

    void check(StandardSubmit submit);
}
