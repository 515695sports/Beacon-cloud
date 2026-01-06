package com.msb.webmaster.config;

import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {
    /**
     * 设置过滤器链的规则
     */
    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition(){

        //1.直接构建ShiroFilterChainDefinition的实现类
        DefaultShiroFilterChainDefinition shiroFilter=new DefaultShiroFilterChainDefinition();

        //2.配置过滤器链
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        //anon代表放行，使用的是AnonymousFilter
        filterChainDefinitionMap.put("/sys/user/login","anon");
        filterChainDefinitionMap.put("/sys/login","anon");
        filterChainDefinitionMap.put("/index.html","anon");
        filterChainDefinitionMap.put("/login.html","anon");
        filterChainDefinitionMap.put("/public/**","anon");
        filterChainDefinitionMap.put("/captcha.jpg","anon");
        filterChainDefinitionMap.put("/logout","logout");
        filterChainDefinitionMap.put("/**","authc");
        //设置
        shiroFilter.addPathDefinitions(filterChainDefinitionMap);

        //3.直接返回配置好的过滤器链
        return shiroFilter;
    }

    @Bean
    public DefaultSecurityManager securityManager(ShiroRealm shiroRealm){

        //1.构建SecurityManager实现类
        DefaultWebSecurityManager securityManager=new DefaultWebSecurityManager();

        //2.设置Realm进去
        securityManager.setRealm(shiroRealm);

        //3.返回安全管理器
        return securityManager;
    }
}
