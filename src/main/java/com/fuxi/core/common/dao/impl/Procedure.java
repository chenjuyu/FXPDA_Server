package com.fuxi.core.common.dao.impl;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;
import org.springframework.jdbc.object.StoredProcedure;

public class Procedure extends StoredProcedure {
    private HashMap<String, Object> map = new HashMap<String, Object>();

    public Procedure() {
        super();

    }


    public HashMap getMap() {
        return this.map;
    }

    public void setValue(String key, Object obj) {
        map.put(key, obj);
    }

    public Map execute() {
        if (this.getSql() == null || this.getSql().equals(""))
            return null;
        this.compile();
        return execute(map);
    }

    public void setVarcharParam(String param) {
        this.declareParameter(new SqlParameter(param, Types.VARCHAR));
    }


    public void setDoubleParam(String param) {
        this.declareParameter(new SqlParameter(param, Types.DOUBLE));
    }

    public void setIntegerParam(String param) {
        this.declareParameter(new SqlParameter(param, Types.INTEGER));
    }

    public void setVarcharOutParam(String param) {
        this.declareParameter(new SqlOutParameter(param, Types.VARCHAR));
    }

    public void setDoubleOutParam(String param) {
        this.declareParameter(new SqlOutParameter(param, Types.DOUBLE));
    }

    public void setIntegerOutParam(String param) {
        this.declareParameter(new SqlOutParameter(param, Types.INTEGER));
    }

    public void setInParam(String param, int valueType) {
        this.declareParameter(new SqlParameter(param, valueType));

    }

    public void setOutParam(String param, int valueType) {
        this.declareParameter(new SqlOutParameter(param, valueType));

    }

    public void setReturnParam(String param, RowMapper rowMapper) {
        this.declareParameter(new SqlReturnResultSet(param, rowMapper));
    }

}
