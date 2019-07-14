package com.fuxi.system.util;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 字符串处理及转换工具类
 * 
 * @author
 */
public class StringUtil {
    private static Pattern numericPattern = Pattern.compile("^[0-9\\-]+$");
    private static Pattern numericStringPattern = Pattern.compile("^[0-9\\-\\-]+$");
    private static Pattern floatNumericPattern = Pattern.compile("^[0-9\\-\\.]+$");
    private static Pattern abcPattern = Pattern.compile("^[a-z|A-Z]+$");
    public static final String splitStrPattern = ",|，|;|；|、|\\.|。|-|_|\\(|\\)|\\[|\\]|\\{|\\}|\\\\|/| |　|\"";
    private static Log logger = LogFactory.getLog(StringUtil.class);

    /**
     * 判断是否数字表示
     * 
     * @param src 源字符串
     * @return 是否数字的标志
     */
    public static boolean isNumeric(String src) {
        boolean return_value = false;
        if (src != null && src.length() > 0) {
            Matcher m = numericPattern.matcher(src);
            if (m.find()) {
                return_value = true;
            }
        }
        return return_value;
    }

    /**
     * 判断是否数字表示
     * 
     * @param src 源字符串
     * @return 是否数字的标志
     */
    public static boolean isNumericString(String src) {
        boolean return_value = false;
        if (src != null && src.length() > 0) {
            Matcher m = numericStringPattern.matcher(src);
            if (m.find()) {
                return_value = true;
            }
        }
        return return_value;
    }

    /**
     * 判断是否纯字母组合
     * 
     * @param src 源字符串
     * @return 是否纯字母组合的标志
     */
    public static boolean isABC(String src) {
        boolean return_value = false;
        if (src != null && src.length() > 0) {
            Matcher m = abcPattern.matcher(src);
            if (m.find()) {
                return_value = true;
            }
        }
        return return_value;
    }

    /**
     * 判断是否浮点数字表示
     * 
     * @param src 源字符串
     * @return 是否数字的标志
     */
    public static boolean isFloatNumeric(String src) {
        boolean return_value = false;
        if (src != null && src.length() > 0) {
            Matcher m = floatNumericPattern.matcher(src);
            if (m.find()) {
                return_value = true;
            }
        }
        return return_value;
    }

