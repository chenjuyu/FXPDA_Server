package com.fuxi.tag.core.easyui;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * 
 * 类描述：列表弹出窗操作项处理标签
 */

public class DataGridOpenOptTag extends TagSupport {
    protected String url;// 弹出页面地址
    protected String width = "100%";// 弹出窗口宽度
    protected String height = "100%";// 弹出窗口高度
    protected String title;// 链接标题
    private String exp;// 判断链接是否显示的表达式
    private String operationCode;// 按钮的操作Code

    private String invokeProper = null;
    private String openModel = "OpenWin"; // 弹出方式
    private final static String FLAG = "flag";

    public int doStartTag() throws JspTagException {
        HttpServletRequest request = (HttpServletRequest) this.pageContext.getRequest();

        return EVAL_PAGE;
    }

    public int doEndTag() throws JspTagException {
        if (this.getValue(FLAG) == null || (Boolean) this.getValue(FLAG)) {
            Tag t = findAncestorWithClass(this, DataGridTag.class);
            DataGridTag parent = (DataGridTag) t;
            parent.setOpenUrl(url, title, width, height, exp, operationCode, openModel);
        }
        return EVAL_PAGE;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public void setHeight(String height) {
        this.height = height;
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

    public void setOperationCode(String operationCode) {
        this.operationCode = operationCode;
    }

    public void setOpenModel(String openModel) {
        this.openModel = openModel;
    }


    public void setInvokeProper(String invokeProper) {
        this.invokeProper = invokeProper;
    }

}
