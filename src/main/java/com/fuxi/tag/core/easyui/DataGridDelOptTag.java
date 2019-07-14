package com.fuxi.tag.core.easyui;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * 
 * 类描述：列表删除操作项标签
 */

public class DataGridDelOptTag extends TagSupport {
    protected String url;
    protected String title;
    private String message;// 询问链接的提示语
    private String exp;// 判断链接是否显示的表达式
    private String funname;// 自定义函数名称

    private String operationCode;// 按钮的操作Code
    private String invokeProper = null;
    private final static String FLAG = "flag";

    public int doStartTag() throws JspTagException {
        HttpServletRequest request = (HttpServletRequest) this.pageContext.getRequest();

        return EVAL_PAGE;
    }

    public int doEndTag() throws JspTagException {
        if (this.getValue(FLAG) == null || (Boolean) this.getValue(FLAG)) {
            Tag t = findAncestorWithClass(this, DataGridTag.class);
            DataGridTag parent = (DataGridTag) t;
            parent.setDelUrl(url, title, message, exp, funname, operationCode);
        }

        return EVAL_PAGE;
    }

    public void setFunname(String funname) {
        this.funname = funname;
    }

    public void setExp(String exp) {
        this.exp = exp;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setOperationCode(String operationCode) {
        this.operationCode = operationCode;
    }


    public void setInvokeProper(String invokeProper) {
        this.invokeProper = invokeProper;
    }



}
