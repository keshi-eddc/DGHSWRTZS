package com.eddc.jnj.service;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/19.
 */
public interface DAService {

    List<Map<String,Object>> getSiChuanOrderData(Map params);
    void exportSiChuanOrderData(Map params);
    void sendSiChuanOrderData(Map params);

    List<Map<String,Object>> getHeNanOrderData(Map params);
    void exportHeNanOrderData(Map params);
    void sendHeNanOrderData(Map params);

    //-----------河南----------
    //1.跟新数据到业务表
    void updateHeNanToTable(Map params);



}
