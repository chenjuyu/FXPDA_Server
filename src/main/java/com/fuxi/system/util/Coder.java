package com.fuxi.system.util;

import java.security.MessageDigest;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Coder {
    public static final String KEY_SHA = "SHA";
    public static final String KEY_MD5 = "MD5";

    /**
     * BASE64解密
     * 
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] decryptBASE64(String key) throws Exception {
        return (new BASE64Decoder()).decodeBuffer(key);
    }

    /**
     * BASE64加密
     * 
     * @param key
     * @return
     * @throws Exception
     */
    public static String encryptBASE64(byte[] key) throws Exception {
        return (new BASE64Encoder()).encodeBuffer(key);
    }

    /**
     * MD5加密
     * 
     * @param data
     * @return
     * @throws Exception
     */
    public static byte[] encryptMD5(byte[] data) throws Exception {

        MessageDigest md5 = MessageDigest.getInstance(KEY_MD5);
        md5.update(data);

        return md5.digest();

    }

    /**
     * SHA加密
     * 
     * @param data
     * @return
     * @throws Exception
     */
    public static byte[] encryptSHA(byte[] data) throws Exception {

        MessageDigest sha = MessageDigest.getInstance(KEY_SHA);
        sha.update(data);

        return sha.digest();

    }

    public static String getByteToString(byte[] data) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        int j = data.length;
        char str[] = new char[j * 2];
        int k = 0;
        for (int i = 0; i < j; i++) {
            byte byte0 = data[i];
            str[k++] = hexDigits[byte0 >>> 4 & 0xf];
            str[k++] = hexDigits[byte0 & 0xf];
        }
        String outPut = new String(str);
        return outPut;
    }

    public static String getEncryption(String data) throws Exception {
        byte[] inputData = data.getBytes();
        byte[] encryptMD5 = Coder.encryptMD5(inputData);
        byte[] encryptSHA = Coder.encryptSHA(encryptMD5);
        byte[] encryptMD52 = Coder.encryptMD5(encryptSHA);
        return getByteToString(encryptMD52);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(getEncryption("998653"));
        // System.out.println(getByteToString(encryptMD5("150846".getBytes())));
    }

}
