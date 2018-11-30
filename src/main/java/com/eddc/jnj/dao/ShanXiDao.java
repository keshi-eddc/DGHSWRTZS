package com.eddc.jnj.dao;

import java.util.List;
import java.util.Map;

public interface ShanXiDao {
    //1.获得议价信息 的内容
    List<Map<String, Object>> getShanXiDiscussPriceData(Map params);

    //2.获得挂网数据 的内容
    List<Map<String, Object>> getShanXiCatalogueData(Map params);

    //3.获得邮件接收者
    List<Map<String, Object>> selectUsersByProviceBySendType(Map params);


}
