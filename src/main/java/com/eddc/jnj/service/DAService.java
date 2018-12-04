package com.eddc.jnj.service;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/19.
 */
public interface DAService {

    List<Map<String, Object>> getSiChuanOrderData(Map params);

    void exportSiChuanOrderData(Map params);

    void sendSiChuanOrderData(Map params);

    /*河南省 邮件推送*/
    List<Map<String, Object>> getHeNanOrderData(Map params);

    //1.跟新数据到业务表
    void updateExportHeNanOrderData(Map params);

    //2.1 sql 获得数据-正常数据 导出附件Excel
    void getDataHeNan(Map params);

    //2.2 sql 获得数据-正常数据 导出附件Excel
    void getDataHeNanabnormal(Map params);

    //3 发送邮件
    void sendHeNanOrderData(Map params);

    /*河南省平台配送企业数据推送*/
    //1.获得邮件附件
    void getHeNanRelationDistributionData(Map params);

    //2.发送邮件
    void sentHeNanRelationDistributionEmail(Map params);

    /*****河南省平台订单未匹配数据邮件推送*****/
    //1.获得邮件附件
    void getHenanUnmatchDataAttachment(Map params);

    //2.发送邮件
    void sendHenanUnmatchDataEmail(Map params);

    /*****陕西省 邮件推送*****/
    //1.获得邮件的附件
    void getShanXiAttachment(Map params);

    //2.发送邮件
    void sendShanXiEmail(Map params);

}
