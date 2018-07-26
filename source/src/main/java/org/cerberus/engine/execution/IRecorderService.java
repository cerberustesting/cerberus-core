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
package org.cerberus.engine.execution;

import java.util.HashMap;
import java.util.List;
import org.apache.commons.fileupload.FileItem;
import org.cerberus.crud.entity.AppService;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseExecutionFile;
import org.cerberus.crud.entity.TestCaseStepActionControlExecution;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.engine.entity.Recorder;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.AnswerItem;

/**
 *
 * @author bcivel
 */
public interface IRecorderService {

    /**
     *
     * @param testCaseStepActionExecution
     * @param testCaseStepActionControlExecution
     * @return
     */
    List<TestCaseExecutionFile> recordExecutionInformationAfterStepActionandControl(TestCaseStepActionExecution testCaseStepActionExecution, TestCaseStepActionControlExecution testCaseStepActionControlExecution);

    /**
     *
     * @param testCaseExecution
     * @param testCaseStepActionExecution
     * @param control
     * @return
     */
    TestCaseExecutionFile recordScreenshot(TestCaseExecution testCaseExecution, TestCaseStepActionExecution testCaseStepActionExecution, Integer control);

    /**
     *
     * @param testCaseExecution
     * @param testCaseStepActionExecution
     * @param control
     * @return
     */
    TestCaseExecutionFile recordPageSource(TestCaseExecution testCaseExecution, TestCaseStepActionExecution testCaseStepActionExecution, Integer control);

    /**
     *
     * @param testCaseExecution
     * @param testCaseStepActionExecution
     * @param control
     * @param property
     * @param service
     * @return
     */
    List<TestCaseExecutionFile> recordServiceCall(TestCaseExecution testCaseExecution, TestCaseStepActionExecution testCaseStepActionExecution, Integer control, String property, AppService service);

    /**
     *
     * @param runId
     * @param property
     * @param propertyIndex
     * @param result
     * @return
     */
    TestCaseExecutionFile recordTestDataLibProperty(Long runId, String property, int propertyIndex, List<HashMap<String, String>> result);

    /**
     *
     * @param runId
     * @param property
     * @param propertyIndex
     * @param content
     * @return
     */
    TestCaseExecutionFile recordProperty(Long runId, String property, int propertyIndex, String content);

    /**
     *
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
    AnswerItem recordManuallyFile(TestCaseStepActionExecution testCaseStepActionExecution, TestCaseStepActionControlExecution testCaseStepActionControlExecution, String extension, String desc, FileItem file, Integer id, String fileName, Integer fileID);

    /**
     *
     * @param testCaseExecution
     * @return
     */
    TestCaseExecutionFile recordSeleniumLog(TestCaseExecution testCaseExecution);

    /**
     *
     * @param executionId
     * @param tcsae
     * @param uploadedFile
     */
    void recordUploadedFile(long executionId, TestCaseStepActionExecution tcsae, FileItem uploadedFile);

    /**
     *
     * @param exeID
     * @return
     */
    String getStorageSubFolderURL(long exeID);

    /**
     *
     * @param exeID
     * @return
     */
    String getStorageSubFolder(long exeID);

    Recorder initFilenames(long exeID, String test, String testCase, String step, String index, String sequence, String controlString, String property, int propertyIndex, String filename, String extention, boolean manual) throws CerberusException;

    /**
     * attach a physique file to the testcase execution and save it to database
     * @param tce   the test case execution
     * @param recorder the recorder that was created to save file on datastorage
     * @param fileDesc  description of file : Exxemple : "Video"
     * @param fileType  File type (PNG, MP4, etc.)
     */
    void addFileToTestCaseExecution(TestCaseExecution tce, Recorder recorder, String fileDesc, String fileType);

}
