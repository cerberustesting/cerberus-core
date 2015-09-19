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


import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.log4j.Level;
import org.cerberus.crud.entity.ExecutionSOAPResponse;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseStepActionControlExecution;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.crud.entity.TestDataLibResult;
import org.cerberus.crud.entity.TestDataLibResultSOAP;
import org.cerberus.enums.TestDataLibTypeEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.service.engine.IRecorderService;
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

        String test = testCaseExecution.getTest();
        String testCase = testCaseExecution.getTestCase();
        String step = String.valueOf(testCaseStepActionExecution.getStep());
        String sequence = String.valueOf(testCaseStepActionExecution.getSequence());
        String controlString = control.equals(0) ? null : String.valueOf(control);
        long runId = testCaseExecution.getId();

        MyLogger.log(RecorderService.class.getName(), Level.INFO, "Doing screenshot.");
        /**
         * Generate FileName
         */
        String screenshotFilename = this.generateScreenshotFilename(test, testCase, step, sequence, controlString, null, "jpg");

        /**
         * Take Screenshot and write it
         */
        String imgPath;
            try {
                BufferedImage newImage = this.webdriverService.takeScreenShot(testCaseExecution.getSession());
                imgPath = parameterService.findParameterByKey("cerberus_picture_path", "").getValue();
                File dir = new File(imgPath + runId);
                dir.mkdirs();
                ImageIO.write(newImage, "jpg", new File(imgPath + runId + File.separator + screenshotFilename));
            } catch (CerberusException ex) {
                Logger.getLogger(RecorderService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (IOException ex) {
            Logger.getLogger(RecorderService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
            Logger.getLogger(RecorderService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
        String screenshotPath = Long.toString(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getId()) + File.separator + screenshotFilename;
        MyLogger.log(RecorderService.class.getName(), Level.DEBUG, "Screenshot done in : " + screenshotPath);

        return screenshotPath;

    }

    /**
     * Generate ScreenshotFileName using 2 method : If pictureName is not null,
     * use it directly. If picture name is null, generate name using test,
     * testcase, step sequence and control.
     *
     * @param test
     * @param testCase
     * @param step
     * @param sequence
     * @param control
     * @param pictureName
     * @param extension
     * @return
     */
    @Override
    public String generateScreenshotFilename(String test, String testCase, String step, String sequence, String control, String pictureName, String extension) {

        StringBuilder sbScreenshotFilename = new StringBuilder();
        if (pictureName == null) {
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

        return sbScreenshotFilename.toString().replaceAll(" ", "");
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

        String screenshotFilename = this.generateScreenshotFilename(test, testCase, step, sequence, controlString, fileName, "xml");

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

        String screenshotFilename = this.generateScreenshotFilename(test, testCase, step, sequence, controlString, null, "html");

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
            if (testCaseStepActionExecution.getAction().contains("callSoap")) {
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
         
        if ( data != null && data.size() > 0) {
            //if the data library contains entries for xml, then save that xml
            
            //list of entries
            Long executionId = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getId();
            Collection<TestDataLibResult> itValues = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getDataLibraryExecutionDataList().values();
            String test = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getTest();
            String testCase = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getTestCase();
            String step = String.valueOf(testCaseStepActionExecution.getStep());
            String sequence = String.valueOf(testCaseStepActionExecution.getSequence());            
            String controlString = controlNumber.equals(0) ? null : String.valueOf(controlNumber);
            
            
            StringBuilder url = new StringBuilder();
            for(TestDataLibResult result : itValues){
                //xml that was retrieved during execution will be saved for debug purposes
                if(result.getType().equals(TestDataLibTypeEnum.SOAP.getCode())){

                    String responseKeyID = ((TestDataLibResultSOAP)result).getSoapResponseKey();
                    //if the key is still in the execution data, we must remove it
                    if(eSResponse.getExecutionSOAPResponse(responseKeyID) != null){
                        //gets the xml associated with the entry
                        String xml = eSResponse.getExecutionSOAPResponse(responseKeyID); 
                        String envelope = eSResponse.getExecutionSOAPResponse(responseKeyID + "_request"); 
                        //the XML execution is available in the entry
                        try {
                            String path = parameterService.findParameterByKey("cerberus_picture_path", "").getValue() + executionId + File.separator;
                            String fileName = this.generateScreenshotFilename(test, testCase, step, sequence, controlString, null,  "xml") ;
                            String requestFileName = fileName.replace(".xml", "_request.xml");
                            
                            url.append(testCaseStepActionExecution.getActionResultMessage().getDescription());
                            url.append(" <a target='_blank' href='");
                            url.append(path).append(requestFileName).append("'> SOAP request </a>");
                            url.append(" | <a target='_blank' href='");
                            url.append(path).append(fileName).append("'> SOAP response </a>");
                            
                            testCaseStepActionExecution.getActionResultMessage().setDescription(url.toString());
                            testCaseStepActionExecution.setReturnMessage(url.toString());
                            
                            recordFile(path, requestFileName, envelope);
                            recordFile(path, fileName, xml);
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
