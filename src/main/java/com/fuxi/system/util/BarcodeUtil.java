package com.fuxi.system.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import com.fuxi.core.common.model.common.BarcodeScan;

/**
 * sizeLength：尺码编码长度，如果系统尺码编码长度不一致，此值传入为NULL colorLength：颜色编码长度，如果系统颜色编码长度不一致，此值传入为NULL
 * sizeLength，colorLength系统启动的时候，初始化此值
 * 
 * @author owen
 * @version 创建时间：2017年7月1日
 */
public class BarcodeUtil {
    public static Integer sizeLength;
    public static Integer colorLength;
    private static List<BarcodeScan> scanList = null;
    private static List<Map<String, Object>> dataList = null;


    public static BarcodeScan readScan(String scan) {
        String[] ss = scan.split(";");
        if (ss.length != 15) {
            SysLogger.info("条码配置参数错误：" + scan);
            return null;
        }
        BarcodeScan bs = new BarcodeScan();
        bs.setBarcodeScan(scan);
        bs.setBrandChar(ss[0]);
        bs.setBarcodeWidth1(Integer.valueOf(ss[1]));
        bs.setBarcodeWidth2(Integer.valueOf(ss[2]));
        bs.setScanFlag1(Integer.valueOf(ss[3]));
        bs.setScanChar1(ss[4]);
        bs.setScanStart1(Integer.valueOf(ss[5]));
        if (StringUtils.isNumeric(ss[6])) {
            bs.setScanNumber1(Integer.valueOf(ss[6]));
        } else {
            bs.setScanNumber1(-100);
        }
        bs.setScanFlag2(Integer.valueOf(ss[7]));
        bs.setScanChar2(ss[8]);
        bs.setScanStart2(Integer.valueOf(ss[9]));
        if (StringUtils.isNumeric(ss[10])) {
            bs.setScanNumber2(Integer.valueOf(ss[10]));
        } else {
            bs.setScanNumber2(-100);
        }
        bs.setScanFlag3(Integer.valueOf(ss[11]));
        bs.setScanChar3(ss[12]);
        bs.setScanStart3(Integer.valueOf(ss[13]));
        if (StringUtils.isNumeric(ss[14])) {
            bs.setScanNumber3(Integer.valueOf(ss[14]));
        } else {
            bs.setScanNumber3(-100);
        }
        return bs;
    }


    static {
        int sum = 100;
        String count = ResourceUtil.getConfigByName("count");
        if (count != null && !count.isEmpty()) {
            sum = Integer.parseInt(count);
        }
        scanList = new ArrayList<BarcodeScan>();
        for (int i = 1; i <= sum; i++) {
            try {
                String scan = ResourceUtil.getConfigByName("BarcodeScan" + i);
                if (scan != null) {
                    BarcodeScan bs = readScan(scan);
                    if (bs == null) {
                        continue;
                    }
                    scanList.add(bs);
                } else {
                    break;
                }
            } catch (Exception e) {

            }

        }
    }

