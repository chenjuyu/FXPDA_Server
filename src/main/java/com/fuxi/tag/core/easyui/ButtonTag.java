package com.fuxi.tag.core.easyui;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import com.fuxi.system.util.SysLogger;


/**
 * 
 * @author
 * 
 */

public class ButtonTag extends TagSupport {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    protected String name = "button"; //
    protected String invokeProper = null;
    protected String hotkey = null;
    protected String iconCls = null;
    protected String invoke = null;
    protected String onclick = null;
    protected String value = null;
    private final static String FLAG = "flag";

    public void setValue(String value) {
        this.value = value;
    }

    public void setIconCls(String iconCls) {
        this.iconCls = iconCls;
    }

    public void setInvoke(String invoke) {
        this.invoke = invoke;
    }

    public void setOnclick(String onclick) {
        this.onclick = onclick;
    }

    public void setButtonClass(String buttonClass) {
        this.buttonClass = buttonClass;
    }

    protected String buttonClass = null;

    public void setName(String name) {
        this.name = name;
    }

    public void setInvokeProper(String invokeProper) {
        this.invokeProper = invokeProper;
    }

    public void setHotkey(String hotkey) {
        this.hotkey = hotkey;
    }



    public int doStartTag() throws JspException {
        HttpServletRequest request = (HttpServletRequest) this.pageContext.getRequest();

        return EVAL_PAGE;
    }



    public int doEndTag() throws JspException {
        try {
            JspWriter out = this.pageContext.getOut();
            if ((Boolean) this.getValue(FLAG)) {
                StringBuffer sb = new StringBuffer();
                sb.append(" <a  id='").append(name).append("' name= '").append(name).append("' ");
                if (iconCls != null) {
                    sb.append(" iconCls =  '").append(iconCls).append("'");
                }

                if (invoke != null) {
                    sb.append(" invoke = '").append(invoke).append("' ");
                }

                if (onclick != null) {
                    sb.append(" onclick='").append(onclick).append("' ");
                }

                if (buttonClass != null) {
                    sb.append(" class= '").append(buttonClass).append("'");
                } else {
                    sb.append(" class= 'easyui-linkbutton' ");
                }

                sb.append("  plain=\"true\" >");
                sb.append(value);
                sb.append("</a>");
                out.print(sb.toString());
            }


        } catch (IOException e) {
            SysLogger.error(e.getMessage(), e);
        }
        return EVAL_PAGE;
    }

}