    /**
     * 把string array or list用给定的符号symbol连接成一个字符串
     * 
     * @param array
     * @param symbol
     * @return
     */
    public static String joinString(List array, String symbol) {
        String result = "";
        if (array != null) {
            for (int i = 0; i < array.size(); i++) {
                String temp = array.get(i).toString();
                if (temp != null && temp.trim().length() > 0)
                    result += (temp + symbol);
            }
            if (result.length() > 1)
                result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    public static String subStringNotEncode(String subject, int size) {
        if (subject != null && subject.length() > size) {
            subject = subject.substring(0, size) + "...";
        }
        return subject;
    }

    /**
     * 截取字符串　超出的字符用symbol代替 　　
     * 
     * @param len 　字符串长度　长度计量单位为一个GBK汉字　　两个英文字母计算为一个单位长度
     * @param str
     * @param symbol
     * @return
     */
    public static String getLimitLengthString(String str, int len, String symbol) {
        int iLen = len * 2;
        int counterOfDoubleByte = 0;
        String strRet = "";
        try {
            if (str != null) {
                byte[] b = str.getBytes("GBK");
                if (b.length <= iLen) {
                    return str;
                }
                for (int i = 0; i < iLen; i++) {
                    if (b[i] < 0) {
                        counterOfDoubleByte++;
                    }
                }
                if (counterOfDoubleByte % 2 == 0) {
                    strRet = new String(b, 0, iLen, "GBK") + symbol;
                    return strRet;
                } else {
                    strRet = new String(b, 0, iLen - 1, "GBK") + symbol;
                    return strRet;
                }
            } else {
                return "";
            }
        } catch (Exception ex) {
            return str.substring(0, len);
        } finally {
            strRet = null;
        }
    }

    /**
     * 截取字符串　超出的字符用symbol代替 　　
     * 
     * @param len 　字符串长度　长度计量单位为一个GBK汉字　　两个英文字母计算为一个单位长度
     * @param str
     * @param symbol
     * @return12
     */
    public static String getLimitLengthString(String str, int len) {
        return getLimitLengthString(str, len, "...");
    }

    /**
     * 
     * 截取字符，不转码
     * 
     * @param subject
     * @param size
     * @return
     */
    public static String subStrNotEncode(String subject, int size) {
        if (subject.length() > size) {
            subject = subject.substring(0, size);
        }
        return subject;
    }

    /**
     * 把string array or list用给定的符号symbol连接成一个字符串
     * 
     * @param array
     * @param symbol
     * @return
     */
    public static String joinString(String[] array, String symbol) {
        String result = "";
        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                String temp = array[i];
                if (temp != null && temp.trim().length() > 0)
                    result += (temp + symbol);
            }
            if (result.length() > 1)
                result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    /**
     * 取得字符串的实际长度（考虑了汉字的情况）
     * 
     * @param SrcStr 源字符串
     * @return 字符串的实际长度
     */
    public static int getStringLen(String SrcStr) {
        int return_value = 0;
        if (SrcStr != null) {
            char[] theChars = SrcStr.toCharArray();
            for (int i = 0; i < theChars.length; i++) {
                return_value += (theChars[i] <= 255) ? 1 : 2;
            }
        }
        return return_value;
    }

    /**
     * 检查数据串中是否包含非法字符集
     * 
     * @param str
     * @return [true]|[false] 包含|不包含
     */
    public static boolean check(String str) {
        String sIllegal = "'\"";
        int len = sIllegal.length();
        if (null == str)
            return false;
        for (int i = 0; i < len; i++) {
            if (str.indexOf(sIllegal.charAt(i)) != -1)
                return true;
        }

        return false;
    }

    /***************************************************************************
     * getHideEmailPrefix - 隐藏邮件地址前缀。
     * 
     * @param email - EMail邮箱地址 例如: linwenguo@koubei.com 等等...
     * @return 返回已隐藏前缀邮件地址, 如 *********@koubei.com.
     * @version 1.0 (2006.11.27) Wilson Lin
     **************************************************************************/
    public static String getHideEmailPrefix(String email) {
        if (null != email) {
            int index = email.lastIndexOf('@');
            if (index > 0) {
                email = repeat("*", index).concat(email.substring(index));
            }
        }
        return email;
    }

    /***************************************************************************
     * repeat - 通过源字符串重复生成N次组成新的字符串。
     * 
     * @param src - 源字符串 例如: 空格(" "), 星号("*"), "浙江" 等等...
     * @param num - 重复生成次数
     * @return 返回已生成的重复字符串
     * @version 1.0 (2006.10.10) Wilson Lin
     **************************************************************************/
    public static String repeat(String src, int num) {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < num; i++)
            s.append(src);
        return s.toString();
    }

    /**
     * 根据指定的字符把源字符串分割成一个数组
     * 
     * @param src
     * @return
     */
    public static List<String> parseString2ListByCustomerPattern(String pattern, String src) {

        if (src == null)
            return null;
        List<String> list = new ArrayList<String>();
        String[] result = src.split(pattern);
        for (int i = 0; i < result.length; i++) {
            list.add(result[i]);
        }
        return list;
    }

    /**
     * 根据指定的字符把源字符串分割成一个数组
     * 
     * @param src
     * @return
     */
    public static List<String> parseString2ListByPattern(String src) {
        String pattern = "，|,|、|。";
        return parseString2ListByCustomerPattern(pattern, src);
    }

    /**
     * 格式化一个float
     * 
     * @param format 要格式化成的格式 such as #.00, #.#
     */

    public static String formatFloat(float f, String format) {
        DecimalFormat df = new DecimalFormat(format);
        return df.format(f);
    }

    /**
     * 判断是否是空字符串 null和"" 都返回 true
     * 
     * @author Robin Chang
     * @param s
     * @return
     */
    public static boolean isEmpty(String s) {
        if (s != null && !s.equals("")) {
            return false;
        }
        return true;
    }

    public static boolean isEmpty(Object s) {
        if (s != null && !s.toString().equals("")) {
            return false;
        }
        return true;
    }

    public static boolean isEmptyOrZore(Integer s) {
        if (s != null && s != 0) {
            return false;
        }
        return true;
    }

    /**
     * 自定义的分隔字符串函数 例如: 1,2,3 =>[1,2,3] 3个元素 ,2,3=>[,2,3] 3个元素 ,2,3,=>[,2,3,] 4个元素 ,,,=>[,,,] 4个元素
     * 
     * 5.22算法修改，为提高速度不用正则表达式 两个间隔符,,返回""元素
     * 
     * @param split 分割字符 默认,
     * @param src 输入字符串
     * @return 分隔后的list
     * @author Robin
     */
    public static List<String> splitToList(String split, String src) {
        // 默认,
        String sp = ",";
        if (split != null && split.length() == 1) {
            sp = split;
        }
        List<String> r = new ArrayList<String>();
        int lastIndex = -1;
        int index = src.indexOf(sp);
        if (-1 == index && src != null) {
            r.add(src);
            return r;
        }
        while (index >= 0) {
            if (index > lastIndex) {
                r.add(src.substring(lastIndex + 1, index));
            } else {
                r.add("");
            }

            lastIndex = index;
            index = src.indexOf(sp, index + 1);
            if (index == -1) {
                r.add(src.substring(lastIndex + 1, src.length()));
            }
        }
        return r;
    }

    /**
     * 把 名=值 参数表转换成字符串 (a=1,b=2 =>a=1&b=2)
     * 
     * @param map
     * @return
     */
    public static String linkedHashMapToString(LinkedHashMap<String, String> map) {
        if (map != null && map.size() > 0) {
            String result = "";
            Iterator it = map.keySet().iterator();
            while (it.hasNext()) {
                String name = (String) it.next();
                String value = (String) map.get(name);
                result += (result.equals("")) ? "" : "&";
                result += String.format("%s=%s", name, value);
            }
            return result;
        }
        return null;
    }

    /**
     * 解析字符串返回 名称=值的参数表 (a=1&b=2 => a=1,b=2)
     * 
     * @see test.koubei.util.StringUtilTest#testParseStr()
     * @param str
     * @return
     */
    @SuppressWarnings("unchecked")
    public static LinkedHashMap<String, String> toLinkedHashMap(String str) {
        if (str != null && !str.equals("") && str.indexOf("=") > 0) {
            LinkedHashMap result = new LinkedHashMap();

            String name = null;
            String value = null;
            int i = 0;
            while (i < str.length()) {
                char c = str.charAt(i);
                switch (c) {
                    case 61: // =
                        value = "";
                        break;
                    case 38: // &
                        if (name != null && value != null && !name.equals("")) {
                            result.put(name, value);
                        }
                        name = null;
                        value = null;
                        break;
                    default:
                        if (value != null) {
                            value = (value != null) ? (value + c) : "" + c;
                        } else {
                            name = (name != null) ? (name + c) : "" + c;
                        }
                }
                i++;

            }

            if (name != null && value != null && !name.equals("")) {
                result.put(name, value);
            }

            return result;

        }
        return null;
    }

    /**
     * 根据输入的多个解释和下标返回一个值
     * 
     * @param captions 例如:"无,爱干净,一般,比较乱"
     * @param index 1
     * @return 一般
     */
    public static String getCaption(String captions, int index) {
        if (index > 0 && captions != null && !captions.equals("")) {
            String[] ss = captions.split(",");
            if (ss != null && ss.length > 0 && index < ss.length) {
                return ss[index];
            }
        }
        return null;
    }

    /**
     * 数字转字符串,如果num<=0 则输出"";
     * 
     * @param num
     * @return
     */
    public static String numberToString(Object num) {
        if (num == null) {
            return null;
        } else if (num instanceof Integer && (Integer) num > 0) {
            return Integer.toString((Integer) num);
        } else if (num instanceof Long && (Long) num > 0) {
            return Long.toString((Long) num);
        } else if (num instanceof Float && (Float) num > 0) {
            return Float.toString((Float) num);
        } else if (num instanceof Double && (Double) num > 0) {
            return Double.toString((Double) num);
        } else {
            return "";
        }
    }

    /**
     * 货币转字符串
     * 
     * @param money
     * @param style 样式 [default]要格式化成的格式 such as #.00, #.#
     * @return
     */

    public static String moneyToString(Object money, String style) {
        if (money != null && style != null && (money instanceof Double || money instanceof Float)) {
            Double num = (Double) money;

            if (style.equalsIgnoreCase("default")) {
                // 缺省样式 0 不输出 ,如果没有输出小数位则不输出.0
                if (num == 0) {
                    // 不输出0
                    return "";
                } else if ((num * 10 % 10) == 0) {
                    // 没有小数
                    return Integer.toString((int) num.intValue());
                } else {
                    // 有小数
                    return num.toString();
                }

            } else {
                DecimalFormat df = new DecimalFormat(style);
                return df.format(num);
            }
        }
        return null;
    }

    /**
     * 在sou中是否存在finds 如果指定的finds字符串有一个在sou中找到,返回true;
     * 
     * @param sou
     * @param find
     * @return
     */
    public static boolean strPos(String sou, String... finds) {
        if (sou != null && finds != null && finds.length > 0) {
            for (int i = 0; i < finds.length; i++) {
                if (sou.indexOf(finds[i]) > -1)
                    return true;
            }
        }
        return false;
    }

    public static boolean strPos(String sou, List<String> finds) {
        if (sou != null && finds != null && finds.size() > 0) {
            for (String s : finds) {
                if (sou.indexOf(s) > -1)
                    return true;
            }
        }
        return false;
    }

    public static boolean strPos(String sou, String finds) {
        List<String> t = splitToList(",", finds);
        return strPos(sou, t);
    }

    /**
     * 判断两个字符串是否相等 如果都为null则判断为相等,一个为null另一个not null则判断不相等 否则如果s1=s2则相等
     * 
     * @param s1
     * @param s2
     * @return
     */
    public static boolean equals(String s1, String s2) {
        if (StringUtil.isEmpty(s1) && StringUtil.isEmpty(s2)) {
            return true;
        } else if (!StringUtil.isEmpty(s1) && !StringUtil.isEmpty(s2)) {
            return s1.equals(s2);
        }
        return false;
    }

    public static int toInt(String s) {
        if (s != null && !"".equals(s.trim())) {
            try {
                return Integer.parseInt(s);
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    public static double toDouble(String s) {
        if (s != null && !"".equals(s.trim())) {
            return Double.parseDouble(s);
        }
        return 0;
    }

    /**
     * 把xml 转为object
     * 
     * @param xml
     * @return
     */
    public static Object xmlToObject(String xml) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes("UTF8"));
            XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(in));
            return decoder.readObject();
        } catch (Exception e) {
            SysLogger.error(e.getMessage(), e);
        }
        return null;
    }

