package com.fuxi.system.util;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;



/**
 * 日志记录类
 * 
 * @author mzhanker
 * 
 */
public class SysLogger {
    public static final String module = "ieap.logger";

    /**
     * 开发, debug级别
     * 
     * @param debugStr
     */
    public static void debug(String debugStr) {
        Logger log = Logger.getLogger("debug");
        log.log(module, Level.DEBUG, debugStr, null);
    }

    public static void infoMessage(String infoStr) {
        Logger log = Logger.getLogger("info");
        log.log(module, Level.INFO, infoStr, null);
    }

    /**
     * 信息级别
     * 
     * @param infoStr
     */
    public static void info(String infoStr) {
        Logger log = Logger.getLogger("info");
        log.log(module, Level.INFO, infoStr, null);
        log2Db("info", infoStr);
    }

    public static void info1(String infoStr) {
        Logger log = Logger.getLogger("info");
        log.log(module, Level.INFO, infoStr, null);

    }

    public static void error1(String errStr, Throwable e) {
        Logger log = Logger.getLogger("system");
        log.log(module, Level.ERROR, errStr, e);
    }

    /**
     * 错误级别
     * 
     * @param errStr
     * @param e
     */

    public static void error(String errStr, Throwable e) {
        Logger log = Logger.getLogger("system");
        log.log(module, Level.ERROR, errStr, e);
        log2Db("error", errStr);
    }

    /**
     * 错误级别
     * 
     * @param errStr
     */
    public static void error(String errStr) {
        Logger log = Logger.getLogger("system");
        log.log(module, Level.ERROR, errStr, null);
        log2Db("error", errStr);
    }

    /**
     * 警告级别
     * 
     * @param warnStr
     */
    public static void warn(String warnStr) {
        Logger log = Logger.getLogger("warn");
        log.log(module, Level.WARN, warnStr, null);
        log2Db("warn", warnStr);
    }



    public static void log2Db(String priority, String message) {
        try {

        } catch (Exception e) {
            e.printStackTrace(System.out);

        }

    }

}
