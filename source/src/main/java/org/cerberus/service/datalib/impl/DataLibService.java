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
package org.cerberus.service.datalib.impl;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.AppService;
import org.cerberus.crud.entity.CountryEnvironmentDatabase;
import org.cerberus.crud.entity.Parameter;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseExecutionData;
import org.cerberus.crud.entity.TestDataLib;
import org.cerberus.crud.entity.TestDataLibData;
import org.cerberus.crud.service.ICountryEnvironmentDatabaseService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITestCaseExecutionDataService;
import org.cerberus.crud.service.ITestDataLibDataService;
import org.cerberus.crud.service.ITestDataLibService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.execution.IRecorderService;
import org.cerberus.engine.gwt.IVariableService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.appservice.IServiceService;
import org.cerberus.service.datalib.IDataLibService;
import org.cerberus.service.file.IFileService;
import org.cerberus.service.json.IJsonService;
import org.cerberus.service.sql.ISQLService;
import org.cerberus.service.xmlunit.IXmlUnitService;
import org.cerberus.util.StringUtil;
import org.cerberus.util.XmlUtil;
import org.cerberus.util.XmlUtilException;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 *
 * @author bcivel
 * @author vertigo17
 *
 */
@Service
public class DataLibService implements IDataLibService {

    private static final Logger LOG = LogManager.getLogger(DataLibService.class);

    @Autowired
    private IFileService fileService;
    @Autowired
    private ITestCaseExecutionDataService testCaseExecutionDataService;
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
    private IServiceService serviceService;
    @Autowired
    private IXmlUnitService xmlUnitService;
    @Autowired
    private IJsonService jsonService;
    @Autowired
    private IRecorderService recorderService;
    @Autowired
    private IVariableService variableService;

    @Override
    public AnswerList<HashMap<String, String>> getFromDataLib(TestDataLib lib, TestCaseCountryProperties testCaseCountryProperty,
            TestCaseExecution tCExecution, TestCaseExecutionData testCaseExecutionData) {
        AnswerItem<HashMap<String, String>> resultColumns;
        AnswerList<HashMap<String, String>> resultData;
        AnswerList<HashMap<String, String>> result;
        MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS);

        // Length contains the nb of rows that the result must fetch. If defined at 0 we force at 1.
        Integer nbRowsRequested = 0;
        try {
            nbRowsRequested = Integer.parseInt(testCaseExecutionData.getLength());
            if (nbRowsRequested < 1) {
                nbRowsRequested = 1;
            }
        } catch (NumberFormatException e) {
            LOG.error(e.toString(), e);
        }

