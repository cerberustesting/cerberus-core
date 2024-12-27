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
package org.cerberus.core.crud.service.impl;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.cerberus.core.crud.dao.ITestCaseExecutionFileDAO;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.crud.entity.TestCaseExecutionFile;
import org.cerberus.core.crud.factory.IFactoryTestCaseExecutionFile;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.service.ITestCaseExecutionFileService;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestCaseExecutionFileService implements ITestCaseExecutionFileService {

    @Autowired
    private ITestCaseExecutionFileDAO testCaseExecutionFileDAO;
    @Autowired
    private IFactoryTestCaseExecutionFile testCaseExecutionFileFactory;

    private static final Logger LOG = LogManager.getLogger("TestCaseExecutionFileService");

    private final String OBJECT_NAME = "TestCaseExecutionFile";

    @Override
    public AnswerItem<TestCaseExecutionFile> readByKey(long id) {
        return testCaseExecutionFileDAO.readByKey(id);
    }

    @Override
    public AnswerItem<TestCaseExecutionFile> readByKey(long exeId, String level, String fileDesc) {
        return testCaseExecutionFileDAO.readByKey(exeId, level, fileDesc);
    }

    @Override
    public AnswerList<TestCaseExecutionFile> readByVariousByCriteria(long exeId, String level, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return testCaseExecutionFileDAO.readByVariousByCriteria(exeId, level, length, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public AnswerList<TestCaseExecutionFile> readByVarious(long ExeId, String level) {
        return testCaseExecutionFileDAO.readByVariousByCriteria(ExeId, level, 0, 0, "ID", "asc", null, null);
    }

    @Override
    public List<TestCaseExecutionFile> getListByFileDesc(long exeId, String fileDesc) throws CerberusException {
        return testCaseExecutionFileDAO.getListByFileDesc(exeId, fileDesc);
    }

    @Override
    public Answer save(long exeId, String level, String fileDesc, String fileName, String fileType, String usrCreated) {
        TestCaseExecutionFile object = null;
        object = testCaseExecutionFileFactory.create(0, exeId, level, fileDesc, fileName, fileType, usrCreated, null, "", null);
        return this.save(object);
    }

    @Override
    public boolean exist(long id) {
        AnswerItem objectAnswer = readByKey(id);
        return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successfull and object was found.
    }

    @Override
    public boolean exist(long exeId, String level, String fileDesc) {
        AnswerItem objectAnswer = readByKey(exeId, level, fileDesc);
        return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successfull and object was found.
    }

    @Override
    public boolean exist(long exeId, String level) {
        AnswerItem objectAnswer = readByKey(exeId, level, null);
        return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successfull and object was found.
    }

    @Override
    public Answer create(TestCaseExecutionFile object) {
        return testCaseExecutionFileDAO.create(object);
    }

    @Override
    public Answer delete(TestCaseExecutionFile object) {
        return testCaseExecutionFileDAO.delete(object);
    }

    @Override
    public Answer update(TestCaseExecutionFile object) {
        return testCaseExecutionFileDAO.update(object);
    }

    @Override
    public Answer save(TestCaseExecutionFile object) {
        if (this.exist(object.getExeId(), object.getLevel(), object.getFileDesc())) {
            return update(object);
        } else {
            return create(object);
        }
    }

    @Override
    public void deleteFile(String root, String fileName) {
        File currentFile = new File(root + File.separator + fileName);
        currentFile.delete();
    }

    @Override
    public Answer saveManual(TestCaseExecutionFile object) {
        if (this.exist(object.getId())) {
            return update(object);
        } else {
            return create(object);
        }
    }

    /**
     * this function allow to check if extension exist in invariants table
     */
    @Override
    public String checkExtension(String fileName, String extension) {
        if (extension.isEmpty() || (extension == null ? fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length()) != null : !extension.equals(fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length())))) {
            if (fileName.contains(".")) {
                extension = fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length());
                extension = extension.trim().toUpperCase();
            } else {
                if (StringUtil.isEmptyOrNULLString(extension)) {

                    extension = "BIN";

                }
            }

            switch (extension) {
                case TestCaseExecutionFile.FILETYPE_JPG:
                    extension = "JPG";
                    break;
                case TestCaseExecutionFile.FILETYPE_PNG:
                    extension = "PNG";
                    break;
                case TestCaseExecutionFile.FILETYPE_JPEG:
                    extension = "JPG";
                    break;
                case TestCaseExecutionFile.FILETYPE_PDF:
                    extension = "PDF";
                    break;
                case TestCaseExecutionFile.FILETYPE_JSON:
                    extension = "JSON";
                    break;
                case TestCaseExecutionFile.FILETYPE_XML:
                    extension = "XML";
                    break;
                case TestCaseExecutionFile.FILETYPE_TXT:
                    extension = "TXT";
                    break;
                case TestCaseExecutionFile.FILETYPE_BIN:
                    extension = "BIN";
                    break;
                default:
                    extension = "BIN";
                    break;
            }
        }

        return extension;
    }

    @Override
    public TestCaseExecutionFile convert(AnswerItem<TestCaseExecutionFile> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<TestCaseExecutionFile> convert(AnswerList<TestCaseExecutionFile> answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerList.getDataList();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public void convert(Answer answer) throws CerberusException {
        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return;
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

}
