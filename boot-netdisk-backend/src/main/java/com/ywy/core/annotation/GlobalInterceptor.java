package com.ywy.core.annotation;

import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.*;

/**
 * 全局拦截注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Mapping
public @interface GlobalInterceptor {
    /**
     * 校验是否登录
     * @return
     */
    boolean login() default true;

    /**
     * Check if you are a super administrator
     * @return
     */
    boolean admin() default false;

    /**
     * 是否校验参数
     * 为true时校验参数，要配合@VerifyParam注解使用
     * @return
     */
    boolean valid() default false;
}