        /**
         * Gets the list of columns (subdata) to get from TestDataLibData.
         */
        resultColumns = getSubDataFromType(lib);
        HashMap<String, String> columnList = null;
        //Manage error message.
        if (resultColumns.getResultMessage().getCode() == MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_SUBDATA.getCode()) {
            AnswerItem answerDecode = new AnswerItem<>();
            columnList = resultColumns.getItem();
            // Now that we have the list of column with subdata and value, we can try to decode it.
            if (columnList != null) {
                for (Map.Entry<String, String> entry : columnList.entrySet()) { // Loop on all Column in order to decode all values.
                    String eKey = entry.getKey(); // SubData
                    String eValue = entry.getValue(); // Parsing Answer
                    try {
                        answerDecode = variableService.decodeStringCompletly(eValue, tCExecution, null, false);
                        columnList.put(eKey, (String) answerDecode.getItem());

                        if (!(answerDecode.isCodeStringEquals("OK"))) {
                            // If anything wrong with the decode --> we stop here with decode message in the action result.
                            result = new AnswerList<>();
                            result.setDataList(null);
                            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_GLOBAL_SUBDATAISSUE);
                            msg.setDescription(msg.getDescription().replace("%SUBDATAMESSAGE%", answerDecode.getMessageDescription().replace("%FIELD%", "Column value '" + eValue + "'")));
                            result.setResultMessage(msg);
                            LOG.debug("Datalib interupted due to decode 'column value' Error.");
                            return result;
                        }
                    } catch (CerberusEventException cex) {
                        LOG.warn(cex);
                    }
                }
            }

        } else if (resultColumns.getResultMessage().getCode() == MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SUBDATA.getCode()) {
            result = new AnswerList<>();
            result.setDataList(null);
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_GLOBAL_SUBDATAISSUE);
            msg.setDescription(msg.getDescription().replace("%SUBDATAMESSAGE%", resultColumns.getMessageDescription()));
            result.setResultMessage(msg);
            return result;
        }

        /**
         * Get List of DataObject in a format List<Map<String, String>> - 1 item
         * per row with key = column and value = content
         */
        int rowLimit = testCaseCountryProperty.getRowLimit();
        if (testCaseCountryProperty.getNature().equalsIgnoreCase(TestCaseCountryProperties.NATURE_STATIC)) { // If Nature of the property is static, we don't need to getch more than reqested record.
            rowLimit = nbRowsRequested;
        }
        resultData = getDataObjectList(lib, columnList, rowLimit, tCExecution, testCaseExecutionData);

        //Manage error message.
        if (resultData.getResultMessage().getCode() == MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_DATA.getCode()) {

            if (resultData.getDataList().size() < nbRowsRequested) { // We check if the data provided is enought to provide the answer.
                result = new AnswerList<>();
                result.setDataList(null);
                msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_GLOBAL_NOTENOUGHTDATA);
                msg.setDescription(msg.getDescription().replace("%DATAMESSAGE%", resultData.getMessageDescription()).replace("%NBREQUEST%", Integer.toString(nbRowsRequested)));
                result.setResultMessage(msg);
                return result;
            }

        } else if (resultData.getResultMessage().getCode() == MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_GENERIC_NODATA.getCode()) {
            result = new AnswerList<>();
            result.setDataList(null);
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_GLOBAL_NODATA);
            msg.setDescription(msg.getDescription().replace("%DATAMESSAGE%", resultData.getMessageDescription()));
            result.setResultMessage(msg);
            return result;
        } else {
            result = new AnswerList<>();
            result.setDataList(null);
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_GLOBAL_DATAISSUE);
            msg.setDescription(msg.getDescription().replace("%DATAMESSAGE%", resultData.getMessageDescription()));
            result.setResultMessage(msg);
            return result;
        }

        /**
         * Filter out the result from requested rows depending on the nature
         */
        result = filterWithNature(testCaseCountryProperty.getNature(), resultData, tCExecution, testCaseCountryProperty, nbRowsRequested);

        //Manage error message.
        if (result.getResultMessage().getCode() == MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_NATURE.getCode()) {
            msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_GLOBAL);
            msg.setDescription(msg.getDescription().replace("%DATAMESSAGE%", resultData.getMessageDescription())
                    .replace("%FILTERNATUREMESSAGE%", result.getMessageDescription()));
            result.setResultMessage(msg);

        } else if (result.getResultMessage().getCode() == MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_GENERIC_NATURENOMORERECORD.getCode()) {
            //if the script does not return 
            result.setDataList(null);
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_GLOBAL_NODATALEFT);
            msg.setDescription(msg.getDescription().replace("%DATAMESSAGE%", resultData.getMessageDescription())
                    .replace("%FILTERNATUREMESSAGE%", result.getMessageDescription()));
            result.setResultMessage(msg);

        } else {
            //other error had occured
            result.setDataList(null);
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
     * @return List of items (dataObject) from the dataObjectList filtered out
     * of records depending on the nature.
     */
    private AnswerList<HashMap<String, String>> filterWithNature(String nature, AnswerList<HashMap<String, String>> dataObjectList, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperties, int outputRequestedDimention) {
        switch (nature) {
            case TestCaseCountryProperties.NATURE_STATIC:
                return filterWithNatureSTATIC(dataObjectList, outputRequestedDimention);
            case TestCaseCountryProperties.NATURE_RANDOM:
                return filterWithNatureRANDOM(dataObjectList, outputRequestedDimention);
            case TestCaseCountryProperties.NATURE_RANDOMNEW:
                return filterWithNatureRANDOMNEW(dataObjectList, tCExecution, testCaseCountryProperties, outputRequestedDimention);
            case TestCaseCountryProperties.NATURE_NOTINUSE:
                return filterWithNatureNOTINUSE(dataObjectList, tCExecution, testCaseCountryProperties, outputRequestedDimention);
        }
        //TODO throw exception when Nature not known
        return null;
    }

    private List<Integer> getRandomListOfInteger(int inputRange, int nbOfResult) {
        List<Integer> listTempRandom;
        listTempRandom = new ArrayList<Integer>();
        for (int i = 0; i < inputRange; i++) {
            listTempRandom.add(i);
        }
        List<Integer> listRandom;
        listRandom = new ArrayList<Integer>();
        for (int i = 0; i < nbOfResult; i++) {
            Random r = new Random();
            int position = r.nextInt(listTempRandom.size());
            listRandom.add(listTempRandom.remove(position));
        }
        return listRandom;
    }

    @Override
    public AnswerList<HashMap<String, String>> filterWithNatureSTATIC(AnswerList<HashMap<String, String>> dataObjectList, int outputRequestedDimention) {
        AnswerList<HashMap<String, String>> result = new AnswerList<>();

        List<HashMap<String, String>> resultObject;
        resultObject = new ArrayList<>();

        for (int i = 0; i < outputRequestedDimention; i++) {
            resultObject.add(dataObjectList.getDataList().get(i));
        }

        result.setDataList(resultObject);
        String rowMessage = "";
        if (outputRequestedDimention < 2) {
            rowMessage = "row";
        } else {
            rowMessage = Integer.toString(outputRequestedDimention) + " rows";
        }
        result.setResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_NATURESTATIC).resolveDescription("ROW", rowMessage));
        return result;
    }

    @Override
    public AnswerList<HashMap<String, String>> filterWithNatureRANDOM(AnswerList<HashMap<String, String>> dataObjectList, int outputRequestedDimention) {
        AnswerList<HashMap<String, String>> result = new AnswerList<>();
        String selectedList = "";

        List<HashMap<String, String>> resultObject;
        resultObject = new ArrayList<HashMap<String, String>>();

        List<Integer> listTempRandom = getRandomListOfInteger(dataObjectList.getDataList().size(), outputRequestedDimention);

        for (int i : listTempRandom) {
            int j = i + 1;
            selectedList += Integer.toString(j) + ",";
            resultObject.add(dataObjectList.getDataList().get(i));
        }
        selectedList = StringUtil.removeLastChar(selectedList, 1);
        result.setDataList(resultObject);

        result.setResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_NATURERANDOM)
                .resolveDescription("POS", selectedList).resolveDescription("TOTALPOS", Integer.toString(dataObjectList.getDataList().size())));
        return result;
    }

    @Override
    public AnswerList<HashMap<String, String>> filterWithNatureRANDOMNEW(AnswerList<HashMap<String, String>> dataObjectList, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseProperties, int outputRequestedDimention) {
        AnswerList<HashMap<String, String>> result = new AnswerList<>();
        List<HashMap<String, String>> list; // Temporary list in order to treat the input list

        List<HashMap<String, String>> resultObject;
        resultObject = new ArrayList<HashMap<String, String>>();

        int initNB = dataObjectList.getDataList().size();
        // We get the list of values that were already used.
        List<String> pastValues = new LinkedList<>();
        try {
            pastValues = this.testCaseExecutionDataService.getPastValuesOfProperty(tCExecution.getId(),
                    testCaseProperties.getProperty(), tCExecution.getTest(), tCExecution.getTestCase(),
                    tCExecution.getCountryEnvParam().getBuild(), tCExecution.getEnvironmentData(), tCExecution.getCountry());
        } catch (CerberusException e) {
            LOG.error(e.getMessage(), e);
            result.setResultMessage(new MessageEvent(MessageEventEnum.GENERIC_ERROR)
                    .resolveDescription("REASON", e.getMessage()));
        }

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

            if (list.size() < outputRequestedDimention) { // Still some results available but not enougth compared to what we requested.
                result.setResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_RANDOMNEW_NOTENOUGTHRECORDS)
                        .resolveDescription("REMNB", Integer.toString(listToremove.size()))
                        .resolveDescription("TOTNB", Integer.toString(initNB))
                        .resolveDescription("NBREQUEST", Integer.toString(outputRequestedDimention)));
            } else {
                // Get a random list.
                List<Integer> listTempRandom = getRandomListOfInteger(dataObjectList.getDataList().size(), outputRequestedDimention);
                String selectedList = "";
                // Pick the result from list.
                for (int i : listTempRandom) {
                    int j = i + 1;
                    selectedList += Integer.toString(j) + ",";
                    resultObject.add(dataObjectList.getDataList().get(i));
                }
                selectedList = StringUtil.removeLastChar(selectedList, 1);
                result.setDataList(resultObject);

                result.setResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_NATURERANDOMNEW)
                        .resolveDescription("TOTNB", Integer.toString(initNB))
                        .resolveDescription("REMNB", Integer.toString(removedNB))
                        .resolveDescription("POS", selectedList)
                        .resolveDescription("TOTALPOS", Integer.toString(list.size())));

            }
        } else { // No more entries available.
            result.setResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_RANDOMNEW_NOMORERECORD)
                    .resolveDescription("TOTNB", Integer.toString(initNB)));
        }
        return result;
    }

    @Override
    public AnswerList<HashMap<String, String>> filterWithNatureNOTINUSE(AnswerList<HashMap<String, String>> dataObjectList, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty, int outputRequestedDimention) {
        AnswerList<HashMap<String, String>> result = new AnswerList<>();
        List<HashMap<String, String>> list = dataObjectList.getDataList(); // Temporary list in order to treat the input list

        List<HashMap<String, String>> resultObject;
        resultObject = new ArrayList<HashMap<String, String>>();

        int initNB = dataObjectList.getDataList().size();
        // We get the list of values that are beeing used.
        Integer peTimeout;
        try {
            peTimeout = Integer.valueOf(parameterService.findParameterByKey("cerberus_notinuse_timeout", tCExecution.getApplicationObj().getSystem()).getValue());

            List<String> pastValues = this.testCaseExecutionDataService.getInUseValuesOfProperty(tCExecution.getId(), testCaseCountryProperty.getProperty(), tCExecution.getEnvironmentData(), tCExecution.getCountry(), peTimeout);

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

                if (list.size() < outputRequestedDimention) { // Still some results available but not enougth compared to what we requested.
                    result.setResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_NOTINUSE_NOTENOUGTHRECORDS)
                            .resolveDescription("REMNB", Integer.toString(listToremove.size()))
                            .resolveDescription("TOTNB", Integer.toString(initNB))
                            .resolveDescription("NBREQUEST", Integer.toString(outputRequestedDimention)));
                } else {
                    // Get a random list.
                    List<Integer> listTempRandom = getRandomListOfInteger(dataObjectList.getDataList().size(), outputRequestedDimention);
                    String selectedList = "";
                    // Pick the result from list.
                    for (int i : listTempRandom) {
                        int j = i + 1;
                        selectedList += Integer.toString(j) + ",";
                        resultObject.add(dataObjectList.getDataList().get(i));
                    }
                    selectedList = StringUtil.removeLastChar(selectedList, 1);
                    result.setDataList(resultObject);

                    result.setResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_NATURENOTINUSE)
                            .resolveDescription("TOTNB", Integer.toString(initNB))
                            .resolveDescription("REMNB", Integer.toString(removedNB))
                            .resolveDescription("POS", selectedList)
                            .resolveDescription("TOTALPOS", Integer.toString(list.size())));
                }
            } else { // No more entries available.
                result.setResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_NOTINUSE_NOMORERECORD)
                        .resolveDescription("TOTNB", Integer.toString(initNB)));
            }
        } catch (CerberusException ex) {
            LOG.warn(ex);
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
        AnswerList<TestDataLibData> answerData = new AnswerList<>();
        AnswerItem<HashMap<String, String>> result = new AnswerItem<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS);

        List<TestDataLibData> objectDataList = new ArrayList<>();
        HashMap<String, String> row = new HashMap<>();

        switch (lib.getType()) {

            case TestDataLib.TYPE_CSV:
                answerData = testDataLibDataService.readByVarious(lib.getTestDataLibID(), null, null, "N");
                if ((answerData.getResultMessage().getCode() == MessageEventEnum.DATA_OPERATION_OK.getCode()) && !answerData.getDataList().isEmpty()) {
                    objectDataList = answerData.getDataList();
                    boolean missingKey = true;
                    for (TestDataLibData tdld : objectDataList) {
                        row.put(tdld.getSubData(), tdld.getColumnPosition());
                        if (tdld.getSubData().equalsIgnoreCase("")) {
                            missingKey = false;
                        }
                    }
                    result.setItem(row);
                    if (missingKey) {
                        msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SUBDATACSVNOKEY);
                        result.setResultMessage(msg);

                    } else {
                        msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_SUBDATA);
                        msg.setDescription(msg.getDescription().replace("%NBROW%", String.valueOf(answerData.getDataList().size())));
                        result.setResultMessage(msg);
                    }

                } else if ((answerData.getResultMessage().getCode() == MessageEventEnum.DATA_OPERATION_OK.getCode()) && answerData.getDataList().isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_NOSUBDATACSV);
                    result.setResultMessage(msg);

                } else {
                    msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SUBDATACSV);
                    result.setResultMessage(msg);

                }
                break;

            case TestDataLib.TYPE_SQL:
                answerData = testDataLibDataService.readByVarious(lib.getTestDataLibID(), "N", null, null);
                if ((answerData.getResultMessage().getCode() == MessageEventEnum.DATA_OPERATION_OK.getCode()) && !answerData.getDataList().isEmpty()) {
                    objectDataList = answerData.getDataList();
                    boolean missingKey = true;
                    for (TestDataLibData tdld : objectDataList) {
                        row.put(tdld.getSubData(), tdld.getColumn());
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

            case TestDataLib.TYPE_SERVICE:
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
                    msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_NOSUBDATA);
                    result.setResultMessage(msg);

                } else {
                    msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SUBDATASERVICE);
                    result.setResultMessage(msg);

                }
                break;

            case TestDataLib.TYPE_INTERNAL:
                // For static Type, there is no need to fetch the subdata as subdata are loaded at the same time of the data.
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
    private AnswerList<HashMap<String, String>> getDataObjectList(TestDataLib lib, HashMap<String, String> columnList, int rowLimit, TestCaseExecution tCExecution, TestCaseExecutionData testCaseExecutionData) {
        AnswerList<HashMap<String, String>> result = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS);
        CountryEnvironmentDatabase countryEnvironmentDatabase;
        AnswerList<HashMap<String, String>> responseList;
        String system = tCExecution.getApplicationObj().getSystem();
        String country = tCExecution.getCountry();
        String environment = tCExecution.getEnvironment();
        Pattern pattern;
        Matcher matcher;
        Parameter p;

        List<HashMap<String, String>> list;

        switch (lib.getType()) {
            case TestDataLib.TYPE_CSV:

                /**
                 * Before making the call we check if the Service Path is
                 * already a proper URL. If it is not, we prefix with the CsvUrl
                 * defined from corresponding database. This is used to get the
                 * data from the correct environment.
                 */
                String servicePathCsv = lib.getCsvUrl();

                LOG.debug("Service Path (Csv) : " + lib.getCsvUrl());
                // Trying making an URL with database context path.
                if (!StringUtil.isURL(servicePathCsv)) {
                    // Url is not valid, we try to get the corresponding DatabaseURL CsvURL to prefix.
                    if (!(StringUtil.isNullOrEmpty(lib.getDatabaseCsv()))) {

                        try {
                            countryEnvironmentDatabase = countryEnvironmentDatabaseService.convert(this.countryEnvironmentDatabaseService.readByKey(system,
                                    country, environment, lib.getDatabaseCsv()));
                            if (countryEnvironmentDatabase == null) {
                                msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_CSV_URLKOANDDATABASECSVURLNOTEXIST);
                                msg.setDescription(msg.getDescription()
                                        .replace("%SERVICEURL%", lib.getCsvUrl())
                                        .replace("%SYSTEM%", system)
                                        .replace("%COUNTRY%", country)
                                        .replace("%ENV%", environment)
                                        .replace("%DATABASE%", lib.getDatabaseCsv()));
                                result.setResultMessage(msg);
                                return result;

                            } else {
                                String csvURL = countryEnvironmentDatabase.getCsvUrl();
                                if (StringUtil.isNullOrEmpty(csvURL)) {
                                    msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_CSV_URLKOANDDATABASECSVURLEMPTY);
                                    msg.setDescription(msg.getDescription()
                                            .replace("%SERVICEURL%", lib.getCsvUrl())
                                            .replace("%SYSTEM%", system)
                                            .replace("%COUNTRY%", country)
                                            .replace("%ENV%", environment)
                                            .replace("%DATABASE%", lib.getDatabaseCsv()));
                                    result.setResultMessage(msg);
                                    return result;
                                }
                                // soapURL from database is not empty so we prefix the Service URL with it.
                                servicePathCsv = csvURL + lib.getCsvUrl();

                                if (!StringUtil.isURL(servicePathCsv)) {
                                    msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_CSV_URLKO);
                                    msg.setDescription(msg.getDescription()
                                            .replace("%SERVICEURL%", servicePathCsv)
                                            .replace("%SOAPURL%", csvURL)
                                            .replace("%SERVICEPATH%", lib.getCsvUrl())
                                            .replace("%ENTRY%", lib.getName())
                                            .replace("%ENTRYID%", lib.getTestDataLibID().toString()));
                                    result.setResultMessage(msg);
                                    return result;
                                }
                            }
                        } catch (CerberusException ex) {
                            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_CSV_URLKOANDDATABASECSVURLNOTEXIST);
                            msg.setDescription(msg.getDescription()
                                    .replace("%SERVICEURL%", lib.getCsvUrl())
                                    .replace("%SYSTEM%", system)
                                    .replace("%COUNTRY%", country)
                                    .replace("%ENV%", environment)
                                    .replace("%DATABASE%", lib.getDatabaseCsv()));
                            result.setResultMessage(msg);
                            return result;
                        }
                    }
                }

                // Trying make a valid path with csv parameter path.
                if (!StringUtil.isURL(servicePathCsv)) {
                    // Url is still not valid. We try to add the path from csv parameter.
                    String csv_path = parameterService.getParameterStringByKey("cerberus_testdatalibcsv_path", "", "");
                    csv_path = StringUtil.addSuffixIfNotAlready(csv_path, File.separator);
                    servicePathCsv = csv_path + servicePathCsv;
                }

                // CSV Call is made here.
                responseList = fileService.parseCSVFile(servicePathCsv, lib.getSeparator(), columnList);
                list = responseList.getDataList();

                //if the query returns sucess then we can get the data
                if (responseList.getResultMessage().getCode() == MessageEventEnum.PROPERTY_SUCCESS_CSV.getCode()) {
                    if (list != null && !list.isEmpty()) {
                        result.setDataList(list);
                        msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_CSV);
                        msg.setDescription(msg.getDescription().replace("%NBROW%", String.valueOf(result.getDataList().size())).replace("%CSVURL%", servicePathCsv));

                    } else {
                        msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_CSVDATABASENODATA);
                        msg.setDescription(msg.getDescription().replace("%CSVURL%", servicePathCsv));
                    }
                } else {
                    msg = responseList.getResultMessage();
                }
                result.setResultMessage(msg);

                break;

            case TestDataLib.TYPE_SQL:
                String connectionName;
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

                                Integer sqlTimeout = parameterService.getParameterIntegerByKey("cerberus_propertyexternalsql_timeout", system, 60);
                                //performs a query that returns several rows containing n columns
                                responseList = sqlService.queryDatabaseNColumns(connectionName, lib.getScript(), rowLimit, sqlTimeout, system, columnList);

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

            case TestDataLib.TYPE_SERVICE:
                AppService appService = new AppService();
                HashMap<String, String> resultHash = new HashMap<>();
                List<HashMap<String, String>> listResult = new ArrayList<>();

                // Temporary list of string.
                List<String> listTemp1 = null;

                // String containing the XML
                String responseString = "";

                /**
                 * Before making the call we check if the Service Path is
                 * already a propper URL. If it is not, we prefix with the
                 * SoapUrl defined from corresponding database. This is used to
                 * get the data from the correct environment.
                 */
                String servicePath = lib.getServicePath();
                LOG.debug("Service Path : " + lib.getServicePath());

                // Service Call is made here.
                AnswerItem ai = serviceService.callService(lib.getService(), null, null, lib.getDatabaseUrl(), lib.getEnvelope(), lib.getServicePath(), lib.getMethod(), tCExecution);

                msg = ai.getResultMessage();

                //if the call returns success then we can process the soap ressponse
                if (msg.getCode() == MessageEventEnum.ACTION_SUCCESS_CALLSERVICE.getCode()) {

                    appService = (AppService) ai.getItem();

                    //Record result in filessytem.
//                testCaseExecutionData.addFileList(recorderService.recordServiceCall(tCExecution, null, 0, testCaseExecutionData.getProperty(), appService));
                    recorderService.recordServiceCall(tCExecution, null, 0, testCaseExecutionData.getProperty(), appService);

                    // Call successful so we can start to parse the result and build RawData per columns from subdata entries.
                    /**
                     * This Step will calculate hashTemp1 : Hash of List from
                     * the Service response.
                     */
                    // Will contain the nb of row of the target list of Hash.
                    int finalnbRow = 0;
                    // Will contain the result of the XML parsing.
                    HashMap<String, List<String>> hashTemp1 = new HashMap<>();

                    if (columnList.isEmpty()) { // No subdata could be found on the testdatalib.
                        msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SERVICE_NOSUBDATA);
                        msg.setDescription(msg.getDescription()
                                .replace("%ENTRY%", lib.getName())
                                .replace("%ENTRYID%", lib.getTestDataLibID().toString()));
                    } else {

                        switch (appService.getResponseHTTPBodyContentType()) {

                            case AppService.RESPONSEHTTPBODYCONTENTTYPE_XML:

                                Document xmlDocument = xmlUnitService.getXmlDocument(appService.getResponseHTTPBody());
                                // We get the content of the XML in order to report it log messages.
                                responseString = appService.getResponseHTTPBody();

                                for (Map.Entry<String, String> entry : columnList.entrySet()) {
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
                                                        msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SERVICE_CHECK_XPATH);
                                                        msg.setDescription(msg.getDescription()
                                                                .replace("%XPATH%", subDataParsingAnswer)
                                                                .replace("%SUBDATA%", subDataColumnToTreat)
                                                                .replace("%ENTRY%", lib.getName())
                                                                .replace("%ENTRYID%", lib.getTestDataLibID().toString()));
                                                    } else {
                                                        //no elements were returned by the XPATH expression
                                                        msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SERVICE_XML_NOTFOUND);
                                                        msg.setDescription(msg.getDescription()
                                                                .replace("%XPATH%", subDataParsingAnswer)
                                                                .replace("%SUBDATA%", subDataColumnToTreat)
                                                                .replace("%ENTRY%", lib.getName())
                                                                .replace("%XMLCONTENT%", responseString)
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
                                            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SERVICE_XML_NOTFOUND);
                                            msg.setDescription(msg.getDescription()
                                                    .replace("%XPATH%", subDataParsingAnswer)
                                                    .replace("%SUBDATA%", subDataColumnToTreat)
                                                    .replace("%ENTRY%", lib.getName())
                                                    .replace("%XMLCONTENT%", responseString)
                                                    .replace("%ENTRYID%", lib.getTestDataLibID().toString())
                                            );
                                        }
                                    } catch (XmlUtilException ex) {
                                        msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SERVICE_XMLEXCEPTION);
                                        msg.setDescription(msg.getDescription()
                                                .replace("%XPATH%", subDataParsingAnswer)
                                                .replace("%SUBDATA%", subDataColumnToTreat)
                                                .replace("%ENTRY%", lib.getName())
                                                .replace("%ENTRYID%", lib.getTestDataLibID().toString())
                                                .replace("%REASON%", ex.toString() + " Detail answer " + responseString));
                                    } catch (Exception ex) {
                                        msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SERVICE_XMLEXCEPTION);
                                        msg.setDescription(msg.getDescription()
                                                .replace("%XPATH%", lib.getSubDataParsingAnswer())
                                                .replace("%SUBDATA%", "")
                                                .replace("%REASON%", ex.toString()));
                                    }
                                }

                                /**
                                 * This Step will convert hashTemp1 (Hash of
                                 * List) to target listResult (list of Hash).
                                 */
                                if (msg.getCode() == MessageEventEnum.ACTION_SUCCESS_CALLSERVICE.getCode()) {
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
                                 * This Step will pick the correct listResult
                                 * (list of Hash) from the type of Property.
                                 */
                                if (msg.getCode() == MessageEventEnum.ACTION_SUCCESS_CALLSERVICE.getCode()) {
                                    result.setDataList(listResult);
                                    msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_SOAP);
                                    msg.setDescription(msg.getDescription().replace("%NBROW%", String.valueOf(result.getDataList().size()))
                                            .replace("%URL%", servicePath).replace("%OPER%", lib.getMethod()));

                                }
                                break;

                            case AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON:

                                // We get the content of the XML in order to report it log messages.
                                responseString = appService.getResponseHTTPBody();

                                for (Map.Entry<String, String> entry : columnList.entrySet()) {
                                    String subDataColumnToTreat = entry.getKey(); // SubData
                                    String subDataParsingAnswer = entry.getValue(); // Parsing Answer
                                    listTemp1 = new ArrayList<>();

                                    try {

                                        // We try to parse the XML with the subdata Parsing Answer.
                                        listTemp1 = jsonService.getFromJson(responseString, subDataParsingAnswer);

                                        if (listTemp1.size() > 0) {

                                            // Add the Subdata with associated list in the HashMap.
                                            hashTemp1.put(subDataColumnToTreat, listTemp1);

                                            // Getting the nb of row of the final result. (Max of all the Subdata retrieved from the XML)
                                            if (listTemp1.size() > finalnbRow) {
                                                finalnbRow = listTemp1.size();
                                            }

                                        } else {
                                            //no elements were returned by the XPATH expression
                                            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SERVICE_JSON_NOTFOUND);
                                            msg.setDescription(msg.getDescription()
                                                    .replace("%XPATH%", subDataParsingAnswer)
                                                    .replace("%SUBDATA%", subDataColumnToTreat)
                                                    .replace("%ENTRY%", lib.getName())
                                                    .replace("%XMLCONTENT%", responseString)
                                                    .replace("%ENTRYID%", lib.getTestDataLibID().toString())
                                            );
                                        }
                                    } catch (Exception ex) {
                                        msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SERVICE_JSONEXCEPTION);
                                        msg.setDescription(msg.getDescription()
                                                .replace("%XPATH%", lib.getSubDataParsingAnswer())
                                                .replace("%SUBDATA%", "")
                                                .replace("%REASON%", ex.toString()
                                                        + "\n api response : " + appService.getResponseHTTPBody()));
                                    }
                                }

                                /**
                                 * This Step will convert hashTemp1 (Hash of
                                 * List) to target listResult (list of Hash).
                                 */
                                if (msg.getCode() == MessageEventEnum.ACTION_SUCCESS_CALLSERVICE.getCode()) {
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
                                 * This Step will pick the correct listResult
                                 * (list of Hash) from the type of Property.
                                 */
                                if (msg.getCode() == MessageEventEnum.ACTION_SUCCESS_CALLSERVICE.getCode()) {
                                    result.setDataList(listResult);
                                    msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_SERVICE);
                                    msg.setDescription(msg.getDescription().replace("%NBROW%", String.valueOf(result.getDataList().size()))
                                            .replace("%URL%", appService.getServicePath()).replace("%METHOD%", appService.getMethod()));

                                }
                                break;

                            default:
                                msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_NOTSUPPORTEDSERVICERESULT);
                                msg.setDescription(msg.getDescription().replace("%FORMAT%", appService.getResponseHTTPBodyContentType()));

                        }

                    }

                } else {
                    String soapError = msg.getDescription();
                    msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SERVICE_SOAPCALLFAILED);
                    msg.setDescription(msg.getDescription()
                            .replace("%SOAPERROR%", soapError));

                }
                msg.setDescription(msg.getDescription()
                        .replace("%SERVICE%", servicePath)
                        .replace("%OPERATION%", lib.getMethod()));
                result.setResultMessage(msg);
                break;

            case TestDataLib.TYPE_INTERNAL:
                result = testDataLibService.readINTERNALWithSubdataByCriteria(lib.getName(), lib.getSystem(), lib.getCountry(), lib.getEnvironment(), rowLimit, system);
                //if the sql service returns a success message then we can process it
                if ((result.getResultMessage().getCode() == MessageEventEnum.DATA_OPERATION_OK.getCode()) && !result.getDataList().isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_INTERNAL);
                    msg.setDescription(msg.getDescription().replace("%NBROW%", String.valueOf(result.getDataList().size())));
                    result.setResultMessage(msg);

                } else if ((result.getResultMessage().getCode() == MessageEventEnum.DATA_OPERATION_OK.getCode()) && result.getDataList().isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_INTERNALNODATA);
                    msg.setDescription(msg.getDescription().replace("%SYSTEM%", lib.getSystem())
                            .replace("%ENV%", lib.getEnvironment()).replace("%COUNTRY%", lib.getCountry()));
                    result.setResultMessage(msg);

                } else {
                    msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_INTERNAL);
                    msg.setDescription(msg.getDescription().replace("%SYSTEM%", lib.getSystem())
                            .replace("%ENV%", lib.getEnvironment()).replace("%COUNTRY%", lib.getCountry()));
                    result.setResultMessage(msg);

                }
                break;

        }

        return result;
    }

    @Override
    public JSONArray convertToJSONObject(List<HashMap<String, String>> object) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (HashMap<String, String> row : object) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("KEY", row.get(""));
            for (Map.Entry<String, String> entry : row.entrySet()) {
                String column = entry.getKey();
                String name = entry.getValue();
                if (!("".equals(column))) {
                    jsonObject.put(column, name);
                }
            }
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

}
