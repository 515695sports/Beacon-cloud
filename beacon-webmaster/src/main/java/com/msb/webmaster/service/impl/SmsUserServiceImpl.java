package com.msb.webmaster.service.impl;

import com.msb.webmaster.entity.SmsUser;
import com.msb.webmaster.entity.SmsUserExample;
import com.msb.webmaster.mapper.SmsUserMapper;
import com.msb.webmaster.service.SmsUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SmsUserServiceImpl implements SmsUserService {

    @Autowired
    private SmsUserMapper smsUserMapper;

    @Override
    public SmsUser findByUsername(String username) {
        //1.封装查询条件
        SmsUserExample example=new SmsUserExample();
        SmsUserExample.Criteria criteria = example.createCriteria();
        criteria.andUsernameEqualTo(username);

        //2.基于smsUserMapper查询
        List<SmsUser> smsUsers = smsUserMapper.selectByExample(example);

        //3.返回
        return smsUsers!=null ? smsUsers.get(0) : null;
    }
}
