package com.eddc.jnj;

import com.eddc.jnj.service.DAService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DaTest_henan_RelationDistribution {

    @Autowired
    DAService daService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String DATE = "2018-11-21";

    /*1测试 获取邮件附件*/
    @Test
    public void getHeNanRelationDistributionDataTest() {
        Map<String, Object> params = new HashMap();
        params.put("date", DATE);
        daService.getHeNanRelationDistributionData(params);
    }

    /*2测试 发送邮件*/
    @Test
    public void sentHeNanRelationDistributionTest() {
        /*
         * 参数说明
         * province_name,BU 邮件接收者
         * date 邮件附件文件路径
         * */

        Map<String, Object> params = new HashMap();
        params.put("date", DATE);
        params.put("province_name", "河南省");
        params.put("BU", "BW");

        daService.sentHeNanRelationDistributionEmail(params);
    }
}
