package com.fuxi.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSONArray;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fuxi.core.common.dao.impl.CommonDao;
import com.fuxi.core.common.model.json.AjaxJson;
import com.fuxi.core.common.service.GiftService;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.ResourceUtil;
import com.fuxi.system.util.SysLogger;
import com.fuxi.system.util.oConvertUtils;

/**
 * Title: SalesTicketController Description: 赠品单逻辑控制器
 * 
 * @author LYJ
 * 
 */
@Controller
@RequestMapping("/gift")
public class GiftController extends BaseController {

    private Logger log = Logger.getLogger(GiftController.class);
    private CommonController commonController = new CommonController();

    @Autowired
    private CommonDao commonDao;

    @Autowired
    private GiftService giftService;

    /**
     * 保存赠品单记录[新增]
     * 
     * @param req
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(params = "saveGift")
    @ResponseBody
    public AjaxJson saveSalesTicket(HttpServletRequest req) {
        AjaxJson j = new AjaxJson();
        j.setAttributes(new HashMap<String, Object>());
        Client client = ResourceUtil.getClientFromSession(req);
        try {
            String employeeId = oConvertUtils.getString(req.getParameter("employeeId"));
            String memo = oConvertUtils.getString(req.getParameter("memo"));
            String qty = oConvertUtils.getString(req.getParameter("qty"));
            String amount = oConvertUtils.getString(req.getParameter("amount"));
            String vipId = oConvertUtils.getString(req.getParameter("vipId"));
            String vipCode = oConvertUtils.getString(req.getParameter("vipCode"));
            String jsonStr = oConvertUtils.getString(req.getParameter("data"));
            JSONArray datas = JSONArray.fromObject(jsonStr);
            List<Map<String, Object>> dataList = JSONArray.toList(datas, Map.class);
            List<Map<String, Object>> checkList = JSONArray.toList(datas, Map.class);
            // 判断检查负库存
            List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
            if (client.getPOSNonZeroStockFlag() && !client.isSuperSalesFlag()) {
                tempList = commonController.checkNegativeInventoryForFrontDesk(commonDao, checkList, client.getDeptID());
            }
            if (tempList.size() == 0) {
                Map<String, String> map = giftService.saveGift(dataList, employeeId, qty, amount, memo, vipId, vipCode, client);
                j.getAttributes().put("PosSalesID", map.get("PosSalesID"));
                j.getAttributes().put("PosSalesNo", map.get("PosSalesNo"));
            } else {
                j.getAttributes().put("PosSalesID", "");
                j.getAttributes().put("PosSalesNo", "");
            }
            j.getAttributes().put("tempList", tempList);
        } catch (Exception e) {
            j.setSuccess(false);
            j.setMsg(e.getMessage());
            SysLogger.error(e.getMessage(), e);
        }
        return j;
    }

}
