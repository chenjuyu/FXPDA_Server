package com.fuxi.core.annotation.query;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 前台的查询时间空间的格式
 * 
 * @author
 * @version 1.0
 */
@Target({java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryTimeFormat {

    public String format();

}
