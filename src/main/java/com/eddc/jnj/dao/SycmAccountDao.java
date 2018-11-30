package com.eddc.jnj.dao;

import com.eddc.jnj.model.SycmAccount;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SycmAccountDao {
    // 增
    int insert(SycmAccount record);

    int insertSelective(SycmAccount record);

    // 删
    int deleteByPrimaryKey(Long id);

    // 改
    int updateByPrimaryKeySelective(SycmAccount record);

    int updateByPrimaryKey(SycmAccount record);

    // 查
    SycmAccount selectByPrimaryKey(Long id);

    List<SycmAccount> selectAll();

    //获得最大的id
    int getMaxId();

    //查看 账号是否 已经 存在

    List<SycmAccount> selectAccountByNameByGroupByShop(@Param("name") String name, @Param("sycm_group") String sycm_group, @Param("shop_name") String shop_name);

}
