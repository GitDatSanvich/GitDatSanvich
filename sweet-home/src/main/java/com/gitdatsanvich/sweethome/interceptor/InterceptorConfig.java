/*
package com.gitdatsanvich.sweethome.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

*/
/**
 * @author TangChen
 * @date 2021/6/11 16:19
 *//*

@Configuration
@Slf4j
public class InterceptorConfig extends WebMvcConfigurerAdapter {

    @Value("#{'${intercept.creditsInterceptPaths}'.split(';')}")
    private List<String> creditsInterceptPaths;
    @Value("#{'${intercept.messageInterceptPaths}'.split(';')}")
    private List<String> messageInterceptPaths;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        for (String creditsPath : creditsInterceptPaths) {
            if (creditsPath != null && !"".equals(creditsPath.trim())) {
                log.info("需要拦截的url::" + creditsPath);
                registry.addInterceptor(CreditsInterceptor()).addPathPatterns(creditsPath);
            }
        }
        for (String messagePath : messageInterceptPaths) {
            if (messagePath != null && !"".equals(messagePath.trim())) {
//                messagePath = "**" + messagePath + "/**";
                System.out.println(messagePath);
                registry.addInterceptor(MessageInterptor()).addPathPatterns(messagePath);
            }
        }
//        System.out.println(creditsInterceptPaths);
        // 拦截所有请求
//        registry.addInterceptor(CreditsInterptor()).addPathPatterns("/**");
//        registry.addInterceptor(MessageInterptor()).addPathPatterns("/**");
        super.addInterceptors(registry);
    }

    private HandlerInterceptor CreditsInterceptor() {
        return new CreditsInterceptor();
    }
}
*/
