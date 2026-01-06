package com.msb.webmaster.service;

import com.msb.webmaster.entity.SmsUser;

/**
 * 用户信息的service
 */
public interface SmsUserService {
    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return
     */
    SmsUser findByUsername(String username);
}
