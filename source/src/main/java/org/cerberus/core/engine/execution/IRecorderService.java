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
package org.cerberus.core.engine.execution;

import org.apache.commons.fileupload.FileItem;
import org.cerberus.core.engine.entity.Recorder;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.AnswerItem;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.entity.RobotCapability;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseExecutionFile;
import org.cerberus.core.crud.entity.TestCaseStepActionControlExecution;
import org.cerberus.core.crud.entity.TestCaseStepActionExecution;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;

/**
 * @author bcivel
 */
public interface IRecorderService {

    /**
     * @param testCaseStepActionExecution
     * @param testCaseStepActionControlExecution
     * @return
     */
    List<TestCaseExecutionFile> recordExecutionInformationAfterStepActionAndControl(TestCaseStepActionExecution testCaseStepActionExecution, TestCaseStepActionControlExecution testCaseStepActionControlExecution);

    /**
     * @param testCaseStepActionExecution
     * @param testCaseStepActionControlExecution
     * @return
     */
    List<TestCaseExecutionFile> recordExecutionInformationBeforeStepActionAndControl(TestCaseStepActionExecution testCaseStepActionExecution, TestCaseStepActionControlExecution testCaseStepActionControlExecution);

    /**
     * @param testCaseExecution
     * @param testCaseStepActionExecution
     * @param control
     * @param cropValues
     * @param fileDescription
     * @param fileName
     * @return
     */
    List<TestCaseExecutionFile> recordScreenshot(TestCaseExecution testCaseExecution, TestCaseStepActionExecution testCaseStepActionExecution, Integer control, String cropValues, String fileDescription, String fileName);

    /**
     * @param actionExecution
     * @param controlId
     * @param locator
     * @param valueFieldName
     * @return
     */
    TestCaseExecutionFile recordPicture(TestCaseStepActionExecution actionExecution, Integer controlId, String locator, String valueFieldName);

    /**
     * @param testCaseExecution
     * @return
     */
    TestCaseExecutionFile recordExeLog(TestCaseExecution testCaseExecution);

    /**
     * @param testCaseExecution
     * @param testCaseStepActionExecution
     * @param control
     * @return
     */
    TestCaseExecutionFile recordPageSource(TestCaseExecution testCaseExecution, TestCaseStepActionExecution testCaseStepActionExecution, Integer control);

    /**
     * @param testCaseExecution
     * @param testCaseStepActionExecution
     * @param control
     * @param property
     * @param service
     * @return
     */
    List<TestCaseExecutionFile> recordServiceCall(TestCaseExecution testCaseExecution, TestCaseStepActionExecution testCaseStepActionExecution, Integer control, String property, AppService service);

    /**
     * @param testCaseExecution
     * @param testCaseStepActionExecution
     * @param service
     * @return
     */
    List<TestCaseExecutionFile> recordServiceCallContent(TestCaseExecution testCaseExecution, TestCaseStepActionExecution testCaseStepActionExecution, AppService service);

    /**
     * @param testCaseExecution
     * @param testCaseStepActionExecution
     * @param control
     * @param property
     * @param service
     * @param withDetail
     * @return
     */
    List<TestCaseExecutionFile> recordNetworkTrafficContent(TestCaseExecution testCaseExecution, TestCaseStepActionExecution testCaseStepActionExecution, Integer control, String property, AppService service, boolean withDetail);

    /**
     * @param testCaseExecution
     * @param testCaseStepActionExecution
     * @param control
     * @param property
     * @param consoleContent
     * @param withDetail
     * @return
     */
    List<TestCaseExecutionFile> recordConsoleContent(TestCaseExecution testCaseExecution, TestCaseStepActionExecution testCaseStepActionExecution, Integer control, String property, JSONObject consoleContent, boolean withDetail);

    /**
     * @param testCaseExecution
     * @param testCaseStepActionExecution
     * @param control
     * @param property
     * @param content
     * @param contentType
     * @return
     */
    List<TestCaseExecutionFile> recordContent(TestCaseExecution testCaseExecution, TestCaseStepActionExecution testCaseStepActionExecution, Integer control, String property, String content, String contentType);

