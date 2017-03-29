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
import javax.xml.soap.SOAPMessage;
import org.apache.commons.fileupload.FileItem;
import org.cerberus.crud.entity.AppService;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseExecutionFile;
import org.cerberus.crud.entity.TestCaseStepActionControlExecution;
import org.cerberus.crud.entity.TestCaseStepActionExecution;

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
    public String getStorageSubFolderURL(long exeID);

    /**
     *
     * @param exeID
     * @return
     */
    public String getStorageSubFolder(long exeID);
}
