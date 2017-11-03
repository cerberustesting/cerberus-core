/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.service.export;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.dto.SummaryStatisticsDTO;
import org.cerberus.enums.ExportServiceEnum;
import org.cerberus.util.answer.Answer;

/**
 * Factory that exports files the exported objects.
 *
 * @author FNogueira
 */
public class ExportServiceFactory {

    private static final Logger LOG = LogManager.getLogger(ExportServiceFactory.class);
    
    private final List<?> list;
    private final String fileName; //name of the file that is going to be generated
    private final ExportServiceEnum type;
    private final String data;
    private final List<String> exportOptions;

    public ExportServiceFactory(List<?> list, String fileName, ExportServiceEnum type, String data, List<String> exportOptions) {
        this.list = list;
        this.fileName = fileName;
        this.type = type;
        this.data = data;
        this.exportOptions = exportOptions;

    }

    private Answer exportToXLS() {
        Answer ans = new Answer();

        //Blank workbook
        Workbook workbook = new XSSFWorkbook();

        if (data.equals("TestCaseWithExecution")) {
            createReportByTagExport(workbook);
        }

        FileOutputStream outputStream;
        try {
            String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
            outputStream = new FileOutputStream(this.fileName + "_" + timeStamp + type.getFileExtension());
            try {
                workbook.write(outputStream);
                outputStream.close();
                workbook.close();
            } catch (IOException ex) {
                LOG.warn(ex);
            }
        } catch (FileNotFoundException ex) {
            LOG.warn(ex);
        }

        //each country will be a page in the xls file
        return ans;
    }

