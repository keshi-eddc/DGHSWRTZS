package com.eddc.jnj;

import com.eddc.jnj.dao.SycmAccountDao;
import com.eddc.jnj.model.SycmAccount;
import com.eddc.jnj.service.heNanAccountManage.HeNanAccountManageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @Description: 测试 管理 河南省的账号
 * @Author: keshi
 * @CreateDate: 2018年11月30日 11:59
 * @UpdateUser:
 * @UpdateDate:
 * @UpdateRemark:
 * @Version: 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class HeNanAccountManageTest {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    HeNanAccountManageService heNanAccountManageService;

    @Resource
    SycmAccountDao accountDao;

    @Test
    public void getAllAccountInfoTest() {
        List<SycmAccount> sycmAccountList = heNanAccountManageService.getAllAccountInfo();
        if (sycmAccountList.size() > 0) {
            for (int i = 0; i < sycmAccountList.size(); i++) {
                SycmAccount sycmAccount = sycmAccountList.get(i);
                logger.info(sycmAccount.toString());
            }
        } else {
            logger.info("！！！未获得数据");
        }
    }

    @Test
    public void getOneAccountInfoTest() {
        SycmAccount sycmAccount = accountDao.selectByPrimaryKey(77L);
        logger.info(sycmAccount.toString());
    }

    @Test
    public void readExcelTest() {
        String filePath = "E:\\data\\ProjectDescription\\强生项目\\河南省\\账号信息\\账号表2018年11月30日.xlsx";
        List<Map<String, String>> mapList = heNanAccountManageService.getExcelContent(filePath);
        System.out.println("账号信息有：" + mapList.size());
        for (int i = 0; i < mapList.size(); i++) {
            Map<String, String> excleMap = mapList.get(i);
            for (String key : excleMap.keySet()) {
                System.out.print(i + " ");
                System.out.print(key + " = " + excleMap.get(key) + " ");
            }
            System.out.println();
        }
    }


    @Test
    public void addAccountInfoToDB() {
        String filePath = "E:\\data\\ProjectDescription\\强生项目\\河南省\\账号信息\\账号表2018年11月30日.xlsx";
        List<Map<String, String>> mapList = heNanAccountManageService.getExcelContent(filePath);
        System.out.println("账号信息有：" + mapList.size());
        for (int i = 0; i < mapList.size(); i++) {
            Map<String, String> excleMap = mapList.get(i);
            for (String key : excleMap.keySet()) {
                System.out.print(key + " = " + excleMap.get(key) + " ");
            }
            System.out.println();
            //组装对象
            SycmAccount sycmAccount = new SycmAccount();
            int id = accountDao.getMaxId() + 1;
            Long id_long = Long.valueOf(id);
            String name = excleMap.get("账号");
            String shop_name = "河南省医用耗材采购";
            String sycm_group = "Johnson";
            String password = excleMap.get("交易系统1126新密码").toString();

            sycmAccount.setId(id_long);
            sycmAccount.setName(name);
            sycmAccount.setPassword(password);
            sycmAccount.setToken("clear");
            sycmAccount.setSellerCookie("clear");
            sycmAccount.setSycmGroup(sycm_group);
            /*
             * 区分哪个账号用于哪个模块的抓取
             * userId:模块一模块二 含义：这个账号抓取模块一，抓取模块二
             * userId:模块一       含义：这个账号抓取模块一
             * userId:模块二       含义：这个账号抓取模块二
             * */
            String userId = "模块二";
            sycmAccount.setUserid(userId);
            sycmAccount.setShopName(shop_name);
            sycmAccount.setLoginStatus("Y");
            sycmAccount.setStatus("permission");
            sycmAccount.setCookieStatus("Y");
            sycmAccount.setChituCookie("clear");
            sycmAccount.setShopUrl("http://hc.hnggzyjy.cn/UserLogin.aspx");

            System.out.println(sycmAccount.toString());

            //判断是否存在
            SycmAccount oldSycmAccount = accountDao.selectByPrimaryKey(sycmAccount.getId());
            if (oldSycmAccount == null) {
                logger.info("- 这个账号不存在，现在插入数据库 " + i);
                //不存在，插入数据库
                accountDao.insertSelective(sycmAccount);
            } else {
                //存在，更新
                accountDao.deleteByPrimaryKey(oldSycmAccount.getId());
                logger.info("- 这个账号已经存在，现在删除了");
            }
        }
    }


}
