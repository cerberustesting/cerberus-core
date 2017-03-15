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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.soap.SOAPMessage;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.cerberus.crud.entity.AppService;
import org.cerberus.crud.entity.AppServiceContent;
import org.cerberus.crud.entity.AppServiceHeader;
import org.cerberus.crud.entity.Application;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseExecutionFile;
import org.cerberus.crud.entity.TestCaseStepActionControlExecution;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.crud.factory.IFactoryTestCaseExecutionFile;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITestCaseExecutionFileService;
import org.cerberus.engine.entity.Recorder;
import org.cerberus.exception.CerberusException;
import org.cerberus.engine.execution.IRecorderService;
import org.cerberus.service.datalib.IDataLibService;
import org.cerberus.service.webdriver.impl.WebDriverService;
import org.cerberus.util.SoapUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.version.Infos;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
    @Autowired
    private IFactoryTestCaseExecutionFile testCaseExecutionFileFactory;

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(RecorderService.class);

    @Override
    public List<TestCaseExecutionFile> recordExecutionInformationAfterStepActionandControl(TestCaseStepActionExecution testCaseStepActionExecution, TestCaseStepActionControlExecution testCaseStepActionControlExecution) {
        List<TestCaseExecutionFile> objectFileList = new ArrayList<TestCaseExecutionFile>();
        TestCaseExecutionFile objectFile = null;

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
            applicationType = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getApplicationObj().getType();
            returnCode = testCaseStepActionExecution.getReturnCode();
        } else {
            myExecution = testCaseStepActionControlExecution.getTestCaseStepActionExecution().getTestCaseStepExecution().gettCExecution();
            doScreenshot = testCaseStepActionControlExecution.getControlResultMessage().isDoScreenshot();
            getPageSource = testCaseStepActionControlExecution.getControlResultMessage().isGetPageSource();
            applicationType = testCaseStepActionControlExecution.getTestCaseStepActionExecution().getTestCaseStepExecution().gettCExecution().getApplicationObj().getType();
            returnCode = testCaseStepActionControlExecution.getReturnCode();
            controlNumber = testCaseStepActionControlExecution.getControlSequence();
        }

        /**
         * SCREENSHOT Management. Screenshot only done when : screenshot
         * parameter is eq to 2 or screenshot parameter is eq to 1 with the
         * correct doScreenshot flag on the last action MessageEvent.
         */
        if ((myExecution.getScreenshot() == 2) || ((myExecution.getScreenshot() == 1) && (doScreenshot))) {

            if (applicationType.equals(Application.TYPE_GUI)
                    || applicationType.equals(Application.TYPE_APK)
                    || applicationType.equals(Application.TYPE_IPA)) {
                /**
                 * Only if the return code is not equal to Cancel, meaning lost
                 * connectivity with selenium.
                 */
                if (!returnCode.equals("CA")) {
                    objectFile = this.recordScreenshot(myExecution, testCaseStepActionExecution, controlNumber);
                    if (objectFile != null) {
                        objectFileList.add(objectFile);
                    }
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

            if (applicationType.equals(Application.TYPE_GUI)
                    || applicationType.equals(Application.TYPE_APK)
                    || applicationType.equals(Application.TYPE_IPA)) {
                /**
                 * Only if the return code is not equal to Cancel, meaning lost
                 * connectivity with selenium.
                 */
                if (!returnCode.equals("CA")) {
                    objectFile = this.recordPageSource(myExecution, testCaseStepActionExecution, controlNumber);
                    if (objectFile != null) {
                        objectFileList.add(objectFile);
                    }
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
        if (applicationType.equals(Application.TYPE_SRV)
                && ((myExecution.getPageSource() == 2) || ((myExecution.getPageSource() == 1) && (getPageSource))
                || (myExecution.getScreenshot() == 2) || ((myExecution.getScreenshot() == 1) && (doScreenshot)))) {
            //Record the Request and Response.
            AppService se = (AppService) testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getLastServiceCalled();
            if (se != null) { // No Calls were performed previously
                List<TestCaseExecutionFile> objectFileSOAPList = new ArrayList<TestCaseExecutionFile>();
                objectFileSOAPList = this.recordServiceCall(myExecution, testCaseStepActionExecution, controlNumber, se);
                if (objectFileSOAPList.isEmpty() != true) {
                    for (TestCaseExecutionFile testCaseExecutionFile : objectFileSOAPList) {
                        objectFileList.add(testCaseExecutionFile);
                    }
                }
            }
        }

        return objectFileList;
    }

    @Override
    public TestCaseExecutionFile recordScreenshot(TestCaseExecution testCaseExecution, TestCaseStepActionExecution testCaseStepActionExecution, Integer control) {

        TestCaseExecutionFile object = null;

        String test = testCaseStepActionExecution.getTest();
        String testCase = testCaseStepActionExecution.getTestCase();
        String step = String.valueOf(testCaseStepActionExecution.getStep());
        String index = String.valueOf(testCaseStepActionExecution.getIndex());
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
                Recorder recorder = this.initFilenames(runId, test, testCase, step, index, sequence, controlString, null, 0, "screenshot", "png");
                LOG.debug(logPrefix + "FullPath " + recorder.getFullPath());

                File dir = new File(recorder.getFullPath());
                if (!dir.exists()) {
                    LOG.debug(logPrefix + "Create directory for execution " + recorder.getFullPath());
                    dir.mkdirs();
                }
                // Getting the max size of the screenshot.
                long maxSizeParam = parameterService.getParameterIntegerByKey("cerberus_screenshot_max_size", "", 1048576);
                if (maxSizeParam < newImage.length()) {
                    LOG.warn(logPrefix + "Screen-shot size exceeds the maximum defined in configurations " + newImage.getName() + " destination: " + recorder.getRelativeFilenameURL());
                }
                //copies the temp file to the execution file
                FileUtils.copyFile(newImage, new File(recorder.getFullFilename()));
                LOG.debug(logPrefix + "Copy file finished with success - source: " + newImage.getName() + " destination: " + recorder.getRelativeFilenameURL());

                // Index file created to database.
                object = testCaseExecutionFileFactory.create(0, testCaseExecution.getId(), recorder.getLevel(), "Screenshot", recorder.getRelativeFilenameURL(), "PNG", "", null, "", null);
                testCaseExecutionFileService.save(object);

                //deletes the temporary file
                FileUtils.forceDelete(newImage);
                LOG.debug(logPrefix + "Temp file deleted with success " + newImage.getName());
                LOG.debug(logPrefix + "Screenshot done in : " + recorder.getRelativeFilenameURL());

            } catch (IOException ex) {
                LOG.error(logPrefix + ex.toString());
            } catch (CerberusException ex) {
                LOG.error(logPrefix + ex.toString());
            }
        } else {
            LOG.warn(logPrefix + "Screenshot returned null.");
        }
        return object;
    }

    @Override
    public TestCaseExecutionFile recordPageSource(TestCaseExecution testCaseExecution, TestCaseStepActionExecution testCaseStepActionExecution, Integer control) {
        // Used for logging purposes
        String logPrefix = Infos.getInstance().getProjectNameAndVersion() + " - ";

        LOG.debug(logPrefix + "Starting to save Page Source File.");

        TestCaseExecutionFile object = null;
        String test = testCaseExecution.getTest();
        String testCase = testCaseExecution.getTestCase();
        String step = String.valueOf(testCaseStepActionExecution.getStep());
        String index = String.valueOf(testCaseStepActionExecution.getIndex());
        String sequence = String.valueOf(testCaseStepActionExecution.getSequence());
        String controlString = control.equals(0) ? null : String.valueOf(control);

        try {
            Recorder recorder = this.initFilenames(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getId(), test, testCase, step, index, sequence, controlString, null, 0, "pagesource", "html");
            File dir = new File(recorder.getFullPath());
            dir.mkdirs();

            File file = new File(recorder.getFullFilename());

            FileOutputStream fileOutputStream;
            try {
                fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(this.webdriverService.getPageSource(testCaseExecution.getSession()).getBytes());
                fileOutputStream.close();

                // Index file created to database.
                object = testCaseExecutionFileFactory.create(0, testCaseExecution.getId(), recorder.getLevel(), "Page Source", recorder.getRelativeFilenameURL(), "HTML", "", null, "", null);
                testCaseExecutionFileService.save(object);

            } catch (FileNotFoundException ex) {
                LOG.error(logPrefix + ex.toString());

            } catch (IOException ex) {
                LOG.error(logPrefix + ex.toString());
            }

            LOG.debug(logPrefix + "Page Source file saved in : " + recorder.getRelativeFilenameURL());
        } catch (CerberusException ex) {
            LOG.error(logPrefix + ex.toString());
        }
        return object;
    }

    @Override
    public List<TestCaseExecutionFile> recordServiceCall(TestCaseExecution testCaseExecution, TestCaseStepActionExecution testCaseStepActionExecution, Integer control, AppService se) {
        // Used for logging purposes
        String logPrefix = Infos.getInstance().getProjectNameAndVersion() + " - ";

        List<TestCaseExecutionFile> objectFileList = new ArrayList<TestCaseExecutionFile>();
        TestCaseExecutionFile object = null;
        String test = testCaseExecution.getTest();
        String testCase = testCaseExecution.getTestCase();
        String step = String.valueOf(testCaseStepActionExecution.getStep());
        String index = String.valueOf(testCaseStepActionExecution.getIndex());
        String sequence = String.valueOf(testCaseStepActionExecution.getSequence());
        String controlString = control.equals(0) ? null : String.valueOf(control);
        long runId = testCaseExecution.getId();

        try {

            // Service Call META data information.
            Recorder recorderRequest = this.initFilenames(runId, test, testCase, step, index, sequence, controlString, null, 0, "call", "json");
            recordFile(recorderRequest.getFullPath(), recorderRequest.getFileName(), convertToJSON(se).toString());
            // Index file created to database.
            object = testCaseExecutionFileFactory.create(0, runId, recorderRequest.getLevel(), "Service Call", recorderRequest.getRelativeFilenameURL(), "JSON", "", null, "", null);
            testCaseExecutionFileService.save(object);
            objectFileList.add(object);

            // REQUEST.
            if (!(StringUtil.isNullOrEmpty(se.getServiceRequest()))) {
                String messageFormatExt = "txt";
                String messageFormat = TestCaseExecutionFile.FILETYPE_TXT;
                if (se.getServiceRequest().startsWith("{")) { // TODO find a better solution to guess the format of the request.
                    messageFormatExt = "json";
                    messageFormat = TestCaseExecutionFile.FILETYPE_JSON;
                } else if (se.getServiceRequest().startsWith("<")) {
                    messageFormatExt = "xml";
                    messageFormat = TestCaseExecutionFile.FILETYPE_XML;
                }
                recorderRequest = this.initFilenames(runId, test, testCase, step, index, sequence, controlString, null, 0, "request", messageFormatExt);
                recordFile(recorderRequest.getFullPath(), recorderRequest.getFileName(), se.getServiceRequest());
                // Index file created to database.
                object = testCaseExecutionFileFactory.create(0, runId, recorderRequest.getLevel(), "Request", recorderRequest.getRelativeFilenameURL(), messageFormat, "", null, "", null);
                testCaseExecutionFileService.save(object);
                objectFileList.add(object);
            }

            // RESPONSE if exists.
            if (!(StringUtil.isNullOrEmpty(se.getResponseHTTPBody()))) {
                String messageFormatExt = "txt";
                String messageFormat = TestCaseExecutionFile.FILETYPE_TXT;

                switch (se.getResponseHTTPBodyContentType()) {
                    case AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON:
                        messageFormatExt = "json";
                        messageFormat = TestCaseExecutionFile.FILETYPE_JSON;
                        break;
                    case AppService.RESPONSEHTTPBODYCONTENTTYPE_XML:
                        messageFormatExt = "xml";
                        messageFormat = TestCaseExecutionFile.FILETYPE_XML;
                        break;
                    default:
                        messageFormatExt = "txt";
                        messageFormat = TestCaseExecutionFile.FILETYPE_TXT;
                        break;
                }

                Recorder recorderResponse = this.initFilenames(runId, test, testCase, step, index, sequence, controlString, null, 0, "response", messageFormatExt);
                recordFile(recorderResponse.getFullPath(), recorderResponse.getFileName(), se.getResponseHTTPBody());

                // Index file created to database.
                object = testCaseExecutionFileFactory.create(0, runId, recorderResponse.getLevel(), "Response", recorderResponse.getRelativeFilenameURL(), messageFormat, "", null, "", null);
                testCaseExecutionFileService.save(object);
                objectFileList.add(object);
            }

        } catch (CerberusException ex) {
            LOG.error(logPrefix + ex.toString());
        }
        return objectFileList;
    }

    private JSONObject convertToJSON(AppService se) {
        JSONObject jsonResponse = new JSONObject();
        JSONObject jsonMyRequest = new JSONObject();
        JSONObject jsonMyResponse = new JSONObject();
        try {
            // Request Information.
            jsonMyRequest.put("CalledURL", se.getServicePath());
            jsonMyRequest.put("HTTP-Method", se.getMethod());
            jsonMyRequest.put("ServiceType", se.getType());
            if (!(se.getHeaderList().isEmpty())) {
                JSONObject jsonHeaders = new JSONObject();
                for (AppServiceHeader header : se.getHeaderList()) {
                    jsonHeaders.put(header.getKey(), header.getValue());
                }
                jsonMyRequest.put("Header", jsonHeaders);
            }
            if (!(se.getContentList().isEmpty())) {
                JSONObject jsonContent = new JSONObject();
                for (AppServiceContent content : se.getContentList()) {
                    jsonContent.put(content.getKey(), content.getValue());
                }
                jsonMyRequest.put("Content", jsonContent);
            }
            jsonMyRequest.put("HTTP-Request", se.getServiceRequest());
            jsonResponse.put("Request", jsonMyRequest);

            // Response Information.
            jsonMyResponse.put("HTTP-ReturnCode", se.getResponseHTTPCode());
            jsonMyResponse.put("HTTP-Version", se.getResponseHTTPVersion());
            jsonMyResponse.put("HTTP-ResponseBody", se.getResponseHTTPBody());
            jsonMyResponse.put("HTTP-ResponseContentType", se.getResponseHTTPBodyContentType());
            if (!(se.getResponseHeaderList().isEmpty())) {
                JSONObject jsonHeaders = new JSONObject();
                for (AppServiceHeader header : se.getResponseHeaderList()) {
                    jsonHeaders.put(header.getKey(), header.getValue());
                }
                jsonMyResponse.put("Header", jsonHeaders);
            }
            jsonResponse.put("Response", jsonMyResponse);

        } catch (JSONException ex) {
            Logger.getLogger(RecorderService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jsonResponse;
    }

    @Override
    public TestCaseExecutionFile recordTestDataLibProperty(Long runId, String property, int propertyIndex, List<HashMap<String, String>> result) {
        TestCaseExecutionFile object = null;
        // Used for logging purposes
        String logPrefix = Infos.getInstance().getProjectNameAndVersion() + " - ";

        try {
            JSONArray jsonResult = null;
            jsonResult = dataLibService.convertToJSONObject(result);

            // RESULT.
            Recorder recorder = this.initFilenames(runId, null, null, null, null, null, null, property, propertyIndex, "result", "json");
            recordFile(recorder.getFullPath(), recorder.getFileName(), jsonResult.toString());

            // Index file created to database.
            object = testCaseExecutionFileFactory.create(0, runId, recorder.getLevel(), "Result", recorder.getRelativeFilenameURL(), "JSON", "", null, "", null);
            testCaseExecutionFileService.save(object);

        } catch (CerberusException | JSONException ex) {
            LOG.error(logPrefix + "TestDataLib file was not saved due to unexpected error." + ex.toString());
        }
        return object;
    }

    @Override
    public TestCaseExecutionFile recordSeleniumLog(TestCaseExecution testCaseExecution) {
        TestCaseExecutionFile object = null;
        // Used for logging purposes
        String logPrefix = Infos.getInstance().getProjectNameAndVersion() + " - ";

        if (testCaseExecution.getApplicationObj().getType().equals(Application.TYPE_GUI)) {

            if (testCaseExecution.getSeleniumLog() == 2 || (testCaseExecution.getSeleniumLog() == 1 && !testCaseExecution.getControlStatus().equals("OK"))) {
                LOG.debug(logPrefix + "Starting to save Selenium log file.");

                try {
                    Recorder recorder = this.initFilenames(testCaseExecution.getId(), null, null, null, null, null, null, null, 0, "selenium_log", "txt");

                    File dir = new File(recorder.getFullPath());
                    dir.mkdirs();

                    File file = new File(recorder.getFullFilename());

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
                        object = testCaseExecutionFileFactory.create(0, testCaseExecution.getId(), recorder.getLevel(), "Selenium log", recorder.getRelativeFilenameURL(), "TXT", "", null, "", null);
                        testCaseExecutionFileService.save(object);

                    } catch (FileNotFoundException ex) {
                        LOG.error(logPrefix + ex.toString());
                    } catch (IOException ex) {
                        LOG.error(logPrefix + ex.toString());
                    }

                    LOG.debug(logPrefix + "Selenium log recorded in : " + recorder.getRelativeFilenameURL());

                } catch (CerberusException ex) {
                    LOG.error(logPrefix + ex.toString());
                }
            }
        } else {
            LOG.debug(logPrefix + "Selenium Log not recorded because test on non GUI application");
        }
        return object;
    }

    @Override
    public void recordUploadedFile(long executionId, TestCaseStepActionExecution tcsae, FileItem uploadedFile) {
        String UploadedfileName = new File(uploadedFile.getName()).getName();

        try {
            // UPLOADED File.
            Recorder recorder = this.initFilenames(executionId, tcsae.getTest(), tcsae.getTestCase(), String.valueOf(tcsae.getStep()), String.valueOf(tcsae.getIndex()), String.valueOf(tcsae.getSequence()), null, null, 0, "image", "jpg");
            File storeFile = new File(recorder.getFullFilename());
            // saves the file on disk
            uploadedFile.write(storeFile);

            // Index file created to database.
            testCaseExecutionFileService.save(executionId, recorder.getLevel(), "Image", recorder.getRelativeFilenameURL(), "JPG", "");

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
        LOG.info("Starting to save File - recordFile.");

        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(dir.getAbsolutePath() + File.separator + fileName);
            fileOutputStream.write(content.getBytes());
            fileOutputStream.close();
            LOG.debug("File saved : " + path + File.separator + fileName);
        } catch (FileNotFoundException ex) {
            LOG.debug("Unable to save : " + path + File.separator + fileName + " ex: " + ex);
        } catch (IOException ex) {
            LOG.debug("Unable to save : " + path + File.separator + fileName + " ex: " + ex);
        }
    }

    private Recorder initFilenames(long exeID, String test, String testCase, String step, String index, String sequence, String controlString, String property, int propertyIndex, String filename, String extention) throws CerberusException {

        Recorder newRecorder = new Recorder();

        /**
         * Root folder initialisation. The root folder is confugures from the
         * parameter cerberus_mediastorage_path.
         */
        String rootFolder = parameterService.findParameterByKey("cerberus_mediastorage_path", "").getValue();
        rootFolder = StringUtil.addSuffixIfNotAlready(rootFolder, File.separator);
        newRecorder.setRootFolder(rootFolder);

        /**
         * SubFolder. Subfolder is split in order to reduce the nb of folder
         * within a folder. 2 levels of 2 digits each. he last level is the
         * execution id.
         */
        String subFolder = getStorageSubFolder(exeID);
        newRecorder.setSubFolder(subFolder);
        String subFolderURL = getStorageSubFolderURL(exeID);
        newRecorder.setSubFolder(subFolderURL);

        /**
         * FullPath. Concatenation of the rootfolder and subfolder.
         */
        String fullPath = rootFolder + subFolder;
        newRecorder.setFullPath(fullPath);

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
        if (!StringUtil.isNullOrEmpty(index)) {
            sbfileName.append("I").append(index).append("-");
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
        String fileName = StringUtil.removeLastChar(sbfileName.toString(), 1) + "." + extention;
        fileName = fileName.replace(" ", "");
        newRecorder.setFileName(fileName);

        /**
         * Level. 5 levels possible. Keys are defined seperated by -. 1/
         * Execution level --> emptyString. 2/ Step level --> test+testcase+Step
         * 3/ Action level --> test+testcase+Step+action 4/ Control level -->
         * test+testcase+Step+action+control 5/ Property level -->
         * property+index
         */
        String level = "";
        if (!(StringUtil.isNullOrEmpty(controlString))) {
            level = test + "-" + testCase + "-" + step + "-" + index + "-" + sequence + "-" + controlString;
        } else if (!(StringUtil.isNullOrEmpty(sequence))) {
            level = test + "-" + testCase + "-" + step + "-" + index + "-" + sequence;
        } else if (!(StringUtil.isNullOrEmpty(step))) {
            level = test + "-" + testCase + "-" + step + "-" + index;
        } else if (!(StringUtil.isNullOrEmpty(property))) {
            level = property + "-" + propertyIndex;
        }
        newRecorder.setLevel(level);

        /**
         * Final Filename with full path.
         */
        String fullFilename = rootFolder + File.separator + subFolder + File.separator + fileName;
        newRecorder.setFullFilename(fullFilename);
        String relativeFilenameURL = subFolderURL + "/" + fileName;
        newRecorder.setRelativeFilenameURL(relativeFilenameURL);

        return newRecorder;
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
