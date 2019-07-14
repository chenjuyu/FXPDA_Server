package com.fuxi.core.common.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fuxi.core.common.dao.impl.CommonDao;
import com.fuxi.core.common.exception.BusinessException;
import com.fuxi.core.common.service.SalesTicketService;
import com.fuxi.system.util.Client;
import com.fuxi.system.util.DataUtils;

/**
 * Title: SalesTicketServiceImpl Description: 销售小票业务逻辑接口实现类
 * 
 * @author LYJ
 * 
 */
@Service("salesTicketService")
@Transactional
public class SalesTicketServiceImpl implements SalesTicketService {

    @Autowired
    private CommonDao commonDao;

    @Override
    public synchronized Map<String, String> saveSalesTicket(List<Map<String, Object>> dataList, String employeeId, String qty, String amount, String retailAmount, String discountMoney, String exchangedPoint, String memo, String vipPointRate, String vipDiscount, String vipId, String vipCode,
            boolean posBackAudit, String type, Client client) throws Exception {
        BigDecimal goodsPointRate;
        // 生成ID
        String posSalesId = commonDao.getNewIDValue(408);
        // 生成No
        String no = commonDao.getNewNOValue(408, posSalesId, client.getDeptCode());
        if (posSalesId == null || no == null) {
            throw new BusinessException("生成主键/单号失败");
        }
        // 子表
        StringBuffer sb = new StringBuffer();
        // 处理未选择VIP时子表的默认VIPID
        if (vipId == null || "".equals(vipId) || "null".equalsIgnoreCase(vipId)) {
            vipId = "-111";
        }
        for (int i = 0; i < dataList.size(); i++) {
            Map map = dataList.get(i);
            String goodsId = (String) map.get("GoodsID");
            String colorId = (String) map.get("ColorID");
            String sizeId = (String) map.get("SizeID");
            String goodsBarcode = (String) map.get("Barcode");
            int quantity = Integer.parseInt(String.valueOf(map.get("Quantity")));
            BigDecimal unitPrice = new BigDecimal(String.valueOf(map.get("UnitPrice")));
            BigDecimal retailSales = new BigDecimal(String.valueOf(map.get("RetailSales")));
            BigDecimal discountPrice = new BigDecimal(String.valueOf(map.get("DiscountPrice")));
            BigDecimal discountRate = new BigDecimal(String.valueOf(map.get("DiscountRate")));
            // if(discountRate == null){
            // if(retailSales.compareTo(BigDecimal.ZERO) == 0){
            // discountRate = new BigDecimal(10);
            // }else{
            // discountRate = unitPrice.divide(retailSales,BigDecimal.ROUND_CEILING, 2).multiply(new
            // BigDecimal(10));
            // }
            // }
            BigDecimal discount = (unitPrice.subtract(discountPrice)).multiply(new BigDecimal(quantity));
            if (discount.compareTo(BigDecimal.ZERO) < 0) {
                discount = null;
            }
            if (discountRate.compareTo(new BigDecimal(10)) > 0) {// 折扣超过10时置为NULL
                discountRate = null;
                discount = null;
            }
            BigDecimal pointRate = null;
            if ("-111".equals(vipId)) {
                pointRate = new BigDecimal(0);
            } else {
                // 货品积分倍数
                goodsPointRate = (BigDecimal) commonDao.getData(" select PointRate from goods where goodsId = ? ", goodsId);
                // 计算积分倍数
                pointRate = new BigDecimal(vipPointRate).multiply(goodsPointRate).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
            // 处理未选择VIP时子表的默认VIPID
            sb.append("insert into PosSalesDetail(PosSalesID, SN, GoodsID, ColorID, SizeID, Quantity, UnitPrice, ").append("Discount, Amount, EmployeeID, RetailSales, RetailAmount,Type,GoodsBarcode, VipID,PointRate,DiscountRate) ").append("values('").append(posSalesId).append("',").append((i + 1))
                    .append(",'").append(goodsId).append("','").append(colorId).append("', '").append(sizeId).append("', ").append(quantity).append(",").append(unitPrice).append(",").append(discount).append(", ").append(discountPrice.multiply(new BigDecimal(quantity))).append(", '")
                    .append(employeeId).append("',").append(retailSales).append(",").append(retailSales.multiply(new BigDecimal(quantity))).append(", '").append(type).append("', '").append(goodsBarcode).append("','").append(vipId).append("',").append(pointRate).append(", ").append(discountRate)
                    .append(") ; ");
        }
        commonDao.executeSql(sb.toString());
        // 主表
        StringBuffer sql = new StringBuffer();
        sql.append("insert into Possales(PosSalesID, No, Type, Date, DepartmentID, EmployeeID, QuantitySum, DiscountSum, ").append("AmountSum, CardMoney, CashMoney,MadeBy, MadeByDate, AuditFlag, Audit, AuditDate, ReceivalFlag, TallyFlag, ")
                .append("VipID,VipCode, FactAmountSum , DiscountMoney, Year, Month,UpdateFlag,DelFlag,Memo,ExchangedPoint) ").append("values('").append(posSalesId).append("', '").append(no).append("','销售单',getdate(),'").append(client.getDeptID()).append("', '").append(employeeId).append("', ")
                .append(qty).append(",0,").append(amount).append(", 0,").append(retailAmount).append(", ").append("'").append(client.getUserName()).append("', getdate(),0,null,null,0,0,'").append(vipId).append("','").append(vipCode).append("',").append(retailAmount).append(", ")
                .append(discountMoney).append(", '").append(DataUtils.getYear()).append("', '").append(DataUtils.getStringMonth()).append("',0,0,'").append(memo).append("',").append(exchangedPoint).append(")");
        commonDao.executeSql(sql.toString());
        // 更新主表(修改,审核)
        sql = new StringBuffer();
        if (posBackAudit) {
            sql.append("update Possales set DiscountSum = (select sum(isnull(Discount,0)) from possalesdetail where possalesId = ?)  where PossalesId = ?");
            commonDao.executeSql(sql.toString(), posSalesId, posSalesId);
        } else {
            sql.append("update Possales set DiscountSum = (select sum(isnull(Discount,0)) from possalesdetail where possalesId = ?), AuditFlag = ? , Audit = ? , AuditDate = getdate(),OrderFlag = 0 where PossalesId = ?");
            commonDao.executeSql(sql.toString(), posSalesId, 1, client.getUserName(), posSalesId);
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put("PosSalesID", posSalesId);
        map.put("PosSalesNo", no);
        return map;
    }

}
