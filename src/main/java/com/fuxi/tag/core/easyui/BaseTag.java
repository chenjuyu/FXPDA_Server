package com.fuxi.tag.core.easyui;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import com.fuxi.system.util.ResourceUtil;
import com.fuxi.system.util.SysLogger;
import com.fuxi.system.util.oConvertUtils;


/**
 * 
 * @author
 * 
 */

public class BaseTag extends TagSupport {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    protected String type = "default";// 加载类型

    public void setType(String type) {
        this.type = type;
    }


    public int doStartTag() throws JspException {
        return EVAL_PAGE;
    }


    public int doEndTag() throws JspException {
        try {
            String version = ResourceUtil.getConfigByName("cssVersion");
            JspWriter out = this.pageContext.getOut();
            StringBuffer sb = new StringBuffer();

            String types[] = type.split(",");
            if (oConvertUtils.isIn("jquery", types)) {
                sb.append("<script type=\"text/javascript\" src=\"plug-in/jquery/jquery-1.8.3.js?v=13\"></script>");
            }
            if (oConvertUtils.isIn("ckeditor", types)) {
                sb.append("<script type=\"text/javascript\" src=\"plug-in/ckeditor/ckeditor.js\"></script>");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/tools/ckeditorTool.js\"></script>");
            }
            if (oConvertUtils.isIn("ckedit", types)) {
                sb.append("<script type=\"text/javascript\" src=\"plug-in/ckedit/ckeditor.js\"></script>");
            }
            if (oConvertUtils.isIn("ckfinder", types)) {
                sb.append("<script type=\"text/javascript\" src=\"plug-in/ckfinder/ckfinder.js\"></script>");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/tools/ckfinderTool.js\"></script>");
            }
            if (oConvertUtils.isIn("easyui", types)) {
                sb.append("<script type=\"text/javascript\" src=\"plug-in/tools/dataformat.js\"></script>");
                sb.append("<link id=\"easyuiTheme\" rel=\"stylesheet\" href=\"plug-in/easyui/themes/default/easyui.css\" type=\"text/css\"></link>");
                sb.append("<link rel=\"stylesheet\" href=\"plug-in/easyui/themes/icon.css\" type=\"text/css\"></link>");
                sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"plug-in/accordion/css/accordion.css\">");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/easyui/jquery.easyui.min.1.3.2.js\"></script>");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/easyui/locale/easyui-lang-zh_CN.js\"></script>");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/tools/syUtil.js\"></script>");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/easyui/extends/datagrid-scrollview.js\"></script>");
            }
            if (oConvertUtils.isIn("DatePicker", types)) {
                sb.append("<script type=\"text/javascript\" src=\"plug-in/My97DatePicker/WdatePicker.js\"></script>");
            }
            if (oConvertUtils.isIn("jqueryui", types)) {
                sb.append("<link rel=\"stylesheet\" href=\"plug-in/jquery-ui/css/ui-lightness/jquery-ui-1.9.2.custom.min.css\" type=\"text/css\"></link>");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/jquery-ui/js/jquery-ui-1.9.2.custom.min.js\"></script>");
            }
            if (oConvertUtils.isIn("jqueryui-sortable", types)) {
                sb.append("<link rel=\"stylesheet\" href=\"plug-in/jquery-ui/css/ui-lightness/jquery-ui-1.9.2.custom.min.css\" type=\"text/css\"></link>");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/jquery-ui/js/ui/jquery.ui.core.js\"></script>");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/jquery-ui/js/ui/jquery.ui.widget.js\"></script>");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/jquery-ui/js/ui/jquery.ui.mouse.js\"></script>");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/jquery-ui/js/ui/jquery.ui.sortable.js\"></script>");
            }
            if (oConvertUtils.isIn("prohibit", types)) {
                sb.append("<script type=\"text/javascript\" src=\"plug-in/tools/prohibitutil.js\"></script>");
            }
            if (oConvertUtils.isIn("designer", types)) {
                sb.append("<script type=\"text/javascript\" src=\"plug-in/designer/easyui/jquery-1.7.2.min.js\"></script>");
                sb.append("<link id=\"easyuiTheme\" rel=\"stylesheet\" href=\"plug-in/designer/easyui/easyui.css\" type=\"text/css\"></link>");
                sb.append("<link rel=\"stylesheet\" href=\"plug-in/designer/easyui/icon.css\" type=\"text/css\"></link>");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/designer/easyui/jquery.easyui.min.1.3.0.js\"></script>");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/designer/easyui/locale/easyui-lang-zh_CN.js\"></script>");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/tools/syUtil.js\"></script>");

                sb.append("<script type=\'text/javascript\' src=\'plug-in/jquery/jquery-autocomplete/lib/jquery.bgiframe.min.js\'></script>");
                sb.append("<script type=\'text/javascript\' src=\'plug-in/jquery/jquery-autocomplete/lib/jquery.ajaxQueue.js\'></script>");
                sb.append("<script type=\'text/javascript\' src=\'plug-in/jquery/jquery-autocomplete/jquery.autocomplete.min.js\'></script>");
                sb.append("<link href=\"plug-in/designer/designer.css\" type=\"text/css\" rel=\"stylesheet\" />");
                sb.append("<script src=\"plug-in/designer/draw2d/wz_jsgraphics.js\"></script>");
                sb.append("<script src=\'plug-in/designer/draw2d/mootools.js\'></script>");
                sb.append("<script src=\'plug-in/designer/draw2d/moocanvas.js\'></script>");
                sb.append("<script src=\'plug-in/designer/draw2d/draw2d.js\'></script>");
                sb.append("<script src=\"plug-in/designer/MyCanvas.js\"></script>");
                sb.append("<script src=\"plug-in/designer/ResizeImage.js\"></script>");
                sb.append("<script src=\"plug-in/designer/event/Start.js\"></script>");
                sb.append("<script src=\"plug-in/designer/event/End.js\"></script>");
                sb.append("<script src=\"plug-in/designer/connection/MyInputPort.js\"></script>");
                sb.append("<script src=\"plug-in/designer/connection/MyOutputPort.js\"></script>");
                sb.append("<script src=\"plug-in/designer/connection/DecoratedConnection.js\"></script>");
                sb.append("<script src=\"plug-in/designer/task/Task.js\"></script>");
                sb.append("<script src=\"plug-in/designer/task/UserTask.js\"></script>");
                sb.append("<script src=\"plug-in/designer/task/ManualTask.js\"></script>");
                sb.append("<script src=\"plug-in/designer/task/ServiceTask.js\"></script>");
                sb.append("<script src=\"plug-in/designer/gateway/ExclusiveGateway.js\"></script>");
                sb.append("<script src=\"plug-in/designer/gateway/ParallelGateway.js\"></script>");
                sb.append("<script src=\"plug-in/designer/boundaryevent/TimerBoundary.js\"></script>");
                sb.append("<script src=\"plug-in/designer/boundaryevent/ErrorBoundary.js\"></script>");
                sb.append("<script src=\"plug-in/designer/subprocess/CallActivity.js\"></script>");
                sb.append("<script src=\"plug-in/designer/task/ScriptTask.js\"></script>");
                sb.append("<script src=\"plug-in/designer/task/MailTask.js\"></script>");
                sb.append("<script src=\"plug-in/designer/task/ReceiveTask.js\"></script>");
                sb.append("<script src=\"plug-in/designer/task/BusinessRuleTask.js\"></script>");
                sb.append("<script src=\"plug-in/designer/designer.js\"></script>");
                sb.append("<script src=\"plug-in/designer/mydesigner.js\"></script>");

            }
            if (oConvertUtils.isIn("tools", types)) {
                sb.append("<link rel=\"stylesheet\" href=\"plug-in/tools/css/common.css\" type=\"text/css\"></link>");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/lhgDialog/lhgdialog.min.js\"></script>");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/tools/curdtools.js?v=2\"></script>");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/tools/easyuiextend.js\"></script>");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/jquery-plugs/hftable/jquery-hftable.js\"></script>");
            }

            if (oConvertUtils.isIn("tool", types)) {
                sb.append("<link rel=\"stylesheet\" href=\"plug-in/tools/css/common.css\" type=\"text/css\"></link>");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/tools/curdtools.js?v=2\"></script>");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/tools/easyuiextend.js\"></script>");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/jquery-plugs/hftable/jquery-hftable.js\"></script>");
            }


            if (oConvertUtils.isIn("toptip", types)) {
                sb.append("<link rel=\"stylesheet\" href=\"plug-in/toptip/css/css.css\" type=\"text/css\"></link>");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/toptip/manhua_msgTips.js\"></script>");
            }
            if (oConvertUtils.isIn("autocomplete", types)) {
                sb.append("<link rel=\"stylesheet\" href=\"plug-in/jquery/jquery-autocomplete/jquery.autocomplete.css\" type=\"text/css\"></link>");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/jquery/jquery-autocomplete/jquery.autocomplete.min.js\"></script>");
            }

            if (oConvertUtils.isIn("public", types)) {
                sb.append("<script type=\"text/javascript\" src=\"plug-in/tools/public.js?v=13\"></script>");
            }
            if (oConvertUtils.isIn("jquery-form", types)) {
                sb.append("<script type=\"text/javascript\" src=\"plug-in/jquery-plugs/form/jquery.form.js\"></script>");
            }


            if (oConvertUtils.isIn("drag", types)) {
                sb.append("<script type=\"text/javascript\" src=\"plug-in/jquery-plugs/drag/jquery.event.drag.js\"></script>");
            }


            if (oConvertUtils.isIn("cookie", types)) {
                sb.append("<script type=\"text/javascript\" src=\"plug-in/jquery-plugs/cookie/jquery.cookie.js\"></script><!--cookie-->");
            }

            if (oConvertUtils.isIn("multiselect", types)) {
                sb.append("<link rel=\"stylesheet\" href=\"plug-in/multiselect/css/multi-select.css\">");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/multiselect/js/jquery.multi-select.js\"></script><!--cookie-->");
            }

            if (oConvertUtils.isIn("qinsi", types)) {
                sb.append("<link rel=\"stylesheet\" href=\"plug-in/mobile/css/main.css\">");
                sb.append("<link rel=\"stylesheet\" href=\"plug-in/mobile/css/ionicons.min.css\">");
            }
            if (oConvertUtils.isIn("weui", types)) {
                sb.append("<link rel=\"stylesheet\" href=\"plug-in/weui/css/weui.css\">");
            }
            if (oConvertUtils.isIn("mobilecss", types)) {
                sb.append("<link rel=\"stylesheet\" href=\"plug-in/public/css/style.css?v=3").append(version).append("\"></script>");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/acer/js/public.js?v=1").append(version).append("\"></script>");;

            }
            if (oConvertUtils.isIn("login", types)) {
                sb.append("<link rel=\"stylesheet\" href=\"plug-in/public/css/login.css\">");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/public/js/login.js\"></script><!--cookie-->");
            }

            if (oConvertUtils.isIn("layer", types)) {
                sb.append("<link rel=\"stylesheet\" href=\"plug-in/public/css/login.css\">");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/layer/layer.js\"></script>");
            }

            if (oConvertUtils.isIn("datetime", types)) {
                sb.append("<link rel=\"stylesheet\" href=\"plug-in/datetimepick/DateTimePicker.min.css\">");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/datetimepick/DateTimePicker.min.js\"></script>");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/datetimepick/iscroll.js\"></script>");

            }

            if (oConvertUtils.isIn("selectpage", types)) {
                sb.append("<script type=\"text/javascript\" src=\"plug-in/acer/js/select-page.js?v=").append(version).append("\"></script>");
            }

            if (oConvertUtils.isIn("selectproduct", types)) {
                sb.append("<script type=\"text/javascript\" src=\"plug-in/acer/js/select-product.js?v=").append(version).append("\"></script>");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/acer/js/order.js?v=").append(version).append("\"></script>");
            }


            if (oConvertUtils.isIn("WXJS", types)) {
                sb.append("<script src=\"http://res.wx.qq.com/open/js/jweixin-1.0.0.js\"></script>");
                sb.append("<script type=\"text/javascript\" src=\"plug-in/acer/js/wx-config.js?v=").append(version).append("\"></script>");
            }



            out.print(sb.toString());
        } catch (IOException e) {
            SysLogger.error(e.getMessage(), e);
        }
        return EVAL_PAGE;
    }

}
