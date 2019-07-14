package com.fuxi.core.common.dao;


public interface ICommonDao extends IGenericBaseCommonDao {
    // 生成表的ID
    public String getNewIDValue(int type);

    // 生成新单号
    public String getNewNOValue(int type, String id, String userID);

    public String getNewNOValue(int type, String id);
}
