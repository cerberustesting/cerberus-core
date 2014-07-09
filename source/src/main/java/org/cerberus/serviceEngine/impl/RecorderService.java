/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.serviceEngine.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import org.apache.log4j.Level;
import org.cerberus.entity.ExecutionSOAPResponse;
import org.cerberus.entity.TestCaseExecution;
import org.cerberus.entity.TestCaseStepActionControlExecution;
import org.cerberus.entity.TestCaseStepActionExecution;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.service.IParameterService;
import org.cerberus.serviceEngine.IRecorderService;
import org.cerberus.serviceEngine.ISeleniumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class RecorderService implements IRecorderService {

    @Autowired
    ISeleniumService seleniumService;
    @Autowired
    IParameterService parameterService;
    @Autowired
    ExecutionSOAPResponse eSResponse;

    @Override
    public String recordScreenshotAndGetName(TestCaseExecution testCaseExecution,
            TestCaseStepActionExecution testCaseStepActionExecution, Integer control) {

        String test = testCaseExecution.getTest();
        String testCase = testCaseExecution.getTestCase();
        String step = String.valueOf(testCaseStepActionExecution.getStep());
        String sequence = String.valueOf(testCaseStepActionExecution.getSequence());
        String controlString = control.equals(0)?null:String.valueOf(control);

        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Doing screenshot.");
        String screenshotFilename = this.generateScreenshotFilename(test, testCase, step, sequence, controlString,null, "jpg");

        this.seleniumService.doScreenShot(testCaseExecution.getSelenium(), Long.toString(testCaseExecution.getId()), screenshotFilename);
        String screenshotPath = Long.toString(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getId()) + File.separator + screenshotFilename;
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Screenshot done in : " + screenshotPath);

        return screenshotPath;

    }

    /**
     * Generate ScreenshotFileName using 2 method :
     * If pictureName is not null, use it directly.
     * If picture name is null, generate name using test, testcase, step sequence and control.
     * @param test
     * @param testCase
     * @param step
     * @param sequence
     * @param control
     * @param pictureName
     * @param extension
     * @return 
     */
    private String generateScreenshotFilename(String test, String testCase, String step, String sequence, String control,String pictureName, String extension){
    
        StringBuilder sbScreenshotFilename = new StringBuilder();
        if (pictureName==null){
        sbScreenshotFilename.append(test);
        sbScreenshotFilename.append("-");
        sbScreenshotFilename.append(testCase);
        sbScreenshotFilename.append("-St");
        sbScreenshotFilename.append(step);
        sbScreenshotFilename.append("Sq");
        sbScreenshotFilename.append(sequence);
        if (control != null) {
            sbScreenshotFilename.append("Ct");
            sbScreenshotFilename.append(control);
        }
        } else {
        sbScreenshotFilename.append(pictureName);
        }
        sbScreenshotFilename.append(".");
        sbScreenshotFilename.append(extension);

        String screenshotFilename = sbScreenshotFilename.toString().replaceAll(" ", "");
    
        return screenshotFilename;
    }
    
    
    @Override
    public String recordXMLAndGetName(TestCaseExecution testCaseExecution, TestCaseStepActionExecution testCaseStepActionExecution, Integer control) {

        MyLogger.log(RunTestCaseService.class.getName(), Level.INFO, "Saving File.");

        String test = testCaseExecution.getTest();
        String testCase = testCaseExecution.getTestCase();
        String step = String.valueOf(testCaseStepActionExecution.getStep());
        String sequence = String.valueOf(testCaseStepActionExecution.getSequence());
        String controlString = control.equals(0)?null:String.valueOf(control);
        String fileName = testCaseStepActionExecution.getProperty().equalsIgnoreCase("")?null:testCaseStepActionExecution.getProperty();

        String screenshotFilename = this.generateScreenshotFilename(test, testCase, step, sequence, controlString,fileName,  "xml");

        String imgPath = "";
        try {
            imgPath = parameterService.findParameterByKey("cerberus_picture_path", "").getValue();
        } catch (CerberusException ex) {
            Logger.getLogger(ExecutionRunService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        File dir = new File(imgPath + testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getId());
        dir.mkdirs();

        File file = new File(dir.getAbsolutePath() + File.separator + screenshotFilename);
        System.err.println(" FILE : " + file.getAbsolutePath());

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(eSResponse.getExecutionSOAPResponse(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getExecutionUUID()).getBytes());
            fileOutputStream.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ExecutionRunService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ExecutionRunService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        String screenshotPath = Long.toString(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getId()) + File.separator + screenshotFilename;
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Screenshot done in : " + screenshotPath);

        return screenshotPath;
    }

    @Override
    public String recordPageSourceAndGetName(TestCaseExecution testCaseExecution, TestCaseStepActionExecution testCaseStepActionExecution, Integer control) {
        MyLogger.log(RunTestCaseService.class.getName(), Level.INFO, "Saving File.");

        String test = testCaseExecution.getTest();
        String testCase = testCaseExecution.getTestCase();
        String step = String.valueOf(testCaseStepActionExecution.getStep());
        String sequence = String.valueOf(testCaseStepActionExecution.getSequence());
        String controlString = control.equals(0)?null:String.valueOf(control);

        String screenshotFilename = this.generateScreenshotFilename(test, testCase, step, sequence, controlString,null,  "html");

        String imgPath = "";
        try {
            imgPath = parameterService.findParameterByKey("cerberus_picture_path", "").getValue();
        } catch (CerberusException ex) {
            Logger.getLogger(ExecutionRunService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        File dir = new File(imgPath + testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getId());
        dir.mkdirs();

        File file = new File(dir.getAbsolutePath() + File.separator + screenshotFilename);
        System.err.println(" FILE : " + file.getAbsolutePath());

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(this.seleniumService.getPageSource(testCaseExecution.getSelenium()).getBytes());
            fileOutputStream.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ExecutionRunService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ExecutionRunService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        String screenshotPath = Long.toString(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getId()) + File.separator + screenshotFilename;
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Screenshot done in : " + screenshotPath);

        return screenshotPath;
    }

    @Override
    public String recordSeleniumLogAndGetName(TestCaseExecution testCaseExecution) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
