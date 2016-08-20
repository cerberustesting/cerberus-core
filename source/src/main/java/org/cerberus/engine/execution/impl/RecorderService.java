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
import java.util.HashMap;
import java.util.List;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseStepActionControlExecution;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITestCaseExecutionFileService;
import org.cerberus.exception.CerberusException;
import org.cerberus.engine.entity.SOAPExecution;
import org.cerberus.engine.execution.IRecorderService;
import org.cerberus.service.datalib.IDataLibService;
import org.cerberus.service.webdriver.impl.WebDriverService;
import org.cerberus.util.SoapUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.version.Infos;
import org.json.JSONArray;
import org.json.JSONException;
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
    ITestCaseExecutionFileService testCaseExecutionFileService;
    @Autowired
    WebDriverService webdriverService;
    @Autowired
    IDataLibService dataLibService;

    private String rootFolder; // Defined from parameter.
    private String subFolder; // Calculated from execution ID in order to spread the files on different folder.
    private String subFolderURL; // Same as subFolder but with / as separator.
    private String fullPath; // Full folder path where the fle will be stored.
    private String fileName; // Final filename.
    private String fullFilename; // Complete filename with system dependant File Separator. --> Used to save the file on FileSystem.
    private String relativeFilenameURL; // relative filename with / as a separator (from root folder). --> Saved into database.
    private String level; // Level where the file will be stored.

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(RecorderService.class);

    @Override
    public void recordExecutionInformationAfterStepActionandControl(TestCaseStepActionExecution testCaseStepActionExecution, TestCaseStepActionControlExecution testCaseStepActionControlExecution) {
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
         * SCREENSHOT Management. Screenshot only done when : screenshot
         * parameter is eq to 2 or screenshot parameter is eq to 1 with the
         * correct doScreenshot flag on the last action MessageEvent.
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
                    recordScreenshot(myExecution, testCaseStepActionExecution, controlNumber);
                } else {
                    LOG.debug(logPrefix + "Not Doing screenshot because connectivity with selenium server lost.");
                }

            }
        } else {
            LOG.debug(logPrefix + "Not Doing screenshot because of the screenshot parameter or flag on the last Action result.");
        }

        /**
         * PAGESOURCE management. Get PageSource if requested by the last Action
         * MessageEvent.
         *
         */
        if ((myExecution.getPageSource() == 2) || ((myExecution.getPageSource() == 1) && (getPageSource))) {

            if (applicationType.equals("GUI")) {
                /**
                 * Only if the return code is not equal to Cancel, meaning lost
                 * connectivity with selenium.
                 */
                if (!returnCode.equals("CA")) {
                    recordPageSource(myExecution, testCaseStepActionExecution, controlNumber);
                } else {
                    LOG.debug(logPrefix + "Not Doing screenshot because connectivity with selenium server lost.");
                }
            }
        } else {
            LOG.debug(logPrefix + "Not getting page source because of the pageSource parameter or flag on the last Action result.");
        }

        /**
         * Last call XML SOURCE management. Get Source of the XML if requested
         * by the last Action or control MessageEvent.
         *
         */
        if (applicationType.equals("WS")
                && ((myExecution.getPageSource() == 2) || ((myExecution.getPageSource() == 1) && (getPageSource))
                || (myExecution.getScreenshot() == 2) || ((myExecution.getScreenshot() == 1) && (doScreenshot)))) {
            //Record the Request and Response.
            SOAPExecution se = (SOAPExecution) testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getLastSOAPCalled().getItem();
            recordSOAPCall(myExecution, testCaseStepActionExecution, controlNumber, se);
        }

    }

    @Override
    public void recordScreenshot(TestCaseExecution testCaseExecution, TestCaseStepActionExecution testCaseStepActionExecution, Integer control) {

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
         * Take Screenshot and write it
         */
        File newImage = this.webdriverService.takeScreenShotFile(testCaseExecution.getSession());
        if (newImage != null) {
            try {
                this.initFilenames(runId, test, testCase, step, sequence, controlString, null, 0, "screenshot", "jpg");
                LOG.debug(logPrefix + "FullPath " + fullPath);

                File dir = new File(fullPath);
                if (!dir.exists()) {
                    LOG.debug(logPrefix + "Create directory for execution " + fullPath);
                    dir.mkdirs();
                }
                // Getting the max size of the screenshot.
                long maxSizeParam = parameterService.getParameterByKey("cerberus_screenshot_max_size", "", 1048576);
                if (maxSizeParam < newImage.length()) {
                    LOG.warn(logPrefix + "Screen-shot size exceeds the maximum defined in configurations " + newImage.getName() + " destination: " + relativeFilenameURL);
                }
                //copies the temp file to the execution file
                FileUtils.copyFile(newImage, new File(fullFilename));
                LOG.debug(logPrefix + "Copy file finished with success - source: " + newImage.getName() + " destination: " + relativeFilenameURL);

                // Index file created to database.
                testCaseExecutionFileService.create(testCaseExecution.getId(), level, "Screenshot", relativeFilenameURL, "JPG", "");

                //deletes the temporary file
                FileUtils.forceDelete(newImage);
                LOG.debug(logPrefix + "Temp file deleted with success " + newImage.getName());
                LOG.debug(logPrefix + "Screenshot done in : " + relativeFilenameURL);

            } catch (IOException ex) {
                LOG.error(logPrefix + ex.toString());
            } catch (CerberusException ex) {
                LOG.error(logPrefix + ex.toString());
            }
        } else {
            LOG.warn(logPrefix + "Screenshot returned null.");
        }
    }

    @Override
    public void recordPageSource(TestCaseExecution testCaseExecution, TestCaseStepActionExecution testCaseStepActionExecution, Integer control) {
        // Used for logging purposes
        String logPrefix = Infos.getInstance().getProjectNameAndVersion() + " - ";

        LOG.debug(logPrefix + "Starting to save Page Source File.");

        String test = testCaseExecution.getTest();
        String testCase = testCaseExecution.getTestCase();
        String step = String.valueOf(testCaseStepActionExecution.getStep());
        String sequence = String.valueOf(testCaseStepActionExecution.getSequence());
        String controlString = control.equals(0) ? null : String.valueOf(control);

        try {
            this.initFilenames(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getId(), test, testCase, step, sequence, controlString, null, 0, "pagesource", "html");
            File dir = new File(fullPath);
            dir.mkdirs();

            File file = new File(fullFilename);

            FileOutputStream fileOutputStream;
            try {
                fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(this.webdriverService.getPageSource(testCaseExecution.getSession()).getBytes());
                fileOutputStream.close();

                // Index file created to database.
                testCaseExecutionFileService.create(testCaseExecution.getId(), level, "Page Source", relativeFilenameURL, "HTML", "");

            } catch (FileNotFoundException ex) {
                LOG.error(logPrefix + ex.toString());

            } catch (IOException ex) {
                LOG.error(logPrefix + ex.toString());
            }

            LOG.debug(logPrefix + "Page Source file saved in : " + relativeFilenameURL);
        } catch (CerberusException ex) {
            LOG.error(logPrefix + ex.toString());
        }
    }

    @Override
    public void recordSOAPCall(TestCaseExecution testCaseExecution, TestCaseStepActionExecution testCaseStepActionExecution, Integer control, SOAPExecution se) {
        // Used for logging purposes
        String logPrefix = Infos.getInstance().getProjectNameAndVersion() + " - ";

        String test = testCaseExecution.getTest();
        String testCase = testCaseExecution.getTestCase();
        String step = String.valueOf(testCaseStepActionExecution.getStep());
        String sequence = String.valueOf(testCaseStepActionExecution.getSequence());
        String controlString = control.equals(0) ? null : String.valueOf(control);
        long runId = testCaseExecution.getId();

        String xmlFullFilename = null;

        try {

            // REQUEST.
            this.initFilenames(runId, test, testCase, step, sequence, controlString, null, 0, "request", "xml");
            recordFile(fullPath, fileName, SoapUtil.convertSoapMessageToString(se.getSOAPRequest()));

            // Index file created to database.
            testCaseExecutionFileService.create(runId, level, "SOAP Request", relativeFilenameURL, "XML", "");

            // RESPONSE.
            this.initFilenames(runId, test, testCase, step, sequence, controlString, null, 0, "response", "xml");
            recordFile(fullPath, fileName, SoapUtil.convertSoapMessageToString(se.getSOAPResponse()));

            // Index file created to database.
            testCaseExecutionFileService.create(runId, level, "SOAP Response", relativeFilenameURL, "XML", "");

            xmlFullFilename = relativeFilenameURL;

        } catch (CerberusException ex) {
            LOG.error(logPrefix + ex.toString());
        }
    }

    @Override
    public void recordSOAPProperty(Long runId, String property, int propertyIndex, SOAPExecution se) {
        // Used for logging purposes
        String logPrefix = Infos.getInstance().getProjectNameAndVersion() + " - ";

        try {

            // REQUEST.
            this.initFilenames(runId, null, null, null, null, null, property, propertyIndex, "request", "xml");
            recordFile(fullPath, fileName, SoapUtil.convertSoapMessageToString(se.getSOAPRequest()));

            // Index file created to database.
            testCaseExecutionFileService.create(runId, level, "SOAP Request", relativeFilenameURL, "XML", "");

            // RESPONSE.
            this.initFilenames(runId, null, null, null, null, null, property, propertyIndex, "response", "xml");
            recordFile(fullPath, fileName, SoapUtil.convertSoapMessageToString(se.getSOAPResponse()));

            // Index file created to database.
            testCaseExecutionFileService.create(runId, level, "SOAP Response", relativeFilenameURL, "XML", "");

        } catch (CerberusException ex) {
            LOG.error(logPrefix + "SOAP XML file was not saved due to unexpected error." + ex.toString());
        }
    }

    @Override
    public void recordTestDataLibProperty(Long runId, String property, int propertyIndex, List<HashMap<String, String>> result) {
        // Used for logging purposes
        String logPrefix = Infos.getInstance().getProjectNameAndVersion() + " - ";

        try {
            JSONArray jsonResult = null;
            jsonResult = dataLibService.convertToJSONObject(result);

            // RESULT.
            this.initFilenames(runId, null, null, null, null, null, property, propertyIndex, "result", "json");
            recordFile(fullPath, fileName, jsonResult.toString());

            // Index file created to database.
            testCaseExecutionFileService.create(runId, level, "Result", relativeFilenameURL, "JSON", "");

        } catch (CerberusException | JSONException ex) {
            LOG.error(logPrefix + "TestDataLib file was not saved due to unexpected error." + ex.toString());
        }
    }

    @Override
    public void recordSeleniumLog(TestCaseExecution testCaseExecution) {
        // Used for logging purposes
        String logPrefix = Infos.getInstance().getProjectNameAndVersion() + " - ";

        if (testCaseExecution.getApplication().getType().equals("GUI")) {

            if (testCaseExecution.getSeleniumLog() == 2 || (testCaseExecution.getSeleniumLog() == 1 && !testCaseExecution.getControlStatus().equals("OK"))) {
                LOG.info(logPrefix + "Starting to save File.");

                try {
                    this.initFilenames(testCaseExecution.getId(), null, null, null, null, null, null, 0, "selenium_log", "txt");

                    File dir = new File(fullPath);
                    dir.mkdirs();

                    File file = new File(fullFilename);

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

                        // Index file created to database.
                        testCaseExecutionFileService.create(testCaseExecution.getId(), level, "Selenium log", relativeFilenameURL, "TXT", "");

                    } catch (FileNotFoundException ex) {
                        LOG.error(logPrefix + ex.toString());
                    } catch (IOException ex) {
                        LOG.error(logPrefix + ex.toString());
                    }

                    LOG.debug(logPrefix + "Log recorded in : " + relativeFilenameURL);

                } catch (CerberusException ex) {
                    LOG.error(logPrefix + ex.toString());
                }
            }
        } else {
            LOG.debug(logPrefix + "Selenium Log not recorded because test on non GUI application");
        }
    }

    @Override
    public void recordUploadedFile(long executionId, TestCaseStepActionExecution tcsae, FileItem uploadedFile) {
        String UploadedfileName = new File(uploadedFile.getName()).getName();

        try {
            // UPLOADED File.
            this.initFilenames(executionId, tcsae.getTest(), tcsae.getTestCase(), String.valueOf(tcsae.getStep()), String.valueOf(tcsae.getSequence()), null, null, 0, "image", "jpg");
            File storeFile = new File(fullFilename);
            // saves the file on disk
            uploadedFile.write(storeFile);

            // Index file created to database.
            testCaseExecutionFileService.create(executionId, level, "Image", relativeFilenameURL, "JPG", "");

        } catch (Exception ex) {
            LOG.error("File: " + UploadedfileName + " failed to be uploaded/saved: " + ex.toString());
        }

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

    private void initFilenames(long exeID, String test, String testCase, String step, String sequence, String controlString, String property, int propertyIndex, String filename, String extention) throws CerberusException {

        /**
         * Root folder initialisation. The root folder is confugures from the
         * parameter cerberus_mediastorage_path.
         */
        rootFolder = parameterService.findParameterByKey("cerberus_mediastorage_path", "").getValue();
        rootFolder = StringUtil.addSuffixIfNotAlready(rootFolder, File.separator);

        /**
         * SubFolder. Subfolder is split in order to reduce the nb of folder
         * within a folder. 2 levels of 2 digits each. he last level is the
         * execution id.
         */
        subFolder = getStorageSubFolder(exeID);
        subFolderURL = getStorageSubFolderURL(exeID);

        /**
         * FullPath. Concatenation of the rootfolder and subfolder.
         */
        fullPath = rootFolder + subFolder;

        /**
         * Filename. If filename is not define, we assign it from the test,
         * testcase, step action and control.
         */
        StringBuilder sbfileName = new StringBuilder();
        if (!StringUtil.isNullOrEmpty(test)) {
            sbfileName.append(test).append("-");
        }
        if (!StringUtil.isNullOrEmpty(testCase)) {
            sbfileName.append(testCase).append("-");
        }
        if (!StringUtil.isNullOrEmpty(step)) {
            sbfileName.append("S").append(step).append("-");
        }
        if (!StringUtil.isNullOrEmpty(sequence)) {
            sbfileName.append("A").append(sequence).append("-");
        }
        if (!StringUtil.isNullOrEmpty(controlString)) {
            sbfileName.append("C").append(controlString).append("-");
        }
        if (!StringUtil.isNullOrEmpty(property)) {
            sbfileName.append(property).append("-");
        }
        if (propertyIndex != 0) {
            sbfileName.append(propertyIndex).append("-");
        }
        if (!StringUtil.isNullOrEmpty(filename)) {
            sbfileName.append(filename).append("-");
        }
        fileName = StringUtil.removeLastChar(sbfileName.toString(), 1) + "." + extention;
        fileName = fileName.replace(" ", "");

        /**
         * Level. 5 levels possible. Keys are defined seperated by -. 1/ Execution
         * level --> emptyString. 2/ Step level --> test+testcase+Step 3/ Action level
         * --> test+testcase+Step+action 4/ Control level -->
         * test+testcase+Step+action+control 5/ Property level --> property+index
         */
        level = "";
        if (!(StringUtil.isNullOrEmpty(controlString))) {
            level = test + "-" + testCase + "-" + step + "-" + sequence + "-" + controlString;
        } else if (!(StringUtil.isNullOrEmpty(sequence))) {
            level = test + "-" + testCase + "-" + step + "-" + sequence;
        } else if (!(StringUtil.isNullOrEmpty(controlString))) {
            level = test + "-" + testCase + "-" + step;
        } else if (!(StringUtil.isNullOrEmpty(property))) {
            level = property + "-" + propertyIndex;
        }

        /**
         * Final Filename with full path.
         */
        fullFilename = rootFolder + File.separator + subFolder + File.separator + fileName;
        relativeFilenameURL = subFolderURL + "/" + fileName;

    }

    @Override
    public String getStorageSubFolderURL(long exeID) {
        String idString = String.valueOf(exeID);
        String subFolderResult;
        if (idString.length() >= 4) {
            return idString.substring((idString.length() - 2)) + "/" + idString.substring((idString.length() - 4), (idString.length() - 2)) + "/" + idString;
        } else {
            return idString;
        }
    }

    @Override
    public String getStorageSubFolder(long exeID) {
        String idString = String.valueOf(exeID);
        String subFolderResult;
        if (idString.length() >= 4) {
            return idString.substring((idString.length() - 2)) + File.separator + idString.substring((idString.length() - 4), (idString.length() - 2)) + File.separator + idString;
        } else {
            return idString;
        }
    }
}
