package com.fuxi.system.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 在线用户对象
 * 
 * @author JueYue
 * @date 2013-9-28
 * @version 1.0
 */
public class Client implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    private String userID;
    private String onLineId;
    private String no;
    private String deptID;
    private String deptCode;
    private String deptName;
    private String deptType;
    private String userName;
    private String priceType;
    private String settleCustID;
    private Map<String, String> map = new HashMap<String, String>();
    private int maxSize;
    private boolean unitPricePermitFlag;
    private boolean superSalesFlag;
    private boolean pOSNonZeroStockFlag;


    public String getOnLineId() {
        return onLineId;
    }

    public void setOnLineId(String onLineId) {
        this.onLineId = onLineId;
    }

    public String getDeptType() {
        return deptType;
    }

    public void setDeptType(String deptType) {
        this.deptType = deptType;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDeptID() {
        return deptID;
    }

    public void setDeptID(String deptID) {
        this.deptID = deptID;
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    public String getSettleCustID() {
        return settleCustID;
    }

    public void setSettleCustID(String settleCustID) {
        this.settleCustID = settleCustID;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public boolean isSuperSalesFlag() {
        return superSalesFlag;
    }

    public void setSuperSalesFlag(boolean superSalesFlag) {
        this.superSalesFlag = superSalesFlag;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public boolean getUnitPricePermitFlag() {
        return unitPricePermitFlag;
    }

    public void setUnitPricePermitFlag(boolean unitPricePermitFlag) {
        this.unitPricePermitFlag = unitPricePermitFlag;
    }

    public boolean getPOSNonZeroStockFlag() {
        return pOSNonZeroStockFlag;
    }

    public void setPOSNonZeroStockFlag(boolean pOSNonZeroStockFlag) {
        this.pOSNonZeroStockFlag = pOSNonZeroStockFlag;
    }

}
