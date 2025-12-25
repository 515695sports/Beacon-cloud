package com.msb.api.controller;

import com.msb.api.filter.CheckFilterContext;
import com.msb.common.model.StandardSubmit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {

    @Autowired
    private CheckFilterContext context;

    @GetMapping("/api/test")
    public void test(){
        System.out.println("===========================");
        context.check(new StandardSubmit());
    }
}
