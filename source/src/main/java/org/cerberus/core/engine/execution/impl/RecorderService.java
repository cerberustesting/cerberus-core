/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.engine.execution.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.RobotCapability;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseExecutionFile;
import org.cerberus.core.crud.entity.TestCaseStepActionControlExecution;
import org.cerberus.core.crud.entity.TestCaseStepActionExecution;
import org.cerberus.core.crud.factory.IFactoryTestCaseExecutionFile;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestCaseExecutionFileService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.Recorder;
import org.cerberus.core.engine.execution.IRecorderService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.Screenshot;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.service.datalib.IDataLibService;
import org.cerberus.core.service.robotextension.ISikuliService;
import org.cerberus.core.service.webdriver.IWebDriverService;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.version.Infos;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriverException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.cerberus.core.engine.entity.ExecutionLog;

/**
 * @author bcivel
 */
@Service
public class RecorderService implements IRecorderService {

    @Autowired
    IParameterService parameterService;
    @Autowired
    ITestCaseExecutionFileService testCaseExecutionFileService;
    @Autowired
    IWebDriverService webdriverService;
    @Autowired
    ISikuliService sikuliService;
    @Autowired
    IDataLibService dataLibService;
    @Autowired
    private IFactoryTestCaseExecutionFile testCaseExecutionFileFactory;

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(RecorderService.class);

    @Override
    public List<TestCaseExecutionFile> recordExecutionInformationAfterStepActionAndControl(TestCaseStepActionExecution actionExecution, TestCaseStepActionControlExecution controlExecution) {
        List<TestCaseExecutionFile> objectFileList = new ArrayList<>();

        // Used for logging purposes
        String logPrefix = Infos.getInstance().getProjectNameAndVersion() + " - ";

        TestCaseExecution myExecution;
        boolean doScreenshot;
        boolean doScreenshotAfter;
        boolean getPageSource;
        String applicationType;
        String returnCode;
        Integer controlNumber = -1;

        if (controlExecution == null) {
            myExecution = actionExecution.getTestCaseStepExecution().gettCExecution();
            doScreenshot = actionExecution.getActionResultMessage().isDoScreenshot();
            doScreenshotAfter = actionExecution.isDoScreenshotAfter();
            getPageSource = actionExecution.getActionResultMessage().isGetPageSource();
            applicationType = actionExecution.getTestCaseStepExecution().gettCExecution().getAppTypeEngine();
            returnCode = actionExecution.getReturnCode();
        } else {
            myExecution = controlExecution.getTestCaseStepActionExecution().getTestCaseStepExecution().gettCExecution();
            doScreenshot = controlExecution.getControlResultMessage().isDoScreenshot();
            doScreenshotAfter = controlExecution.isDoScreenshotAfter();
            getPageSource = controlExecution.getControlResultMessage().isGetPageSource();
            applicationType = controlExecution.getTestCaseStepActionExecution().getTestCaseStepExecution().gettCExecution().getAppTypeEngine();
            returnCode = controlExecution.getReturnCode();
            controlNumber = controlExecution.getControlId();
        }

        /*
         * SCREENSHOT Management. Screenshot only done when : screenshot
         * parameter is eq to 2 or screenshot parameter is eq to 1 with the
         * correct doScreenshot flag on the last action MessageEvent.
         */
        if (Screenshot.printScreenSystematicaly(myExecution.getScreenshot())
                || (Screenshot.printScreenOnError(myExecution.getScreenshot()) && (doScreenshot))
                || (doScreenshotAfter)) {
            if (applicationType.equals(Application.TYPE_GUI)
                    || applicationType.equals(Application.TYPE_APK)
                    || applicationType.equals(Application.TYPE_IPA)
                    || applicationType.equals(Application.TYPE_FAT)) {
                /*
                 * Only if the return code is not equal to Cancel, meaning lost
                 * connectivity with selenium.
                 */
                if (!returnCode.equals("CA")) {
                    objectFileList.addAll(this.recordScreenshot(myExecution, actionExecution, controlNumber, "", "Screenshot After", "screenshot-after"));
                } else {
                    LOG.debug("{}Not Doing screenshot because connectivity with selenium server lost.", logPrefix);
                }

            }
        } else {
            LOG.debug("{}Not Doing screenshot because of the screenshot parameter or flag on the last Action result.", logPrefix);
        }

        // PAGESOURCE management. Get PageSource if requested by the last Action MessageEvent.
        if ((myExecution.getPageSource() == 2) || ((myExecution.getPageSource() == 1) && (getPageSource))) {

            if (applicationType.equals(Application.TYPE_GUI)
                    || applicationType.equals(Application.TYPE_APK)
                    || applicationType.equals(Application.TYPE_IPA)) {
                /*
                 * Only if the return code is not equal to Cancel, meaning lost
                 * connectivity with selenium.
                 */
                if (!returnCode.equals("CA")) {
                    objectFileList.add(this.recordPageSource(myExecution, actionExecution, controlNumber));
                } else {
                    LOG.debug("{}Not getting page source because connectivity with selenium server lost.", logPrefix);
                }
            }
        } else {
            LOG.debug("{}Not getting page source because of the pageSource parameter or flag on the last Action result.", logPrefix);
        }

        // Last call XML SOURCE management. Get Source of the XML if requested by the last Action or control MessageEvent.
        if (applicationType.equals(Application.TYPE_SRV)
                && ((myExecution.getPageSource() == 2) || ((myExecution.getPageSource() == 1) && (getPageSource))
                || (myExecution.getScreenshot() == 2) || ((myExecution.getScreenshot() == 1) && (doScreenshot)))) {
            //Record the Request and Response.
            AppService se = actionExecution.getTestCaseStepExecution().gettCExecution().getLastServiceCalled();
            if (se != null) { // No Calls were performed previously
                List<TestCaseExecutionFile> objectFileSOAPList = new ArrayList<>();
                objectFileSOAPList = this.recordServiceCall(myExecution, actionExecution, controlNumber, null, se);
                if (!objectFileSOAPList.isEmpty()) {
                    for (TestCaseExecutionFile testCaseExecutionFile : objectFileSOAPList) {
                        objectFileList.add(testCaseExecutionFile);
                    }
                }
            }
        }

        return objectFileList;
    }

