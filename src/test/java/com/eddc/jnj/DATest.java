package com.eddc.jnj;

import com.eddc.jnj.dao.DADao;
import com.eddc.jnj.service.DAService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DATest {

	@Autowired
	private DAService daService;

    @Autowired
    private Configuration configuration;

	@Autowired
	private JavaMailSender jms;



	@Test
	public void export() {
		Map<String,Object> params=new HashMap<String, Object>(3);
		params.put("BU", "BW");
		params.put("date_from", "2018-10-22");
		params.put("date_end","2018-10-29");
		params.put("period","09");
		daService.exportSiChuanOrderData(params);
	}
	@Test
	public void sendSiChuanOrderData(){
        Map<String,Object> params=new HashMap<String, Object>(3);
        params.put("province_name","四川省");
        params.put("BU", "BW");
        params.put("date_from", "2018-10-22");
        params.put("date_end","2018-10-29");
        params.put("period","09");
        daService.sendSiChuanOrderData(params);
    }
    @Test
	public void sendEmailTest() throws MessagingException {
        MimeMessage message =jms.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper( message,true);
            helper.setFrom("eddc@earlydata.com");
            helper.setTo("viking.luan@earlydata.com");
            helper.setSubject("使用模板");

            Map<String, Object> model = new HashMap();
            model.put("UserName", "yao");

            try {
                Template template = configuration.getTemplate("siChuanOrder.ftl");
                String html = FreeMarkerTemplateUtils.processTemplateIntoString(template,model);

                helper.setText(html,true);
            } catch (Exception e) {
                e.printStackTrace();
            }


        } catch (MessagingException e) {
            e.printStackTrace();
        }

        jms.send(message);
	}
}
