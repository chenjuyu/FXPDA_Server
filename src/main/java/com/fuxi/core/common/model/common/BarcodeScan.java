package com.fuxi.core.common.model.common;

public class BarcodeScan {

    private String brandChar; // 品牌标识
    private int barcodeWidth1; // 最短的条码长度
    private int barcodeWidth2; // 最长的条码长度
    private int scanFlag1; // 编码标识
    private String scanChar1;// 字符标识符
    private int scanStart1;// [ScanFlag]的编码在条码中的开始位置
    private int scanNumber1;// [ScanFlag]的编码在条码中的连续个数
    private int scanFlag2; // 编码标识
    private String scanChar2;
    private int scanStart2;
    private int scanNumber2;
    private int scanFlag3; // 编码标识
    private String scanChar3;
    private int scanStart3;
    private int scanNumber3;
    private String barcodeScan;


    public String getBarcodeScan() {
        return barcodeScan;
    }

    public void setBarcodeScan(String barcodeScan) {
        this.barcodeScan = barcodeScan;
    }

    public String getBrandChar() {
        return brandChar;
    }

    public void setBrandChar(String brandChar) {
        this.brandChar = brandChar;
    }

    public int getBarcodeWidth1() {
        return barcodeWidth1;
    }

    public void setBarcodeWidth1(int barcodeWidth1) {
        this.barcodeWidth1 = barcodeWidth1;
    }

    public int getBarcodeWidth2() {
        return barcodeWidth2;
    }

    public void setBarcodeWidth2(int barcodeWidth2) {
        this.barcodeWidth2 = barcodeWidth2;
    }

    public int getScanFlag1() {
        return scanFlag1;
    }

    public void setScanFlag1(int scanFlag1) {
        this.scanFlag1 = scanFlag1;
    }

    public String getScanChar1() {
        return scanChar1;
    }

    public void setScanChar1(String scanChar1) {
        this.scanChar1 = scanChar1;
    }

    public int getScanStart1() {
        return scanStart1;
    }

    public void setScanStart1(int scanStart1) {
        this.scanStart1 = scanStart1;
    }

    public int getScanNumber1() {
        return scanNumber1;
    }

    public void setScanNumber1(int scanNumber1) {
        this.scanNumber1 = scanNumber1;
    }

    public String getScanChar2() {
        return scanChar2;
    }

    public void setScanChar2(String scanChar2) {
        this.scanChar2 = scanChar2;
    }

    public int getScanStart2() {
        return scanStart2;
    }

    public void setScanStart2(int scanStart2) {
        this.scanStart2 = scanStart2;
    }

    public int getScanNumber2() {
        return scanNumber2;
    }

    public void setScanNumber2(int scanNumber2) {
        this.scanNumber2 = scanNumber2;
    }

    public String getScanChar3() {
        return scanChar3;
    }

    public void setScanChar3(String scanChar3) {
        this.scanChar3 = scanChar3;
    }

    public int getScanStart3() {
        return scanStart3;
    }

    public void setScanStart3(int scanStart3) {
        this.scanStart3 = scanStart3;
    }

    public int getScanNumber3() {
        return scanNumber3;
    }

    public void setScanNumber3(int scanNumber3) {
        this.scanNumber3 = scanNumber3;
    }

    public int getScanFlag2() {
        return scanFlag2;
    }

    public void setScanFlag2(int scanFlag2) {
        this.scanFlag2 = scanFlag2;
    }

    public int getScanFlag3() {
        return scanFlag3;
    }

    public void setScanFlag3(int scanFlag3) {
        this.scanFlag3 = scanFlag3;
    }


}
