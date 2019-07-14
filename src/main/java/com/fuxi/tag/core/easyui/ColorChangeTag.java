package com.fuxi.tag.core.easyui;

import java.io.IOException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import com.fuxi.system.util.SysLogger;

/**
 * 
 * 类描述：改变HTML控件颜色
 */

public class ColorChangeTag extends TagSupport {
    public int doStartTag() throws JspTagException {
        return EVAL_PAGE;
    }

    public int doEndTag() throws JspTagException {
        try {
            JspWriter out = this.pageContext.getOut();
            out.print(end().toString());
        } catch (IOException e) {
            SysLogger.error(e.getMessage(), e);
        }
        return EVAL_PAGE;
    }

    public StringBuffer end() {
        StringBuffer sb = new StringBuffer();
        return sb;
    }

}
