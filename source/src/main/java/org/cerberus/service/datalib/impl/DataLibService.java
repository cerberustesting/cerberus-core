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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cerberus.crud.dao.ITestCaseExecutionDataDAO;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestDataLib;
import org.cerberus.crud.entity.TestDataLibData;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITestDataLibDataService;
import org.cerberus.engine.entity.TestDataLibResult;
import org.cerberus.engine.entity.TestDataLibResultStatic;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.datalib.IDataLibService;
import org.cerberus.service.file.IFileService;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class DataLibService implements IDataLibService {

    @Autowired
    IFileService fileService;
    @Autowired
    private ITestCaseExecutionDataDAO testCaseExecutionDataDAO;
    @Autowired
    private IParameterService parameterService;
    @Autowired
    private ITestDataLibDataService testDataLibDataService;

    @Override
    public AnswerItem<TestDataLibResult> getFromDataLib(TestDataLib lib, TestCaseCountryProperties testCaseCountryProperty, TestCaseExecution tCExecution) {
        AnswerItem<TestDataLibResult> result = null;

        /**
         * Gets the list of columns to get from TestDataLibData.
         */
        HashMap<String, String> columnList = getListOfSubData(lib);

        /**
         * Get List of DataObject in a format List<Map<String>>
         */
        AnswerList<List<HashMap<String, String>>> dataObjectList = getDataObjectList(lib, columnList);

        /**
         * Get the dataObject from the list depending on the nature
         */
        AnswerItem<HashMap<String, String>> dataObject = getDataSetFromList(testCaseCountryProperty.getNature(), dataObjectList, tCExecution, testCaseCountryProperty);

        /**
         * Save the result to the Lib object.
         */
        TestDataLibResult tdlResult = new TestDataLibResultStatic();
        tdlResult.setDataLibRawData((HashMap<String, String>) dataObject.getItem());
        tdlResult.setTestDataLibID(lib.getTestDataLibID());

        result.setItem(tdlResult);

        return result;
    }

    /**
     * This method route to the method regarding the nature
     * @param nature : Nature of the property
     * @param dataObjectList : List of dataObject
     * @param tCExecution : TestCaseExecution
     * @param testCaseCountryProperties : TestCaseCountryProperties
     * @return one item (dataObject) from the dataObjectList
     */
    private AnswerItem<HashMap<String, String>> getDataSetFromList(String nature, AnswerList dataObjectList, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperties) {
        switch (nature) {
            case TestCaseCountryProperties.NATURE_STATIC:
                return getStaticFromDataSet(dataObjectList);
            case TestCaseCountryProperties.NATURE_RANDOM:
                return getRandomFromDataSet(dataObjectList);
            case TestCaseCountryProperties.NATURE_RANDOMNEW:
                return getRandomNewFromDataSet(dataObjectList, tCExecution, testCaseCountryProperties);
            case TestCaseCountryProperties.NATURE_NOTINUSE:
                return getNotInUseFromDataSet(dataObjectList, tCExecution, testCaseCountryProperties);
        }
        //TODO throw exception when Nature not known
        return null;
    }

    /**
     * This method return the first ObjectData from DataSet
     * @param dataObjectList : List of dataObject
     * @return The first item from dataObjectList
     */
    @Override
    public AnswerItem<HashMap<String, String>> getStaticFromDataSet(AnswerList dataObjectList) {
        AnswerItem<HashMap<String, String>> result = new AnswerItem();
        result.setItem((HashMap<String, String>) dataObjectList.getDataList().get(0));
        result.setResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_SQL_STATIC));
        return result;
    }

    /**
     * This method return an ObjectData from DataSet picked randomly
     * @param dataObjectList : List of dataObject
     * @return An item from dataObjectList choosen randomly
     */
    @Override
    public AnswerItem<HashMap<String, String>> getRandomFromDataSet(AnswerList dataObjectList) {
        AnswerItem<HashMap<String, String>> result = new AnswerItem();
        Random r = new Random();
        int position = r.nextInt(dataObjectList.getDataList().size());
        result.setItem((HashMap<String, String>) dataObjectList.getDataList().get(position));
        result.setResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_SQL_RANDOM)
                .resolveDescription("%POS%", Integer.toString(position)).resolveDescription("%TOTALPOS%", Integer.toString(dataObjectList.getDataList().size())));
        return result;
    }

    /**
     * This method return an ObjectData from DataSet picked randomly after excluding ObjectData already used in previous execution
     * @param dataObjectList : List of dataObject
     * @param tCExecution : TestCaseExecution
     * @param testCaseProperties : TestCaseCountryProperties
     * @return An item from dataObjectList choosen randomly
     */
    @Override
    public AnswerItem<HashMap<String, String>> getRandomNewFromDataSet(AnswerList dataObjectList, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseProperties) {
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
            result.setResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_SQL_RANDOMNEW)
                    .resolveDescription("%TOTNB%", Integer.toString(initNB))
                    .resolveDescription("%REMNB%", Integer.toString(removedNB))
                    .resolveDescription("%POS%", Integer.toString(position))
                    .resolveDescription("%TOTALPOS%", Integer.toString(list.size())));
        } else { // No more entries available.
            result.setResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SQL_RANDOMNEW_NOMORERECORD)
                    .resolveDescription("%TOTNB%", Integer.toString(initNB)));
        }
        return result;
    }

    /**
     * This method return an ObjectData from dataObjectList that is not used in another execution
     * @param dataObjectList : List of dataObject
     * @param tCExecution : TestCaseExecution
     * @param testCaseCountryProperty : TestCaseCountryProperties
     * @return An item from dataObjectList excluding the one used in other execution choosen randomly
     */
    @Override
    public AnswerItem<HashMap<String, String>> getNotInUseFromDataSet(AnswerList dataObjectList, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseCountryProperty) {
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
                result.setResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIB_SQL_NOTINUSE)
                        .resolveDescription("%TOTNB%", Integer.toString(initNB))
                        .resolveDescription("%REMNB%", Integer.toString(removedNB))
                        .resolveDescription("%POS%", Integer.toString(position))
                        .resolveDescription("%TOTALPOS%", Integer.toString(list.size())));
            } else { // No more entries available.
                result.setResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SQL_NOTINUSE_NOMORERECORD)
                        .resolveDescription("%TOTNB%", Integer.toString(initNB)));
            }
        } catch (CerberusException ex) {
            Logger.getLogger(DataLibService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * Get the list of subData
     * @param lib
     * @return 
     */
    private HashMap<String, String> getListOfSubData(TestDataLib lib) {
        AnswerList answerData = new AnswerList();
        answerData = testDataLibDataService.readByVarious(lib.getTestDataLibID(), "N", null);
        List<TestDataLibData> objectDataList = new ArrayList<TestDataLibData>();
        objectDataList = answerData.getDataList();
        HashMap<String, String> row = new HashMap<String, String>();

        switch (lib.getType()) {
            case TestDataLib.TYPE_CSV:
                for (TestDataLibData tdld : objectDataList) {
                    row.put(tdld.getColumn(), tdld.getSubData());
                }
            case TestDataLib.TYPE_SQL:
                for (TestDataLibData tdld : objectDataList) {
                    row.put(tdld.getColumn(), tdld.getSubData());
                }
            case TestDataLib.TYPE_SOAP:
                for (TestDataLibData tdld : objectDataList) {
                    row.put(tdld.getParsingAnswer(), tdld.getSubData());
                }
            case TestDataLib.TYPE_STATIC:
                for (TestDataLibData tdld : objectDataList) {
                    row.put(tdld.getValue(), tdld.getSubData());
                }
        }
        return row;
    }

    /**
     * Get the dataObject List depending on the type
     * @param lib
     * @param columnList
     * @return 
     */
    private AnswerList getDataObjectList(TestDataLib lib, HashMap<String, String> columnList) {
        AnswerList result = new AnswerList();

        switch (lib.getType()) {
            case TestDataLib.TYPE_CSV:
                result = fileService.parseCSVFile(lib.getCsvUrl(), lib.getSeparator(), columnList);
                return result;
            case TestDataLib.TYPE_SQL:
                result = null;
            case TestDataLib.TYPE_SOAP:
                result = null;
            case TestDataLib.TYPE_STATIC:
                result = null;
        }

        
        return result;
    }
}
