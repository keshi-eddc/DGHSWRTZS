package com.eddc.jnj.service.impl;

import com.avalon.holygrail.excel.bean.SXSSFExcelTitle;
import com.avalon.holygrail.excel.norm.ExcelWorkBookExport;
import com.avalon.holygrail.util.Export;
import com.eddc.jnj.config.Export_path_config;
import com.eddc.jnj.dao.DADao;
import com.eddc.jnj.service.DAService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
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
import java.io.IOException;
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
    private static SimpleDateFormat df1 = new SimpleDateFormat("yyyy.MM.dd");
    private final String allStartDate = "2015.01.01";

    //正常数据附件匹配
    //异常数据附件匹配

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

            String datestr = params.get("date").toString();
            String dateyear = StringUtils.substringBefore(datestr, "-");
            String datemon = StringUtils.substringBetween(datestr, "-", "-");

            String outPath = resource.getHeNan_basicPath() + "\\" + "存储过程结果" + "\\" + dateyear + "\\" + datemon + "\\" +
                    "河南省平台订单数据存储过程结果-" + params.get("date").toString() + " " + last_week_first_day + " " + last_week_last_day + ".xlsx";
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
        //遍历用户，写到一个表的不同sheet
        //获得所有的用户
//        String sql_user = "SELECT * FROM [dbo].[sycm_account] where shop_name like '%河南%'";
//        logger.info("- 查询所有用户sql :" + sql_user);
//        List<Map<String, Object>> userListMap = daDao.getaHeNanUsers(sql_user);
        /*构建导出文件路径*/
        String datestr = params.get("date").toString();
        String dateyear = StringUtils.substringBefore(datestr, "-");
        String datemon = StringUtils.substringBetween(datestr, "-", "-");
        String dateDay = StringUtils.substringAfterLast(datestr, "-");
        String outPath = resource.getHeNan_basicPath() + "\\" + dateyear + "\\" + datemon + "\\"
                + "河南省中标产品配送情况汇总 " + allStartDate + "_" + dateyear + "." + datemon + "." + dateDay + ".xlsx";
        logger.info("- 文件输出路径:" + outPath);
        //初始化 SXSSFWorkbook 的大小
        String sql_allCount = "select COUNT(1) as allNum from Johnson_henan_OrderDistributeGoodsDetails_OrderDetailed_forcust";
        logger.info("- 查询所有Johnson_henan 数据量 :" + sql_allCount);
        List<Map<String, Object>> allCountListMap = daDao.getaHeNanUsers(sql_allCount);
        Map<String, Object> allCountMap = allCountListMap.get(0);
        int rowAWSize = Integer.valueOf(allCountMap.get("allNum").toString());
//        System.out.println("rowAWSize:" + rowAWSize);
        SXSSFWorkbook wb = new SXSSFWorkbook(rowAWSize);
        ExcelWorkBookExport ewb = Export.buildSXSSFExportExcelWorkBook(wb);
        //创建sheet
