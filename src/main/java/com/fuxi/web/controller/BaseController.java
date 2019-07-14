package com.fuxi.web.controller;

import javax.servlet.http.HttpServletRequest;
import com.fuxi.system.util.Globals;
import com.fuxi.system.util.SysLogger;
import com.fuxi.system.util.TokenProccessor;

/**
 * Title: BaseController Description: 基础控制器，其他控制器需集成此控制器获得initBinder自动转换的功能
 * 
 * @author LJ
 * 
 */
public class BaseController {

    public String getModuleName() {
        return null;
    }

    /**
     * 保存TOKEN
     * 
     * @param request
     */
    public void saveToken(HttpServletRequest request) {
        // 生成Token
        String token = TokenProccessor.getInstance().makeToken().replace("+", "");
        request.getSession().setAttribute(Globals.Token, token); // 在服务器使用session保存token(令牌)
        request.setAttribute("token", token);
    }

    /**
     * 判断客户端提交上来的令牌和服务器端生成的令牌是否一致
     * 
     * @param request
     * @return true 用户重复提交了表单 false 用户没有重复提交表单
     */
    public synchronized boolean isRepeatSubmit(HttpServletRequest request) {
        String client_token = request.getParameter(Globals.Token);
        // 1、如果用户提交的表单数据中没有token，则用户是重复提交了表单
        if (client_token == null) {
            return true;
        }
        // 取出存储在Session中的token
        String server_token = (String) request.getSession().getAttribute(Globals.Token);
        // 2、如果当前用户的Session中不存在Token(令牌)，则用户是重复提交了表单
        if (server_token == null) {
            SysLogger.error("token为空");
            return true;
        }
        // 3、存储在Session中的Token(令牌)与表单提交的Token(令牌)不同，则用户是重复提交了表单
        if (!client_token.equals(server_token)) {
            SysLogger.error("token不一致");
            return true;
        }

        // 4.更新SESSION中token
        saveToken(request);
        return false;
    }

}
