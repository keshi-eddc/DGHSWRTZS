package com.eddc.jnj.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017/12/7.
 */
@Component
public class ExportExcel {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static void main(String[] args) throws Exception {
  /*      String path = "D:\\test.xls";
        XSSFWorkbook workbook = new  XSSFWorkbook();
        XSSFSheet sheet  = workbook.createSheet("2017-11");
        XSSFRow row = sheet.createRow(0);// 创建行,从0开始
        row.createCell(0).setCellValue("李志伟");// 创建行的单元格,也是从0开始
        row.createCell(1).setCellValue(false);// 设置单元格内容,重载
        row.createCell(2).setCellValue(new Date());// 设置单元格内容,重载
        row.createCell(3).setCellValue(12.345);// 设置单元格内容,重载
        FileOutputStream out = new FileOutputStream(path);
        workbook.write(out);//保存Excel文件
        out.close();//关闭文件流
        System.out.println("OK!");*/

        String path = "D:\\test.xls";
        List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
        for(int i = 0;i<10;i++){
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("姓名","张三"+i);
            data.put("年龄","18");
            datas.add(data);
        }
       // ExportExcel.exportExcel(path,datas);

    }
    public void exportExcel(String filePath, List<Map<String, Object>> sheetDatas) {
        XSSFWorkbook workbook = new  XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();

        for(int i=1;i<=sheetDatas.size();i++){
            if(i==1){
                sheet.createRow(0);
            }
            XSSFRow row = sheet.createRow(i);
            Map<String, Object> sheetData = sheetDatas.get(i-1);
            int j = 0;
            for(Map.Entry<String, Object> entry:sheetData.entrySet()){
                if(i==1){
                    sheet.getRow(0).createCell(j).setCellValue(entry.getKey().toString());
                    if(null!=entry.getValue()&& StringUtils.isNotBlank(entry.getValue().toString())){
                        row.createCell(j).setCellValue(entry.getValue().toString());
                    }else{
                        row.createCell(j).setCellValue("");
                    }

                }else{
                    if(null!=entry.getValue()&& StringUtils.isNotBlank(entry.getValue().toString())){
                        row.createCell(j).setCellValue(entry.getValue().toString());
                    }else{
                        row.createCell(j).setCellValue("");
                    }

                }
                j++;
            }

        }

        try {
            FileOutputStream out = new FileOutputStream(filePath);
            workbook.write(out);//保存Excel文件
            out.close();//关闭文件流
        } catch (Exception e) {
            logger.info("文件导出异常："+e.getMessage());
        }
        logger.info("文件导出成功！");
    }


}
