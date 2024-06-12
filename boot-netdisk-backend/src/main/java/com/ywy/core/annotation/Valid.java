package com.ywy.core.annotation;

import com.ywy.core.enums.VerifyRegexEnum;
import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.*;

/**
 * Parameter verification annotation
 * When used, the @GlobalInterceptor annotation parameter checkParams must be true to work
 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Valid {
    /**
     * Whether the parameter can be empty
     * @return
     */
    boolean required() default false;

    /**
     * Minimum parameter length
     * @return
     */
    int min() default -1;

    /**
     * Maximum parameter length
     * @return
     */
    int max() default -1;

    /**
     * Formal verification rules
     * @return
     */
    VerifyRegexEnum regex() default VerifyRegexEnum.NO;
}