    /**
     *
     * @param execution
     * @param actionExecution
     * @param control
     * @param property
     * @param fileContent
     * @param preFileName
     * @param fileName
     * @param ext
     * @return
     */
    TestCaseExecutionFile recordRobotFile(TestCaseExecution execution, TestCaseStepActionExecution actionExecution, Integer control, String property, byte[] fileContent, String preFileName, String fileName, String ext);

    /**
     * @param runId
     * @param property
     * @param propertyIndex
     * @param result
     * @param secrets
     * @return
     */
    TestCaseExecutionFile recordTestDataLibProperty(Long runId, String property, int propertyIndex, List<HashMap<String, String>> result, HashMap<String, String> secrets);

    /**
     * @param runId
     * @param property
     * @param propertyIndex
     * @param content
     * @param secrets
     * @return
     */
    TestCaseExecutionFile recordProperty(Long runId, String property, int propertyIndex, String content, HashMap<String, String> secrets);

    /**
     * records into a file in a JSON format the requested capability from the
     * robot and the final ones set to the Selenium/Appium driver.
     *
     * @param testCaseExecution
     * @param requestedCapabilities
     * @param finalCapabilities
     * @return
     */
    TestCaseExecutionFile recordCapabilities(TestCaseExecution testCaseExecution, MutableCapabilities requestedCapabilities, MutableCapabilities finalCapabilities);

    /**
     * records into a file in a JSON format the requested capability from the
     * robot and the final ones set to the Selenium/Appium driver.
     *
     * @param testCaseExecution
     * @param capsInputList
     * @param capsFinalList
     * @return
     */
    TestCaseExecutionFile recordCapabilities(TestCaseExecution testCaseExecution, List<RobotCapability> capsInputList, List<RobotCapability> capsFinalList);

    /**
     * records into a file in a JSON format the capabilities reported from the
     * server.
     *
     * @param testCaseExecution
     * @param serverCapabilities
     * @return
     */
    TestCaseExecutionFile recordServerCapabilities(TestCaseExecution testCaseExecution, Capabilities serverCapabilities);

    /**
     * records into a file in a JSON format the capabilities reported from the
     * server.
     *
     * @param testCaseExecution
     * @param capsFinalList
     * @return
     */
    TestCaseExecutionFile recordServerCapabilities(TestCaseExecution testCaseExecution, List<RobotCapability> capsFinalList);

    /**
     *
     * @param execution
     * @param har
     * @return
     */
    public TestCaseExecutionFile recordHar(TestCaseExecution execution, JSONObject har);

    /**
     * @param testCaseStepActionControlExecution
     * @param testCaseStepActionExecution
     * @param extension
     * @param desc
     * @param id
     * @param file
     * @param fileName
     * @param fileID
     * @return
     */
    AnswerItem<TestCaseExecutionFile> recordManuallyFile(TestCaseStepActionExecution testCaseStepActionExecution, TestCaseStepActionControlExecution testCaseStepActionControlExecution,
            String extension, String desc, FileItem file, Integer id, String fileName, Integer fileID);

    /**
     * @param testCaseExecution
     * @return
     */
    TestCaseExecutionFile recordSeleniumLog(TestCaseExecution testCaseExecution);

    /**
     * @param testCaseExecution
     * @return
     */
    TestCaseExecutionFile recordConsoleLog(TestCaseExecution testCaseExecution);

    /**
     * @param exeID
     * @return
     */
    String getStorageSubFolderURL(long exeID);

    /**
     * @param exeID
     * @return
     */
    String getStorageSubFolder(long exeID);

    Recorder initFilenames(long exeID, String test, String testCase, String step, String index, String sequence, String controlString, String property, int propertyIndex, String filename, String extention, boolean manual) throws CerberusException;

    /**
     * attach a physique file to the testcase execution and save it to database
     *
     * @param tce the test case execution
     * @param recorder the recorder that was created to save file on datastorage
     * @param fileDesc description of file : Exxemple : "Video"
     * @param fileType File type (PNG, MP4, etc.)
     */
    void addFileToTestCaseExecution(TestCaseExecution tce, Recorder recorder, String fileDesc, String fileType);

}