    private void createReportByTagExport(Workbook workbook) {
        //handles the export of the execution by tag data
        HashMap<String, SummaryStatisticsDTO> summaryMap = new HashMap<String, SummaryStatisticsDTO>();

        HashMap<String, HashMap<String, List<TestCaseExecution>>> mapList = new HashMap<String, HashMap<String, List<TestCaseExecution>>>();
        List<String> mapCountries = new ArrayList<String>();
        List<CellStyle> stylesList = new LinkedList<CellStyle>();

        if (exportOptions.contains("chart") || exportOptions.contains("list")) {
            //then we need to create the default colors for each cell
            HSSFWorkbook hwb = new HSSFWorkbook();
            HSSFPalette palette = hwb.getCustomPalette();

            CellStyle okStyle = workbook.createCellStyle();

            // get the color which most closely matches the color you want to use
            // code to get the style for the cell goes here
            okStyle.setFillForegroundColor(palette.findSimilarColor(92, 184, 0).getIndex());
            okStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
            //okStyle.setFont();

            stylesList.add(okStyle);

        }
        for (TestCaseExecution execution : (List<TestCaseExecution>) list) {
            //check if the country and application shows

            if (exportOptions.contains("chart") || exportOptions.contains("summary")) {
                String keySummaryTable = execution.getApplication() + " " + execution.getCountry() + " " + execution.getEnvironment();
                SummaryStatisticsDTO stats;

                String status = execution.getControlStatus();

                if (summaryMap.containsKey(keySummaryTable)) {
                    stats = summaryMap.get(keySummaryTable);
                } else {
                    stats = new SummaryStatisticsDTO();
                    stats.setApplication(execution.getApplication());
                    stats.setCountry(execution.getCountry());
                    stats.setEnvironment(execution.getEnvironment());
                }
                stats.updateStatisticByStatus(status);
                summaryMap.put(keySummaryTable, stats); //updates the map
            }
            if (exportOptions.contains("list")) {
                if (exportOptions.contains("filter")) {
                    //filter active
                } else {
                    //all data is saved

                }
                HashMap<String, List<TestCaseExecution>> listExecution;
                List<TestCaseExecution> testCaseList;
                String testKey = execution.getTest();
                String testCaseKey = execution.getTestCase();

                if (mapList.containsKey(testKey)) {
                    listExecution = mapList.get(testKey);
                } else {
                    listExecution = new HashMap<String, List<TestCaseExecution>>();
                }
                if (listExecution.containsKey(testCaseKey)) {
                    testCaseList = listExecution.get(testCaseKey);
                } else {
                    testCaseList = new ArrayList<TestCaseExecution>();
                }
                testCaseList.add(execution);
                listExecution.put(testCaseKey, testCaseList);
                mapList.put(testKey, listExecution);

                if (mapCountries.indexOf(execution.getCountry()) == -1) {
                    mapCountries.add(execution.getCountry());
                }

            }

        }
        int rowCount = -1;

        //Create a blank sheet
        Sheet sheet = workbook.createSheet("Report by Tag");
        sheet.getPrintSetup().setLandscape(true);

        PrintSetup ps = sheet.getPrintSetup();

        sheet.setAutobreaks(true);

        //ps.setFitHeight((short) 1);
        ps.setFitWidth((short) 1);
        sheet.setFitToPage(true);
        sheet.setColumnWidth(0, 9000);

        if (exportOptions.contains("chart")) {
            SummaryStatisticsDTO sumsTotal = calculateTotalValues(summaryMap);

            Row row = sheet.createRow(++rowCount);
            row.createCell(0).setCellValue("Report By Status");

            row = sheet.createRow(++rowCount);
            row.createCell(0).setCellValue("Status");
            row.createCell(1).setCellValue("Total");
            row.createCell(2).setCellValue("Percentage");

            row = sheet.createRow(++rowCount);
            CellStyle okStyle = stylesList.get(0);
            Cell cellOk = row.createCell(0);
            cellOk.setCellValue("OK");
            cellOk.setCellStyle(okStyle);

            row.createCell(1).setCellValue(sumsTotal.getOK());
            row.createCell(2).setCellValue(sumsTotal.getPercOK());

            row = sheet.createRow(++rowCount);
            row.createCell(0).setCellValue("KO");
            row.createCell(1).setCellValue(sumsTotal.getKO());
            row.createCell(2).setCellValue(sumsTotal.getPercKO());

            row = sheet.createRow(++rowCount);
            row.createCell(0).setCellValue("FA");
            row.createCell(1).setCellValue(sumsTotal.getFA());
            row.createCell(2).setCellValue(sumsTotal.getPercFA());

            row = sheet.createRow(++rowCount);
            row.createCell(0).setCellValue("NA");
            row.createCell(1).setCellValue(sumsTotal.getNA());
            row.createCell(2).setCellValue(sumsTotal.getPercNA());

            row = sheet.createRow(++rowCount);
            row.createCell(0).setCellValue("NE");
            row.createCell(1).setCellValue(sumsTotal.getNE());
            row.createCell(2).setCellValue(sumsTotal.getPercNE());

            row = sheet.createRow(++rowCount);
            row.createCell(0).setCellValue("QU");
            row.createCell(1).setCellValue(sumsTotal.getQU());
            row.createCell(2).setCellValue(sumsTotal.getPercQU());

            row = sheet.createRow(++rowCount);
            row.createCell(0).setCellValue("PE");
            row.createCell(1).setCellValue(sumsTotal.getPE());
            row.createCell(2).setCellValue(sumsTotal.getPercPE());

            row = sheet.createRow(++rowCount);
            row.createCell(0).setCellValue("CA");
            row.createCell(1).setCellValue(sumsTotal.getCA());
            row.createCell(2).setCellValue(sumsTotal.getPercCA());

            row = sheet.createRow(++rowCount);
            row.createCell(0).setCellValue("Total");
            row.createCell(1).setCellValue(sumsTotal.getTotal());

            sheet.createRow(++rowCount).createCell(0).setCellValue("");
            sheet.createRow(++rowCount).createCell(0).setCellValue("");
            sheet.createRow(++rowCount).createCell(0).setCellValue("");
            sheet.createRow(++rowCount).createCell(0).setCellValue("");

        }
        if (exportOptions.contains("summary")) {
            //draw the table with data

            Row row = sheet.createRow(++rowCount);
            row.createCell(0).setCellValue("Summary Table");

            //start creating data
            row = sheet.createRow(++rowCount);

            row.createCell(0).setCellValue("Application");
            row.createCell(1).setCellValue("Country");
            row.createCell(2).setCellValue("Environment");
            row.createCell(3).setCellValue("OK");
            row.createCell(4).setCellValue("KO");
            row.createCell(5).setCellValue("FA");
            row.createCell(6).setCellValue("NA");
            row.createCell(7).setCellValue("NE");
            row.createCell(8).setCellValue("PE");
            row.createCell(8).setCellValue("QU");
            row.createCell(9).setCellValue("CA");
            row.createCell(10).setCellValue("NOT OK");
            row.createCell(11).setCellValue("Total");

            /*temporary styles*/
            CellStyle styleBlue = workbook.createCellStyle();
            CellStyle styleGreen = workbook.createCellStyle();
            HSSFWorkbook hwb = new HSSFWorkbook();
            HSSFPalette palette = hwb.getCustomPalette();
            // get the color which most closely matches the color you want to use
            HSSFColor myColor = palette.findSimilarColor(66, 139, 202);

            // get the palette index of that color 
            short palIndex = myColor.getIndex();
            // code to get the style for the cell goes here
            styleBlue.setFillForegroundColor(palIndex);
            styleBlue.setFillPattern(CellStyle.SPARSE_DOTS);

            HSSFColor myColorGreen = palette.findSimilarColor(92, 184, 0);
            styleGreen.setFillForegroundColor(myColorGreen.getIndex());
            styleGreen.setFillPattern(CellStyle.SPARSE_DOTS);

            int startRow = (rowCount + 2);
            TreeMap<String, SummaryStatisticsDTO> sortedSummaryMap = new TreeMap<String, SummaryStatisticsDTO>(summaryMap);
            for (String key : sortedSummaryMap.keySet()) {
                row = sheet.createRow(++rowCount);
                SummaryStatisticsDTO sumStats = summaryMap.get(key);
                //application
                row.createCell(0).setCellValue((String) sumStats.getApplication());
                //country
                row.createCell(1).setCellValue((String) sumStats.getCountry());
                //environment
                row.createCell(2).setCellValue((String) sumStats.getEnvironment());

                //OK
                row.createCell(3).setCellValue(sumStats.getOK());
                //KO
                row.createCell(4).setCellValue(sumStats.getKO());
                //FA
                row.createCell(5).setCellValue(sumStats.getFA());
                //NA
                row.createCell(6).setCellValue(sumStats.getNA());
                //NE
                row.createCell(7).setCellValue(sumStats.getNE());
                //PE
                row.createCell(8).setCellValue(sumStats.getPE());
                //QU
                row.createCell(9).setCellValue(sumStats.getQU());
                //CA
                row.createCell(10).setCellValue(sumStats.getCA());
                int rowNumber = row.getRowNum() + 1;
                //NOT OK
                //row.createCell(11).setCellValue(sumStats.getNotOkTotal());
                row.createCell(11).setCellFormula("SUM(E" + rowNumber + ":J" + rowNumber + ")");
                //Total
                row.createCell(12).setCellFormula("SUM(D" + rowNumber + ",K" + rowNumber + ")");
                //row.createCell(12).setCellValue(sumStats.getTotal());

                if (sumStats.getOK() == sumStats.getTotal()) {
                    for (int i = 0; i < 13; i++) {
                        row.getCell(i).setCellStyle(styleGreen);
                    }
                }
            }
            //TODO:FN percentages missing
            //Total row
            row = sheet.createRow(++rowCount);

            row.createCell(0).setCellValue("Total");
            row.createCell(1).setCellValue("");
            row.createCell(2).setCellValue("");
            //OK
            row.createCell(3).setCellFormula("SUM(D" + startRow + ":D" + rowCount + ")");
            //KO
            row.createCell(4).setCellFormula("SUM(E" + startRow + ":E" + rowCount + ")");
            //FA
            row.createCell(5).setCellFormula("SUM(F" + startRow + ":F" + rowCount + ")");
            //NA
            row.createCell(6).setCellFormula("SUM(G" + startRow + ":G" + rowCount + ")");
            //NE
            row.createCell(7).setCellFormula("SUM(H" + startRow + ":H" + rowCount + ")");
            //PE
            row.createCell(8).setCellFormula("SUM(I" + startRow + ":I" + rowCount + ")");
            //QU
            row.createCell(9).setCellFormula("SUM(J" + startRow + ":I" + rowCount + ")");
            //CA
            row.createCell(10).setCellFormula("SUM(K" + startRow + ":J" + rowCount + ")");

            int rowNumberTotal = row.getRowNum() + 1;
            //NOT OK
            row.createCell(11).setCellFormula("SUM(E" + rowNumberTotal + ":J" + rowNumberTotal + ")");
            //Total
            row.createCell(12).setCellFormula("SUM(D" + rowNumberTotal + ",K" + rowNumberTotal + ")");
            for (int i = 0; i < 13; i++) {
                row.getCell(i).setCellStyle(styleBlue);
            }

            //add some empty rows
            sheet.createRow(++rowCount).createCell(0).setCellValue("");
            sheet.createRow(++rowCount).createCell(0).setCellValue("");
            sheet.createRow(++rowCount).createCell(0).setCellValue("");
            sheet.createRow(++rowCount).createCell(0).setCellValue("");

        }

        if (exportOptions.contains("list")) {
            //exports the data from test cases' executions
            Row r = sheet.createRow(++rowCount);
            r.createCell(0).setCellValue("Test");
            r.createCell(1).setCellValue("Test Case");
            r.createCell(2).setCellValue("Description");
            r.createCell(3).setCellValue("Application");
            r.createCell(4).setCellValue("Environment");
            r.createCell(5).setCellValue("Browser");
            //creates the country list

            Collections.sort(mapCountries);//sorts the list of countries
            int startIndexForCountries = 6;
            for (String country : mapCountries) {
                r.createCell(startIndexForCountries).setCellValue(country);
                startIndexForCountries++;
            }

            TreeMap<String, HashMap<String, List<TestCaseExecution>>> sortedKeys = new TreeMap<String, HashMap<String, List<TestCaseExecution>>>(mapList);
            rowCount++;
            for (String keyMapList : sortedKeys.keySet()) {
                rowCount = createRow(keyMapList, mapList.get(keyMapList), sheet, rowCount, mapCountries);
            }
        }
    }

