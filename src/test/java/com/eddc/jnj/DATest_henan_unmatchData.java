package com.eddc.jnj;

import com.eddc.jnj.service.DAService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 河南省平台订单未匹配数据
 * @Author: keshi
 * @CreateDate: 2018年12月4日 16:23
 * @UpdateUser:
 * @UpdateDate:
 * @UpdateRemark:
 * @Version: 1.0
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class DATest_henan_unmatchData {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    static SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
    static String dateStr = sf.format(new Date());
    private static final String DATE = dateStr;
    private static final String BU = "CNV";

    @Autowired
    DAService daService;

    //1.获取邮件附件
    @Test
    public void getHenanUnmatchDataTest() {
        Map<String, Object> params = new HashMap();
        params.put("BU", BU);
        params.put("date", DATE);
        params.put("num", "-7");
        params.put("num2", "6");
        daService.getHenanUnmatchDataAttachment(params);
    }

    //2.发送邮件
    @Test
    public void sendHenanUnmatchDataEmailTest() {
        /*
         * 参数说明
         * province_name,BU 邮件接收者
         * date 邮件附件文件路径
         * */
        Map<String, Object> params = new HashMap();
        params.put("date", DATE);
        params.put("province_name", "河南省");
        params.put("BU", "BW");
        daService.sendHenanUnmatchDataEmail(params);
    }

}
