package com.fuxi.system.util;

public class LoadUserCount {

    // 系统参数设置
    public static String regId = null; // 注册ID
    public static String regName = null; // 注册名
    public static String corpName = null; // 账套名称
    public static int userCount = 0; // 在线人数
    public static boolean flag = true; // 系统参数设置正误
    public static int relationMovein = 0;
    public static int checkStockOpt = 1; // 负库存检查时用的库存类别 0:可用库存,1:实际库存,2:可发库存

    // 货品颜色,尺码信息
    public static int maxSizeLength;
    public static int minSizeLength;
    public static int maxColorLength;
    public static int minColorLength;

    // 货品颜色类型
    public static int colorOption;

    // 打印固定设置
    public static String address;
    public static String phone;
    public static String mobile;
    public static String bankTypeOne;
    public static String bankOneNo;
    public static String bankCardOneName;
    public static String bankTypeTwo;
    public static String bankTwoNo;
    public static String bankCardTwoName;

}
