package com.CC.annotation;

import com.CC.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标识方法需要进行功能字段自动填充
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AutoFill {
    //数据库操作类型:update,insert
    OperationType value();

}