    @Override
    public List<TestCaseExecutionFile> recordExecutionInformationBeforeStepActionAndControl(TestCaseStepActionExecution actionExecution, TestCaseStepActionControlExecution controlExecution) {
        List<TestCaseExecutionFile> objectFileList = new ArrayList<>();

        // Used for logging purposes
        String logPrefix = Infos.getInstance().getProjectNameAndVersion() + " - ";

        TestCaseExecution myExecution;
        boolean doScreenshot;
        boolean doScreenshotBefore;
        boolean getPageSource;
        String applicationType;
        String returnCode;
        Integer controlNumber = -1;

        if (controlExecution == null) {
            myExecution = actionExecution.getTestCaseStepExecution().gettCExecution();
            doScreenshot = actionExecution.getActionResultMessage().isDoScreenshot();
            doScreenshotBefore = actionExecution.isDoScreenshotBefore();
            getPageSource = actionExecution.getActionResultMessage().isGetPageSource();
            applicationType = actionExecution.getTestCaseStepExecution().gettCExecution().getAppTypeEngine();
            returnCode = actionExecution.getReturnCode();
        } else {
            myExecution = controlExecution.getTestCaseStepActionExecution().getTestCaseStepExecution().gettCExecution();
            doScreenshot = controlExecution.getControlResultMessage().isDoScreenshot();
            doScreenshotBefore = controlExecution.isDoScreenshotBefore();
            getPageSource = controlExecution.getControlResultMessage().isGetPageSource();
            applicationType = controlExecution.getTestCaseStepActionExecution().getTestCaseStepExecution().gettCExecution().getAppTypeEngine();
            returnCode = controlExecution.getReturnCode();
            controlNumber = controlExecution.getControlId();
        }

        /*
         * SCREENSHOT Management. Screenshot only done when : screenshot
         * parameter is eq to 2 or screenshot parameter is eq to 1 with the
         * correct doScreenshot flag on the last action MessageEvent.
         */
        if (doScreenshotBefore) {
            if (applicationType.equals(Application.TYPE_GUI)
                    || applicationType.equals(Application.TYPE_APK)
                    || applicationType.equals(Application.TYPE_IPA)
                    || applicationType.equals(Application.TYPE_FAT)) {
                /*
                 * Only if the return code is not equal to Cancel, meaning lost
                 * connectivity with selenium.
                 */
                if (!returnCode.equals("CA")) {
                    objectFileList.addAll(this.recordScreenshot(myExecution, actionExecution, controlNumber, "", "Screenshot Before", "screenshot-before"));
                } else {
                    LOG.debug("{}Not Doing screenshot because connectivity with selenium server lost.", logPrefix);
                }

            }
        } else {
            LOG.debug("{}Not Doing screenshot because of the screenshot parameter or flag on the last Action result.", logPrefix);
        }

        return objectFileList;
    }

