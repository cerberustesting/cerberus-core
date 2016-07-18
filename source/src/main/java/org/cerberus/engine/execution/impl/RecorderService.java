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
package org.cerberus.engine.execution.impl;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.xml.soap.SOAPMessage;
import org.apache.commons.io.FileUtils;
import org.cerberus.crud.entity.Parameter;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseStepActionControlExecution;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.exception.CerberusException;
import org.cerberus.engine.entity.SOAPExecution;
import org.cerberus.engine.execution.IRecorderService;
import org.cerberus.service.webdriver.impl.WebDriverService;
import org.cerberus.util.FileUtil;
import org.cerberus.util.SoapUtil;
import org.cerberus.version.Infos;
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
    WebDriverService webdriverService;

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(RecorderService.class);

    @Override
    public void recordExecutionInformation(TestCaseStepActionExecution testCaseStepActionExecution, TestCaseStepActionControlExecution testCaseStepActionControlExecution) {
        // Used for logging purposes
        String logPrefix = Infos.getInstance().getProjectNameAndVersion() + " - ";

        TestCaseExecution myExecution;
        boolean doScreenshot;
        boolean getPageSource;
        String applicationType;
        String returnCode;
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

            if (applicationType.equals("GUI")
                    || applicationType.equals("APK")
                    || applicationType.equals("IPA")) {
                /**
                 * Only if the return code is not equal to Cancel, meaning lost
                 * connectivity with selenium.
                 */
                if (!returnCode.equals("CA")) {
                    String screenshotPath = recordScreenshotAndGetName(myExecution, testCaseStepActionExecution, controlNumber);
                    if (testCaseStepActionControlExecution == null) {
                        testCaseStepActionExecution.setScreenshotFilename(screenshotPath);
                    } else {
                        testCaseStepActionControlExecution.setScreenshotFilename(screenshotPath);
                    }
                } else {
                    LOG.debug(logPrefix + "Not Doing screenshot because connectivity with selenium server lost.");
                }

            } else if (applicationType.equals("WS")) {
                //Record the Request and Response.
                TestCaseExecution tce = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution();
                String name = testCaseStepActionExecution.getProperty();
                SOAPExecution se = (SOAPExecution) testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getLastSOAPCalled().getItem();
                String responseFilePath = recordXMLAndGetName(tce.getId(), name, SoapUtil.convertSoapMessageToString(se.getSOAPResponse()));
                String requestFilePath = recordXMLAndGetName(tce.getId(), name + "_request", SoapUtil.convertSoapMessageToString(se.getSOAPRequest()));

                if (testCaseStepActionControlExecution == null) {
                    testCaseStepActionExecution.setScreenshotFilename(responseFilePath);
                } else {
                    testCaseStepActionControlExecution.setScreenshotFilename(responseFilePath);
                }
            }
        } else {
            LOG.debug(logPrefix + "Not Doing screenshot because of the screenshot parameter or flag on the last Action result.");
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
                    LOG.debug(logPrefix + "Not Doing screenshot because connectivity with selenium server lost.");
                }
            } else if (applicationType.equals("WS")) {
                //Record the Request and Response.
                TestCaseExecution tce = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution();
                String name = testCaseStepActionExecution.getProperty();
                SOAPExecution se = (SOAPExecution) testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getLastSOAPCalled().getItem();
                String responseFilePath = recordXMLAndGetName(tce.getId(), name, SoapUtil.convertSoapMessageToString(se.getSOAPResponse()));
                String requestFilePath = recordXMLAndGetName(tce.getId(), name + "_request", SoapUtil.convertSoapMessageToString(se.getSOAPRequest()));

                if (testCaseStepActionControlExecution == null) {
                    testCaseStepActionExecution.setPageSourceFilename(responseFilePath);
                } else {
                    testCaseStepActionControlExecution.setPageSourceFilename(responseFilePath);
                }
            }
        } else {
            LOG.debug(logPrefix + "Not getting page source because of the pageSource parameter or flag on the last Action result.");
        }

        if (testCaseStepActionExecution.getActionResultMessage().isGetPageSource()) {
            if (testCaseStepActionExecution.getAction().startsWith("callSoap")) {
                //Record the Request and Response.
                TestCaseExecution tce = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution();
                String name = testCaseStepActionExecution.getProperty();
                SOAPExecution se = (SOAPExecution) testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getLastSOAPCalled().getItem();
                String responseFilePath = recordXMLAndGetName(tce.getId(), name, SoapUtil.convertSoapMessageToString(se.getSOAPResponse()));
                String requestFilePath = recordXMLAndGetName(tce.getId(), name + "_request", SoapUtil.convertSoapMessageToString(se.getSOAPRequest()));

                testCaseStepActionExecution.setScreenshotFilename(responseFilePath);
            }
        }
    }

    @Override
    public String recordScreenshotAndGetName(TestCaseExecution testCaseExecution, TestCaseStepActionExecution testCaseStepActionExecution, Integer control) {

        String test = testCaseStepActionExecution.getTest();
        String testCase = testCaseStepActionExecution.getTestCase();
        String step = String.valueOf(testCaseStepActionExecution.getStep());
        String sequence = String.valueOf(testCaseStepActionExecution.getSequence());
        String controlString = control.equals(0) ? null : String.valueOf(control);
        long runId = testCaseExecution.getId();

        // Used for logging purposes
        String logPrefix = Infos.getInstance().getProjectNameAndVersion() + " - [" + test + " - " + testCase + " - step: " + step + " action: " + sequence + "] ";

        LOG.debug(logPrefix + "Doing screenshot.");
        /**
         * Generate FileName
         */
        String screenshotFilename = FileUtil.generateScreenshotFilename(test, testCase, step, sequence, controlString, null, "jpg");

        /**
         * Take Screenshot and write it
         */
        File newImage = this.webdriverService.takeScreenShotFile(testCaseExecution.getSession());
        if (newImage != null) {
            try {
                String imgPath = parameterService.findParameterByKey("cerberus_picture_path", "").getValue();
                File dir = new File(imgPath + runId);
                if (!dir.exists()) {
                    LOG.debug(logPrefix + "Create directory for execution " + runId);
                    dir.mkdirs();
                }
                long maxSizeParam = 1048576; //default max size that should be saved in the database
                Parameter paramMaxSize = parameterService.findParameterByKey("cerberus_screenshot_max_size", "");
                if (paramMaxSize != null) {
                    maxSizeParam = Long.valueOf(paramMaxSize.getValue());
                }
                if (maxSizeParam < newImage.length()) {
                    LOG.warn(logPrefix + "Screen-shot size exceeds the maximum defined in configurations " + newImage.getName() + " destination: " + screenshotFilename);
                }
                //copies the temp file to the execution file
                FileUtils.copyFile(newImage, new File(imgPath + runId + File.separator + screenshotFilename));
                LOG.debug(logPrefix + "Copy file finished with success - source: " + newImage.getName() + " destination: " + screenshotFilename);

                //deletes the temporary file
                FileUtils.forceDelete(newImage);
                LOG.debug(logPrefix + "Temp file deleted with success " + newImage.getName());
            } catch (IOException ex) {
                LOG.error(logPrefix + ex.toString());
            } catch (CerberusException ex) {
                LOG.error(logPrefix + ex.toString());
            }
        } else {
            LOG.warn(logPrefix + "Screenshot returned null.");
        }

        String screenshotPath = Long.toString(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getId()) + File.separator + screenshotFilename;
        LOG.debug(logPrefix + "Screenshot done in : " + screenshotPath);

        return screenshotPath;

    }

    @Override
    public String recordXMLAndGetName(Long executionId, String fileName, String content) {
        // Used for logging purposes
        String logPrefix = Infos.getInstance().getProjectNameAndVersion() + " - ";

        String xmlFilename = FileUtil.generateScreenshotFilename(null, null, null, null, null, fileName, "xml");
        StringBuilder filePath = new StringBuilder();

        try {
            filePath.append(parameterService.findParameterByKey("cerberus_picture_path", "").getValue());
            filePath.append(File.separator);
            filePath.append(executionId);
            filePath.append(File.separator);
        } catch (CerberusException ex) {
            LOG.error(logPrefix + ex.toString());
        }

        recordFile(filePath.toString(), xmlFilename, content);

        String xmlFullFilename = Long.toString(executionId) + File.separator + xmlFilename;

        return xmlFullFilename;
    }

    @Override
    public String recordPageSourceAndGetName(TestCaseExecution testCaseExecution, TestCaseStepActionExecution testCaseStepActionExecution, Integer control) {
        // Used for logging purposes
        String logPrefix = Infos.getInstance().getProjectNameAndVersion() + " - ";

        LOG.debug(logPrefix + "Starting to save Page Source File.");

        String test = testCaseExecution.getTest();
        String testCase = testCaseExecution.getTestCase();
        String step = String.valueOf(testCaseStepActionExecution.getStep());
        String sequence = String.valueOf(testCaseStepActionExecution.getSequence());
        String controlString = control.equals(0) ? null : String.valueOf(control);

        String pageSourceFilename = FileUtil.generateScreenshotFilename(test, testCase, step, sequence, controlString, null, "html");

        String imgPath = "";
        try {
            imgPath = parameterService.findParameterByKey("cerberus_picture_path", "").getValue();
        } catch (CerberusException ex) {
            LOG.error(logPrefix + ex.toString());
        }
        File dir = new File(imgPath + testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getId());
        dir.mkdirs();

        File file = new File(dir.getAbsolutePath() + File.separator + pageSourceFilename);

        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(this.webdriverService.getPageSource(testCaseExecution.getSession()).getBytes());
            fileOutputStream.close();
        } catch (FileNotFoundException ex) {
            LOG.error(logPrefix + ex.toString());
        } catch (IOException ex) {
            LOG.error(logPrefix + ex.toString());
        }

        String screenshotPath = Long.toString(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getId()) + File.separator + pageSourceFilename;
        LOG.debug(logPrefix + "Page Source file saved in : " + screenshotPath);

        return screenshotPath;
    }

    @Override
    public String recordSeleniumLogAndGetName(TestCaseExecution testCaseExecution) {
        // Used for logging purposes
        String logPrefix = Infos.getInstance().getProjectNameAndVersion() + " - ";

        if (testCaseExecution.getApplication().getType().equals("GUI")) {
            if (testCaseExecution.getSeleniumLog() == 2 || (testCaseExecution.getSeleniumLog() == 1 && !testCaseExecution.getControlStatus().equals("OK"))) {
                LOG.info(logPrefix + "Starting to save File.");

                String logFilename = "selenium_log.txt";

                String imgPath = "";
                try {
                    imgPath = parameterService.findParameterByKey("cerberus_picture_path", "").getValue();
                } catch (CerberusException ex) {
                    LOG.error(logPrefix + ex.toString());
                }
                File dir = new File(imgPath + testCaseExecution.getId());
                dir.mkdirs();

                File file = new File(dir.getAbsolutePath() + File.separator + logFilename);

                FileOutputStream fileOutputStream;
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
                    LOG.error(logPrefix + ex.toString());
                } catch (IOException ex) {
                    LOG.error(logPrefix + ex.toString());
                }

                String seleniumLogPath = Long.toString(testCaseExecution.getId()) + File.separator + logFilename;
                LOG.debug(logPrefix + "Log recorded in : " + logFilename);

                return seleniumLogPath;
            }
        } else {
            LOG.debug(logPrefix + "Selenium Log not recorded because test on non GUI application");
        }
        return null;
    }

    @Override
    public String recordSoapMessageAndGetPath(Long executionId, SOAPMessage soapMessage, String fileName) {
        // Used for logging purposes
        String logPrefix = Infos.getInstance().getProjectNameAndVersion() + " - ";

        //list of entries
        String path = "";

        try {
            String mes = SoapUtil.convertSoapMessageToString(soapMessage);

            //name of the generated files
            path = parameterService.findParameterByKey("cerberus_picture_path", "").getValue() + executionId + File.separator;

            //save the request and response files
            recordFile(path, fileName, mes);
        } catch (CerberusException ex) {
            LOG.error(logPrefix + "SOAP XML file was not saved due to unexpected error." + ex.toString());
        }

        return path + fileName;
    }

    /**
     * Auxiliary method that saves a file
     *
     * @param path - directory path
     * @param fileName - name of the file
     * @param content -content of the file
     */
    private void recordFile(String path, String fileName, String content) {
        // Used for logging purposes
        String logPrefix = Infos.getInstance().getProjectNameAndVersion() + " - ";

        LOG.info(logPrefix + "Starting to save File.");

        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(dir.getAbsolutePath() + File.separator + fileName);
            fileOutputStream.write(content.getBytes());
            fileOutputStream.close();
            LOG.debug(logPrefix + "File saved : " + path + File.separator + fileName);
        } catch (FileNotFoundException ex) {
            LOG.debug(logPrefix + "Unable to save : " + path + File.separator + fileName + " ex: " + ex);
        } catch (IOException ex) {
            LOG.debug(logPrefix + "Unable to save : " + path + File.separator + fileName + " ex: " + ex);
        }
    }

}
