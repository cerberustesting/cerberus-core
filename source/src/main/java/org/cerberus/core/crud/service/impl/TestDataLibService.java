/**
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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

import java.util.ArrayList;
import java.util.List;
import org.cerberus.core.api.exceptions.EntityNotFoundException;
import org.cerberus.core.api.exceptions.FailedInsertOperationException;
import org.cerberus.core.api.exceptions.InvalidRequestException;
import java.util.*;

import jakarta.servlet.http.Part;
import org.cerberus.core.crud.dao.ITestCaseCountryPropertiesDAO;
import org.cerberus.core.crud.dao.ITestDataLibDAO;
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.crud.entity.TestDataLib;
import org.cerberus.core.crud.entity.TestDataLibData;
import org.cerberus.core.crud.factory.IFactoryTestDataLibData;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestDataLibDataService;
import org.cerberus.core.crud.service.ITestDataLibService;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestDataLibService implements ITestDataLibService {

    @Autowired
    private DatabaseSpring dbManager;
    @Autowired
    private ITestDataLibDAO testDataLibDAO;
    @Autowired
    private IFactoryTestDataLibData testDataLibDataFactory;
    @Autowired
    private ITestDataLibDataService testDataLibDataService;
    @Autowired
    private IParameterService parameterService;
    @Autowired
    private ITestCaseCountryPropertiesDAO testCaseCountryProperties;

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(TestDataLibService.class);

    @Override
    public AnswerItem<TestDataLib> readByNameBySystemByEnvironmentByCountry(String name, String system, String environment, String country) {
        return testDataLibDAO.readByNameBySystemByEnvironmentByCountry(name, system, environment, country);
    }

    @Override
    public AnswerItem<TestDataLib> readByKey(int testDatalib) {
        return testDataLibDAO.readByKey(testDatalib);
    }

    @Override
    public Answer uploadFile(int id, Part filePart) {
        return testDataLibDAO.uploadFile(id, filePart);
    }

    @Override
    public AnswerList<TestDataLib> readNameListByName(String testDataLibName, int limit, boolean like) {
        return testDataLibDAO.readNameListByName(testDataLibName, limit, like);
    }

    @Override
    public AnswerList<TestDataLib> readAll() {
        return testDataLibDAO.readAll();
    }

    @Override
    public AnswerList<TestDataLib> readByVariousByCriteria(String name, List<String> systems, String environment, String country, String type, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        return testDataLibDAO.readByVariousByCriteria(name, systems, environment, country, type, start, amount, column, dir, searchTerm, individualSearch);
    }

    @Override
    public AnswerList<String> readDistinctGroups() {
        return testDataLibDAO.readDistinctGroups();
    }

    @Override
    public AnswerList<HashMap<String, String>> readINTERNALWithSubdataByCriteria(String dataName, String dataSystem, String dataCountry, String dataEnvironment, int rowLimit, String system, TestCaseExecution execution) {
        AnswerList<HashMap<String, String>> answer = new AnswerList<>();
        AnswerList<TestDataLib> answerDataLib = new AnswerList<>();
        AnswerList<TestDataLibData> answerData = new AnswerList<>();
        MessageEvent msg;

        List<HashMap<String, String>> result = new ArrayList<>();

        // We start by calculating the max nb of row we can fetch. Either specified by rowLimit either defined by a parameter.
        int maxSecurityFetch = parameterService.getParameterIntegerByKey(Parameter.VALUE_cerberus_testdatalib_sql_fetchmax, system, 100);
        int maxFetch;
        if (rowLimit > 0 && rowLimit < maxSecurityFetch) {
            maxFetch = rowLimit;
        } else {
            maxFetch = maxSecurityFetch;
        }
        answerDataLib = this.readByVariousByCriteria(dataName, new ArrayList<>(Arrays.asList(dataSystem)), dataEnvironment, dataCountry, "INTERNAL", 0, maxFetch, null, null, null, null);
        List<TestDataLib> objectList = new ArrayList<>();
        objectList = answerDataLib.getDataList();
        for (TestDataLib tdl : objectList) {

            answerData = testDataLibDataService.readByVarious(tdl.getTestDataLibID(), null, null, null);
            List<TestDataLibData> objectDataList = new ArrayList<>();
            objectDataList = answerData.getDataList();
            HashMap<String, String> row = new HashMap<>();
            for (TestDataLibData tdld : objectDataList) {
                row.put(tdld.getSubData(), tdld.getValue());
                if (ParameterParserUtil.parseBooleanParam(tdld.getEncrypt(), false)) {
                    LOG.debug("Adding string to secret list : " + tdld.getSubData() + " - " + tdld.getValue() + " --> " + tdld.getEncrypt());
                    execution.addSecret(tdld.getValue());
                }
            }
            row.put("TestDataLibID", String.valueOf(tdl.getTestDataLibID()));
            result.add(row);
        }
        answer.setDataList(result);
        answer.setResultMessage(answerDataLib.getResultMessage());
        answer.setTotalRows(answerDataLib.getTotalRows());
        return answer;
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        return testDataLibDAO.readDistinctValuesByCriteria(searchTerm, individualSearch, columnName);
    }

    @Override
    public AnswerItem<TestDataLib> create(TestDataLib object) {
        return testDataLibDAO.create(object);
    }

    @Override
    public Answer delete(TestDataLib object) {
        return testDataLibDAO.delete(object);
    }

    @Override
    public Answer update(TestDataLib object) {
        return testDataLibDAO.update(object);
    }

    @Override
    public List<Answer> bulkRename(String oldName, String newName) {
        // Call the 2 DAO updates
        Answer answerDataLib = testDataLibDAO.bulkRenameDataLib(oldName, newName);
        Answer answerProperties = testCaseCountryProperties.bulkRenameProperties(oldName, newName);
        List<Answer> ansList = new ArrayList<>();
        ansList.add(answerDataLib);
        ansList.add(answerProperties);
        return ansList;
        // TO DO : get the updated numbers of datalib and properties
    }

    @Override
    public TestDataLib convert(AnswerItem<TestDataLib> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<TestDataLib> convert(AnswerList<TestDataLib> answerList) throws CerberusException {
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

    @Override
    public boolean userHasPermission(TestDataLib lib, String userName) {
        if ("Y".equals(lib.getPrivateData())) {
            if (!userName.equals(lib.getCreator())) {
                return false;
            }
        }
        return true;
    }

    // ---- Methods used by the public API


    private static final List<String> TYPES = List.of(TestDataLib.TYPE_INTERNAL, TestDataLib.TYPE_SQL, TestDataLib.TYPE_SERVICE, TestDataLib.TYPE_FILE);


    @Override
    public List<TestDataLib> readByVariousAPI(String name, String system, String environment, String country, String type) {
        AnswerList<TestDataLib> answer = this.readByVariousByCriteria(name,
                system == null ? null : new ArrayList<>(List.of(system)), environment, country, type,
                0, 0, "Name", "asc", null, null);
        return answer.getDataList() == null ? List.of() : answer.getDataList();
    }

    /**
     * Reads a library together with its sub-data.
     */
    @Override
    public TestDataLib readByKeyAPI(Integer id) {
        TestDataLib lib = readLib(id);
        lib.setSubDataLib(readSubData(id));
        return lib;
    }

    @Override
    public TestDataLib createAPI(TestDataLib newLib, String login) {
        if (StringUtil.isEmptyOrNull(newLib.getName())) {
            throw new InvalidRequestException("Field 'name' is mandatory");
        }
        checkType(newLib.getType());
        List<TestDataLibData> subData = newLib.getSubDataLib() == null ? new ArrayList<>() : newLib.getSubDataLib();
        checkKeyEntry(subData);

        newLib.setSystem(nullToEmpty(newLib.getSystem()));
        newLib.setEnvironment(nullToEmpty(newLib.getEnvironment()));
        newLib.setCountry(nullToEmpty(newLib.getCountry()));
        // The lookup used by the engine matches wildcards too, so the exact key is compared here.
        for (TestDataLib candidate : readByVariousAPI(newLib.getName(), null, null, null, null)) {
            if (newLib.getName().equalsIgnoreCase(candidate.getName())
                    && newLib.getSystem().equalsIgnoreCase(nullToEmpty(candidate.getSystem()))
                    && newLib.getEnvironment().equalsIgnoreCase(nullToEmpty(candidate.getEnvironment()))
                    && newLib.getCountry().equalsIgnoreCase(nullToEmpty(candidate.getCountry()))) {
                throw new InvalidRequestException("Data library already exists: name=" + newLib.getName() + " system=" + newLib.getSystem()
                        + " environment=" + newLib.getEnvironment() + " country=" + newLib.getCountry() + " id=" + candidate.getTestDataLibID());
            }
        }

        newLib.setTestDataLibID(null);
        newLib.setPrivateData(StringUtil.isEmptyOrNull(newLib.getPrivateData()) ? "N" : newLib.getPrivateData());
        newLib.setSeparator(StringUtil.isEmptyOrNull(newLib.getSeparator()) ? "," : newLib.getSeparator());
        newLib.setCreator(login);
        newLib.setLastModifier(login);
        AnswerItem<TestDataLib> created = this.create(newLib);
        if (!created.isCodeStringEquals("OK") || created.getItem() == null || created.getItem().getTestDataLibID() == null) {
            throw new FailedInsertOperationException(created.getMessageDescription());
        }
        Integer id = created.getItem().getTestDataLibID();

        for (TestDataLibData data : subData) {
            data.setTestDataLibID(id);
            data.setTestDataLibDataID(null);
            data.setSubData(nullToEmpty(data.getSubData()));
            data.setEncrypt(StringUtil.isEmptyOrNull(data.getEncrypt()) ? "N" : data.getEncrypt());
            fillEmpty(data);
        }
        if (!subData.isEmpty()) {
            check(testDataLibDataService.createList(subData));
        }
        return readByKeyAPI(id);
    }

    /**
     * @param ignoreFirstLine carried apart because the entity cannot tell false from absent
     * @param patch when true only the provided fields are changed and the provided sub-data are merged (by
     * subData name) into the existing ones. When false the library fields are replaced, and a provided sub-data
     * list replaces the existing one.
     */
    @Override
    public TestDataLib updateAPI(Integer id, TestDataLib incoming, Boolean ignoreFirstLine, String login, boolean patch) {
        TestDataLib existing = readLib(id);

        if (!StringUtil.isEmptyOrNull(incoming.getType())) {
            checkType(incoming.getType());
        } else if (!patch) {
            throw new InvalidRequestException("Field 'type' is mandatory");
        }
        if (StringUtil.isEmptyOrNull(incoming.getName()) && !patch) {
            throw new InvalidRequestException("Field 'name' is mandatory");
        }

        if (!StringUtil.isEmptyOrNull(incoming.getName())) {
            existing.setName(incoming.getName());
        }
        if (!StringUtil.isEmptyOrNull(incoming.getType())) {
            existing.setType(incoming.getType());
        }
        existing.setSystem(pick(incoming.getSystem(), existing.getSystem(), patch));
        existing.setEnvironment(pick(incoming.getEnvironment(), existing.getEnvironment(), patch));
        existing.setCountry(pick(incoming.getCountry(), existing.getCountry(), patch));
        existing.setGroup(pick(incoming.getGroup(), existing.getGroup(), patch));
        existing.setDescription(pick(incoming.getDescription(), existing.getDescription(), patch));
        existing.setDatabase(pick(incoming.getDatabase(), existing.getDatabase(), patch));
        existing.setScript(pick(incoming.getScript(), existing.getScript(), patch));
        existing.setDatabaseUrl(pick(incoming.getDatabaseUrl(), existing.getDatabaseUrl(), patch));
        existing.setService(pick(incoming.getService(), existing.getService(), patch));
        existing.setServicePath(pick(incoming.getServicePath(), existing.getServicePath(), patch));
        existing.setMethod(pick(incoming.getMethod(), existing.getMethod(), patch));
        existing.setDatabaseCsv(pick(incoming.getDatabaseCsv(), existing.getDatabaseCsv(), patch));
        existing.setCsvUrl(pick(incoming.getCsvUrl(), existing.getCsvUrl(), patch));
        // The envelope column accepts null.
        if (incoming.getEnvelope() != null || !patch) {
            existing.setEnvelope(incoming.getEnvelope());
        }
        if (incoming.getPrivateData() != null) {
            existing.setPrivateData(incoming.getPrivateData());
        }
        if (incoming.getSeparator() != null) {
            existing.setSeparator(incoming.getSeparator());
        } else if (!patch) {
            existing.setSeparator(",");
        }
        if (ignoreFirstLine != null) {
            existing.setIgnoreFirstLine(ignoreFirstLine);
        } else if (!patch) {
            existing.setIgnoreFirstLine(false);
        }
        existing.setLastModifier(login);

        List<TestDataLibData> newSubData = null;
        if (incoming.getSubDataLib() != null) {
            newSubData = buildSubData(id, incoming.getSubDataLib(), readSubData(id), patch);
            checkKeyEntry(newSubData);
        }

        check(this.update(existing));
        if (newSubData != null) {
            check(testDataLibDataService.compareListAndUpdateInsertDeleteElements(id, newSubData));
        }
        return readByKeyAPI(id);
    }

    @Override
    public void deleteAPI(Integer id) {
        TestDataLib lib = readLib(id);
        List<TestDataLibData> subData = readSubData(id);
        if (!subData.isEmpty()) {
            check(testDataLibDataService.deleteList(subData));
        }
        check(this.delete(lib));
    }

    private List<TestDataLibData> buildSubData(Integer libId, List<TestDataLibData> incoming, List<TestDataLibData> existing, boolean patch) {
        List<TestDataLibData> result = new ArrayList<>();
        List<String> handled = new ArrayList<>();
        for (TestDataLibData in : incoming) {
            String name = nullToEmpty(in.getSubData());
            if (handled.contains(name)) {
                throw new InvalidRequestException("Duplicated subData: '" + name + "'");
            }
            handled.add(name);
            TestDataLibData target = existing.stream().filter(e -> name.equals(nullToEmpty(e.getSubData()))).findFirst().orElse(null);
            boolean isNew = target == null;
            if (isNew) {
                target = new TestDataLibData();
                target.setTestDataLibID(libId);
                target.setSubData(name);
                target.setEncrypt("N");
            }
            boolean merge = patch && !isNew;
            target.setValue(pick(in.getValue(), target.getValue(), merge));
            target.setColumn(pick(in.getColumn(), target.getColumn(), merge));
            target.setParsingAnswer(pick(in.getParsingAnswer(), target.getParsingAnswer(), merge));
            target.setColumnPosition(pick(in.getColumnPosition(), target.getColumnPosition(), merge));
            target.setDescription(pick(in.getDescription(), target.getDescription(), merge));
            if (in.getEncrypt() != null) {
                target.setEncrypt(in.getEncrypt());
            }
            result.add(target);
        }
        if (patch) {
            // Merge mode keeps the entries that are not mentioned.
            for (TestDataLibData e : existing) {
                if (!handled.contains(nullToEmpty(e.getSubData()))) {
                    result.add(e);
                }
            }
        }
        return result;
    }

    private TestDataLib readLib(Integer id) {
        AnswerItem<TestDataLib> answer = this.readByKey(id);
        if (answer.getItem() == null) {
            throw new EntityNotFoundException(TestDataLib.class, "id", id);
        }
        return answer.getItem();
    }

    private List<TestDataLibData> readSubData(Integer id) {
        AnswerList<TestDataLibData> answer = testDataLibDataService.readByVarious(id, null, null, null);
        return answer.getDataList() == null ? new ArrayList<>() : new ArrayList<>(answer.getDataList());
    }

    private static void checkType(String type) {
        if (type == null || !TYPES.contains(type)) {
            throw new InvalidRequestException("Unsupported type: " + type + ". Supported types: " + TYPES);
        }
    }

    /**
     * The engine reads the entry whose subData is empty whenever a property names the library without a
     * sub-data, and fails the execution when it is missing.
     */
    private static void checkKeyEntry(List<TestDataLibData> subData) {
        for (TestDataLibData data : subData) {
            if (nullToEmpty(data.getSubData()).isEmpty()) {
                return;
            }
        }
        throw new InvalidRequestException("A data library needs a key entry: one subData whose 'subData' is an empty string");
    }

    private static void fillEmpty(TestDataLibData data) {
        data.setValue(nullToEmpty(data.getValue()));
        data.setColumn(nullToEmpty(data.getColumn()));
        data.setParsingAnswer(nullToEmpty(data.getParsingAnswer()));
        data.setColumnPosition(nullToEmpty(data.getColumnPosition()));
        data.setDescription(nullToEmpty(data.getDescription()));
    }

    private static String pick(String incoming, String existing, boolean patch) {
        if (incoming != null) {
            return incoming;
        }
        return patch ? existing : "";
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static void check(Answer answer) {
        if (!answer.isCodeStringEquals("OK")) {
            throw new FailedInsertOperationException(answer.getMessageDescription());
        }
    }
}
