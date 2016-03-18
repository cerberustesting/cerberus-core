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
package org.cerberus.service.engine.impl;


import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.cerberus.crud.entity.ExecutionSOAPResponse;
import org.cerberus.crud.entity.Parameter;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseStepActionControlExecution;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.enums.TestDataLibTypeEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.service.engine.IRecorderService;
import org.cerberus.service.engine.testdata.TestDataLibResult;
import org.cerberus.service.engine.testdata.TestDataLibResultSOAP;
import org.cerberus.util.FileUtil;
import org.cerberus.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class RecorderService implements IRecorderService {

    @Autowired
    IParameterService parameterService;
    @Autowired
    ExecutionSOAPResponse eSResponse;
    @Autowired
    WebDriverService webdriverService;

    @Override
    public String recordScreenshotAndGetName(TestCaseExecution testCaseExecution,
            TestCaseStepActionExecution testCaseStepActionExecution, Integer control) {

        String test = testCaseStepActionExecution.getTest();
        String testCase = testCaseStepActionExecution.getTestCase();
        String step = String.valueOf(testCaseStepActionExecution.getStep());
        String sequence = String.valueOf(testCaseStepActionExecution.getSequence());
        String controlString = control.equals(0) ? null : String.valueOf(control);
        long runId = testCaseExecution.getId();

        //used for logging purposes
        String testDescription = "[" + test + " - " + testCase + " - step: " + step + " action: " + sequence + "]";
        
        MyLogger.log(RecorderService.class.getName(), Level.INFO, testDescription + "Doing screenshot.");
        /**
         * Generate FileName
         */
        String screenshotFilename = FileUtil.generateScreenshotFilename(test, testCase, step, sequence, controlString, null, "jpg");
        
        /**
         * Take Screenshot and write it
         */
        File newImage = this.webdriverService.takeScreenShotFile(testCaseExecution.getSession());
        if(newImage != null){
            try {
                String imgPath = parameterService.findParameterByKey("cerberus_picture_path", "").getValue();
                File dir = new File(imgPath + runId);
                if(!dir.exists()){
                    MyLogger.log(RecorderService.class.getName(), Level.INFO, testDescription + "Create directory for execution " + runId);
                    dir.mkdirs();
                }
                long maxSize = 1048576; //default max size that should be saved in the database
                Parameter paramMaxSize = parameterService.findParameterByKey("cerberus_screenshot_max_size", "");
                if(paramMaxSize != null){
                    maxSize = Long.valueOf(paramMaxSize.getValue());                
                }
                if(maxSize < newImage.length()){
                    MyLogger.log(RecorderService.class.getName(), Level.WARN, testDescription + "Screen-shot size exceeds the maximum defined in configurations " + 
                            newImage.getName() + " destination: " + screenshotFilename);
                }
                //copies the temp file to the execution file
                FileUtils.copyFile(newImage, new File(imgPath + runId + File.separator + screenshotFilename));
                MyLogger.log(RecorderService.class.getName(), Level.INFO, testDescription + "Copy file finished with success - source: " + newImage.getName() + " destination: " + screenshotFilename);
                                
                //deletes the temporary file
                FileUtils.forceDelete(newImage);
                MyLogger.log(RecorderService.class.getName(), Level.INFO, testDescription + "Temp file deleted with success " + newImage.getName());
            } catch (IOException ex) {
                Logger.getLogger(RecorderService.class.getName()).log(java.util.logging.Level.SEVERE, testDescription, ex);
            } catch (CerberusException ex) {
                Logger.getLogger(RecorderService.class.getName()).log(java.util.logging.Level.SEVERE, testDescription, ex);
            } 
        }else{
            MyLogger.log(RecorderService.class.getName(), Level.WARN, testDescription + "Screenshot returned null " );        
        }
//old version  TODO:delete      
//        String imgPath;
//            try {
//                BufferedImage newImage = this.webdriverService.takeScreenShot(testCaseExecution.getSession());
//                    imgPath = parameterService.findParameterByKey("cerberus_picture_path", "").getValue();
//                    File dir = new File(imgPath + runId);
//                    dir.mkdirs();
//                ImageIO.write(newImage, "jpg", new File(imgPath + runId + File.separator + screenshotFilename));
//            } catch (CerberusException ex) {
//                Logger.getLogger(RecorderService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//            } catch (IOException ex) {
//                Logger.getLogger(RecorderService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//            } catch (IllegalArgumentException ex) {
//                Logger.getLogger(RecorderService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//            }
        String screenshotPath = Long.toString(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getId()) + File.separator + screenshotFilename;
        MyLogger.log(RecorderService.class.getName(), Level.DEBUG, testDescription +  "Screenshot done in : " + screenshotPath);

        return screenshotPath;

    }

    

    @Override
    public String recordXMLAndGetName(TestCaseExecution testCaseExecution, TestCaseStepActionExecution testCaseStepActionExecution, Integer control) {

        MyLogger.log(RecorderService.class.getName(), Level.INFO, "Saving File.");

        String test = testCaseExecution.getTest();
        String testCase = testCaseExecution.getTestCase();
        String step = String.valueOf(testCaseStepActionExecution.getStep());
        String sequence = String.valueOf(testCaseStepActionExecution.getSequence());
        String controlString = control.equals(0) ? null : String.valueOf(control);
        String fileName = testCaseStepActionExecution.getProperty().equalsIgnoreCase("") ? null : testCaseStepActionExecution.getProperty();

        String screenshotFilename = FileUtil.generateScreenshotFilename(test, testCase, step, sequence, controlString, fileName, "xml");

        String imgPath = "";
        try {
            imgPath = parameterService.findParameterByKey("cerberus_picture_path", "").getValue();
        } catch (CerberusException ex) {
            Logger.getLogger(ExecutionRunService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        File dir = new File(imgPath + testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getId());
        dir.mkdirs();

        File file = new File(dir.getAbsolutePath() + File.separator + screenshotFilename);

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
        MyLogger.log(RecorderService.class.getName(), Level.DEBUG, "Screenshot done in : " + screenshotPath);

        return screenshotPath;
    }

    @Override
    public String recordPageSourceAndGetName(TestCaseExecution testCaseExecution, TestCaseStepActionExecution testCaseStepActionExecution, Integer control) {
        MyLogger.log(RecorderService.class.getName(), Level.INFO, "Saving File.");

        String test = testCaseExecution.getTest();
        String testCase = testCaseExecution.getTestCase();
        String step = String.valueOf(testCaseStepActionExecution.getStep());
        String sequence = String.valueOf(testCaseStepActionExecution.getSequence());
        String controlString = control.equals(0) ? null : String.valueOf(control);

        String screenshotFilename = FileUtil.generateScreenshotFilename(test, testCase, step, sequence, controlString, null, "html");

        String imgPath = "";
        try {
            imgPath = parameterService.findParameterByKey("cerberus_picture_path", "").getValue();
        } catch (CerberusException ex) {
            Logger.getLogger(ExecutionRunService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        File dir = new File(imgPath + testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getId());
        dir.mkdirs();

        File file = new File(dir.getAbsolutePath() + File.separator + screenshotFilename);

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(this.webdriverService.getPageSource(testCaseExecution.getSession()).getBytes());
            fileOutputStream.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ExecutionRunService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ExecutionRunService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        String screenshotPath = Long.toString(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getId()) + File.separator + screenshotFilename;
        MyLogger.log(RecorderService.class.getName(), Level.DEBUG, "Screenshot done in : " + screenshotPath);

        return screenshotPath;
    }

    @Override
    public String recordSeleniumLogAndGetName(TestCaseExecution testCaseExecution) {
        if (testCaseExecution.getApplication().getType().equals("GUI")){
        if (testCaseExecution.getSeleniumLog()==2 || (testCaseExecution.getSeleniumLog()==1 && !testCaseExecution.getControlStatus().equals("OK"))){
        MyLogger.log(RecorderService.class.getName(), Level.INFO, "Saving File.");

        String logFilename = "selenium_log.txt";

        String imgPath = "";
        try {
            imgPath = parameterService.findParameterByKey("cerberus_picture_path", "").getValue();
        } catch (CerberusException ex) {
            Logger.getLogger(ExecutionRunService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        File dir = new File(imgPath + testCaseExecution.getId());
        dir.mkdirs();

        File file = new File(dir.getAbsolutePath() + File.separator + logFilename);

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);
            for (String element : this.webdriverService.getSeleniumLog(testCaseExecution.getSession())) {
                out.writeBytes(element);
            }
            byte[] bytes = baos.toByteArray();
            fileOutputStream.write(bytes);
            out.close();
            baos.close();
            fileOutputStream.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ExecutionRunService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ExecutionRunService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        String seleniumLogPath = Long.toString(testCaseExecution.getId()) + File.separator + logFilename;
        MyLogger.log(RecorderService.class.getName(), Level.DEBUG, "Log recorded in : " + logFilename);

        return seleniumLogPath;
        }
        } else {
        MyLogger.log(RecorderService.class.getName(), Level.INFO, "Selenium Log not recorded because test on non GUI application");
        }
        return null;
    }

    @Override
    public void recordExecutionInformation(TestCaseStepActionExecution testCaseStepActionExecution, TestCaseStepActionControlExecution testCaseStepActionControlExecution) {

        TestCaseExecution myExecution = null;
        boolean doScreenshot = false;
        boolean getPageSource = false;
        String applicationType = null;
        String returnCode = null;
        Integer controlNumber = 0;
        
        if (testCaseStepActionControlExecution == null) {
            myExecution = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution();
            doScreenshot = testCaseStepActionExecution.getActionResultMessage().isDoScreenshot();
            getPageSource = testCaseStepActionExecution.getActionResultMessage().isGetPageSource();
            applicationType = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getApplication().getType();
            returnCode = testCaseStepActionExecution.getReturnCode();
        } else {
            myExecution = testCaseStepActionControlExecution.getTestCaseStepActionExecution().getTestCaseStepExecution().gettCExecution();
            doScreenshot = testCaseStepActionControlExecution.getControlResultMessage().isDoScreenshot();
            getPageSource = testCaseStepActionControlExecution.getControlResultMessage().isGetPageSource();
            applicationType = testCaseStepActionControlExecution.getTestCaseStepActionExecution().getTestCaseStepExecution().gettCExecution().getApplication().getType();
            returnCode = testCaseStepActionControlExecution.getReturnCode();
            controlNumber = testCaseStepActionControlExecution.getControl();
        }

        /**
         * Screenshot only done when : screenshot parameter is eq to 2 or
         * screenshot parameter is eq to 1 with the correct doScreenshot flag on
         * the last action MessageEvent.
         */
        if ((myExecution.getScreenshot() == 2) || ((myExecution.getScreenshot() == 1) && (doScreenshot))) {

            if (applicationType.equals("GUI")) {
                /**
                 * Only if the return code is not equal to Cancel, meaning lost
                 * connectivity with selenium.
                 */
                if (!returnCode.equals("CA")) {
                    String screenshotPath = recordScreenshotAndGetName(myExecution, testCaseStepActionExecution, controlNumber);
                    System.out.print(screenshotPath);
                    if (testCaseStepActionControlExecution == null) {
                        testCaseStepActionExecution.setScreenshotFilename(screenshotPath);
                    } else {
                        testCaseStepActionControlExecution.setScreenshotFilename(screenshotPath);
                    }
                } else {
                    MyLogger.log(RecorderService.class.getName(), Level.DEBUG, "Not Doing screenshot because connectivity with selenium server lost.");
                }

            } else if (applicationType.equals("WS")) {
                String screenshotPath = recordXMLAndGetName(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution(), testCaseStepActionExecution, controlNumber);
                if (testCaseStepActionControlExecution == null) {
                    testCaseStepActionExecution.setScreenshotFilename(screenshotPath);
                } else {
                    testCaseStepActionControlExecution.setScreenshotFilename(screenshotPath);
                }
            }
        } else {
            MyLogger.log(RecorderService.class.getName(), Level.DEBUG, "Not Doing screenshot because of the screenshot parameter or flag on the last Action result.");
        }

        /**
         * Get PageSource if requested by the last Action MessageEvent.
         */
        if ((myExecution.getPageSource() == 2) || ((myExecution.getPageSource() == 1) && (getPageSource))) {

            if (applicationType.equals("GUI")) {
                /**
                 * Only if the return code is not equal to Cancel, meaning lost
                 * connectivity with selenium.
                 */
                if (!returnCode.equals("CA")) {
                    String psPath = recordPageSourceAndGetName(myExecution, testCaseStepActionExecution, controlNumber);
                    if (testCaseStepActionControlExecution == null) {
                        testCaseStepActionExecution.setPageSourceFilename(psPath);
                    } else {
                        testCaseStepActionControlExecution.setPageSourceFilename(psPath);
                    }
                } else {
                    MyLogger.log(RecorderService.class.getName(), Level.DEBUG, "Not Doing screenshot because connectivity with selenium server lost.");
                }
            } else if (applicationType.equals("WS")) {
                String screenshotPath = recordXMLAndGetName(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution(), testCaseStepActionExecution, controlNumber);
                if (testCaseStepActionControlExecution == null) {
                    testCaseStepActionExecution.setPageSourceFilename(screenshotPath);
                } else {
                    testCaseStepActionControlExecution.setPageSourceFilename(screenshotPath);
                }
            }
        } else {
            MyLogger.log(RecorderService.class.getName(), Level.DEBUG, "Not getting page source because of the pageSource parameter or flag on the last Action result.");
        }

        if (testCaseStepActionExecution.getActionResultMessage().isGetPageSource()) {
            //TODO: to be updated / ensures that the old actions callSoap and callSoapWithBase are still saving the xml file
            if (testCaseStepActionExecution.getAction().equals("callSoap") || 
                    testCaseStepActionExecution.getAction().equals("callSoapWithBase")) {
                String screenshotPath = recordXMLAndGetName(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution(), testCaseStepActionExecution, controlNumber);
                testCaseStepActionExecution.setScreenshotFilename(screenshotPath);
            }
        }
        //saves the soap responses that were collected during action execution
        recordXMLSOAPResponses(testCaseStepActionExecution, controlNumber);
    }
    
    private void recordXMLSOAPResponses(TestCaseStepActionExecution testCaseStepActionExecution, Integer controlNumber) {
        //checks if soap calls were performed and save the data into the database
         HashMap<String, TestDataLibResult> data = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getDataLibraryExecutionDataList();
         
        if ( data != null && !data.isEmpty()) {
            //if the data library contains entries for xml, then save that xml
            
            //list of entries
            Long executionId = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getId();
            Collection<TestDataLibResult> itValues = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getDataLibraryExecutionDataList().values();
            String test = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getTest();
            String testCase = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getTestCase();
            String step = String.valueOf(testCaseStepActionExecution.getStep());
            String sequence = String.valueOf(testCaseStepActionExecution.getSequence());            
            String controlString = controlNumber.equals(0) ? null : String.valueOf(controlNumber);
            
            
            for(TestDataLibResult result : itValues){
                //xml that was retrieved during execution will be saved for debug purposes
                if(result.getType().equals(TestDataLibTypeEnum.SOAP.getCode())){
                    TestDataLibResultSOAP soapCallToBeSaved = ((TestDataLibResultSOAP)result);
                    String responseKeyID = soapCallToBeSaved.getSoapResponseKey();
                    //if the key is still in the execution data, we must remove it
                    if(eSResponse.getExecutionSOAPResponse(responseKeyID) != null){
                        //gets the xml associated with the entry
                        String soapResponse = eSResponse.getExecutionSOAPResponse(responseKeyID); 
                        String envelope = eSResponse.getExecutionSOAPResponse(responseKeyID + "_request"); 
                        //the XML execution is available in the entry
                        try {
                            //name of the generated files
                            String fileName, requestFileName;
                            String descId = Long.toString(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getId());
                            
                            if((StringUtil.isNullOrEmpty(testCaseStepActionExecution.getProperty()) && testCaseStepActionExecution.getAction().startsWith("callSoap")) || 
                                    !testCaseStepActionExecution.getAction().startsWith("callSoap")){ 
                                //if the response name is not defined then we need to define it
                                fileName = FileUtil.generateScreenshotFilename(test, testCase, step, sequence, controlString, null,  "xml") ;
                                requestFileName = fileName.replace(".xml", "_request.xml");
                                //updates the return message with the new generated file name
                                String returnMessage = testCaseStepActionExecution.getReturnMessage().replace("%REQUEST_NAME%" , 
                                    descId + File.separator + requestFileName).replace("%RESPONSE_NAME%", descId + File.separator + fileName);
                                testCaseStepActionExecution.setReturnMessage(returnMessage);
                            }else{
                                fileName = testCaseStepActionExecution.getProperty() + ".xml";
                                requestFileName = testCaseStepActionExecution.getProperty() + "_request.xml";
                            }     
                            
                            String path = parameterService.findParameterByKey("cerberus_picture_path", "").getValue() + executionId + File.separator;
                            //name of the requested file
                                                        
                            //saves the xml file when is the getPageSource option is activated
                            if (testCaseStepActionExecution.getActionResultMessage().isGetPageSource()) {
                                testCaseStepActionExecution.setScreenshotFilename(descId + File.separator + fileName);
                            }
                            
                            
                            //save the request and response files
                            recordFile(path, requestFileName, envelope);
                            recordFile(path, fileName, soapResponse);
                        } catch (CerberusException ex) {
                            MyLogger.log(RecorderService.class.getName(), Level.DEBUG, "XML file was not saved due to unexpected error." + ex.toString());
                        }
                        
                        
                        //after saving then remove it from the list
                        eSResponse.removeExecutionSOAPResponse(responseKeyID);
                        eSResponse.removeExecutionSOAPResponse(responseKeyID + "_request");
                    }
                }
            }
        }
    }

    /**
     * Auxiliary method that saves a file
     * @param path - directory path
     * @param fileName - name of the file
     * @param content -content of the file
     */
    private void recordFile(String path, String fileName, String content) {
        MyLogger.log(RecorderService.class.getName(), Level.INFO, "Saving File.");

        File dir = new File(path);
        if(!dir.exists()){
            dir.mkdirs();
        }
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(dir.getAbsolutePath() + File.separator + fileName);
            fileOutputStream.write(content.getBytes());
            fileOutputStream.close();
            MyLogger.log(RecorderService.class.getName(), Level.DEBUG, "xml location : " + path + File.separator + fileName);
        } catch (FileNotFoundException ex) {
            MyLogger.log(RecorderService.class.getName(), Level.DEBUG, "Unable to save : " + path + File.separator + fileName + " ex: " + ex);
        } catch (IOException ex) {
            MyLogger.log(RecorderService.class.getName(), Level.DEBUG, "Unable to save : " + path + File.separator + fileName + " ex: " + ex);
        }
    }
}
