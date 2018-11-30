package com.eddc.jnj.service.heNanAccountManage.Impl;

import com.eddc.jnj.dao.SycmAccountDao;
import com.eddc.jnj.model.SycmAccount;
import com.eddc.jnj.service.heNanAccountManage.HeNanAccountManageService;
import com.eddc.jnj.utils.ExcelHandle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(value = "HeNanAccountManageService")
public class HeNanAccountManageServiceImpl implements HeNanAccountManageService {

    @Resource
    SycmAccountDao sycmAccountDao;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<SycmAccount> getAllAccountInfo() {
        logger.info("- 开始获取所有强生账户管理所有信息");
        List<SycmAccount> sycmAccountList = sycmAccountDao.selectAll();
        return sycmAccountList;
    }

    @Override
    public List<Map<String, String>> getExcelContent(String filePath) {
        List<Map<String, String>> excelContent = new ArrayList<>();
        //检查路径
        File file = new File(filePath);
        //路径是否存在
        if (file.exists()) {
            if (!file.isDirectory()) {
                try {
                    Workbook workbook = ExcelHandle.getWorkbook(file);
                    //workbook的sheet个数
                    int sheetCount = workbook.getNumberOfSheets();
                    for (int s = 0; s < sheetCount; s++) {
//                        System.out.println("————————————————————————Start processing sheet" + (s + 1) + "————————————————————————");
//                        String sheetname = workbook.getSheetName(s);
//                        System.out.println("Name of sheet" + (s + 1) + ":" + sheetname);
                        // Set the subscript of the excel sheet: 0 start
                        Sheet sheet = workbook.getSheetAt(s);// The first sheet
                        // Set the count to skip the first line of the directory
                        int count = 0;
                        // Total number of rows
                        int rowLength = sheet.getLastRowNum() + 1;
//                        System.out.println("Total number of rows:" + rowLength);
                        // Get the first line
                        Row rowzero = sheet.getRow(0);
                        // Total number of columns
                        int colLength = rowzero.getLastCellNum();
                        //表头作为key
                        Map<Integer, String> headMap = new HashMap<>();
                        for (int i = 0; i < colLength; i++) {
                            headMap.put(i, rowzero.getCell(i).toString());
                        }
                        for (int i = 1; i < rowLength; i++) {
                            Row row = sheet.getRow(i);
                            Map<String, String> excleMap = new HashMap<>();
                            for (int positon : headMap.keySet()) {
                                String excleMapKey = headMap.get(positon);
                                String excleMapVal = row.getCell(positon).toString();
                                excleMap.put(headMap.get(positon), row.getCell(positon).toString());
                            }
                            excelContent.add(excleMap);
                        }
//                        System.out.println("————————————————————————sheet" + (s + 1) + " processing ends————————————————————————");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("！！！文件路径是一个文件夹：" + filePath);
            }
        } else {
            System.out.println("！！！文件路径不存在：" + filePath);
            return null;
        }
        return excelContent;
    }
}