    public static long toLong(String s) {
        try {
            if (s != null && !"".equals(s.trim()))
                return Long.parseLong(s);
        } catch (Exception exception) {
        }
        return 0L;
    }

    public static String simpleEncrypt(String str) {
        if (str != null && str.length() > 0) {
            // str = str.replaceAll("0","a");
            str = str.replaceAll("1", "b");
            // str = str.replaceAll("2","c");
            str = str.replaceAll("3", "d");
            // str = str.replaceAll("4","e");
            str = str.replaceAll("5", "f");
            str = str.replaceAll("6", "g");
            str = str.replaceAll("7", "h");
            str = str.replaceAll("8", "i");
            str = str.replaceAll("9", "j");
        }
        return str;

    }

    /**
     * 过滤用户输入的URL地址（防治用户广告） 目前只针对以http或www开头的URL地址 本方法调用的正则表达式，不建议用在对性能严格的地方例如:循环及list页面等
     * 
     * @author fengliang
     * @param str 需要处理的字符串
     * @return 返回处理后的字符串
     */
    public static String removeURL(String str) {
        if (str != null)
            str = str.toLowerCase().replaceAll("(http|www|com|cn|org|\\.)+", "");
        return str;
    }

    /**
     * 随即生成指定位数的含数字验证码字符串
     * 
     * @author Peltason
     * @date 2007-5-9
     * @param bit 指定生成验证码位数
     * @return String
     */
    public static String numRandom(int bit) {
        if (bit == 0)
            bit = 6; // 默认6位
        String str = "";
        str = "0123456789";// 初始化种子
        return RandomStringUtils.random(bit, str);// 返回6位的字符串
    }

