package com.eddc.jnj.service.heNanAccountManage;

import com.eddc.jnj.model.SycmAccount;

import java.util.List;
import java.util.Map;

/**
 * @Description: 管理河南省的账号
 * @Author: keshi
 * @CreateDate: 2018年11月30日 11:33
 * @UpdateUser:
 * @UpdateDate:
 * @UpdateRemark:
 * @Version: 1.0
 */
public interface HeNanAccountManageService {

    //查看所有的账号信息
    List<SycmAccount> getAllAccountInfo();

    //读账号的Excel
    List<Map<String, String>> getExcelContent(String filePath);

}
