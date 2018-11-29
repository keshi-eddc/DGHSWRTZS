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
 * @Description: 陕西省 邮件推送测试
 * @Author: keshi
 * @CreateDate: 2018年11月29日 10:26
 * @UpdateUser:
 * @UpdateDate:
 * @UpdateRemark:
 * @Version: 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ShanXiEmailTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    DAService daService;

    static SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
    static String dateStr = sf.format(new Date());
    private static final String DATE = dateStr;

    @Test
    public void getShanXiAttachmentTest() {
        Map<String, Object> params = new HashMap();
        params.put("date", DATE);
        daService.getShanXiAttachment(params);
    }

    @Test
    public void sendShanXiEmailTest() {
        Map<String, Object> params = new HashMap();
        params.put("date", DATE);
        params.put("province_name", "陕西省");

        daService.sendShanXiEmail(params);
    }

}
