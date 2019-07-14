package com.fuxi.core.common.license;

import java.io.Serializable;


public class License implements Serializable {

    String maxOnlineCount;
    String regid;
    String begindate;
    String enddate;
    String str1;
    String str2;
    String str3;
    String str4;
    String str5;
    String str6;

    public License() {
        maxOnlineCount = "0";
    }



    public String getMaxOnlineCount() {
        return maxOnlineCount;
    }



    public void setMaxOnlineCount(String maxOnlineCount) {
        this.maxOnlineCount = maxOnlineCount;
    }



    public String getRegid() {
        return regid;
    }

    public void setRegid(String regid) {
        this.regid = regid;
    }

    public String getBegindate() {
        return begindate;
    }

    public void setBegindate(String begindate) {
        this.begindate = begindate;
    }

    public String getEnddate() {
        return enddate;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }

    public String getStr1() {
        return str1;
    }

    public void setStr1(String str1) {
        this.str1 = str1;
    }

    public String getStr2() {
        return str2;
    }

    public void setStr2(String str2) {
        this.str2 = str2;
    }

    public String getStr3() {
        return str3;
    }

    public void setStr3(String str3) {
        this.str3 = str3;
    }

    public String getStr4() {
        return str4;
    }

    public void setStr4(String str4) {
        this.str4 = str4;
    }

    public String getStr5() {
        return str5;
    }

    public void setStr5(String str5) {
        this.str5 = str5;
    }

    public String getStr6() {
        return str6;
    }

    public void setStr6(String str6) {
        this.str6 = str6;
    }


}
