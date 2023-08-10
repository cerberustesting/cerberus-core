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
package org.cerberus.core.service.pdf.impl;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import java.io.File;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.ITestCaseExecutionService;
import org.cerberus.core.exception.CerberusException;
import org.springframework.stereotype.Service;

import org.cerberus.core.service.pdf.IPDFService;
import org.cerberus.core.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author bcivel
 */
@Service
public class PDFService implements IPDFService {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(PDFService.class);

    @Autowired
    private ITestCaseExecutionService testCaseExecutionService;

    @Override
    public String generatePdf(Tag tag) throws FileNotFoundException {

        UUID fileUUID = UUID.randomUUID();

        String rootPath = "";
        if (System.getProperty("java.io.tmpdir") != null) {
            rootPath = System.getProperty("java.io.tmpdir");
        } else {
            String sep = "" + File.separatorChar;
            LOG.info(sep);
            if (sep.equalsIgnoreCase("/")) {
                rootPath = "/tmp";
            } else {
                rootPath = "C:";
            }
            LOG.warn("Java Property for temporary folder not defined. Default to :" + rootPath);
        }

        // Creating a PdfWriter
        String dest = rootPath + File.separatorChar + "campaignExecutionReport-" + fileUUID.toString().substring(0, 17) + ".pdf";
        LOG.info("Starting to generate PDF Report on :" + dest);
        PdfWriter writer = new PdfWriter(dest);

        // Creating a PdfDocument       
        PdfDocument pdfDoc = new PdfDocument(writer);

        try ( // Creating a Document
                Document document = new Document(pdfDoc)) {

            // Tittle
            document.add(new Paragraph("Campaign Execution Report").setBold().setFontSize(20).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph(tag.getTag()).setBold().setFontSize(20).setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Triggered from campaign: " + tag.getCampaign() + "by " + tag.getUsrCreated()));
            document.add(new Paragraph("Countries: " + StringUtil.convertToString(new JSONArray(tag.getCountryList()), ",")));
            document.add(new Paragraph("Environments: " + StringUtil.convertToString(new JSONArray(tag.getEnvironmentList()), ",")));
            document.add(new Paragraph("Robots: " + StringUtil.convertToString(new JSONArray(tag.getRobotDecliList()), ",")));

            document.add(new Paragraph("CI results: " + tag.getCiResult() + " (Score=" + tag.getCiScore() + " vs " + tag.getCiScoreThreshold() + ")"));

            document.add(new Paragraph("Started: " + tag.getDateCreated()));
            document.add(new Paragraph("Ended: " + tag.getDateEndQueue()));

            document.add(new Paragraph(tag.getNbExe() + " Executions performed (Over " + tag.getNbExe() + " in Total including retries)"));

            /**
             * Result information per status
             */
            document.add(new Paragraph("Global Status").setMarginTop(10).setBold().setFontSize(14));
            // Creating a table
            Table tableGlobalStatus = new Table(new float[]{50, 50, 40})
                    .addHeaderCell(new Cell().add(new Paragraph("Status")).setBackgroundColor(ColorConstants.LIGHT_GRAY))
                    .addHeaderCell(new Cell().add(new Paragraph("Number")).setBackgroundColor(ColorConstants.LIGHT_GRAY))
                    .addHeaderCell(new Cell().add(new Paragraph("%")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            // Map that will contain the color of every status.
            Map<String, String> statColorMap = new HashMap<>();
            statColorMap.put(TestCaseExecution.CONTROLSTATUS_OK, TestCaseExecution.CONTROLSTATUS_OK_COL_EXT);
            statColorMap.put(TestCaseExecution.CONTROLSTATUS_KO, TestCaseExecution.CONTROLSTATUS_KO_COL_EXT);
            statColorMap.put(TestCaseExecution.CONTROLSTATUS_FA, TestCaseExecution.CONTROLSTATUS_FA_COL_EXT);
            statColorMap.put(TestCaseExecution.CONTROLSTATUS_NA, TestCaseExecution.CONTROLSTATUS_NA_COL_EXT);
            statColorMap.put(TestCaseExecution.CONTROLSTATUS_NE, TestCaseExecution.CONTROLSTATUS_NE_COL_EXT);
            statColorMap.put(TestCaseExecution.CONTROLSTATUS_WE, TestCaseExecution.CONTROLSTATUS_WE_COL_EXT);
            statColorMap.put(TestCaseExecution.CONTROLSTATUS_PE, TestCaseExecution.CONTROLSTATUS_PE_COL_EXT);
            statColorMap.put(TestCaseExecution.CONTROLSTATUS_QU, TestCaseExecution.CONTROLSTATUS_QU_COL_EXT);
            statColorMap.put(TestCaseExecution.CONTROLSTATUS_QE, TestCaseExecution.CONTROLSTATUS_QE_COL_EXT);
            statColorMap.put(TestCaseExecution.CONTROLSTATUS_CA, TestCaseExecution.CONTROLSTATUS_CA_COL_EXT);
            // Map that will contain the nb of execution for global status.
            Map<String, Integer> statNbMap = new HashMap<>();
            statNbMap.put(TestCaseExecution.CONTROLSTATUS_OK, tag.getNbOK());
            statNbMap.put(TestCaseExecution.CONTROLSTATUS_KO, tag.getNbKO());
            statNbMap.put(TestCaseExecution.CONTROLSTATUS_FA, tag.getNbFA());
            statNbMap.put(TestCaseExecution.CONTROLSTATUS_NA, tag.getNbNA());
            statNbMap.put(TestCaseExecution.CONTROLSTATUS_NE, tag.getNbNE());
            statNbMap.put(TestCaseExecution.CONTROLSTATUS_WE, tag.getNbWE());
            statNbMap.put(TestCaseExecution.CONTROLSTATUS_PE, tag.getNbPE());
            statNbMap.put(TestCaseExecution.CONTROLSTATUS_QU, tag.getNbQU());
            statNbMap.put(TestCaseExecution.CONTROLSTATUS_QE, tag.getNbQE());
            statNbMap.put(TestCaseExecution.CONTROLSTATUS_CA, tag.getNbCA());
            // Status list in the correct order.
            float per = 0;
            List<String> statList = new ArrayList<>(Arrays.asList("OK", "KO", "FA", "NA", "NE", "WE", "PE", "QU", "QE", "CA"));
            for (String string : statList) {
                if (statNbMap.get(string) > 0) {
                    per = statNbMap.get(string) / (float) tag.getNbExeUsefull();
                    per *= 100;
                    tableGlobalStatus
                            .addCell(getStatusCell(string, 1, 1))
                            .addCell(String.valueOf(statNbMap.get(string))).setTextAlignment(TextAlignment.RIGHT)
                            .addCell(String.format("%.2f", per));

                }
            }
            // Adding Table to document        
            document.add(tableGlobalStatus);

            /**
             * Summary result per execution
             */
            document.add(new Paragraph("Execution list summary").setMarginTop(10).setBold().setFontSize(14));
            List<TestCaseExecution> listOfExecutions = testCaseExecutionService.readLastExecutionAndExecutionInQueueByTag(tag.getTag());
            // Creating a table
            Table tableExe = new Table(new float[]{25, 90, 40, 80, 20, 20, 40, 20, 50})
                    .addHeaderCell(new Cell().add(new Paragraph("Exe ID")).setBackgroundColor(ColorConstants.LIGHT_GRAY).setFontSize(8))
                    .addHeaderCell(new Cell().add(new Paragraph("Test Folder")).setBackgroundColor(ColorConstants.LIGHT_GRAY).setFontSize(8))
                    .addHeaderCell(new Cell().add(new Paragraph("Test ID")).setBackgroundColor(ColorConstants.LIGHT_GRAY).setFontSize(8))
                    .addHeaderCell(new Cell().add(new Paragraph("Application")).setBackgroundColor(ColorConstants.LIGHT_GRAY).setFontSize(8))
                    .addHeaderCell(new Cell().add(new Paragraph("Country")).setBackgroundColor(ColorConstants.LIGHT_GRAY).setFontSize(8))
                    .addHeaderCell(new Cell().add(new Paragraph("Environment")).setBackgroundColor(ColorConstants.LIGHT_GRAY).setFontSize(8))
                    .addHeaderCell(new Cell().add(new Paragraph("Robot")).setBackgroundColor(ColorConstants.LIGHT_GRAY).setFontSize(8))
                    .addHeaderCell(new Cell().add(new Paragraph("Prio")).setBackgroundColor(ColorConstants.LIGHT_GRAY).setFontSize(8))
                    .addHeaderCell(new Cell().add(new Paragraph("Result")).setBackgroundColor(ColorConstants.LIGHT_GRAY).setFontSize(8));
            for (TestCaseExecution execution : listOfExecutions) {
                Cell cellID = new Cell(2, 1).add(new Paragraph(String.valueOf(execution.getId()))).setVerticalAlignment(VerticalAlignment.MIDDLE).setTextAlignment(TextAlignment.CENTER);
                Cell cellRes = getStatusCell(execution.getControlStatus(), 2, 1);
                Cell cellTCDesc = new Cell(1, 7).add(new Paragraph(execution.getDescription())).setFontSize(7);

                tableExe
                        .addCell(cellID.setAction(PdfAction.createGoTo(String.valueOf(execution.getId()))))
                        .addCell(execution.getTest())
                        .addCell(execution.getTestCase())
                        .addCell(execution.getApplication())
                        .addCell(execution.getCountry())
                        .addCell(execution.getEnvironment())
                        .addCell(execution.getRobot())
                        .addCell(String.valueOf(execution.getTestCasePriority()))
                        .addCell(cellRes);
                tableExe
                        .addCell(cellTCDesc);
            }
            document.add(tableExe);
            AreaBreak aB = new AreaBreak();
            document.add(aB);

            /**
             * Detail information per execution
             */
            int i = 1;
            for (TestCaseExecution execution : listOfExecutions) {
                document.add(new Paragraph("Execution: " + execution.getId()).setBold().setFontSize(20).setTextAlignment(TextAlignment.CENTER).setDestination(String.valueOf(execution.getId())));
                // Adding exeution details

                // Adding area break to the PDF
                if (i++ < listOfExecutions.size()) {
                    document.add(aB);
                }
            }

            // Closing the document
            return dest;
        } catch (ParseException | CerberusException ex) {
            LOG.error(ex, ex);
        } catch (JSONException ex) {
            LOG.error(ex, ex);
        }
        return null;
    }

    private Cell getStatusCell(String status, int rowspan, int colspan) {
        String coloHex = getColor(status);

        Cell cellRes = new Cell(rowspan, colspan)
                .add(new Paragraph(status))
                .setBackgroundColor(new DeviceRgb(decodeColor(coloHex, "R"), decodeColor(coloHex, "G"), decodeColor(coloHex, "B")))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.CENTER);

        return cellRes;
    }

    private int decodeColor(String hexColor, String indexRGB) {
        String hexValue = hexColor.replace("#", "0x");

        int i = Integer.decode(hexValue).intValue();
        switch (indexRGB) {
            case "R":
                return (i >> 16) & 0xFF;
            case "G":
                return (i >> 8) & 0xFF;
            case "B":
                return i & 0xFF;
        }

        return 0;
    }

    private String getColor(String controlStatus) {
        String color = null;

        if ("OK".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_OK_COL_EXT;
        } else if ("KO".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_KO_COL_EXT;
        } else if ("FA".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_FA_COL_EXT;
        } else if ("CA".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_CA_COL_EXT;
        } else if ("NA".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_NA_COL_EXT;
        } else if ("NE".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_NE_COL_EXT;
        } else if ("WE".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_WE_COL_EXT;
        } else if ("PE".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_PE_COL_EXT;
        } else if ("QU".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_QU_COL_EXT;
        } else if ("QE".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_QE_COL_EXT;
        } else {
            color = "#000000";
        }
        return color;
    }

}
