package com.eddc.jnj.service.impl;

import com.avalon.holygrail.excel.bean.SXSSFExcelTitle;
import com.avalon.holygrail.util.Export;
import com.eddc.jnj.config.Export_path_config;
import com.eddc.jnj.dao.DADao;
import com.eddc.jnj.service.DAService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.internet.MimeMessage;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2017/8/16.
 */
@Service(value = "daService")
public class DAServiceImpl implements DAService {


    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DADao daDao;//这里会报错，但是并不会影响

    @Autowired
    private Configuration configuration;

    @Autowired
    private Export_path_config resource;

    @Autowired
    private JavaMailSender jms;

    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    static String datestr = df.format(new Date());
    static String dateyear = StringUtils.substringBefore(datestr, "-");
    static String datemon = StringUtils.substringBetween(datestr, "-", "-");
    static String dateday = StringUtils.substringAfterLast(datestr, "-");

    /*获取邮件正文及附件数据*/
    public List<Map<String, Object>> getSiChuanOrderData(Map params) {
        return daDao.getSiChuanOrderDataMessage(params);
    }

    /*导出数据
     * outType取值：列表附件、详情附件
     * */
    public void exportSiChuanOrderData(Map params) {

        params.put("outType", "列表附件");
        List<Map<String, Object>> list = this.getSiChuanOrderData(params);//获取首页数据
        SXSSFExcelTitle[][] list_titles = this.getTitles(list);
        List<String> list_columnFields = this.getColumnFields(list);

        params.put("outType", "详情附件");
        List<Map<String, Object>> detail = this.getSiChuanOrderData(params);//获取详情页数据
        SXSSFExcelTitle[][] detail_titles = this.getTitles(detail);
        List<String> detail_columnFields = this.getColumnFields(detail);


        if (list.size() < 0 || detail.size() < 0) {
            logger.info("四川省BU：" + params.get("BU").toString() + "无数据产出,日期：" + params.get("date_from").toString() + "|" + params.get("date_end").toString());
            return;
        } else {
            logger.info("四川省BU：" + params.get("BU").toString() + ",日期：" + params.get("date_from").toString() + "|" + params.get("date_end").toString() + "开始导出数据");

            /*构建导出文件路径*/
            Calendar now = Calendar.getInstance();
            now.setTime(new Date());
            String now_date_str = DateFormatUtils.format(now, "yyyy.MM.dd");
            now.add(Calendar.DAY_OF_MONTH, -7);
            now.set(Calendar.DAY_OF_WEEK, now.getActualMinimum(Calendar.DAY_OF_WEEK) + 1);
            String last_week_first_day = DateFormatUtils.format(now, "yyyy.MM.dd");
            now.add(Calendar.DAY_OF_MONTH, 7);
            String last_week_last_day = DateFormatUtils.format(now, "yyyy.MM.dd");

            String outPath = resource.getSiChuan_basicPath() + "\\" + "四川省平台订单数据-" + params.get("BU").toString() + " " + last_week_first_day + "_" + last_week_last_day + ".xlsx";
            try {
                Export.buildSXSSFExportExcelWorkBook().createSheet("首页").setTitles(list_titles).setColumnFields(list_columnFields).importData(list)
                        .getOwnerWorkBook().createSheet("详情页").setTitles(detail_titles).setColumnFields(detail_columnFields).importData(detail)
                        .export(outPath);
                logger.info("四川省BU：" + params.get("BU").toString() + ",日期：" + params.get("date_from").toString() + "|" + params.get("date_end").toString() + "导出数据完毕");
            } catch (Exception e) {
                logger.error("文件生成异常：" + e.getMessage());
            }
        }
        /*Map<String,Object> params = new HashMap<>();
        params.put("BU",bu);
        params.put("date_from", date_end);
        params.put("date_end",date_end);
        params.put("period",period);
        params.put("outType",outType);*/

    }