    /**
     * 优先解析配置参数中的条码规则，如果解析成功，则不继续往下执行 如无配置参数中的条码规则，则按颜色尺码等长的规则拆分条码，商品编码长度可以不一致
     * 
     * @param barcodes
     * @return
     */
    public static List<Map<String, Object>> barcodeToGoods(String barcode) {
        dataList = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < scanList.size(); i++) {
            System.out.println(scanList.get(i).getBarcodeScan());
            Map map = analysisBarcode(barcode, scanList.get(i));
            if (map != null) {
                dataList.add(map);
            }
        }
        if (sizeLength != null && colorLength != null) {
            String temp = new StringBuilder(barcode).reverse().toString();
            Map ret = new HashMap();
            String sizeCode = new StringBuilder(temp.substring(0, sizeLength)).reverse().toString();
            String colorCode = new StringBuilder(temp.substring(sizeLength, sizeLength + colorLength)).reverse().toString();
            String goodsCode = new StringBuilder(temp.substring(sizeLength + colorLength)).reverse().toString();
            ret.put("sizeCode", sizeCode);
            ret.put("colorCode", colorCode);
            ret.put("goodsCode", goodsCode);
            dataList.add(ret);
        }
        return dataList;
    }

    private static Map analysisBarcode(String barcode, BarcodeScan bs) {
        if (!"0".equals(bs.getBrandChar()) && !barcode.startsWith(bs.getBrandChar())) {
            return null;
        }

        if (barcode.length() > bs.getBarcodeWidth2() || barcode.length() < bs.getBarcodeWidth1()) {
            return null;
        }
        Map remain = new HashMap();

        String value1 = splitBarcode(barcode, bs.getScanChar1(), bs.getScanStart1(), bs.getScanNumber1(), remain);
        String value2 = splitBarcode(barcode, bs.getScanChar2(), bs.getScanStart2(), bs.getScanNumber2(), remain);
        String value3 = null;
        if ("0".equals(bs.getScanChar3()) && bs.getScanStart3() == 0 && bs.getScanNumber3() == 0) {
            value3 = (String) remain.get("value");
        } else {
            value3 = splitBarcode(barcode, bs.getScanChar3(), bs.getScanStart3(), bs.getScanNumber3(), remain);
        }
        Map tempMap = new HashMap();
        tempMap.put("value" + bs.getScanFlag1(), value1);
        tempMap.put("value" + bs.getScanFlag2(), value2);
        tempMap.put("value" + bs.getScanFlag3(), value3);

        String goodsCode = (String) tempMap.get("value1");
        String colorCode = (String) tempMap.get("value2");
        String sizeCode = (String) tempMap.get("value3");

        // 判断货品颜色尺码的合法性
        if (sizeLength != null && sizeCode.length() != sizeLength) {
            return null;
        }

        if (colorLength != null && colorCode.length() != colorLength) {
            return null;
        }

        Map retMap = new HashMap();
        retMap.put("goodsCode", goodsCode);
        retMap.put("colorCode", colorCode);
        retMap.put("sizeCode", sizeCode);

        System.out.print("\tgoodsCode:" + goodsCode + "\tcolorCode:" + colorCode + "\tsizeCode:" + sizeCode);

        return retMap;
    }

    // public static void main(String[] args) {
    // List<Map<String, Object>> datas = barcodeToGoods("8373019B0950004");
    // for (int i = 0; i < datas.size(); i++) {
    // Map map = datas.get(i);
    // String goodsCode = String.valueOf(map.get("goodsCode"));
    // String colorNo = String.valueOf(map.get("colorCode"));
    // String sizeNo = String.valueOf(map.get("sizeCode"));
    // System.out.println("货号\t颜色\t尺码");
    // System.out.println(goodsCode+"\t"+colorNo+"\t"+sizeNo);
    // }
    // }

    private static String splitBarcode(String barcode, String scanChar, int scanStart, int scanNumber, Map remain) {
        if ("00".equals(scanChar)) {
            return null;
        }
        if (scanStart == 0 && scanNumber == 0) {
            return scanChar;
        }

        if ("0".equals(scanChar)) {
            String retCode = null;
            String temp = barcode;
            if (scanStart < 0) {
                temp = new StringBuilder(barcode).reverse().toString();
            }
            if (scanNumber > 0) {
                retCode = temp.substring(Math.abs(scanStart) - 1, Math.abs(scanStart) - 1 + scanNumber);
                remain.put("value", temp.substring(Math.abs(scanStart) - 1 + scanNumber));
            } else if (scanNumber == 0) {
                retCode = temp.substring(Math.abs(scanStart) - 1);
                remain.put("value", null);
            }
            if (scanStart < 0) {
                retCode = new StringBuilder(retCode).reverse().toString();
                if (remain.get("value") != null) {
                    remain.put("value", new StringBuilder(remain.get("value").toString()).reverse().toString());
                }
            }
            return retCode;
        } else {
            String retCode = null;
            String temp = barcode;
            if (scanStart < 0) {
                temp = new StringBuilder(barcode).reverse().toString();
            }
            int start = getIndex(barcode, scanChar, Math.abs(scanStart));
            if (scanNumber > 0) {
                retCode = temp.substring(start + 1, start + 1 + scanNumber);
                remain.put("value", temp.substring(start + 1 + scanNumber));
            } else if (scanNumber == 0) {
                retCode = temp.substring(start + 1);
                remain.put("value", null);
            } else if (scanNumber < 0) {
                int end = getIndex(barcode, scanChar, Math.abs(scanStart) + 1);
                retCode = temp.substring(start + 1, end);
                remain.put("value", temp.substring(end));
            }
            if (scanStart < 0) {
                retCode = new StringBuilder(retCode).reverse().toString();
                if (remain.get("value") != null) {
                    remain.put("value", new StringBuilder(remain.get("value").toString()).reverse().toString());
                }
            }
            return retCode;

        }
    }


    private static int getIndex(String barcode, String split, int times) {
        int start = -1;
        for (int i = 0; i < times; i++) {
            int index = barcode.indexOf(split, start + 1);
            start = index;
        }
        return start;
    }

    public static Integer getSizeLength() {
        return sizeLength;
    }

    public static void setSizeLength(Integer sizeLength) {
        BarcodeUtil.sizeLength = sizeLength;
    }

    public static Integer getColorLength() {
        return colorLength;
    }

    public static void setColorLength(Integer colorLength) {
        BarcodeUtil.colorLength = colorLength;
    }

    public static List<BarcodeScan> getScanList() {
        return scanList;
    }

    public static void setScanList(List<BarcodeScan> scanList) {
        BarcodeUtil.scanList = scanList;
    }


}
