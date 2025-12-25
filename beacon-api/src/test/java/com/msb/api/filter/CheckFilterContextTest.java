package com.msb.api.filter;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@SpringBootTest
public class CheckFilterContextTest {

    @Autowired
    private CheckFilterContext context;

    @Test
    public void checkTest(){
        context.check(null);
    }

    @Test
    public void testSignCut(){
        String message="【智能空调】";
        System.out.println(message.substring(1,message.indexOf("】")));
    }
}