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
package org.cerberus.service.datalib.impl;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cerberus.crud.dao.ITestCaseExecutionDataDAO;
import org.cerberus.crud.entity.CountryEnvironmentDatabase;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestDataLib;
import org.cerberus.crud.entity.TestDataLibData;
import org.cerberus.crud.service.ICountryEnvironmentDatabaseService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITestDataLibDataService;
import org.cerberus.crud.service.ITestDataLibService;
import org.cerberus.engine.entity.SOAPExecution;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.datalib.IDataLibService;
import org.cerberus.service.file.IFileService;
import org.cerberus.service.soap.ISoapService;
import org.cerberus.service.sql.ISQLService;
import org.cerberus.service.xmlunit.IXmlUnitService;
import org.cerberus.util.SoapUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.XmlUtil;
import org.cerberus.util.XmlUtilException;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 *
 * @author bcivel
 */
@Service
public class DataLibService implements IDataLibService {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DataLibService.class);

    @Autowired
    IFileService fileService;
    @Autowired
    private ITestCaseExecutionDataDAO testCaseExecutionDataDAO;
    @Autowired
    private IParameterService parameterService;
    @Autowired
    private ITestDataLibService testDataLibService;
    @Autowired
    private ITestDataLibDataService testDataLibDataService;
    @Autowired
    private ICountryEnvironmentDatabaseService countryEnvironmentDatabaseService;
    @Autowired
    private ISQLService sqlService;
    @Autowired
    private ISoapService soapService;
    @Autowired
    private IXmlUnitService xmlUnitService;

    @Override
    public AnswerItem<HashMap<String, String>> getFromDataLib(TestDataLib lib, TestCaseCountryProperties testCaseCountryProperty, TestCaseExecution tCExecution) {
        AnswerItem<HashMap<String, String>> resultColumns;
        AnswerList<HashMap<String, String>> resultData;
        AnswerItem<HashMap<String, String>> result;
        MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS);

        /**
         * Gets the list of columns to get from TestDataLibData.
         */
        resultColumns = getSubDataFromType(lib);
        HashMap<String, String> columnList = null;
        //Manage error message.
        if (resultColumns.getResultMessage().getCode() == MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_SUBDATA.getCode()) {
            columnList = resultColumns.getItem();
        } else if (resultColumns.getResultMessage().getCode() == MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SUBDATA.getCode()) {
            result = new AnswerItem();
            result.setItem(null);
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_GLOBAL_SUBDATAISSUE);
            msg.setDescription(msg.getDescription().replace("%SUBDATAMESSAGE%", resultColumns.getMessageDescription()));
            result.setResultMessage(msg);
            return result;
        }

        /**
         * Get List of DataObject in a format List<Map<String>>
         */
        int rowLimit = testCaseCountryProperty.getRowLimit();
        if (testCaseCountryProperty.getNature().equalsIgnoreCase(TestCaseCountryProperties.NATURE_STATIC)) { // If Nature of the property is static, we don't need to getch more than 1 record.
            rowLimit = 1;
        }
        resultData = getDataObjectList(lib, columnList, rowLimit, tCExecution.getApplication().getSystem(), tCExecution.getCountryEnvironmentParameters().getCountry(), tCExecution.getCountryEnvironmentParameters().getEnvironment());

        //Manage error message.
        if (resultData.getResultMessage().getCode() == MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_DATA.getCode()) {
//
        } else if (resultData.getResultMessage().getCode() == MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_GENERIC_NODATA.getCode()) {
            result = new AnswerItem();
            result.setItem(null);
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_GLOBAL_NODATA);
            msg.setDescription(msg.getDescription().replace("%DATAMESSAGE%", resultData.getMessageDescription()));
            result.setResultMessage(msg);
            return result;
        } else {
            result = new AnswerItem();
            result.setItem(null);
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_GLOBAL_DATAISSUE);
            msg.setDescription(msg.getDescription().replace("%DATAMESSAGE%", resultData.getMessageDescription()));
            result.setResultMessage(msg);
            return result;
        }

        /**
         * Get the dataObject from the list depending on the nature
         */
        result = filterWithNature(testCaseCountryProperty.getNature(), resultData, tCExecution, testCaseCountryProperty);

        //Manage error message.
        if (result.getResultMessage().getCode() == MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_NATURE.getCode()) {
            msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_GLOBAL);
            msg.setDescription(msg.getDescription().replace("%DATAMESSAGE%", resultData.getMessageDescription())
                    .replace("%FILTERNATUREMESSAGE%", result.getMessageDescription()).replace("%RESULT%", result.getItem().toString()));
            result.setResultMessage(msg);

        } else if (result.getResultMessage().getCode() == MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_GENERIC_NATURENOMORERECORD.getCode()) {
            //if the script does not return 
            result.setItem(null);
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_GLOBAL_NODATALEFT);
            msg.setDescription(msg.getDescription().replace("%DATAMESSAGE%", resultData.getMessageDescription())
                    .replace("%FILTERNATUREMESSAGE%", result.getMessageDescription()));
            result.setResultMessage(msg);

        } else {
            //other error had occured
            result.setItem(null);
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_GLOBAL_GENERIC);
            msg.setDescription(msg.getDescription().replace("%DATAMESSAGE%", resultData.getMessageDescription())
                    .replace("%FILTERNATUREMESSAGE%", result.getMessageDescription()));
            result.setResultMessage(msg);

        }

        return result;
    }

    /**
     * This method route to the method regarding the nature
     *
     * @param nature : Nature of the property
     * @param dataObjectList : List of dataObject
     * @param tCExecution : TestCaseExecution
     * @param testCaseCountryProperties : TestCaseCountryProperties
     * @return one item (dataObject) from the dataObjectList
     */
    private AnswerItem<HashMap<String, String>> filterWithNature(String nature, AnswerList dataObjectList, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperties) {
        switch (nature) {
            case TestCaseCountryProperties.NATURE_STATIC:
                return filterWithNatureSTATIC(dataObjectList);
            case TestCaseCountryProperties.NATURE_RANDOM:
                return filterWithNatureRANDOM(dataObjectList);
            case TestCaseCountryProperties.NATURE_RANDOMNEW:
                return filterWithNatureRANDOMNEW(dataObjectList, tCExecution, testCaseCountryProperties);
            case TestCaseCountryProperties.NATURE_NOTINUSE:
                return filterWithNatureNOTINUSE(dataObjectList, tCExecution, testCaseCountryProperties);
        }
        //TODO throw exception when Nature not known
        return null;
    }

    @Override
    public AnswerItem<HashMap<String, String>> filterWithNatureSTATIC(AnswerList<HashMap<String, String>> dataObjectList) {
        AnswerItem<HashMap<String, String>> result = new AnswerItem();
        result.setItem((HashMap<String, String>) dataObjectList.getDataList().get(0));
        result.setResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_NATURESTATIC));
        return result;
    }

    @Override
    public AnswerItem<HashMap<String, String>> filterWithNatureRANDOM(AnswerList<HashMap<String, String>> dataObjectList) {
        AnswerItem<HashMap<String, String>> result = new AnswerItem();
        Random r = new Random();
        int position = r.nextInt(dataObjectList.getDataList().size());
        result.setItem((HashMap<String, String>) dataObjectList.getDataList().get(position));
        result.setResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_NATURERANDOM)
                .resolveDescription("POS", Integer.toString(position)).resolveDescription("TOTALPOS", Integer.toString(dataObjectList.getDataList().size())));
        return result;
    }

    @Override
    public AnswerItem<HashMap<String, String>> filterWithNatureRANDOMNEW(AnswerList<HashMap<String, String>> dataObjectList, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseProperties) {
        AnswerItem<HashMap<String, String>> result = new AnswerItem();
        List<HashMap<String, String>> list;
        int initNB = dataObjectList.getDataList().size();
        // We get the list of values that are already used.
        List<String> pastValues = this.testCaseExecutionDataDAO.getPastValuesOfProperty(tCExecution.getId(),
                testCaseProperties.getProperty(), tCExecution.getTest(), tCExecution.getTestCase(),
                tCExecution.getCountryEnvParam().getBuild(), tCExecution.getEnvironmentData(), tCExecution.getCountry());

        int removedNB = 0;
        // We save all rows that needs to be removed to listToremove.
        List<Map<String, String>> listToremove = new ArrayList<Map<String, String>>();
        list = dataObjectList.getDataList();
        for (String valueToRemove : pastValues) {
            for (Map<String, String> curentRow : list) {
                if (curentRow.get("").equals(valueToRemove)) {
                    if (true) {
                        listToremove.add(curentRow);
                        removedNB++;
                    }
                }
            }
        }
        // We remove all listToremove entries from list.
        list.removeAll(listToremove);

        if (list != null && !list.isEmpty()) { // We pick a random value from the left entries of the list.
            Random r = new Random();
            int position = r.nextInt(list.size());
            result.setItem(list.get(position));
            result.setResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_NATURERANDOMNEW)
                    .resolveDescription("TOTNB", Integer.toString(initNB))
                    .resolveDescription("REMNB", Integer.toString(removedNB))
                    .resolveDescription("POS", Integer.toString(position + 1))
                    .resolveDescription("TOTALPOS", Integer.toString(list.size())));
        } else { // No more entries available.
            result.setResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_RANDOMNEW_NOMORERECORD)
                    .resolveDescription("TOTNB", Integer.toString(initNB)));
        }
        return result;
    }

    @Override
    public AnswerItem<HashMap<String, String>> filterWithNatureNOTINUSE(AnswerList<HashMap<String, String>> dataObjectList, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty) {
        AnswerItem<HashMap<String, String>> result = new AnswerItem();
        List<HashMap<String, String>> list = dataObjectList.getDataList();
        int initNB = dataObjectList.getDataList().size();
        // We get the list of values that are already used.
        Integer peTimeout;
        try {
            peTimeout = Integer.valueOf(parameterService.findParameterByKey("cerberus_notinuse_timeout", tCExecution.getApplication().getSystem()).getValue());

            List<String> pastValues = this.testCaseExecutionDataDAO.getInUseValuesOfProperty(tCExecution.getId(), testCaseCountryProperty.getProperty(), tCExecution.getEnvironmentData(), tCExecution.getCountry(), peTimeout);

            int removedNB = 0;
            // We save all rows that needs to be removed to listToremove.
            List<Map<String, String>> listToremove = new ArrayList<>();
            for (String valueToRemove : pastValues) {
                for (Map<String, String> curentRow : list) {
                    if (curentRow.get("").equals(valueToRemove)) {
                        if (true) {
                            listToremove.add(curentRow);
                            removedNB++;
                        }
                    }
                }
            }
            // We remove all listToremove entries from list.
            list.removeAll(listToremove);

            if (list != null && !list.isEmpty()) { // We pick a random value from the left entries of the list.
                Random r = new Random();
                int position = r.nextInt(list.size());
                result.setItem(list.get(position));
                result.setResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_NATURENOTINUSE)
                        .resolveDescription("TOTNB", Integer.toString(initNB))
                        .resolveDescription("REMNB", Integer.toString(removedNB))
                        .resolveDescription("POS", Integer.toString(position + 1))
                        .resolveDescription("TOTALPOS", Integer.toString(list.size())));
            } else { // No more entries available.
                result.setResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_NOTINUSE_NOMORERECORD)
                        .resolveDescription("TOTNB", Integer.toString(initNB)));
            }
        } catch (CerberusException ex) {
            Logger.getLogger(DataLibService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * Get the list of subData
     *
     * @param lib
     * @return
     */
    private AnswerItem<HashMap<String, String>> getSubDataFromType(TestDataLib lib) {
        AnswerList answerData = new AnswerList();
        AnswerItem<HashMap<String, String>> result = new AnswerItem();
        MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS);

        List<TestDataLibData> objectDataList = new ArrayList<TestDataLibData>();
        HashMap<String, String> row = new HashMap<String, String>();

        switch (lib.getType()) {

            case TestDataLib.TYPE_CSV:
                answerData = testDataLibDataService.readByVarious(lib.getTestDataLibID(), "N", null, null);
                objectDataList = answerData.getDataList();
                for (TestDataLibData tdld : objectDataList) {
                    row.put(tdld.getColumn(), tdld.getSubData());
                }
                result.setItem(row);
                break;

            case TestDataLib.TYPE_SQL:
                answerData = testDataLibDataService.readByVarious(lib.getTestDataLibID(), "N", null, null);
                if ((answerData.getResultMessage().getCode() == MessageEventEnum.DATA_OPERATION_OK.getCode()) && !answerData.getDataList().isEmpty()) {
                    objectDataList = answerData.getDataList();
                    boolean missingKey = true;
                    for (TestDataLibData tdld : objectDataList) {
                        row.put(tdld.getColumn(), tdld.getSubData());
                        if (tdld.getSubData().equalsIgnoreCase("")) {
                            missingKey = false;
                        }
                    }
                    result.setItem(row);
                    if (missingKey) {
                        msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SUBDATASQLNOKEY);
                        result.setResultMessage(msg);

                    } else {
                        msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_SUBDATA);
                        msg.setDescription(msg.getDescription().replace("%NBROW%", String.valueOf(answerData.getDataList().size())));
                        result.setResultMessage(msg);
                    }

                } else if ((answerData.getResultMessage().getCode() == MessageEventEnum.DATA_OPERATION_OK.getCode()) && answerData.getDataList().isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_NOSUBDATASQL);
                    result.setResultMessage(msg);

                } else {
                    msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SUBDATASQL);
                    result.setResultMessage(msg);

                }
                break;

            case TestDataLib.TYPE_SOAP:
                answerData = testDataLibDataService.readByVarious(lib.getTestDataLibID(), null, "N", null);
                if ((answerData.getResultMessage().getCode() == MessageEventEnum.DATA_OPERATION_OK.getCode()) && !answerData.getDataList().isEmpty()) {

                    objectDataList = answerData.getDataList();
                    boolean missingKey = true;
                    for (TestDataLibData tdld : objectDataList) {
                        row.put(tdld.getSubData(), tdld.getParsingAnswer());
                        if (tdld.getSubData().equalsIgnoreCase("")) {
                            missingKey = false;
                        }
                    }
                    result.setItem(row);
                    if (missingKey) {
                        msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SUBDATASOAPNOKEY);
                        result.setResultMessage(msg);

                    } else {
                        msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_SUBDATA);
                        msg.setDescription(msg.getDescription().replace("%NBROW%", String.valueOf(answerData.getDataList().size())));
                        result.setResultMessage(msg);
                    }

                } else if ((answerData.getResultMessage().getCode() == MessageEventEnum.DATA_OPERATION_OK.getCode()) && answerData.getDataList().isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_NOSUBDATASOAP);
                    result.setResultMessage(msg);

                } else {
                    msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SUBDATASOAP);
                    result.setResultMessage(msg);

                }
                break;

            case TestDataLib.TYPE_STATIC:
                // For static Type, there is no need to fetch the subdata.
                msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_SUBDATA);
                result.setResultMessage(msg);
                result.setItem(null);
                break;
        }
        return result;
    }

    /**
     * Get the dataObject List depending on the type
     *
     * @param lib
     * @param columnList
     * @return
     */
    private AnswerList<HashMap<String, String>> getDataObjectList(TestDataLib lib, HashMap<String, String> columnList, int rowLimit, String system, String country, String environment) {
        AnswerList result = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS);
        CountryEnvironmentDatabase countryEnvironmentDatabase;

        switch (lib.getType()) {
            case TestDataLib.TYPE_CSV:
                result = fileService.parseCSVFile(lib.getCsvUrl(), lib.getSeparator(), columnList);
                break;

            case TestDataLib.TYPE_SQL:
                String connectionName;
                List<HashMap<String, String>> list;
                String db = lib.getDatabase();
                try {

                    if (StringUtil.isNullOrEmpty(db)) {
                        msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SQLDATABASEEMPTY);

                    } else {

                        countryEnvironmentDatabase = countryEnvironmentDatabaseService.convert(countryEnvironmentDatabaseService.readByKey(system,
                                country, environment, db));
                        if (countryEnvironmentDatabase == null) {
                            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SQLDATABASENOTCONFIGURED);
                            msg.setDescription(msg.getDescription().replace("%SYSTEM%", system)
                                    .replace("%COUNTRY%", country).replace("%ENV%", environment)
                                    .replace("%DATABASE%", db));

                        } else {

                            connectionName = countryEnvironmentDatabase.getConnectionPoolName();

                            if (!(StringUtil.isNullOrEmpty(connectionName))) {

                                Integer sqlTimeout = parameterService.getParameterByKey("cerberus_propertyexternalsql_timeout", system, 60);
                                //performs a query that returns several rows containing n columns
                                AnswerList responseList = sqlService.queryDatabaseNColumns(connectionName, lib.getScript(), rowLimit, sqlTimeout, system, columnList);

                                //if the query returns sucess then we can get the data
                                if (responseList.getResultMessage().getCode() == MessageEventEnum.PROPERTY_SUCCESS_SQL.getCode()) {

                                    list = responseList.getDataList();

                                    if (list != null && !list.isEmpty()) {
                                        result.setDataList(list);
                                        msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_SQL);
                                        msg.setDescription(msg.getDescription().replace("%NBROW%", String.valueOf(result.getDataList().size())));

                                    } else {
                                        msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SQLDATABASENODATA);
                                    }

                                } else {
                                    msg = responseList.getResultMessage();
                                }

                                msg.setDescription(msg.getDescription().replace("%DATABASE%", db));
                                msg.setDescription(msg.getDescription().replace("%SQL%", lib.getScript()));
                                msg.setDescription(msg.getDescription().replace("%JDBCPOOLNAME%", connectionName));

                            } else {
                                msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SQLDATABASEJDBCRESSOURCEMPTY);
                                msg.setDescription(msg.getDescription().replace("%SYSTEM%", system).replace("%COUNTRY%", country).replace("%ENV%", environment).replace("%DATABASE%", db));
                            }
                        }
                    }
                } catch (CerberusException ex) {
                    msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SQLDATABASENOTCONFIGURED);
                    msg.setDescription(msg.getDescription().replace("%SYSTEM%", system).replace("%COUNTRY%", country).replace("%ENV%", environment).replace("%DATABASE%", db));
                }
                result.setResultMessage(msg);
                break;

            case TestDataLib.TYPE_SOAP:
                SOAPExecution executionSoap = new SOAPExecution();
                HashMap<String, String> resultHash = new HashMap<>();
                List<HashMap<String, String>> listResult = new ArrayList<HashMap<String, String>>();

                // Temporary list of string.
                List<String> listTemp1 = null;

                // String containing the XML
                String xmlResponseString = "";

                /**
                 * Before making the call we check if the Service Path is
                 * already a propper URL. If it is not, we prefix with the
                 * SoapUrl defined from corresponding database. This is used to
                 * get the data from the correct environment.
                 */
                String servicePath = lib.getServicePath();
                LOG.debug("Service Path : " + lib.getServicePath());
                if (!StringUtil.isURL(servicePath)) {
                    // Url is not valid, we try to get the corresponding DatabaseURL SoapURL to prefix.
                    if (!(StringUtil.isNullOrEmpty(lib.getDatabaseUrl()))) {

                        try {
                            countryEnvironmentDatabase = this.countryEnvironmentDatabaseService.convert(this.countryEnvironmentDatabaseService.readByKey(system,
                                    country, environment, lib.getDatabaseUrl()));
                            if (countryEnvironmentDatabase == null) {
                                msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SOAP_URLKOANDDATABASESOAPURLNOTEXIST);
                                msg.setDescription(msg.getDescription()
                                        .replace("%SERVICEURL%", lib.getServicePath())
                                        .replace("%SYSTEM%", system)
                                        .replace("%COUNTRY%", country)
                                        .replace("%ENV%", environment)
                                        .replace("%DB%", lib.getDatabaseUrl()));
                                result.setResultMessage(msg);
                                return result;

                            } else {
                                String soapURL = countryEnvironmentDatabase.getSoapUrl();
                                if (StringUtil.isNullOrEmpty(soapURL)) {
                                    msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SOAP_URLKOANDDATABASESOAPURLEMPTY);
                                    msg.setDescription(msg.getDescription()
                                            .replace("%SERVICEURL%", lib.getServicePath())
                                            .replace("%SYSTEM%", system)
                                            .replace("%COUNTRY%", country)
                                            .replace("%ENV%", environment)
                                            .replace("%DB%", lib.getDatabaseUrl()));
                                    result.setResultMessage(msg);
                                    return result;
                                }
                                // soapURL from database is not empty so we prefix the Service URL with it.
                                servicePath = soapURL + lib.getServicePath();

                                if (!StringUtil.isURL(servicePath)) {
                                    msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SOAP_URLKO);
                                    msg.setDescription(msg.getDescription()
                                            .replace("%SERVICEURL%", servicePath)
                                            .replace("%SOAPURL%", soapURL)
                                            .replace("%SERVICEPATH%", lib.getServicePath())
                                            .replace("%ENTRY%", lib.getName())
                                            .replace("%ENTRYID%", lib.getTestDataLibID().toString()));
                                    result.setResultMessage(msg);
                                    return result;

                                }

                            }

                        } catch (CerberusException ex) {
                            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SOAP_URLKOANDDATABASESOAPURLNOTEXIST);
                            msg.setDescription(msg.getDescription()
                                    .replace("%SERVICEURL%", lib.getServicePath())
                                    .replace("%SYSTEM%", system)
                                    .replace("%COUNTRY%", country)
                                    .replace("%ENV%", environment)
                                    .replace("%DB%", lib.getDatabaseUrl()));
                            result.setResultMessage(msg);
                            return result;
                        }

                    } else { // URL is not valid and DatabaseUrl is not defined.
                        msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SOAP_URLKOANDNODATABASE);
                        msg.setDescription(msg.getDescription()
                                .replace("%SERVICEURL%", lib.getServicePath())
                                .replace("%ENTRY%", lib.getName())
                                .replace("%ENTRYID%", lib.getTestDataLibID().toString()));
                        result.setResultMessage(msg);
                        return result;
                    }
                }

                // SOAP Call is made here.
                AnswerItem ai = soapService.callSOAP(lib.getEnvelope(), servicePath, lib.getMethod(), null);
                msg = ai.getResultMessage();

                //if the call returns success then we can process the soap ressponse
                if (msg.getCode() == MessageEventEnum.ACTION_SUCCESS_CALLSOAP.getCode()) {

                    executionSoap = (SOAPExecution) ai.getItem();
                    Document xmlDocument = xmlUnitService.getXmlDocument(SoapUtil.convertSoapMessageToString(executionSoap.getSOAPResponse()));

                    // Call successful so we can start to parse the result and build RawData per columns from subdata entries.
                    try {

                        // We get the content of the XML in order to report it log messages.
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        executionSoap.getSOAPResponse().writeTo(out);
                        xmlResponseString = new String(out.toByteArray());

                        /**
                         * This Step will calculate hashTemp1 : Hash of List
                         * from the XML.
                         */
                        // Will contain the nb of row of the target list of Hash.
                        int finalnbRow = 0;
                        // Will contain the result of the XML parsing.
                        HashMap<String, List<String>> hashTemp1 = new HashMap<>();

                        if (columnList.isEmpty()) { // No subdata could be found on the testdatalib.
                            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SOAP_NOSUBDATA);
                            msg.setDescription(msg.getDescription()
                                    .replace("%ENTRY%", lib.getName())
                                    .replace("%ENTRYID%", lib.getTestDataLibID().toString()));
                        } else {

                            for (Map.Entry<String, String> entry : columnList.entrySet()) {
                                String column = entry.getKey();
                                String name = entry.getValue();

                                String subDataColumnToTreat = entry.getKey(); // SubData
                                String subDataParsingAnswer = entry.getValue(); // Parsing Answer
                                listTemp1 = new ArrayList<>();

                                try {

                                    // We try to parse the XML with the subdata Parsing Answer.
                                    NodeList candidates = XmlUtil.evaluate(xmlDocument, subDataParsingAnswer);

                                    if (candidates.getLength() > 0) {

                                        for (int i = 0; i < candidates.getLength(); i++) { // Loop on all Values that match in XML.

                                            //We get the value from XML
                                            String value = candidates.item(i).getNodeValue();

                                            if (value == null) { // No value found.
                                                if (candidates.item(i) != null) {
                                                    msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SOAP_CHECK_XPATH);
                                                    msg.setDescription(msg.getDescription()
                                                            .replace("%XPATH%", subDataParsingAnswer)
                                                            .replace("%SUBDATA%", subDataColumnToTreat)
                                                            .replace("%ENTRY%", lib.getName())
                                                            .replace("%ENTRYID%", lib.getTestDataLibID().toString()));
                                                } else {
                                                    //no elements were returned by the XPATH expression
                                                    msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SOAP_XML_NOTFOUND);
                                                    msg.setDescription(msg.getDescription()
                                                            .replace("%XPATH%", subDataParsingAnswer)
                                                            .replace("%SUBDATA%", subDataColumnToTreat)
                                                            .replace("%ENTRY%", lib.getName())
                                                            .replace("%XMLCONTENT%", xmlResponseString)
                                                            .replace("%ENTRYID%", lib.getTestDataLibID().toString())
                                                    );
                                                }
                                            } else { // Value were found we add it to the current list.

                                                listTemp1.add(value);

                                            }
                                        }

                                        // Add the Subdata with associated list in the HashMap.
                                        hashTemp1.put(subDataColumnToTreat, listTemp1);

                                        // Getting the nb of row of the final result. (Max of all the Subdata retrieved from the XML)
                                        if (listTemp1.size() > finalnbRow) {
                                            finalnbRow = listTemp1.size();
                                        }

                                    } else {
                                        //no elements were returned by the XPATH expression
                                        msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SOAP_XML_NOTFOUND);
                                        msg.setDescription(msg.getDescription()
                                                .replace("%XPATH%", subDataParsingAnswer)
                                                .replace("%SUBDATA%", subDataColumnToTreat)
                                                .replace("%ENTRY%", lib.getName())
                                                .replace("%XMLCONTENT%", xmlResponseString)
                                                .replace("%ENTRYID%", lib.getTestDataLibID().toString())
                                        );
                                    }
                                } catch (XmlUtilException ex) {
                                    msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SOAP_XMLEXCEPTION);
                                    msg.setDescription(msg.getDescription()
                                            .replace("%XPATH%", subDataParsingAnswer)
                                            .replace("%SUBDATA%", subDataColumnToTreat)
                                            .replace("%ENTRY%", lib.getName())
                                            .replace("%ENTRYID%", lib.getTestDataLibID().toString())
                                            .replace("%REASON%", ex.toString() + " Detail answer " + xmlResponseString));
                                }
                            }

                            /**
                             * This Step will convert hashTemp1 (Hash of List)
                             * to target listResult (list of Hash).
                             */
                            if (msg.getCode() == MessageEventEnum.ACTION_SUCCESS_CALLSOAP.getCode()) {
                                for (int i = 0; i < finalnbRow; i++) { // Loop on all Values that match in XML.
                                    resultHash = new HashMap<String, String>();
                                    for (Map.Entry<String, String> entry : columnList.entrySet()) { // Loop on all SubData of the TestDataLib.
                                        listTemp1 = hashTemp1.get(entry.getKey());
                                        if (listTemp1 != null) {
                                            if (i < listTemp1.size()) {
                                                resultHash.put(entry.getKey(), listTemp1.get(i));
                                            } else {
                                                resultHash.put(entry.getKey(), "");
                                            }
                                        }
                                    }
                                    listResult.add(resultHash);
                                }
                            }

                            /**
                             * This Step will pick the correct listResult (list
                             * of Hash) from the type of Property.
                             */
                            if (msg.getCode() == MessageEventEnum.ACTION_SUCCESS_CALLSOAP.getCode()) {
                                result.setDataList(listResult);
                                msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_SOAP);
                                msg.setDescription(msg.getDescription().replace("%NBROW%", String.valueOf(result.getDataList().size()))
                                        .replace("%URL%", servicePath).replace("%OPER%", lib.getMethod()));

                            }

                        }

                        // Save the result to the Lib object.
                    } catch (Exception ex) {
                        msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SOAP_XMLEXCEPTION);
                        msg.setDescription(msg.getDescription()
                                .replace("%XPATH%", lib.getSubDataParsingAnswer())
                                .replace("%SUBDATA%", "")
                                .replace("%REASON%", ex.toString()));
                    }

                } else {
                    String soapError = msg.getDescription();
                    msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SOAP_SOAPCALLFAILED);
                    msg.setDescription(msg.getDescription()
                            .replace("%SOAPERROR%", soapError));

                }
                msg.setDescription(msg.getDescription()
                        .replace("%SERVICE%", servicePath)
                        .replace("%OPERATION%", lib.getMethod()));
                result.setResultMessage(msg);
                break;

            case TestDataLib.TYPE_STATIC:
                result = testDataLibService.readSTATICWithSubdataByCriteria(lib.getName(), lib.getSystem(), lib.getCountry(), lib.getEnvironment(), rowLimit, system);
                //if the sql service returns a success message then we can process it
                if ((result.getResultMessage().getCode() == MessageEventEnum.DATA_OPERATION_OK.getCode()) && !result.getDataList().isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_STATIC);
                    msg.setDescription(msg.getDescription().replace("%NBROW%", String.valueOf(result.getDataList().size())));
                    result.setResultMessage(msg);

                } else if ((result.getResultMessage().getCode() == MessageEventEnum.DATA_OPERATION_OK.getCode()) && result.getDataList().isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_STATICNODATA);
                    msg.setDescription(msg.getDescription().replace("%SYSTEM%", lib.getSystem())
                            .replace("%ENV%", lib.getEnvironment()).replace("%COUNTRY%", lib.getCountry()));
                    result.setResultMessage(msg);

                } else {
                    msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_STATIC);
                    msg.setDescription(msg.getDescription().replace("%SYSTEM%", lib.getSystem())
                            .replace("%ENV%", lib.getEnvironment()).replace("%COUNTRY%", lib.getCountry()));
                    result.setResultMessage(msg);

                }
                break;

        }

        return result;
    }

}
