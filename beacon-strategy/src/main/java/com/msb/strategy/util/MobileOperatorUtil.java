package com.msb.strategy.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


/**
 * 根据号码前7位 获取手机号归属地和运营商的工具
 */
@Component
public class MobileOperatorUtil {

    //通过restTemplate发送http请求
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private final String url1="https://cx.shouji.360.cn/phonearea.php?number=";
    private final String CODE="code";
    private final String DATA="data";
    private final String PROVINCE="province";
    private final String CITY="city";
    private final String SP="sp";
    private final String SPACE=" ";
    private final String SEPARATE=",";

    /**
     * 获取手机号归属地和运营商
     * @param mobile 手机号前7位
     * @return
     */
    public String getMobileInfoBy360(String mobile){
        String url=url1;
        //1.发请求获取信息
        String mobileInfoJSON = restTemplate.getForObject(url + mobile, String.class);

        //2.解析JSON
        //{"code":0,"data":{"province":"\u5317\u4eac","city":"","sp":"\u79fb\u52a8"}}
        Map map = null;
        try {
            map = objectMapper.readValue(mobileInfoJSON, Map.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Integer code = (Integer)map.get(CODE);
        if(code!=0){
            return null;
        }
        Map<String,String> areaAndOperator = (Map<String,String>)map.get(DATA);
        String province = areaAndOperator.get(PROVINCE);
        String city = areaAndOperator.get(CITY);
        String sp = areaAndOperator.get(SP);

        //3.封装为：[省 市,运营商] 格式的字符串
        return province+SPACE+city+SEPARATE+sp;
    }
}
