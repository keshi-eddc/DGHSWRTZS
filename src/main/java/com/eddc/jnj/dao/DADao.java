package com.eddc.jnj.dao;


import com.eddc.jnj.model.UserDomain;

import java.util.List;
import java.util.Map;

public interface DADao {


    int insert(UserDomain record);

    List<Map<String,Object>> selectUsersByProviceByBu(Map params);

    List<Map<String,Object>> getSiChuanOrderDataMessage(Map params);

    List<Map<String,Object>> getHeNanOrderDataMessage(Map params);

    //更新数据到业务表
    List<Map<String,Object>> updateHeNanToTableDetail(Map params);
    //执行sql获得数据 -正常
    List<Map<String,Object>> getDataHeNan(String sql);





}