    /**
     * 随即生成指定位数的含验证码字符串
     * 
     * @author Peltason
     * 
     * @date 2007-5-9
     * @param bit 指定生成验证码位数
     * @return String
     */
    public static String random(int bit) {
        if (bit == 0)
            bit = 6; // 默认6位
        // 因为o和0,l和1很难区分,所以,去掉大小写的o和l
        String str = "";
        str = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz";// 初始化种子
        return RandomStringUtils.random(bit, str);// 返回6位的字符串
    }

    /**
     * Wap页面的非法字符检查
     * 
     * @author hugh115
     * @date 2007-06-29
     * @param str
     * @return
     */
    public static String replaceWapStr(String str) {
        if (str != null) {
            str = str.replaceAll("<span class=\"keyword\">", "");
            str = str.replaceAll("</span>", "");
            str = str.replaceAll("<strong class=\"keyword\">", "");
            str = str.replaceAll("<strong>", "");
            str = str.replaceAll("</strong>", "");

            str = str.replace('$', '＄');

            str = str.replaceAll("&amp;", "＆");
            str = str.replace('&', '＆');

            str = str.replace('<', '＜');

            str = str.replace('>', '＞');

        }
        return str;
    }

    /**
     * 字符串转float 如果异常返回0.00
     * 
     * @param s 输入的字符串
     * @return 转换后的float
     */
    public static Float toFloat(String s) {
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return new Float(0);
        }
    }

    /**
     * 页面中去除字符串中的空格、回车、换行符、制表符
     * 
     * @author shazao
     * @date 2007-08-17
     * @param str
     * @return
     */
    public static String replaceBlank(String str) {
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            str = m.replaceAll("");
        }
        return str;
    }

    /**
     * 全角生成半角
     * 
     * @author bailong
     * @date 2007-08-29
     * @param str
     * @return
     */
    public static String Q2B(String QJstr) {
        String outStr = "";
        String Tstr = "";
        byte[] b = null;
        for (int i = 0; i < QJstr.length(); i++) {
            try {
                Tstr = QJstr.substring(i, i + 1);
                b = Tstr.getBytes("unicode");
            } catch (java.io.UnsupportedEncodingException e) {
                if (logger.isErrorEnabled()) {
                    logger.error(e);
                }
            }
            if (b[3] == -1) {
                b[2] = (byte) (b[2] + 32);
                b[3] = 0;
                try {
                    outStr = outStr + new String(b, "unicode");
                } catch (java.io.UnsupportedEncodingException ex) {
                    if (logger.isErrorEnabled()) {
                        logger.error(ex);
                    }
                }
            } else {
                outStr = outStr + Tstr;
            }
        }
        return outStr;
    }



    /**
     * 判断对象是否为空
     * 
     * @param str
     * @return
     */
    public static boolean isNotEmpty(Object str) {
        boolean flag = true;
        if (str != null && !str.equals("")) {
            if (str.toString().length() > 0) {
                flag = true;
            }
        } else {
            flag = false;
        }
        return flag;
    }


    public static String getProperty(String property) {
        if (property.contains("_")) {
            return property.replaceAll("_", "\\.");
        }
        return property;
    }

    /**
     * 判断这个类是不是java自带的类
     * 
     * @param clazz
     * @return
     */
    public static boolean isJavaClass(Class<?> clazz) {
        boolean isBaseClass = false;
        if (clazz.isArray()) {
            isBaseClass = false;
        } else if (clazz.isPrimitive() || clazz.getPackage() == null || clazz.getPackage().getName().equals("java.lang") || clazz.getPackage().getName().equals("java.math") || clazz.getPackage().getName().equals("java.util")) {
            isBaseClass = true;
        }
        return isBaseClass;
    }


}
