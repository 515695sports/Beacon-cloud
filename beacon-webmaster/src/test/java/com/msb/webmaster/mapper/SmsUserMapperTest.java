package com.msb.webmaster.mapper;

import com.msb.webmaster.entity.SmsUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;
@SpringBootTest
@RunWith(SpringRunner.class)
public class SmsUserMapperTest {

    @Autowired
    private SmsUserMapper smsUserMapper;

    @Test
    public void findById(){
        SmsUser smsUser = smsUserMapper.selectByPrimaryKey(1);
        System.out.println(smsUser);
    }
}