    private int createRow(String test, HashMap<String, List<TestCaseExecution>> executionsPerTestCase, Sheet sheet, int currentIndex, List<String> mapCountries) {

        int lastRow = currentIndex + executionsPerTestCase.size();

        int current = currentIndex;

        TreeMap<String, List<TestCaseExecution>> sortedKeys = new TreeMap<String, List<TestCaseExecution>>(executionsPerTestCase);
        CellStyle wrapStyle = sheet.getColumnStyle(0); //Create new style
        wrapStyle.setWrapText(true); //Set wordwrap

        for (String testCaseKey : sortedKeys.keySet()) {
            List<String> browserEnvironment = new LinkedList<String>();
            String application;
            String description;
            Row r = sheet.createRow(current);
            List<TestCaseExecution> executionList = executionsPerTestCase.get(testCaseKey);
            Cell testCell = r.createCell(0);
            testCell.setCellValue(test);
            testCell.setCellStyle(wrapStyle);
            r.createCell(1).setCellValue(testCaseKey);

            //gets the first object to retrieve the application - at least exists one test case execution
            if (executionList.isEmpty()) {
                application = "N/D";
                description = "N/D";
            } else {
                application = executionList.get(0).getApplication();
                description = executionList.get(0).getTestCaseObj().getBehaviorOrValueExpected();
            }
            //Sets the application and description
            r.createCell(2).setCellValue(application);
            r.createCell(3).setCellValue(description);

            int rowStartedTestCaseInfo = current;

            for (TestCaseExecution exec : executionList) {
                if (browserEnvironment.isEmpty()) {
                    browserEnvironment.add(exec.getEnvironment() + "_" + exec.getBrowser());
                    r.createCell(4).setCellValue(exec.getEnvironment());
                    r.createCell(5).setCellValue(exec.getBrowser());
                } else {
                    int index = browserEnvironment.indexOf(exec.getEnvironment() + "_" + exec.getBrowser());

                    //Does not exist any information about browser and environment
                    if (browserEnvironment.indexOf(exec.getEnvironment() + "_" + exec.getBrowser()) == -1) {
                        //need to add another row with the same characteristics
                        r = sheet.createRow(++current);
                        r.createCell(0).setCellValue(test);
                        r.createCell(1).setCellValue(testCaseKey);
                        r.createCell(2).setCellValue(application);
                        r.createCell(3).setCellValue(description);
                        r.createCell(4).setCellValue(exec.getEnvironment());
                        r.createCell(5).setCellValue(exec.getBrowser());

                        browserEnvironment.add(exec.getEnvironment() + "_" + exec.getBrowser());
                    } else {
                        //there is information about the browser and environment
                        Row rowExisting = sheet.getRow(rowStartedTestCaseInfo + index);
                        r = rowExisting;
                    }

                }

                //TODO:FN tirar daqui estes valores
                int indexOfCountry = mapCountries.indexOf(exec.getCountry()) + 6;
                Cell executionResult = r.createCell(indexOfCountry);
                executionResult.setCellValue(exec.getControlStatus());
                //Create hyperling
                CreationHelper createHelper = sheet.getWorkbook().getCreationHelper();
                CellStyle hlinkstyle = sheet.getWorkbook().createCellStyle();
                Font hlinkfont = sheet.getWorkbook().createFont();
                hlinkfont.setUnderline(XSSFFont.U_SINGLE);
                hlinkfont.setColor(HSSFColor.BLUE.index);
                hlinkstyle.setFont(hlinkfont);

                Hyperlink link = (Hyperlink) createHelper.createHyperlink(Hyperlink.LINK_URL);
                link.setAddress("http://www.tutorialspoint.com/");
                executionResult.setHyperlink((Hyperlink) link);
                executionResult.setCellStyle(hlinkstyle);

            }
            current++;

        }

        /*r.createCell(1).setCellValue("");
         r.createCell(2).setCellValue("");
         r.createCell(3).setCellValue("");
         r.createCell(4).setCellValue("");
         r.createCell(5).setCellValue("");
         */
//        for(TestCaseWithExecution exec : execution){
//            
//            //r.createCell(2).setCellValue(exec.getDescription());
//            //r.createCell(3).setCellValue(exec.getApplication());
//            //r.createCell(4).setCellValue(exec.getEnvironment());
//            //r.createCell(5).setCellValue(exec.getBrowser());
//            int indexOfCountry = mapCountries.indexOf(exec.getCountry()) + 6;
//            r.createCell(indexOfCountry).setCellValue(exec.getControlStatus());
//            //current++;
//        }
        //puts the test name in the first column
        /*r = sheet.getRow(currentIndex);
         r.getCell(0).setCellValue(test);
         */
        /*CellRangeAddress range = new CellRangeAddress(currentIndex, lastRow, 0, 0);
         sheet.addMergedRegion(range);*/
        return lastRow;
    }

