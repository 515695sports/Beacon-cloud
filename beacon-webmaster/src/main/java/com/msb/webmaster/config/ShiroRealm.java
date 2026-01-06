package com.msb.webmaster.config;

import com.msb.webmaster.entity.SmsUser;
import com.msb.webmaster.service.SmsUserService;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 自定义Realm
 */
@Component
public class ShiroRealm extends AuthorizingRealm {

    @Autowired
    private SmsUserService smsUserService;

    //设置加密方式和次数
    {
        HashedCredentialsMatcher credentialsMatcher = new HashedCredentialsMatcher();
        credentialsMatcher.setHashAlgorithmName("MD5");
        credentialsMatcher.setHashIterations(1024);
        this.setCredentialsMatcher(credentialsMatcher);
    }
    /**
     * 认证Authentication
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        //1.基于token拿到用户名(这里不用基于token拿密码)
        String username = (String) authenticationToken.getPrincipal();

        //2.基于用户名获取用户信息(模拟数据库操作)
        SmsUser smsUser = smsUserService.findByUsername(username);

        //3.查询完毕后，查看用户是否为null,为null直接返回即可
        if(smsUser==null){
            //用户名错误
            return null;
        }
        String salt=smsUser.getSalt();
        String password=smsUser.getPassword();

        //4.不为null，用户名正确，设置密码加密方式和信息
        SimpleAuthenticationInfo authenticationInfo=new SimpleAuthenticationInfo(smsUser,password,"shiroRealm");

        //5.封装AuthenticationInfo返回即可
        //封装盐
        authenticationInfo.setCredentialsSalt(ByteSource.Util.bytes(salt));
        return authenticationInfo;
    }

    /**
     * 授权Authorization
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }


}
