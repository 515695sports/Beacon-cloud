package com.msb.webmaster.service.impl;

import com.msb.webmaster.entity.SmsUser;
import com.msb.webmaster.service.SmsUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;
@SpringBootTest
//版本比较低，一定要加这个注释
@RunWith(SpringRunner.class)
public class SmsUserServiceImplTest {

    @Autowired
    private SmsUserService smsUserService;

    @Test
    public void findByUsername(){
        SmsUser admin = smsUserService.findByUsername("admin");
        System.out.println(admin);
    }
}