    /*发送正文及数据*/
    public void sendSiChuanOrderData(Map params) {
        /*获取所有的邮件接收者*/
        List<Map<String, Object>> users = daDao.selectUsersByProviceByBu(params);
        String[] tos = new String[users.size()];
        for (int i = 0; i < users.size(); i++) {
            tos[i] = users.get(i).get("mail_address").toString();
        }

        /*获取所有要发送的数据*/
        params.put("outType", "邮件正文");
        List<Map<String, Object>> text = this.getSiChuanOrderData(params);//获取邮件正文
        params.put("outType", "正文统计");
        List<Map<String, Object>> statistics = this.getSiChuanOrderData(params);//获取正文统计
        params.put("outType", "异常正文");
        List<Map<String, Object>> text_exception = this.getSiChuanOrderData(params);//获取异常正文
        params.put("outType", "异常详情");
        List<Map<String, Object>> statistics_exception = this.getSiChuanOrderData(params);//获取异常详情

        /*构建文件路径*/
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        String subject_date = DateFormatUtils.format(now, "yyyy.MM.dd");//邮件主题日期
        String now_date_str = DateFormatUtils.format(now, "yyyy.MM.dd");
        now.add(Calendar.DAY_OF_MONTH, -7);
        now.set(Calendar.DAY_OF_WEEK, now.getActualMinimum(Calendar.DAY_OF_WEEK) + 1);
        String last_week_first_day = DateFormatUtils.format(now, "yyyy.MM.dd");
        now.add(Calendar.DAY_OF_MONTH, 7);
        String last_week_last_day = DateFormatUtils.format(now, "yyyy.MM.dd");
        /*附件文件*/
        String filePath = resource.getSiChuan_basicPath() + "\\" + "四川省平台订单数据-" + params.get("BU").toString() + " " + last_week_first_day + "_" + last_week_last_day + ".xlsx";
        /*附件名称*/
        String attachmentFilename = "四川省平台订单数据-" + params.get("BU").toString() + " " + last_week_first_day + "_" + last_week_last_day + ".xlsx";

        /*邮件主题*/
        String subject = "四川省平台订单数据推送-" + params.get("BU").toString() + "," + subject_date;
        /*邮件正文*/
        String mail_text = text.get(0).get("describe").toString();
        if (StringUtils.equals("12", params.get("period").toString())) {
            StringUtils.replaceOnce(mail_text, "日", "日第1次");
        } else if (StringUtils.equals("15", params.get("period").toString())) {
            StringUtils.replaceOnce(mail_text, "日", "日第2次");
        } else if (StringUtils.equals("18", params.get("period").toString())) {
            StringUtils.replaceOnce(mail_text, "日", "日第3次");
        }
        /*获取正文统计*/
        String mail_statistics_text = "";
        if (CollectionUtils.isNotEmpty(text_exception)) {
            mail_statistics_text = statistics.get(0).get("describe").toString();
        }

        /*异常正文*/
        String mail_text_exception = "";
        if (CollectionUtils.isNotEmpty(text_exception)) {
            mail_text_exception = text_exception.get(0).get("describe").toString();
        }

        System.setProperty("mail.mime.splitlongparameters", "false");
        MimeMessage message = jms.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("eddc@earlydata.com");
            String to = "viking.luan@earlydata.com";
            helper.setTo(to);
            helper.setSubject(subject);


            FileSystemResource file = new FileSystemResource(new File(filePath));
            helper.addAttachment(attachmentFilename, file);//添加附件

            Map<String, Object> datas = new HashMap();
            datas.put("mail_text", mail_text);
            datas.put("statistics_text", mail_statistics_text);
            datas.put("mail_text_exception", mail_text_exception);
            datas.put("statistics_exception", statistics_exception);


            Template template = configuration.getTemplate("siChuanOrder.ftl");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, datas);
            helper.setText(html, true);

