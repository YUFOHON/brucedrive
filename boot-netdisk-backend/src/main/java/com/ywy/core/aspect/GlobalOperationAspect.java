package com.ywy.core.aspect;

import com.ywy.core.constants.SystemConstants;
import com.ywy.core.annotation.GlobalInterceptor;
import com.ywy.core.annotation.Valid;
import com.ywy.core.enums.ResponseCodeEnum;
import com.ywy.core.exception.BusinessException;
import com.ywy.pojo.dto.SessionLoginUserDto;
import com.ywy.util.ValidUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 全局操作切面：登录校验、参数校验
 */
@Slf4j
@Aspect
@Component
public class GlobalOperationAspect {
    private static final String TYPE_STRING = "java.lang.String";
    private static final String TYPE_INTEGER = "java.lang.Integer";
    private static final String TYPE_LONG = "java.lang.Long";


    /**
     *      Pointcuts: A pointcut is a predicate that matches one or more joinpoints.
     *     It's used to specify where the aspects should be applied.
     *      Pointcuts are defined using expressions, often based on method signatures.
     */
    @Pointcut("@annotation(com.ywy.core.annotation.GlobalInterceptor)")
    private void requestInterceptor() {}

    @Before("requestInterceptor()")
    public void interceptorJob(JoinPoint point) {
        try {
            Object target = point.getTarget();
            Object[] args = point.getArgs();
            String methodName = point.getSignature().getName();
            Class<?>[] parameterTypes = ((MethodSignature) point.getSignature()).getMethod().getParameterTypes();
            Method method = target.getClass().getMethod(methodName, parameterTypes);
            /*

            The GlobalInterceptor annotation likely contains properties that control the behavior
            of the global interceptor, such as whether login verification or parameter validation
            should be performed.
             */
            GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
            if ( interceptor == null)
                return;

            // 登录校验
            if (interceptor.login() || interceptor.admin())
                checkLogin(interceptor.admin());

            // 参数校验
            if (interceptor.valid()) 
                checkParams(method, args);

        } catch (BusinessException e) {
            log.error("Global interceptor exception", e);
            throw e;
        } catch (Exception e) {
            log.error("Global interceptor exception", e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }
    }

    /**
     * LOGIN verification
     * @param checkAdmin
     */
    private void checkLogin(boolean checkAdmin) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        SessionLoginUserDto loginUserDto = (SessionLoginUserDto) session.getAttribute(SystemConstants.SESSION_LOGIN_USER);
// Login session invalid
        if (loginUserDto == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

// Only super administrators can access
        if (checkAdmin && !loginUserDto.getIsAdmin()) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
    }

    /**
     * PARAM verification
     * @param method
     * @param args
     */
    private void checkParams(Method method, Object[] args) {
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Object value = args[i];
            Valid valid = parameter.getAnnotation(Valid.class);
            if (null == valid) {
                continue;
            }
            // 基本參數類型
            String typeName = parameter.getParameterizedType().getTypeName();
            if (TYPE_STRING.equals(typeName) || TYPE_INTEGER.equals(typeName) || TYPE_LONG.equals(typeName)) {
                checkValue(parameter.getName(), value, valid);
            } else {
                // Object Type
                checkObjValue(parameter, value);
            }
        }
    }

    /**
     * Basic type parameter verification
     * @param paramName
     * @param value
     * @param valid
     */
    private void checkValue(String paramName, Object value, Valid valid) {
        Boolean isEmpty = value == null || StringUtils.isEmpty(value.toString());
        Integer length = value == null ? 0 : value.toString().length();

        ResponseCodeEnum code601 = ResponseCodeEnum.CODE_602;
        // 检验空
        if (isEmpty && valid.required()) {
            throw new BusinessException(code601.getCode(), code601.getMsg() + "，" + paramName + "Can't be empty");
        }

        // 检验长度
        if (!isEmpty && (valid.max() != -1 && valid.max() < length || valid.min() != -1 && valid.min() > length)) {
            throw new BusinessException(code601.getCode(), code601.getMsg() + "，" + paramName + "length must between " + valid.min() + "~" + valid.max());
        }

        // 检验正则
        if (!isEmpty && StringUtils.isNotEmpty(valid.regex().getRegex()) && !ValidUtil.verify(valid.regex(), String.valueOf(value))) {
            throw new BusinessException(code601.getCode(), code601.getMsg() + "，" + paramName + "format is： " + valid.regex().getDesc());
        }
    }

    /**
     * Object type parameter verification
     * @param parameter
     * @param objValue
     */
    private void checkObjValue(Parameter parameter, Object objValue) {
        try {
            String typeName = parameter.getParameterizedType().getTypeName();
            Class<?> clazz = Class.forName(typeName);
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                Valid valid = field.getAnnotation(Valid.class);
                if (null == valid) {
                    continue;
                }
                field.setAccessible(true);
                Object value = field.get(objValue);
                checkValue(field.getName(), value, valid);
            }
        } catch (Exception e) {
            log.error("Check parameter exception", e);
            throw new BusinessException(ResponseCodeEnum.CODE_602);
        }
    }
}