    public Answer export() {
        Answer ans = new Answer();
        if (type.getCode() == ExportServiceEnum.XLSX.getCode()) {
            exportToXLS();
        }

        //save to file
        return ans;
    }

    private SummaryStatisticsDTO calculateTotalValues(Map<String, SummaryStatisticsDTO> summaryMap) {
        int okTotal = 0;
        int koTotal = 0;
        int naTotal = 0;
        int neTotal = 0;
        int peTotal = 0;
        int quTotal = 0;
        int faTotal = 0;
        int caTotal = 0;

        for (String key : summaryMap.keySet()) {
            SummaryStatisticsDTO sumStats = summaryMap.get(key);
            //percentage values
            okTotal += sumStats.getOK();
            koTotal += sumStats.getKO();
            naTotal += sumStats.getNA();
            neTotal += sumStats.getNE();
            peTotal += sumStats.getPE();
            quTotal += sumStats.getQU();
            faTotal += sumStats.getFA();
            caTotal += sumStats.getCA();
        }
        SummaryStatisticsDTO sumGlobal = new SummaryStatisticsDTO();
        sumGlobal.setApplication("Total");
        sumGlobal.setOK(okTotal);
        sumGlobal.setKO(koTotal);
        sumGlobal.setNA(naTotal);
        sumGlobal.setNE(neTotal);
        sumGlobal.setPE(peTotal);
        sumGlobal.setQU(quTotal);
        sumGlobal.setFA(faTotal);
        sumGlobal.setCA(caTotal);

        int notOkTotal = koTotal + naTotal + peTotal + faTotal + caTotal + neTotal + quTotal;
        sumGlobal.setNotOKTotal(notOkTotal);

        int totalGlobal = notOkTotal + okTotal;
        sumGlobal.setTotal(totalGlobal);

        sumGlobal.updatePercentageStatistics();
        return sumGlobal;
    }
}