            jms.send(message);

        } catch (Exception e) {
            logger.info("发送邮件异常：" + e.getMessage());
        }

    }

    public List<String> getColumnFields(List<Map<String, Object>> datas) {
        List<String> columnFields = new ArrayList<>();
        for (String key : datas.get(0).keySet()) {
            columnFields.add(key);
        }
        return columnFields;
    }

    //获得表头
    public SXSSFExcelTitle[][] getTitles(List<Map<String, Object>> datas) {
        List<String> columnFields = new ArrayList<>();
        for (String key : datas.get(0).keySet()) {
            if (key.equals("link")) {
                key = "操作link";
            } else if (key.equals("product_code")) {
                key = "产品代码";
            } else if (key.equals("purchaseListName")) {
                key = "采购单名称";
            } else if (key.equals("logistics")) {
                key = "配送企业";
            } else if (key.equals("hospital")) {
                key = "采购医院";
            } else if (key.equals("price_limit")) {
                key = "采购限价";
            } else if (key.equals("purchasePrice")) {
                key = "采购价";
            } else if (key.equals("purchaseCnt")) {
                key = "采购量";
            } else if (key.equals("deliveryCnt")) {
                key = "配送量";
            } else if (key.equals("inboundCnt")) {
                key = "入库总量";
            } else if (key.equals("returnedCnt")) {
                key = "退货量";
            } else if (key.equals("begin_time")) {
                key = "开始时间";
            } else if (key.equals("end_time")) {
                key = "结束时间";
            } else if (key.equals("delivery_time")) {
                key = "企业配送时间";
            } else if (key.equals("reached_time")) {
                key = "医院入库时间";
            } else if (key.equals("insert_time")) {
                key = "数据更新时间";
            } else if (key.equals("datetime")) {
                key = "更新日期";
            }
//            System.out.println("表头：" + key);
            columnFields.add(key);
        }
        //构建用于描述标题的二维数组,获取数据代码略
        SXSSFExcelTitle[][] titles = new SXSSFExcelTitle[1][];
        SXSSFExcelTitle[] title = new SXSSFExcelTitle[columnFields.size()];
        for (int i = 0; i < title.length; i++) {
            title[i] = new SXSSFExcelTitle();
            title[i].setTitle(columnFields.get(i));
            title[i].setField(columnFields.get(i));
            title[i].setWriteEmpty(true);
        }
        titles[0] = title;

      /*  title[0].setField("username");//设置标题所在列对应数据的字段
        title[1].setField("password");
        title[2].setField("mobile");
        title[0].setTitle("姓名");//设置标题名称
        title[1].setTitle("密码");
        title[2].setTitle("手机号");
        title[0].setWidth(50);//设置列宽,默认10,由于计算原因,实际效果稍有误差
        title[1].setWidth(60);
        title[2].setWidth(70);
        title[0].setHAlign(CellStyle.H_AlignType.LEFT);//设置水平对齐方式为左对齐,标题默认水平居中
        title[0].setVAlign(CellStyle.V_AlignType.BOTTOM);//设置垂直对齐方式为底部对齐,标题默认垂直居中
        title[0].setBorderLeft(CellStyle.BorderStyle.DASH_DOT);//设置左边框样式,同样还可以设置其它方向边框
        titles[0] = title;*/
        return titles;
    }

    //-----------------河南---------------

    @Override
    public List<Map<String, Object>> getHeNanOrderData(Map params) {
        return daDao.getHeNanOrderDataMessage(params);
    }

    @Override
    public void updateExportHeNanOrderData(Map params) {

        List<Map<String, Object>> detail = this.getHeNanOrderData(params);
        logger.info("--- 执行存储过程，返回结果集有 :" + detail.size() + " 行");

        if (detail.size() == 0) {
            logger.info("河南省BU：" + params.get("BU").toString() + "无数据产出,日期：" + params.get("date").toString() + "|" + params.get("num").toString());
            return;
        } else {
            SXSSFExcelTitle[][] detail_titles = this.getTitles(detail);
            List<String> detail_columnFields = this.getColumnFields(detail);

            logger.info("河南省BU：" + params.get("BU").toString() + ",日期：" + params.get("date").toString() + "|" + params.get("num").toString() + " 开始导出数据");
            /*构建导出文件路径*/
            Calendar now = Calendar.getInstance();
            now.setTime(new Date());
            String now_date_str = DateFormatUtils.format(now, "yyyy.MM.dd");
            now.add(Calendar.DAY_OF_MONTH, -7);
            now.set(Calendar.DAY_OF_WEEK, now.getActualMinimum(Calendar.DAY_OF_WEEK) + 1);
            String last_week_first_day = DateFormatUtils.format(now, "yyyy.MM.dd");
            now.add(Calendar.DAY_OF_MONTH, 7);
            String last_week_last_day = DateFormatUtils.format(now, "yyyy.MM.dd");

            String outPath = resource.getHeNan_basicPath() + "\\" + "存储过程结果" + "\\" + dateyear + "\\" + datemon + "\\" +
                    "河南省平台订单数据存储过程结果-" + params.get("date").toString() + " " + last_week_first_day + "_" + last_week_last_day + ".xlsx";
            logger.info("文件输出路径:" + outPath);
            try {
                Export.buildSXSSFExportExcelWorkBook().createSheet("详情页").setTitles(detail_titles).setColumnFields(detail_columnFields).importData(detail)
                        .export(outPath);
                logger.info("河南省BU：" + params.get("BU").toString() + ",日期：" + params.get("date_from").toString() + "|" + params.get("date_end").toString() + "导出数据完毕");
            } catch (Exception e) {
                logger.error("文件生成异常：" + e.getMessage());
            }
        }
    }

    //执行sql,正常数据
    @Override
    public void getDataHeNan(Map params) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String sql = "select * from Johnson_henan_OrderDistributeGoodsDetails_OrderDetailed_forcust  where " +
                " datetime='" + params.get("datetime") + "'" +
                " and bu='" + params.get("bu") + "'" +
                " and 是否新增='" + params.get("isNew") + "'" +
                " and 账号='" + params.get("user") + "'";
        logger.info("sql:   " + sql);
        List<Map<String, Object>> mapList = daDao.getDataHeNan(sql);
        logger.info("执行sql，获得:" + mapList.size() + " 条数据");
        if (mapList.size() > 0) {
            SXSSFExcelTitle[][] detail_titles = this.getTitles(mapList);
            List<String> detail_columnFields = this.getColumnFields(mapList);
            //修改表头

            logger.info(" 河南省 正常数据 " + params.get("user") + " " + params.get("bu") + " 日期：" + params.get("datetime") + " 开始导出数据");
            /*构建导出文件路径*/
            Calendar now = Calendar.getInstance();
            try {
                now.setTime(format.parse((String) params.get("datetime")));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String now_date_str = DateFormatUtils.format(now, "yyyy-MM-dd");
            now.add(Calendar.DAY_OF_MONTH, Integer.valueOf(params.get("num").toString()));
            now.set(Calendar.DAY_OF_WEEK, now.getActualMinimum(Calendar.DAY_OF_WEEK) + 3);
            String last_week_first_day = DateFormatUtils.format(now, "yyyy-MM-dd");
            now.add(Calendar.DAY_OF_MONTH, -Integer.valueOf(params.get("num").toString()));
            String last_week_last_day = DateFormatUtils.format(now, "yyyy-MM-dd");

            String outPath = resource.getHeNan_basicPath() + "\\" + dateyear + "\\" + datemon + "\\"
                    + params.get("user") + " 中标产品配送情况汇总详情【" + last_week_first_day + "至" + last_week_last_day + "】.xlsx";
            logger.info("文件输出路径:" + outPath);
            try {
                Export.buildSXSSFExportExcelWorkBook().createSheet("Sheet0").setTitles(detail_titles).setColumnFields(detail_columnFields).importData(mapList)
                        .export(outPath);
                logger.info(" 河南省 " + params.get("user") + " " + params.get("bu") + " 日期：" + params.get("datetime") + "导出数据完毕");
            } catch (Exception e) {
                logger.error("文件生成异常：" + e.getMessage());
            }
        } else {
            logger.error("执行sql，获得数据 0 个");
        }
    }

    @Override
    public void getDataHeNanabnormal(Map params) {
        String sql = "select * from Johnson_henan_OrderDistributeGoodsDetails_OrderDetailed_forcust  where " +
                " datetime='" + params.get("datetime") + "'" +
                " and bu='" + params.get("bu") + "'" +
                " and 是否新增='" + params.get("isNew") + "'" +
                " and " + params.get("dataType");
        logger.info("sql:   " + sql);
        List<Map<String, Object>> mapList = daDao.getDataHeNan(sql);
        logger.info("执行sql，获得:" + mapList.size() + " 条数据");
        if (mapList.size() > 0) {
            SXSSFExcelTitle[][] detail_titles = this.getTitles(mapList);
            List<String> detail_columnFields = this.getColumnFields(mapList);

            logger.info(" 河南省 异常数据 " + " " + params.get("bu") + " 日期：" + params.get("datetime") + " 开始导出数据");
            /*构建导出文件路径*/

            String outPath = resource.getHeNan_basicPath() + "\\" + dateyear + "\\" + datemon + "\\"
                    + params.get("bu") + " 河南异常订单跟进" + params.get("datetime") + ".xlsx";
            logger.info("文件输出路径:" + outPath);
            try {
                Export.buildSXSSFExportExcelWorkBook().createSheet("Sheet0").setTitles(detail_titles).setColumnFields(detail_columnFields).importData(mapList)
                        .export(outPath);
                logger.info(" 河南省 " + params.get("user") + " " + params.get("bu") + " 日期：" + params.get("datetime") + "导出数据完毕");
            } catch (Exception e) {
                logger.error("文件生成异常：" + e.getMessage());
            }
        } else {
            logger.error("执行sql，获得数据 0 个");
        }
    }

    @Override
    public void sendHeNanOrderData(Map params) {
        /*获取所有的邮件接收者*/
        List<Map<String, Object>> users = daDao.selectUsersByProviceByBu(params);
        String[] tos = new String[users.size()];
        for (int i = 0; i < users.size(); i++) {
            tos[i] = users.get(i).get("mail_address").toString();
            logger.info("发送给：" + tos[i].toString());
        }

        /*获取所有要发送的数据*/
        //获取邮件正文
        params.put("num2", "4");
        List<Map<String, Object>> text = this.getHeNanOrderData(params);
        //获取正文统计表
        params.put("num2", "2");
        List<Map<String, Object>> statistics = this.getHeNanOrderData(params);

        /*构建文件路径*/
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
//        String subject_date = DateFormatUtils.format(now, "yyyy.MM.dd");//邮件主题日期
        String subject_date = String.valueOf(now.get(Calendar.YEAR)) + "年" + String.valueOf(now.get(Calendar.MONTH) + 1) + "月" + String.valueOf(now.get(Calendar.DATE)) + "日";
        String now_date_str = DateFormatUtils.format(now, "yyyy.MM.dd");
//        now.add(Calendar.DAY_OF_MONTH, -7);
        now.set(Calendar.DAY_OF_WEEK, now.getActualMinimum(Calendar.DAY_OF_WEEK) + 3);
        String last_week_first_day = DateFormatUtils.format(now, "yyyy.MM.dd");
        now.add(Calendar.DAY_OF_MONTH, 7);
        String last_week_last_day = DateFormatUtils.format(now, "yyyy.MM.dd");
        /*附件文件*/
        String outPath = resource.getHeNan_basicPath() + "\\" + dateyear + "\\" + datemon + "\\";
        File fileDir = new File(outPath);
        File[] files = new File[100];
        if (fileDir.isDirectory()) {
            files = fileDir.listFiles();
        } else {
            logger.error("路径错误");
        }
        /*附件名称*/

        /*邮件主题*/
        String subject = "河南省平台订单数据推送-" + params.get("BU").toString() + " " + subject_date;
        /*邮件正文*/
        String mail_text = "";
        for (int i = 0; i < text.size(); i++) {
            Map<String, Object> temp = text.get(i);
            for (String key : temp.keySet()) {
                mail_text = temp.get(key).toString();
            }
        }
        logger.info("mail_text:" + mail_text);

        System.setProperty("mail.mime.splitlongparameters", "false");
        MimeMessage message = jms.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("eddc@earlydata.com");
            String to = "keshi.wang@earlydata.com";
            //测试
//            helper.setTo(to);
            //正式
            helper.setTo(tos);
            helper.setSubject(subject);

            for (File filefile : files) {
                String fileName = filefile.getName();
                //正常数据的文件
                String matchedNormalFileName = "至" + params.get("date").toString() + "】";
                //异常数据的文件
                String matchedAbnormalFileName = "订单跟进" + params.get("date").toString();
                if (fileName.contains(matchedNormalFileName) || fileName.contains(matchedAbnormalFileName)) {
                    if (!fileName.contains("副本")) {
                        FileSystemResource file = new FileSystemResource(filefile);
                        helper.addAttachment(file.getFilename(), file);//添加附件
                        logger.info("- 添加附件：" + fileName);
                    }
                } else {
//                    logger.error("没有发现 文件 ：**至" + params.get("date"));
                }
            }

            Map<String, Object> datas = new HashMap();
            datas.put("mail_text", mail_text);
            datas.put("statistics", statistics);

            Template template = configuration.getTemplate("heNanOrder.ftl");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, datas);
            helper.setText(html, true);

            jms.send(message);

        } catch (Exception e) {
            logger.info("发送邮件异常：" + e.getMessage());
        }

    }
}
