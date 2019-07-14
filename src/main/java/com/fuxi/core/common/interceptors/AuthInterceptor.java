package com.fuxi.core.common.interceptors;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import com.fuxi.core.common.dao.impl.CommonDao;
import com.fuxi.core.common.model.json.AjaxJson;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.ResourceUtil;


/**
 * 权限拦截器
 * 
 * @author
 * 
 */

public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger logger = Logger.getLogger(AuthInterceptor.class);
    private List<String> excludeUrls;

    @Autowired
    private CommonDao commonDao;



    public List<String> getExcludeUrls() {
        return excludeUrls;
    }

    public void setExcludeUrls(List<String> excludeUrls) {
        this.excludeUrls = excludeUrls;
    }



    /**
     * 在controller后拦截
     */
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object object, Exception exception) throws Exception {}

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object object, ModelAndView modelAndView) throws Exception {

    }

    /**
     * 在controller前拦截
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
        String requestPath = ResourceUtil.getRequestPath(request);// 用户访问的资源地址
        Client client = ResourceUtil.getClientFromSession(request);
        if (excludePath(requestPath, excludeUrls)) {
            return true;
        } else {
            if (client != null) {
                return true;
            } else {
                forward(request, response, requestPath);
                return false;
            }

        }
    }

    /**
     * 转发
     * 
     * @param user
     * @param req
     * @return
     */
    @RequestMapping(params = "forword")
    public ModelAndView forword(HttpServletRequest request) {
        return new ModelAndView(new RedirectView("login.do?login"));
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, String requestPath) throws ServletException, IOException {
        response.setContentType("application/json");
        response.getWriter().write("{toLogin:true}");
    }


    private Boolean excludePath(String path, List list) {
        Boolean flag = false;
        for (int i = 0; i < list.size(); i++) {
            if (path.startsWith(list.get(i).toString())) {
                return true;
            }
        }
        return flag;
    }

}