//        SheetExportHandler sheetExportHandler = null;
//        try {
//            sheetExportHandler = ewb.createSheet("Sheet1");
//        } catch (ExportException e) {
//            e.printStackTrace();
//        }

        try {
            String sql = "select * from Johnson_henan_OrderDistributeGoodsDetails_OrderDetailed_forcust  where " +
                    " 更新日期='" + params.get("date") + "'" +
                    " and BU='" + params.get("BU") + "'" +
//                            " and 账号='" + userName + "'" +
                    " and GAP <> '退市'" +
                    " order by 本周新增 desc,是否异常 desc,cast (GAP as float) asc";
            logger.info("sql:   " + sql);
            List<Map<String, Object>> mapList = daDao.getDataHeNan(sql);
            if (mapList.size() > 0) {
                logger.info("执行sql，获得:" + mapList.size() + " 条数据");

                SXSSFExcelTitle[][] detail_titles = this.getTitles(mapList);
                List<String> detail_columnFields = this.getColumnFields(mapList);

                logger.info(" 河南省 正常数据 " + params.get("BU") + " 日期：" + params.get("date") + " 数据导入表格");
                //数据导入表格
                try {
//                    //                Export.buildSXSSFExportExcelWorkBook().createSheet("Sheet0").setTitles(detail_titles).setColumnFields(detail_columnFields).importData(mapList)
//                    //                        .export(outPath);
////                            wb = new SXSSFWorkbook(mapList.size() + 1);
//                    if (i == 0) {
//                        sheetExportHandler.setTitles(detail_titles).setColumnFields(detail_columnFields).importData(mapList);
//                    } else {
//                        sheetExportHandler.setColumnFields(detail_columnFields).importData(mapList);
//                    }
                    //创建以userName为sheet 多个
                    ewb.createSheet("Sheet1").setTitles(detail_titles).setColumnFields(detail_columnFields).importData(mapList);
                } catch (Exception e) {
                    logger.error("文件生成异常：" + e.getMessage());
                }
                //设置表的样式
                try {
                    //设置表头加粗
                    CellStyle headCellStyle = wb.createCellStyle();
                    Font headFont = wb.createFont();
                    headFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);//粗体显示
                    headCellStyle.setFont(headFont);
                    Sheet sheet = wb.getSheetAt(0);
                    Row headRow = sheet.getRow(0);
                    int goalPosition_thisWeekNewAdd = 0;
                    int goalPosition_isAbnormal = 0;
                    int goalPosition_remark = 0;
                    for (Cell cell : headRow) {
                        cell.setCellStyle(headCellStyle);
                        String cellValue = cell.toString();
                        if (cellValue.equals("本周新增")) {
                            goalPosition_thisWeekNewAdd = cell.getColumnIndex();
                        } else if (cellValue.equals("是否异常")) {
                            goalPosition_isAbnormal = cell.getColumnIndex();
                        } else if (cellValue.equals("备注提示")) {
                            goalPosition_remark = cell.getColumnIndex();
                        }
                    }
                    //设置目标列背景色
                    XSSFCellStyle goalCellStyle = (XSSFCellStyle) wb.createCellStyle();
                    goalCellStyle.setFillForegroundColor(HSSFColor.YELLOW.index);
                    goalCellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                    //设置字体（在发现设置背景颜色后字体改变后）
                    Font goalFont = wb.createFont();
                    goalFont.setFontName("Calibri");
                    goalCellStyle.setFont(goalFont);
                    for (int rowNum = 1; rowNum < sheet.getLastRowNum() + 1; rowNum++) {
//                                System.out.println(rowNum + " ");
                        Row row = sheet.getRow(rowNum);
                        String goalValue_thisWeekNewAdd = row.getCell(goalPosition_thisWeekNewAdd).toString();
                        String goalValue_isAbnormal = row.getCell(goalPosition_isAbnormal).toString();
                        String goalValue__remark = row.getCell(goalPosition_remark).toString();

                        if (goalValue_thisWeekNewAdd.equals("是") && goalValue_isAbnormal.equals("是")) {
                            for (Cell cell : row) {
                                cell.setCellStyle(goalCellStyle);
//                                        System.out.print(cell.toString() + " ");
                            }
                        }
                        if (goalValue__remark.equals("已红冲退货")) {
                            XSSFCellStyle goalCellStyle_red = (XSSFCellStyle) wb.createCellStyle();
                            Font goalFont_red = wb.createFont();
                            //字体颜色
                            goalFont_red.setColor(HSSFColor.RED.index);
                            goalCellStyle_red.setFont(goalFont_red);
                            row.getCell(goalPosition_remark).setCellStyle(goalCellStyle_red);
                        }
                    }
                } catch (Exception e) {
                    logger.error("表格设置格式错误，请检查。");
                    e.printStackTrace();
                }
            }
            ewb.export(outPath);
            logger.info(" 河南省 正常数据 " + params.get("BU") + " 日期：" + params.get("date") + " 导出数据完毕");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getDataHeNanabnormal(Map params) {
        String sql = "select * from Johnson_henan_OrderDistributeGoodsDetails_OrderDetailed_forcust  where " +
                " 更新日期='" + params.get("date") + "'" +
                " and BU='" + params.get("BU") + "'" +
                " and GAP <> '退市'" +
                " and cast (GAP as float)<0" +
                " and 备注提示 <>'已红冲退货'" +
                " order by 本周新增 desc, cast (GAP as float) asc";
        logger.info("sql:   " + sql);
        List<Map<String, Object>> mapList = daDao.getDataHeNan(sql);
        if (mapList.size() > 0) {
            logger.info("执行sql，获得:" + mapList.size() + " 条数据");

            SXSSFExcelTitle[][] detail_titles = this.getTitles(mapList);
            List<String> detail_columnFields = this.getColumnFields(mapList);

            logger.info(" 河南省 异常数据 " + " " + params.get("BU") + " 日期：" + params.get("date") + " 开始导出数据");
            /*构建导出文件路径*/
            String fileDate = "";
            try {
                fileDate = df1.format(df.parse(params.get("date").toString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String datestr = params.get("date").toString();
            String dateyear = StringUtils.substringBefore(datestr, "-");
            String datemon = StringUtils.substringBetween(datestr, "-", "-");

            String outPath = resource.getHeNan_basicPath() + "\\" + dateyear + "\\" + datemon + "\\"
                    + "河南省平台异常订单数据-" + params.get("BU").toString() + " " + fileDate + ".xlsx";
            logger.info("文件输出路径:" + outPath);
            try {
//                Export.buildSXSSFExportExcelWorkBook().createSheet("Sheet0").setTitles(detail_titles).setColumnFields(detail_columnFields).importData(mapList)
//                        .export(outPath);

                SXSSFWorkbook wb = new SXSSFWorkbook(mapList.size() + 1);
                CellStyle cellStyle = wb.createCellStyle();
                Font font = wb.createFont();

                ExcelWorkBookExport ewb = Export.buildSXSSFExportExcelWorkBook(wb);
                ewb.createSheet("Sheet0").setTitles(detail_titles).setColumnFields(detail_columnFields).importData(mapList);

                font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);//粗体显示
                cellStyle.setFont(font);
                Sheet sheet = wb.getSheetAt(0);
                //设置表头的样式
                Row row = sheet.getRow(0);
                for (Cell cell : row) {
                    cell.setCellStyle(cellStyle);
                }
                ewb.export(outPath);

                logger.info(" 河南省 " + " " + params.get("BU") + " 日期：" + params.get("date") + "导出数据完毕");
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
        params.put("sendType", "customer");
        List<Map<String, Object>> users = daDao.selectUsersByProviceByBu(params);
        String[] tos = new String[users.size()];
        for (int i = 0; i < users.size(); i++) {
            tos[i] = users.get(i).get("mail_address").toString();
            logger.info("- 发送给：" + tos[i].toString());
        }
        /*获取所有的邮件抄送者*/
        params.put("sendType", "copyto");
        List<Map<String, Object>> ccusers = daDao.selectUsersByProviceByBu(params);
        String[] cctos = new String[ccusers.size()];
        for (int i = 0; i < ccusers.size(); i++) {
            cctos[i] = ccusers.get(i).get("mail_address").toString();
            logger.info("- 抄送给：" + cctos[i].toString());
        }

        /*获取所有要发送的数据*/
        //获取邮件正文
        params.put("num2", "4");
        List<Map<String, Object>> text = this.getHeNanOrderData(params);
        //获取正文统计表
        params.put("num2", "2");
        List<Map<String, Object>> statistics = this.getHeNanOrderData(params);
        //返回新增异常数据个数
        params.put("num2", "3");
        List<Map<String, Object>> newAddAbnormal_num = this.getHeNanOrderData(params);
        Map<String, Object> temp_newAddAbnormal_num = newAddAbnormal_num.get(0);
        String newAddAbnormalNumStr = "";
        for (String key : temp_newAddAbnormal_num.keySet()) {
            newAddAbnormalNumStr = temp_newAddAbnormal_num.get(key).toString();
            logger.info("新增异常数据条数:" + key + " " + newAddAbnormalNumStr);
        }
        //返回新增异常数据的 结果
        List<Map<String, Object>> newAddAbnormal_date = new ArrayList<>();
        if (Integer.valueOf(newAddAbnormalNumStr) != 0) {
            params.put("num2", "5");
            newAddAbnormal_date = this.getHeNanOrderData(params);
        }

        /*构建文件路径*/
//        Calendar now = Calendar.getInstance();
//        now.setTime(new Date());
//        String subject_date = DateFormatUtils.format(now, "yyyy.MM.dd");//邮件主题日期
//        String subject_date = String.valueOf(now.get(Calendar.YEAR)) + "年" + String.valueOf(now.get(Calendar.MONTH) + 1) + "月" + String.valueOf(now.get(Calendar.DATE)) + "日";
//        String now_date_str = DateFormatUtils.format(now, "yyyy.MM.dd");
//        now.add(Calendar.DAY_OF_MONTH, -7);
//        now.set(Calendar.DAY_OF_WEEK, now.getActualMinimum(Calendar.DAY_OF_WEEK) + 3);
//        String last_week_first_day = DateFormatUtils.format(now, "yyyy.MM.dd");
//        now.add(Calendar.DAY_OF_MONTH, 7);
//        String last_week_last_day = DateFormatUtils.format(now, "yyyy.MM.dd");
        /*附件文件*/
        String datestr = params.get("date").toString();
        String dateyear = StringUtils.substringBefore(datestr, "-");
        String datemon = StringUtils.substringBetween(datestr, "-", "-");
        String fileDate = "";
        try {
            fileDate = df1.format(df.parse(datestr));
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
        String subject = "";
        if (Integer.valueOf(newAddAbnormalNumStr) != 0) {
            //异常的标题
            subject = "异常！河南省平台订单数据推送-" + params.get("BU").toString() + "，" + fileDate;
        } else {
            //正常的标题
            subject = "河南省平台订单数据推送-" + params.get("BU").toString() + "，" + fileDate;
        }
        logger.info("- 邮件标题：" + subject);
        /*邮件正文*/
        String mail_text = "";
        for (int i = 0; i < text.size(); i++) {
            Map<String, Object> temp = text.get(i);
            for (String key : temp.keySet()) {
                mail_text = temp.get(key).toString();
            }
        }
        logger.info("- 邮件正文:" + mail_text);

        System.setProperty("mail.mime.splitlongparameters", "false");
        MimeMessage message = jms.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
//            helper.setFrom("jjmcgaop@earlydata.com");
            helper.setFrom("jjmcgaop@earlydata.com", "JJMC GA OP");
            String to = "keshi.wang@earlydata.com";
            //测试
//            helper.setTo(to);
            //正式
            //发送
            helper.setTo(tos);
            //抄送
            if (cctos.length > 0) {
                helper.setCc(cctos);
            }
            helper.setSubject(subject);

            try {
                for (File filefile : files) {
                    String fileName = filefile.getName();
                    //正常数据的文件
                    String matchedNormalFileName = "河南省中标产品配送情况汇总";
                    //异常数据的文件
                    String matchedAbnormalFileName = "河南省平台异常订单数据";
                    if (!fileName.contains("副本")) {
                        if (fileName.contains(matchedNormalFileName)) {
                            if (fileName.contains("_" + fileDate)) {
                                FileSystemResource file = new FileSystemResource(filefile);
                                helper.addAttachment(file.getFilename(), file);//添加附件
                                logger.info("- 添加附件：" + fileName);
                            }
                        } else if (fileName.contains(matchedAbnormalFileName)) {
                            if (fileName.contains(fileDate)) {
                                FileSystemResource file = new FileSystemResource(filefile);
                                helper.addAttachment(file.getFilename(), file);//添加附件
                                logger.info("- 添加附件：" + fileName);
                            }
                        }
                    } else {
//                    logger.error("没有发现 文件 ：**至" + params.get("date"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Map<String, Object> datas = new HashMap();
            datas.put("mail_text", mail_text);
            datas.put("statistics", statistics);
            datas.put("newAddAbnormalNumStr", newAddAbnormalNumStr);

            if (Integer.valueOf(newAddAbnormalNumStr) != 0) {
                logger.info("- 加入新增异常数据用于生产邮件里的表");
                datas.put("newAddAbnormal_date", newAddAbnormal_date);
            }

            Template template = configuration.getTemplate("heNanOrder.ftl");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, datas);
            helper.setText(html, true);

            jms.send(message);

        } catch (Exception e) {
            logger.info("发送邮件异常：" + e.getMessage());
        }

    }
}