    @Override
    public AnswerItem<TestCaseExecutionFile> recordManuallyFile(TestCaseStepActionExecution actionExecution, TestCaseStepActionControlExecution controlExecution,
            String extension, String desc, FileItem file, Integer id, String fileName, Integer fileID) {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION", "Can't upload file");
        AnswerItem<TestCaseExecutionFile> a = new AnswerItem<>();
        TestCaseExecutionFile object = null;
        String returnCode;
        Integer controlNumber = 0;
        String test = "";
        String testCase = "";
        String step = "";
        String index = "";
        String sequence = "";
        String controlString = "";
        Integer myExecution = id;

        if (controlExecution == null) {
            test = actionExecution.getTest();
            testCase = actionExecution.getTestCase();
            step = String.valueOf(actionExecution.getStepId());
            index = String.valueOf(actionExecution.getIndex());
            sequence = String.valueOf(actionExecution.getSequence());
            controlString = null;
            returnCode = actionExecution.getReturnCode();
        } else {
            returnCode = controlExecution.getReturnCode();
            controlNumber = controlExecution.getControlId();
            test = controlExecution.getTest();
            testCase = controlExecution.getTestCase();
            step = String.valueOf(controlExecution.getStepId());
            index = String.valueOf(controlExecution.getIndex());
            sequence = String.valueOf(controlExecution.getActionId());
            controlString = String.valueOf(controlNumber);
        }
        // Used for logging purposes
        String logPrefix = Infos.getInstance().getProjectNameAndVersion() + " - [" + test + " - " + testCase + " - step: " + step + " action: " + sequence + "] ";
        try {
            Recorder recorder = new Recorder();
            String name = "";
            File dir = null;
            if (file != null) {
                name = file.getName();
                extension = testCaseExecutionFileService.checkExtension(name, extension);
                recorder = this.initFilenames(myExecution, test, testCase, step, index, sequence, controlString, null, 0, name, extension, true);
                dir = new File(recorder.getFullPath());
            } else {
                name = fileName;
                extension = testCaseExecutionFileService.checkExtension(name, extension);

                if (name.contains(".")) {
                    recorder = this.initFilenames(myExecution, test, testCase, step, index, sequence, controlString, null, 0, name.substring(0, name.lastIndexOf('.')), extension, true);
                    dir = new File(recorder.getFullPath());
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "manual testcase execution file")
                            .replace("%OPERATION%", "Create")
                            .replace("%REASON%", "file is missing!"));
                    a.setResultMessage(msg);
                    return a;
                }
            }
            if (!dir.exists()) {
                try {
                    boolean isCreated = dir.mkdirs();
                    if (!isCreated) {
                        throw new SecurityException();
                    }
                } catch (SecurityException se) {
                    LOG.warn("Unable to create manual execution file dir: {}", se.getMessage());
                    msg = new MessageEvent(MessageEventEnum.FILE_ERROR).resolveDescription("DESCRIPTION",
                            se.toString()).resolveDescription("MORE", "Please check the parameter cerberus_exemanualmedia_path");
                    a.setResultMessage(msg);
                    return a;
                }
            }
            if (file != null) {
                AnswerItem<TestCaseExecutionFile> current = testCaseExecutionFileService.readByKey(myExecution, recorder.getLevel(), desc);
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                if (current.getItem() != null) {
                    try {
                        File temp = new File(recorder.getRootFolder() + current.getItem().getFileName());
                        temp.delete();
                    } catch (SecurityException se) {
                        LOG.warn("Unable to create manual execution file dir: {}", se.getMessage());
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                                se.toString());
                    }
                }
                try {
                    file.write(new File(recorder.getFullFilename()));
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("DESCRIPTION",
                            "Manual Execution File uploaded");
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "Manual Execution File").replace("%OPERATION%", "Upload"));
                    LOG.debug("{}Copy file finished with success - source: {} destination: {}", logPrefix, file.getName(), recorder.getRelativeFilenameURL());
                    object = testCaseExecutionFileFactory.create(fileID, myExecution, recorder.getLevel(), desc, recorder.getRelativeFilenameURL(), extension, "", null, "", null);
                } catch (Exception e) {
                    LOG.warn("Unable to upload Manual Execution File: {}", e.getMessage());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                            e.toString());
                }
            } else {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("DESCRIPTION",
                        "Manual Execution File updated");
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Manual Execution File").replace("%OPERATION%", "updated"));
                LOG.debug("{}Updated test case manual file finished with success", logPrefix);
                object = testCaseExecutionFileFactory.create(fileID, myExecution, recorder.getLevel(), desc, name, extension, "", null, "", null);
            }
            testCaseExecutionFileService.saveManual(object);

        } catch (CerberusException e) {
            LOG.error("{} {}", logPrefix, e.toString(), e);
        }
        a.setResultMessage(msg);
        a.setItem(object);
        return a;
    }

    @Override
    public List<TestCaseExecutionFile> recordScreenshot(TestCaseExecution execution, TestCaseStepActionExecution actionExecution, Integer control, String cropValues, String fileDescription, String fileName) {

        List<TestCaseExecutionFile> objectList = new ArrayList<>();
        TestCaseExecutionFile object = null;

        String test = actionExecution.getTest();
        String testCase = actionExecution.getTestCase();
        String step = String.valueOf(actionExecution.getStepId());
        String index = String.valueOf(actionExecution.getIndex());
        String sequence = String.valueOf(actionExecution.getSequence());
        String controlString = (control < 0) ? null : String.valueOf(control);
        long runId = execution.getId();
        String applicationType = execution.getAppTypeEngine();

        // Used for logging purposes
        String logPrefix = Infos.getInstance().getProjectNameAndVersion() + " - [" + test + " - " + testCase + " - step: " + step + " action: " + sequence + "] ";

        LOG.debug("{}Doing screenshot.", logPrefix);

        // Take Screenshot and write it
        File newImage = null;
        File newImageDesktop = null;
        if (applicationType.equals(Application.TYPE_GUI)
                || applicationType.equals(Application.TYPE_APK)
                || applicationType.equals(Application.TYPE_IPA)) {

            newImage = this.webdriverService.takeScreenShotFile(execution.getSession(), cropValues);
            // If Sikuli is available we also take a full desktop screenshot
            if (execution.getSession().isSikuliAvailable()) {
                newImageDesktop = this.sikuliService.takeScreenShotFile(execution.getSession());
            }
        } else if (applicationType.equals(Application.TYPE_FAT)) {

            newImage = this.sikuliService.takeScreenShotFile(execution.getSession());
        }

        long maxSizeParam = parameterService.getParameterIntegerByKey("cerberus_screenshot_max_size", "", 1048576);
        if (newImage != null) {
            try {

                Recorder recorder = this.initFilenames(runId, test, testCase, step, index, sequence, controlString, null, 0, fileName, "png", false);
                LOG.debug("{}FullPath {}", logPrefix, recorder.getFullPath());

                File dir = new File(recorder.getFullPath());
                if (!dir.exists()) {
                    LOG.debug("{}Create directory for execution {}", logPrefix, recorder.getFullPath());
                    dir.mkdirs();
                }
                // Getting the max size of the screenshot.
                String fileDesc = fileDescription;
                if (maxSizeParam < newImage.length()) {
                    LOG.warn("{}Screenshot size exceeds the maximum defined in configurations ({}>={}) {} destination: {}", logPrefix, newImage.length(), maxSizeParam, newImage.getName(), recorder.getRelativeFilenameURL());
                    fileDesc = "Screenshot Too Big !!";
                } else {
                    // Copies the temp file to the execution file
                    FileUtils.copyFile(newImage, new File(recorder.getFullFilename()));
                    LOG.debug("{}Copy file finished with success - source: {} destination: {}", logPrefix, newImage.getName(), recorder.getRelativeFilenameURL());
                    LOG.info("File saved : {}", recorder.getFullFilename());
                }

                // Index file created to database.
                object = testCaseExecutionFileFactory.create(0, execution.getId(), recorder.getLevel(), fileDesc, recorder.getRelativeFilenameURL(), "PNG", "", null, "", null);
                objectList.add(object);
                testCaseExecutionFileService.save(object);

                //deletes the temporary file
                FileUtils.forceDelete(newImage);
                LOG.debug("{}Temp file deleted with success {}", logPrefix, newImage.getName());
                LOG.debug("{}Screenshot done in : {}", logPrefix, recorder.getRelativeFilenameURL());

            } catch (IOException | CerberusException ex) {
                LOG.error("{}{}", logPrefix, ex.toString(), ex);
            }
        } else {
            LOG.warn("{}Screenshot returned null.", logPrefix);
        }

        if (newImageDesktop != null) {

            try {

                Recorder recorderDestop = this.initFilenames(runId, test, testCase, step, index, sequence, controlString, null, 0, fileName + "-desktop", "png", false);
                LOG.debug("{}FullPath {}", logPrefix, recorderDestop.getFullPath());

                // Getting the max size of the screenshot.
                String fileDesc = "Desktop " + fileDescription;
                if (maxSizeParam < newImageDesktop.length()) {
                    LOG.warn("{}Screenshot size exceeds the maximum defined in configurations ({}>={}) {} destination: {}", logPrefix, newImageDesktop.length(), maxSizeParam, newImageDesktop.getName(), recorderDestop.getRelativeFilenameURL());
                    fileDesc = "Desktop Screenshot Too Big !!";
                } else {
                    // Copies the temp file to the execution file
                    FileUtils.moveFile(newImageDesktop, new File(recorderDestop.getFullFilename()));
                    LOG.debug("{}Moving file finished with success - source: {} destination: {}", logPrefix, newImageDesktop.getName(), recorderDestop.getRelativeFilenameURL());
                    LOG.info("File saved : {}", recorderDestop.getFullFilename());
                }

                // Index file created to database.
                object = testCaseExecutionFileFactory.create(0, execution.getId(), recorderDestop.getLevel(), fileDesc, recorderDestop.getRelativeFilenameURL(), "PNG", "", null, "", null);
                objectList.add(object);
                testCaseExecutionFileService.save(object);

                //deletes the temporary file
                LOG.debug("{}Desktop Screenshot done in : {}", logPrefix, recorderDestop.getRelativeFilenameURL());
            } catch (IOException | CerberusException ex) {
                LOG.error("{}{}", logPrefix, ex.toString(), ex);
            }

        }

        return objectList;
    }

    @Override
    public TestCaseExecutionFile recordPicture(TestCaseStepActionExecution actionExecution, Integer control, String locator, String valueFieldName) {

        TestCaseExecutionFile object = null;

        String test = actionExecution.getTest();
        String testCase = actionExecution.getTestCase();
        String step = String.valueOf(actionExecution.getStepId());
        String index = String.valueOf(actionExecution.getIndex());
        String sequence = String.valueOf(actionExecution.getSequence());
        String controlString = (control < 0) ? null : String.valueOf(control);
        long runId = actionExecution.getId();
        String extension = "";

        LOG.debug("Saving picture.");

        //Take Screenshot and write it
        try {

            URL url = new URL(locator);
            URLConnection connection = url.openConnection();

            InputStream istream = new BufferedInputStream(connection.getInputStream());
//            String mimeType = URLConnection.guessContentTypeFromStream(istream);
//            MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
//            MimeType mt = allTypes.forName(mimeType);
//            extension = mt.getExtension();
            byte[] bytes = IOUtils.toByteArray(istream);

            Recorder recorder = this.initFilenames(runId, test, testCase, step, index, sequence, controlString, null, 0, "picture" + valueFieldName, "png", false);

            LOG.debug("Picture FullPath {}", recorder.getFullPath());

            File dir = new File(recorder.getFullPath());
            if (!dir.exists()) {
                LOG.debug("Create directory for execution {}", recorder.getFullPath());
                dir.mkdirs();
            }
            File newImage = new File(recorder.getFullFilename());
            OutputStream outStream = new FileOutputStream(newImage);
            outStream.write(bytes);
            outStream.close();
//            IOUtils.close(outStream);

            // Index file created to database.
            object = testCaseExecutionFileFactory.create(0, runId, recorder.getLevel(), "Picture " + valueFieldName, recorder.getRelativeFilenameURL(), "PNG", "", null, "", null);
            testCaseExecutionFileService.save(object);

            //deletes the temporary file
            LOG.debug("Picture saved to : {}", recorder.getRelativeFilenameURL());

        } catch (IOException | CerberusException ex) {
            LOG.error("{}", ex.toString(), ex);
        }
        return object;
    }

    @Override
    public TestCaseExecutionFile recordExeLog(TestCaseExecution execution) {
        LOG.debug("Starting to save Execution log File.");

        TestCaseExecutionFile object = null;
        if ((execution != null) && (execution.getExecutionLog() != null)) {
            String test = execution.getTest();
            String testCase = execution.getTestCase();

            try {
                Recorder recorder = this.initFilenames(execution.getId(), test, testCase, null, null, null, null, null, 0, "exeLog", "log", false);
                File dir = new File(recorder.getFullPath());
                dir.mkdirs();

                File file = new File(recorder.getFullFilename());

                try (FileOutputStream fileOutputStream = new FileOutputStream(file);) {
                    for (ExecutionLog executionLog : execution.getExecutionLog()) {
                        fileOutputStream.write(executionLog.getLogText().getBytes());
                        fileOutputStream.write("\r\n".getBytes());
                        LOG.debug(executionLog.getLogText());
                    }

                    LOG.info("File saved : {}", recorder.getFullFilename());

                    // Index file created to database.
                    object = testCaseExecutionFileFactory.create(0, execution.getId(), recorder.getLevel(), "Execution Log", recorder.getRelativeFilenameURL(), "TXT", "", null, "", null);
                    testCaseExecutionFileService.save(object);

                } catch (IOException ex) {
                    LOG.error(ex.toString(), ex);
                } catch (WebDriverException ex) {
                    LOG.debug("Exception recording execution log on execution : {}", execution.getId(), ex);
                    object = testCaseExecutionFileFactory.create(0, execution.getId(), recorder.getLevel(), "Execution Log [ERROR]", recorder.getRelativeFilenameURL(), "TXT", "", null, "", null);
                    testCaseExecutionFileService.save(object);
                }
            } catch (CerberusException ex) {
                LOG.error(ex.toString(), ex);
            }
        }
        return object;

    }

    @Override
    public TestCaseExecutionFile recordPageSource(TestCaseExecution execution, TestCaseStepActionExecution actionExecution, Integer control) {
        LOG.debug("Starting to save Page Source File.");

        TestCaseExecutionFile object = null;
        String test = execution.getTest();
        String testCase = execution.getTestCase();
        String step = String.valueOf(actionExecution.getStepId());
        String index = String.valueOf(actionExecution.getIndex());
        String sequence = String.valueOf(actionExecution.getSequence());
        String controlString = (control < 0) ? null : String.valueOf(control);

        try {
            Recorder recorder = this.initFilenames(actionExecution.getTestCaseStepExecution().gettCExecution().getId(), test, testCase, step, index, sequence, controlString, null, 0, "pagesource", "html", false);
            File dir = new File(recorder.getFullPath());
            dir.mkdirs();

            File file = new File(recorder.getFullFilename());

            try (FileOutputStream fileOutputStream = new FileOutputStream(file);) {
                fileOutputStream.write(StringUtil.secureFromSecrets(this.webdriverService.getPageSource(execution.getSession()), execution.getSecrets()).getBytes());

                LOG.info("File saved : {}", recorder.getFullFilename());

                // Index file created to database.
                object = testCaseExecutionFileFactory.create(0, execution.getId(), recorder.getLevel(), "Page Source", recorder.getRelativeFilenameURL(), "HTML", "", null, "", null);
                testCaseExecutionFileService.save(object);

            } catch (IOException ex) {
                LOG.error(ex.toString(), ex);
            } catch (WebDriverException ex) {
                LOG.debug("Exception recording Page Source on execution : {}", execution.getId(), ex);
                object = testCaseExecutionFileFactory.create(0, execution.getId(), recorder.getLevel(), "Page Source [ERROR]", recorder.getRelativeFilenameURL(), "HTML", "", null, "", null);
                testCaseExecutionFileService.save(object);
            }
        } catch (CerberusException ex) {
            LOG.error(ex.toString(), ex);
        }
        return object;
    }

    @Override
    public List<TestCaseExecutionFile> recordServiceCall(TestCaseExecution execution, TestCaseStepActionExecution actionExecution, Integer control, String property, AppService service) {
        // Used for logging purposes
        String logPrefix = Infos.getInstance().getProjectNameAndVersion() + " - ";

        List<TestCaseExecutionFile> objectFileList = new ArrayList<>();

        if (!service.isRecordTraceFile()) {
            return objectFileList;
        }
        TestCaseExecutionFile object = null;
        String test = null;
        String testCase = null;
        String step = null;
        String index = null;
        String sequence = null;
        if (actionExecution != null) {
            test = execution.getTest();
            testCase = execution.getTestCase();
            step = String.valueOf(actionExecution.getStepId());
            index = String.valueOf(actionExecution.getIndex());
            sequence = String.valueOf(actionExecution.getSequence());
        }
        String controlString = control.equals(0) ? null : String.valueOf(control);
        long runId = execution.getId();
        int propertyIndex = 0;
        if (!(StringUtil.isEmptyOrNull(property))) {
            propertyIndex = 1;
        }
        try {

            // Service Call META data information.
            Recorder recorderRequest = this.initFilenames(runId, test, testCase, step, index, sequence, controlString, property, propertyIndex, "call", "json", false);

            recordFile(recorderRequest.getFullPath(), recorderRequest.getFileName(), service.toJSONOnExecution().toString(), execution.getSecrets());

            // Index file created to database.
            object = testCaseExecutionFileFactory.create(0, runId, recorderRequest.getLevel(), "Service Call", recorderRequest.getRelativeFilenameURL(), "JSON", "", null, "", null);
            testCaseExecutionFileService.save(object);
            objectFileList.add(object);

            // REQUEST.
            if (!(StringUtil.isEmptyOrNull(service.getServiceRequest()))) {
                String messageFormatExt = "txt";
                String messageFormat = TestCaseExecutionFile.FILETYPE_TXT;
                if (service.getServiceRequest().startsWith("{")) { // TODO find a better solution to guess the format of the request.
                    messageFormatExt = "json";
                    messageFormat = TestCaseExecutionFile.FILETYPE_JSON;
                } else if (service.getServiceRequest().startsWith("<")) {
                    messageFormatExt = "xml";
                    messageFormat = TestCaseExecutionFile.FILETYPE_XML;
                }
                recorderRequest = this.initFilenames(runId, test, testCase, step, index, sequence, controlString, property, propertyIndex, "request", messageFormatExt, false);
                recordFile(recorderRequest.getFullPath(), recorderRequest.getFileName(), service.getServiceRequest(), execution.getSecrets());
                // Index file created to database.
                object = testCaseExecutionFileFactory.create(0, runId, recorderRequest.getLevel(), "Request", recorderRequest.getRelativeFilenameURL(), messageFormat, "", null, "", null);
                testCaseExecutionFileService.save(object);
                objectFileList.add(object);
            }
            // RESPONSE if exists.
            if (!(StringUtil.isEmptyOrNull(service.getResponseHTTPBody()))) {
                String messageFormatExt = "txt";
                String messageFormat = TestCaseExecutionFile.FILETYPE_TXT;
                switch (service.getResponseHTTPBodyContentType()) {
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
                Recorder recorderResponse = this.initFilenames(runId, test, testCase, step, index, sequence, controlString, property, propertyIndex, "response", messageFormatExt, false);
                recordFile(recorderResponse.getFullPath(), recorderResponse.getFileName(), service.getResponseHTTPBody(), execution.getSecrets());

                // Index file created to database.
                object = testCaseExecutionFileFactory.create(0, runId, recorderResponse.getLevel(), "Response", recorderResponse.getRelativeFilenameURL(), messageFormat, "", null, "", null);
                testCaseExecutionFileService.save(object);
                objectFileList.add(object);

            } else if (service.getFile() != null) {
                Recorder recorderResponse = this.initFilenames(runId, test, testCase, step, index, sequence, controlString, property, propertyIndex, "response", service.getResponseHTTPBodyContentType().toLowerCase(), false);
                File file = new File(recorderResponse.getFullPath());
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(new File(file.getAbsolutePath() + File.separator + recorderResponse.getFileName())));
                InputStream ftpFile = new ByteArrayInputStream(service.getFile());
                IOUtils.copy(ftpFile, outputStream);
                outputStream.close();
                ftpFile.close();
                service.setFile(null);
                // Index file created to database.
                object = testCaseExecutionFileFactory.create(0, runId, recorderResponse.getLevel(), "Response", recorderResponse.getRelativeFilenameURL(), service.getResponseHTTPBodyContentType(), "", null, "", null);
                testCaseExecutionFileService.save(object);
                objectFileList.add(object);
            }
        } catch (Exception ex) {
            LOG.error(logPrefix + ex.toString(), ex);
        }
        return objectFileList;
    }

    @Override
    public List<TestCaseExecutionFile> recordServiceCallContent(TestCaseExecution execution, TestCaseStepActionExecution actionExecution, AppService service) {
        // Used for logging purposes
        String logPrefix = Infos.getInstance().getProjectNameAndVersion() + " - ";

        List<TestCaseExecutionFile> objectFileList = new ArrayList<>();

        if (!service.isRecordTraceFile()) {
            return objectFileList;
        }
        TestCaseExecutionFile object = null;
        String test = null;
        String testCase = null;
        String step = null;
        String index = null;
        String sequence = null;
        if (actionExecution != null) {
            test = execution.getTest();
            testCase = execution.getTestCase();
            step = String.valueOf(actionExecution.getStepId());
            index = String.valueOf(actionExecution.getIndex());
            sequence = String.valueOf(actionExecution.getSequence());
        }
        long runId = execution.getId();

        try {

            // Service Call META data information.
            Recorder recorderRequest = this.initFilenames(runId, test, testCase, step, index, sequence, null, null, 0, "call", "json", false);

            recordFile(recorderRequest.getFullPath(), recorderRequest.getFileName(), service.toJSONOnExecution().toString(), execution.getSecrets());

            // Index file created to database.
            object = testCaseExecutionFileFactory.create(0, runId, recorderRequest.getLevel(), "Service Call", recorderRequest.getRelativeFilenameURL(), "JSON", "", null, "", null);
            testCaseExecutionFileService.save(object);
            objectFileList.add(object);

        } catch (Exception ex) {
            LOG.error("{}{}", logPrefix, ex.toString(), ex);
        }
        return objectFileList;
    }

    @Override
    public List<TestCaseExecutionFile> recordNetworkTrafficContent(TestCaseExecution execution, TestCaseStepActionExecution actionExecution, Integer control, String property, AppService se, boolean withDetail) {
        List<TestCaseExecutionFile> objectFileList = new ArrayList<>();
        TestCaseExecutionFile object = null;
        String test = null;
        String testCase = null;
        String step = null;
        String index = null;
        String sequence = null;
        if (actionExecution != null) {
            test = execution.getTest();
            testCase = execution.getTestCase();
            step = String.valueOf(actionExecution.getStepId());
            index = String.valueOf(actionExecution.getIndex());
            sequence = String.valueOf(actionExecution.getSequence());
        }
        String controlString = control.equals(0) ? null : String.valueOf(control);
        long runId = execution.getId();
        int propertyIndex = 0;
        if (!(StringUtil.isEmptyOrNull(property))) {
            propertyIndex = 1;
        }
        try {

            // Full Network Traffic.
            if (withDetail) {
                if (!(StringUtil.isEmptyOrNull(se.getResponseHTTPBody()))) {
                    Recorder recorderResponse = this.initFilenames(runId, test, testCase, step, index, sequence, controlString, property, propertyIndex, "networktraffic_content", "json", false);
                    recordFile(recorderResponse.getFullPath(), recorderResponse.getFileName(), se.getResponseHTTPBody(), execution.getSecrets());

                    // Index file created to database.
                    object = testCaseExecutionFileFactory.create(0, runId, recorderResponse.getLevel(), "Network Content", recorderResponse.getRelativeFilenameURL(), TestCaseExecutionFile.FILETYPE_JSON, "", null, "", null);
                    testCaseExecutionFileService.save(object);
                    objectFileList.add(object);

                }
            }

            // Stat.
            LOG.debug("Size of HAR message : {}", se.getResponseHTTPBody().length());
            // If JSON Size is higher than 1 Meg, we save the stat.
            if (!(StringUtil.isEmptyOrNull(se.getResponseHTTPBody())) && se.getResponseHTTPBody().length() > 1000000) {
                JSONObject stat = new JSONObject(se.getResponseHTTPBody());
                Recorder recorderResponse = this.initFilenames(runId, test, testCase, step, index, sequence, controlString, property, propertyIndex, "networktraffic_stat", "json", false);
                if (stat.has("stat")) {
                    if (stat.has("log")) {
                        stat.remove("log");
                    }
                    recordFile(recorderResponse.getFullPath(), recorderResponse.getFileName(), stat.toString(1), execution.getSecrets());

                    // Index file created to database.
                    object = testCaseExecutionFileFactory.create(0, runId, recorderResponse.getLevel(), "Stat Only", recorderResponse.getRelativeFilenameURL(), TestCaseExecutionFile.FILETYPE_JSON, "", null, "", null);
                    testCaseExecutionFileService.save(object);
                    objectFileList.add(object);
                } else {
                    LOG.warn("Could not write stat entry of JSON HAR for execution :{}", runId);
                }

            }

        } catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }
        return objectFileList;
    }

    @Override
    public List<TestCaseExecutionFile> recordConsoleContent(TestCaseExecution execution, TestCaseStepActionExecution actionExecution, Integer control, String property, JSONObject consoleContent, boolean withDetail) {
        List<TestCaseExecutionFile> objectFileList = new ArrayList<>();
        TestCaseExecutionFile object = null;
        String test = null;
        String testCase = null;
        String step = null;
        String index = null;
        String sequence = null;
        if (actionExecution != null) {
            test = execution.getTest();
            testCase = execution.getTestCase();
            step = String.valueOf(actionExecution.getStepId());
            index = String.valueOf(actionExecution.getIndex());
            sequence = String.valueOf(actionExecution.getSequence());
        }
        String controlString = control.equals(0) ? null : String.valueOf(control);
        long runId = execution.getId();
        int propertyIndex = 0;
        if (!(StringUtil.isEmptyOrNull(property))) {
            propertyIndex = 1;
        }
        try {

            // Full Network Traffic.
            if (withDetail) {
                Recorder recorderResponse = this.initFilenames(runId, test, testCase, step, index, sequence, controlString, property, propertyIndex, "console_content", "json", false);
                recordFile(recorderResponse.getFullPath(), recorderResponse.getFileName(), consoleContent.toString(1), execution.getSecrets());

                // Index file created to database.
                object = testCaseExecutionFileFactory.create(0, runId, recorderResponse.getLevel(), "Console", recorderResponse.getRelativeFilenameURL(), TestCaseExecutionFile.FILETYPE_JSON, "", null, "", null);
                testCaseExecutionFileService.save(object);
                objectFileList.add(object);

            }

        } catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }
        return objectFileList;
    }

    @Override
    public List<TestCaseExecutionFile> recordContent(TestCaseExecution execution, TestCaseStepActionExecution actionExecution, Integer control, String property, String content, String contentType) {
        List<TestCaseExecutionFile> objectFileList = new ArrayList<>();
        TestCaseExecutionFile object = null;
        String test = null;
        String testCase = null;
        String step = null;
        String index = null;
        String sequence = null;
        if (actionExecution != null) {
            test = execution.getTest();
            testCase = execution.getTestCase();
            step = String.valueOf(actionExecution.getStepId());
            index = String.valueOf(actionExecution.getIndex());
            sequence = String.valueOf(actionExecution.getSequence());
        }
        String controlString = control.equals(0) ? null : String.valueOf(control);
        long runId = execution.getId();
        int propertyIndex = 0;
        if (!(StringUtil.isEmptyOrNull(property))) {
            propertyIndex = 1;
        }
        try {

            Recorder recorderResponse = this.initFilenames(runId, test, testCase, step, index, sequence, controlString, property, propertyIndex, "content", contentType.toLowerCase(), false);
            recordFile(recorderResponse.getFullPath(), recorderResponse.getFileName(), content, execution.getSecrets());

            // Index file created to database.
            object = testCaseExecutionFileFactory.create(0, runId, recorderResponse.getLevel(), "Content", recorderResponse.getRelativeFilenameURL(), contentType, "", null, "", null);
            testCaseExecutionFileService.save(object);
            objectFileList.add(object);

        } catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }
        return objectFileList;
    }

    @Override
    public TestCaseExecutionFile recordRobotFile(TestCaseExecution execution, TestCaseStepActionExecution actionExecution, Integer control, String property, byte[] fileContent, String preFileName, String fileName, String ext) {
        TestCaseExecutionFile object = null;
        String test = null;
        String testCase = null;
        String step = null;
        String index = null;
        String sequence = null;
        if (actionExecution != null) {
            test = execution.getTest();
            testCase = execution.getTestCase();
            step = String.valueOf(actionExecution.getStepId());
            index = String.valueOf(actionExecution.getIndex());
            sequence = String.valueOf(actionExecution.getSequence());
        }
        String controlString = control.equals(0) ? null : String.valueOf(control);
        long runId = execution.getId();
        int propertyIndex = 0;
        if (!(StringUtil.isEmptyOrNull(property))) {
            propertyIndex = 1;
        }
        try {

            Recorder recorderResponse = this.initFilenames(runId, test, testCase, step, index, sequence, controlString, property, propertyIndex, preFileName, fileName, false);
            recordFile(recorderResponse.getFullPath(), recorderResponse.getFileName(), fileContent);

            // Index file created to database.
            object = testCaseExecutionFileFactory.create(0, runId, recorderResponse.getLevel(), fileName, recorderResponse.getRelativeFilenameURL(), ext, "", null, "", null);
            testCaseExecutionFileService.save(object);

        } catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }
        return object;
    }

    @Override
    public TestCaseExecutionFile recordTestDataLibProperty(Long runId, String property, int propertyIndex, List<HashMap<String, String>> result, HashMap<String, String> secrets) {
        TestCaseExecutionFile object = null;
        // Used for logging purposes
        String logPrefix = Infos.getInstance().getProjectNameAndVersion() + " - ";

        try {
            JSONArray jsonResult = null;
            jsonResult = dataLibService.convertToJSONObject(result);

            // RESULT.
            Recorder recorder = this.initFilenames(runId, null, null, null, null, null, null, property, propertyIndex, "result", "json", false);
            recordFile(recorder.getFullPath(), recorder.getFileName(), jsonResult.toString(), secrets);

            // Index file created to database.
            object = testCaseExecutionFileFactory.create(0, runId, recorder.getLevel(), "Result", recorder.getRelativeFilenameURL(), "JSON", "", null, "", null);
            testCaseExecutionFileService.save(object);

        } catch (CerberusException | JSONException ex) {
            LOG.error("{}TestDataLib file was not saved due to unexpected error. {}", logPrefix, ex.toString(), ex);
        }
        return object;
    }

    @Override
    public TestCaseExecutionFile recordProperty(Long runId, String property, int propertyIndex, String content, HashMap<String, String> secrets) {
        TestCaseExecutionFile object = null;
        // Used for logging purposes
        String logPrefix = Infos.getInstance().getProjectNameAndVersion() + " - ";

        try {
            // RESULT.
            Recorder recorder = this.initFilenames(runId, null, null, null, null, null, null, property, propertyIndex, "result", "json", false);
            recordFile(recorder.getFullPath(), recorder.getFileName(), content, secrets);

            // Index file created to database.
            object = testCaseExecutionFileFactory.create(0, runId, recorder.getLevel(), "Content", recorder.getRelativeFilenameURL(), "JSON", "", null, "", null);
            testCaseExecutionFileService.save(object);

        } catch (CerberusException ex) {
            LOG.error("{}Property file was not saved due to unexpected error. {}", logPrefix, ex.toString(), ex);
        }
        return object;
    }

    @Override
    public TestCaseExecutionFile recordCapabilities(TestCaseExecution execution, List<RobotCapability> capsInputList, List<RobotCapability> capFinalList) {
        TestCaseExecutionFile object = null;

        LOG.debug("Starting to save Robot caps file.");

        if ((capsInputList == null) && (capFinalList == null)) {
            LOG.debug("No caps to record.");
            return null;
        }

        JSONObject outputMessage = new JSONObject();

        try {

            JSONObject capsInput = new JSONObject();
            if (capsInputList != null) {
                for (RobotCapability robotCapability : capsInputList) {
                    capsInput.append(robotCapability.getCapability(), robotCapability.getValue());
                }
            }
            outputMessage.put("RequestedCapabilities", capsInput);

            JSONObject capsFinal = new JSONObject();
            if (capFinalList != null) {
                for (RobotCapability robotCapability : capFinalList) {
                    capsFinal.append(robotCapability.getCapability(), robotCapability.getValue());
                }
            }
            outputMessage.put("FinalCapabilities", capsFinal);

            // RESULT.
            Recorder recorder = this.initFilenames(execution.getId(), null, null, null, null, null, null, null, 0, "robot_caps", "json", false);
            recordFile(recorder.getFullPath(), recorder.getFileName(), outputMessage.toString(4), execution.getSecrets());

            // Index file created to database.
            object = testCaseExecutionFileFactory.create(0, execution.getId(), recorder.getLevel(), "Robot Caps", recorder.getRelativeFilenameURL(), "JSON", "", null, "", null);
            testCaseExecutionFileService.save(object);

        } catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }

        return object;
    }

    @Override
    public TestCaseExecutionFile recordCapabilities(TestCaseExecution execution, MutableCapabilities requestedCapabilities, MutableCapabilities finalCapabilities) {
        LOG.debug("Starting to save Robot caps file.");

        TestCaseExecutionFile object = null;
        Map<String, JsonNode> outputMessage = new HashMap<>();

        try {

            //Build the output file
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            JsonNode requestedCapabilitiesJson = objectMapper.valueToTree(requestedCapabilities.toJson());
            JsonNode finalCapabilitiesJson = objectMapper.valueToTree(finalCapabilities.toJson());
            outputMessage.put("RequestedCapabilities", requestedCapabilitiesJson);
            outputMessage.put("FinalCapabilities", finalCapabilitiesJson);

            Recorder recorder = this.initFilenames(execution.getId(), null, null, null, null, null, null, null, 0, "robot_caps", "json", false);
            recordFile(recorder.getFullPath(), recorder.getFileName(), objectMapper.valueToTree(outputMessage).toPrettyString(), execution.getSecrets());

            // Index file created to database.
            object = testCaseExecutionFileFactory.create(0, execution.getId(), recorder.getLevel(), "Robot Caps", recorder.getRelativeFilenameURL(), "JSON", "", null, "", null);
            testCaseExecutionFileService.save(object);

        } catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }

        return object;
    }

    @Override
    public TestCaseExecutionFile recordServerCapabilities(TestCaseExecution execution, List<RobotCapability> capFinalList) {
        TestCaseExecutionFile object = null;

        LOG.debug("Starting to save Server Robot caps file.");

        if ((capFinalList == null)) {
            LOG.debug("No caps to record.");
            return null;
        }

        JSONObject outputMessage = new JSONObject();

        try {

            JSONObject capsFinal = new JSONObject();
            if (capFinalList != null) {
                for (RobotCapability robotCapability : capFinalList) {
                    capsFinal.append(robotCapability.getCapability(), robotCapability.getValue());
                }
            }
            outputMessage.put("ServerCapabilities", capsFinal);

            // RESULT.
            Recorder recorder = this.initFilenames(execution.getId(), null, null, null, null, null, null, null, 0, "robot_server_caps", "json", false);
            recordFile(recorder.getFullPath(), recorder.getFileName(), outputMessage.toString(4), execution.getSecrets());

            // Index file created to database.
            object = testCaseExecutionFileFactory.create(0, execution.getId(), recorder.getLevel(), "Robot Server Caps", recorder.getRelativeFilenameURL(), "JSON", "", null, "", null);
            testCaseExecutionFileService.save(object);

        } catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }

        return object;
    }

    @Override
    public TestCaseExecutionFile recordServerCapabilities(TestCaseExecution execution, Capabilities serverCapabilities) {
        TestCaseExecutionFile object = null;

        LOG.debug("Starting to save Server Robot caps file.");

        if ((serverCapabilities == null)) {
            LOG.debug("No caps to record.");
            return null;
        }
        Map<String, JsonNode> outputMessage = new HashMap<>();
        try {

            //Build the output file
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            JsonNode serverCapabilitiesJson = objectMapper.valueToTree(new MutableCapabilities(serverCapabilities).toJson());
            outputMessage.put("ServerCapabilities", serverCapabilitiesJson);

            // RESULT.
            Recorder recorder = this.initFilenames(execution.getId(), null, null, null, null, null, null, null, 0, "robot_server_caps", "json", false);
            recordFile(recorder.getFullPath(), recorder.getFileName(), objectMapper.valueToTree(outputMessage).toPrettyString(), execution.getSecrets());

            // Index file created to database.
            object = testCaseExecutionFileFactory.create(0, execution.getId(), recorder.getLevel(), "Robot Server Caps", recorder.getRelativeFilenameURL(), "JSON", "", null, "", null);
            testCaseExecutionFileService.save(object);

        } catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }

        return object;
    }


    @Override
    public TestCaseExecutionFile recordHar(TestCaseExecution execution, JSONObject har) {
        TestCaseExecutionFile object = null;

        LOG.debug("Starting to save Har file.");

        if ((StringUtil.isEmptyOrNull(har.toString()))) {
            LOG.debug("No har to record.");
            return null;
        }

        if ((execution.getRobotLog() == 2 || (execution.getRobotLog() == 1 && !execution.getControlStatus().equals("OK")))
                && parameterService.getParameterBooleanByKey("cerberus_executionloghar_enable", execution.getSystem(), true)) {

            try {

                // RESULT.
                Recorder recorder = this.initFilenames(execution.getId(), null, null, null, null, null, null, null, 0, "enriched_har", "json", false);
                recordFile(recorder.getFullPath(), recorder.getFileName(), har.toString(1), execution.getSecrets());

                // Index file created to database.
                object = testCaseExecutionFileFactory.create(0, execution.getId(), recorder.getLevel(), "Network HAR File", recorder.getRelativeFilenameURL(), "JSON", "", null, "", null);
                testCaseExecutionFileService.save(object);

            } catch (Exception ex) {
                LOG.error(ex.toString(), ex);
            }

        }

        return object;
    }

    @Override
    public TestCaseExecutionFile recordSeleniumLog(TestCaseExecution execution) {
        TestCaseExecutionFile object = null;

        if (execution.getApplicationObj().getType().equals(Application.TYPE_GUI)) {

            if (execution.getRobotLog() == 2 || (execution.getRobotLog() == 1 && !execution.getControlStatus().equals("OK"))) {
                LOG.debug("Starting to save Selenium log file.");

                try {
                    Recorder recorder = this.initFilenames(execution.getId(), null, null, null, null, null, null, null, 0, "selenium_log", "txt", false);

                    File dir = new File(recorder.getFullPath());
                    dir.mkdirs();

                    File file = new File(recorder.getFullFilename());

                    try (FileOutputStream fileOutputStream = new FileOutputStream(file);) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        DataOutputStream out = new DataOutputStream(baos);
                        for (String element : this.webdriverService.getSeleniumLog(execution.getSession())) {
                            out.writeBytes(StringUtil.secureFromSecrets(element, execution.getSecrets()));
                        }
                        byte[] bytes = baos.toByteArray();
                        fileOutputStream.write(bytes);
                        out.close();
                        baos.close();
                        fileOutputStream.close();

                        LOG.info("File saved : {}", recorder.getFullFilename());

                        // Index file created to database.
                        object = testCaseExecutionFileFactory.create(0, execution.getId(), recorder.getLevel(), "Selenium Log", recorder.getRelativeFilenameURL(), "TXT", "", null, "", null);
                        testCaseExecutionFileService.save(object);

                    } catch (IOException ex) {
                        LOG.error("Exception on recording Selenium file.", ex);

                    } catch (WebDriverException ex) {
                        LOG.debug("Exception recording Selenium Log on execution : {}", execution.getId(), ex);
                        object = testCaseExecutionFileFactory.create(0, execution.getId(), recorder.getLevel(), "Selenium Log [ERROR]", recorder.getRelativeFilenameURL(), "HTML", "", null, "", null);
                        testCaseExecutionFileService.save(object);

                    }

                    LOG.debug("Selenium log recorded in : {}", recorder.getRelativeFilenameURL());

                } catch (CerberusException ex) {
                    LOG.error("Exception on recording Selenium file.", ex);
                }
            }
        } else {
            LOG.debug("Selenium Log not recorded because test on non GUI application");
        }
        return object;
    }

    @Override
    public TestCaseExecutionFile recordConsoleLog(TestCaseExecution execution) {
        TestCaseExecutionFile object = null;

        if (execution.getApplicationObj().getType().equals(Application.TYPE_GUI)) {

            if (execution.getConsoleLog() == 2 || (execution.getConsoleLog() == 1 && !execution.getControlStatus().equals("OK"))) {
                LOG.debug("Starting to save Console log file.");

                try {
                    Recorder recorder = this.initFilenames(execution.getId(), null, null, null, null, null, null, null, 0, "console_log", "txt", false);

                    File dir = new File(recorder.getFullPath());
                    dir.mkdirs();

                    File file = new File(recorder.getFullFilename());

                    try (FileOutputStream fileOutputStream = new FileOutputStream(file);) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        DataOutputStream out = new DataOutputStream(baos);
                        for (String element : this.webdriverService.getConsoleLog(execution.getSession())) {
                            out.writeBytes(StringUtil.secureFromSecrets(element, execution.getSecrets()));
                        }
                        byte[] bytes = baos.toByteArray();
                        fileOutputStream.write(bytes);
                        out.close();
                        baos.close();
                        fileOutputStream.close();

                        LOG.info("File saved : {}", recorder.getFullFilename());

                        // Index file created to database.
                        object = testCaseExecutionFileFactory.create(0, execution.getId(), recorder.getLevel(), "Console Log", recorder.getRelativeFilenameURL(), "TXT", "", null, "", null);
                        testCaseExecutionFileService.save(object);

                    } catch (IOException ex) {
                        LOG.error("Exception on recording Console log file.", ex);

                    } catch (WebDriverException ex) {
                        LOG.debug("Exception recording Console Log on execution : {}", execution.getId(), ex);
                        object = testCaseExecutionFileFactory.create(0, execution.getId(), recorder.getLevel(), "Console Log [ERROR]", recorder.getRelativeFilenameURL(), "TXT", "", null, "", null);
                        testCaseExecutionFileService.save(object);

                    }

                    LOG.debug("Console log recorded in : {}", recorder.getRelativeFilenameURL());

                } catch (CerberusException ex) {
                    LOG.error("Exception on recording Console log file.", ex);
                }
            }
        } else {
            LOG.debug("Console Log not recorded because test on non GUI application");
        }
        return object;
    }

    @Override
    public void addFileToTestCaseExecution(TestCaseExecution tce, Recorder recorder, String fileDesc, String fileType) {
        TestCaseExecutionFile object = testCaseExecutionFileFactory.create(0, tce.getId(), recorder.getLevel(), fileDesc, recorder.getRelativeFilenameURL(), fileType, "", null, "", null);
        testCaseExecutionFileService.save(object);
    }

    /**
     * Auxiliary method that saves a file
     *
     * @param path - directory path
     * @param fileName - name of the file
     * @param content -content of the file
     */
    private void recordFile(String path, String fileName, String content, HashMap<String, String> secrets) {
        LOG.debug("Starting to save File (recordFile) : {} {}", path, fileName);

        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dir.getAbsolutePath() + File.separator + fileName), StandardCharsets.UTF_8));) {
            writer.write(StringUtil.secureFromSecrets(content, secrets));
            writer.close();
            LOG.info("File saved : {}{}{}", path, File.separator, fileName);
        } catch (IOException ex) {
            LOG.error("Unable to save : {}{}{} ex: {}", path, File.separator, fileName, ex, ex);
        }
    }

    /**
     * Auxiliary method that saves a file
     *
     * @param path - directory path
     * @param fileName - name of the file
     * @param content -content of the file
     */
    private void recordFile(String path, String fileName, byte[] content) {
        LOG.debug("Starting to save File (recordRobotFile) : {} {}", path, fileName);

        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File outputFile = new File(dir.getAbsolutePath() + File.separator + fileName);
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(content);
            LOG.info("File saved : {}{}{}", path, File.separator, fileName);
        } catch (IOException ex) {
            LOG.error("Unable to save : {}{}{} ex: {}", path, File.separator, fileName, ex, ex);
        }

    }

    @Override
    public Recorder initFilenames(long exeID, String test, String testCase, String stepID, String index, String actionID, String controlID, String property, int propertyIndex, String filename, String extention, boolean manual) throws CerberusException {
        Recorder newRecorder = new Recorder();

        try {

            String rootFolder = "";

            /*
             * Root folder initialisation. The root folder is configures from
             * the parameter cerberus_exeautomedia_path or
             * cerberus_exemanualmedia_path .
             */
            if (!manual) {
                rootFolder = parameterService.getParameterStringByKey("cerberus_exeautomedia_path", "", "");
            } else {
                rootFolder = parameterService.getParameterStringByKey("cerberus_exemanualmedia_path", "", "");
            }

            rootFolder = StringUtil.addSuffixIfNotAlready(rootFolder, File.separator);
            newRecorder.setRootFolder(rootFolder);

            /*
             * SubFolder. Subfolder is split in order to reduce the nb of folder
             * within a folder. 2 levels of 2 digits each. he last level is the
             * execution id.
             */
            String subFolder = getStorageSubFolder(exeID);
            newRecorder.setSubFolder(subFolder);
            String subFolderURL = getStorageSubFolderURL(exeID);
            newRecorder.setSubFolder(subFolderURL);

            // FullPath. Concatenation of the rootfolder and subfolder.
            String fullPath = rootFolder + subFolder;
            newRecorder.setFullPath(fullPath);

            //  Filename. If filename is not define, we assign it from the test, testcase, step action and control.
            StringBuilder sbfileName = new StringBuilder();
            if (!StringUtil.isEmptyOrNull(test)) {
                sbfileName.append(test.replace("/", "").replace("\\", "")).append("-");
            }
            if (!StringUtil.isEmptyOrNull(testCase)) {
                sbfileName.append(testCase.replace("/", "").replace("\\", "")).append("-");
            }
            if (!StringUtil.isEmptyOrNull(stepID)) {
                sbfileName.append("S").append(stepID).append("-");
            }
            if (!StringUtil.isEmptyOrNull(index)) {
                sbfileName.append("I").append(index).append("-");
            }
            if (!StringUtil.isEmptyOrNull(actionID)) {
                sbfileName.append("A").append(actionID).append("-");
            }
            if (!StringUtil.isEmptyOrNull(controlID)) {
                sbfileName.append("C").append(controlID).append("-");
            }
            if (!StringUtil.isEmptyOrNull(property)) {
                sbfileName.append(property).append("-");
            }
            if (propertyIndex != 0) {
                sbfileName.append(propertyIndex).append("-");
            }
            if (!StringUtil.isEmptyOrNull(filename)) {
                sbfileName.append(filename).append("-");
            }

            String fileName = StringUtil.removeLastChar(sbfileName.toString()) + "." + extention;
            fileName = fileName.replace(" ", "");
            newRecorder.setFileName(fileName);

            /*
             * Level. 5 levels possible. Keys are defined seperated by -. 1/
             * Execution level --> emptyString. 2/ Step level -->
             * test+testcase+Step 3/ Action level --> test+testcase+Step+action
             * 4/ Control level --> test+testcase+Step+action+control 5/
             * Property level --> property+index
             */
            String level = "";
            if (!(StringUtil.isEmptyOrNull(controlID))) {
                level = test + "-" + testCase + "-" + stepID + "-" + index + "-" + actionID + "-" + controlID;
            } else if (!(StringUtil.isEmptyOrNull(actionID))) {
                level = test + "-" + testCase + "-" + stepID + "-" + index + "-" + actionID;
            } else if (!(StringUtil.isEmptyOrNull(stepID))) {
                level = test + "-" + testCase + "-" + stepID + "-" + index;
            } else if (!(StringUtil.isEmptyOrNull(property))) {
                level = property + "-" + propertyIndex;
            }
            newRecorder.setLevel(level);

            // Final Filename with full path.
            String fullFilename = rootFolder + File.separator + subFolder + File.separator + fileName;
            newRecorder.setFullFilename(fullFilename);
            String relativeFilenameURL = subFolderURL + "/" + fileName;
            newRecorder.setRelativeFilenameURL(relativeFilenameURL);

        } catch (Exception ex) {
            LOG.error("Error on data init. {}", ex.toString(), ex);
        }

        return newRecorder;
    }

    @Override
    public String getStorageSubFolderURL(long exeID) {
        String idString = String.valueOf(exeID);
        if (idString.length() >= 4) {
            return idString.substring((idString.length() - 2)) + "/" + idString.substring((idString.length() - 4), (idString.length() - 2)) + "/" + idString;
        } else {
            return idString;
        }
    }

    @Override
    public String getStorageSubFolder(long exeID) {
        String idString = String.valueOf(exeID);
        if (idString.length() >= 4) {
            return idString.substring((idString.length() - 2)) + File.separator + idString.substring((idString.length() - 4), (idString.length() - 2)) + File.separator + idString;
        } else {
            return idString;
        }
    }

    private static void deleteFolder(File folder, boolean deleteit) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f, true);
                } else {
                    f.delete();
                }
            }
        }
        if (deleteit) {
            folder.delete();
        }
    }
}
