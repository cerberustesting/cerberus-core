package com.redcats.tst.serviceEngine.impl;

import com.redcats.tst.dao.ITestCaseExecutionDataDAO;
import com.redcats.tst.entity.*;
import com.redcats.tst.exception.CerberusEventException;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.service.ICountryEnvironmentDatabaseService;
import com.redcats.tst.service.ISqlLibraryService;
import com.redcats.tst.service.ITestCaseExecutionDataService;
import com.redcats.tst.service.ITestCaseExecutionService;
import com.redcats.tst.serviceEngine.IConnectionPoolDAO;
import com.redcats.tst.serviceEngine.IPropertyService;
import com.redcats.tst.serviceEngine.ISeleniumService;
import com.redcats.tst.util.DateUtil;
import com.redcats.tst.util.ParameterParserUtil;
import com.redcats.tst.util.StringUtil;
import org.apache.log4j.Level;
import org.openqa.selenium.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import org.openqa.selenium.JavascriptExecutor;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 10/01/2013
 * @since 2.0.0
 */
@Service
public class PropertyService implements IPropertyService {

    @Autowired
    private ISeleniumService seleniumService;
    @Autowired
    private ISqlLibraryService sqlLibraryService;
    @Autowired
    private IConnectionPoolDAO connectionPoolDAO;
    @Autowired
    private ICountryEnvironmentDatabaseService countryEnvironmentDatabaseService;
    @Autowired
    private ITestCaseExecutionDataDAO testCaseExecutionDataDAO;
    @Autowired
    private ITestCaseExecutionDataService testCaseExecutionDataService;
    @Autowired
    private ITestCaseExecutionService testCaseExecutionService;

    @Override
    public TestCaseExecutionData calculateProperty(TestCaseExecutionData testCaseExecutionData, TestCaseStepActionExecution testCaseStepActionExecution, TestCaseCountryProperties testCaseCountryProperty) {
        testCaseExecutionData.setStart(new Date().getTime());
        MessageEvent res;

        List<TestCaseCountryProperties> tccpList = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().gettCase().getTestCaseCountryProperties();
        TCExecution tCExecution = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution();

        if (testCaseCountryProperty.getValue().contains("%")) {
            String decodedValue = this.decodeValue(testCaseCountryProperty.getValue(), testCaseStepActionExecution.getTestCaseExecutionDataList(), tCExecution);
            testCaseExecutionData.setValue(decodedValue);
            testCaseCountryProperty.setValue(decodedValue);
        }

        if ((testCaseCountryProperty.getType().equals("executeSqlFromLib")) || (testCaseCountryProperty.getType().equals("executeSql"))) {
            if (testCaseCountryProperty.getType().equals("executeSqlFromLib")) {
                try {
                    String script = this.sqlLibraryService.findSqlLibraryByKey(testCaseCountryProperty.getValue()).getScript();
                    testCaseExecutionData.setValue(script);
                } catch (CerberusException ex) {
                    Logger.getLogger(PropertyService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_SQLLIB_NOTEXIT);
                    res.setDescription(res.getDescription().replaceAll("%SQLLIB%", testCaseCountryProperty.getValue()));
                    testCaseExecutionData.setPropertyResultMessage(res);
                    testCaseExecutionData.setEnd(new Date().getTime());
                    return testCaseExecutionData;
                }
            }

            testCaseExecutionData = this.calculateOnDatabase(testCaseExecutionData, testCaseCountryProperty, tCExecution);

        } else if (testCaseCountryProperty.getType().equals("text")) {
            if (testCaseCountryProperty.getNature().equals("RANDOM")) {
                String charset = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
                String value = StringUtil.getRandomString(testCaseCountryProperty.getLength(), charset);
                testCaseExecutionData.setValue(value);
                res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_RANDOM);
                res.setDescription(res.getDescription().replaceAll("%VALUE%", ParameterParserUtil.securePassword(value, testCaseCountryProperty.getProperty())));
                testCaseExecutionData.setPropertyResultMessage(res);
            } else if (testCaseCountryProperty.getNature().equals("RANDOM_NEW")) {
                String charset = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
                String value = StringUtil.getRandomString(testCaseCountryProperty.getLength(), charset);
                testCaseExecutionData.setValue(value);
                res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_RANDOM_NEW);
                res.setDescription(res.getDescription().replaceAll("%VALUE%", ParameterParserUtil.securePassword(value, testCaseCountryProperty.getProperty())));
                testCaseExecutionData.setPropertyResultMessage(res);
                //TODO check if value exist on DB ( used in another test case of the revision )
            } else {
                MyLogger.log(PropertyService.class.getName(), Level.DEBUG, "Setting value : " + testCaseCountryProperty.getValue());
                String value = testCaseCountryProperty.getValue();
                testCaseExecutionData.setValue(value);
                res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_TEXT);
                res.setDescription(res.getDescription().replaceAll("%VALUE%", ParameterParserUtil.securePassword(value, testCaseCountryProperty.getProperty())));
                testCaseExecutionData.setPropertyResultMessage(res);
            }

        } else if (testCaseCountryProperty.getType().equals("getFromHtmlVisible")) {
            try {
                String valueFromHTML = this.seleniumService.getValueFromHTMLVisible(testCaseCountryProperty.getValue());
                if (valueFromHTML != null) {
                    testCaseExecutionData.setValue(valueFromHTML);
                    res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_HTMLVISIBLE);
                    res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseCountryProperty.getValue()));
                    res.setDescription(res.getDescription().replaceAll("%VALUE%", valueFromHTML));
                    testCaseExecutionData.setPropertyResultMessage(res);
                } else {
                }
            } catch (NoSuchElementException exception) {
                MyLogger.log(PropertyService.class.getName(), Level.ERROR, exception.toString());
                res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_HTMLVISIBLE_ELEMENTDONOTEXIST);
                res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseCountryProperty.getValue()));
                testCaseExecutionData.setPropertyResultMessage(res);
            }
        } else if (testCaseCountryProperty.getType().equals("getFromHtml")) {
            try {
                String valueFromHTML = this.seleniumService.getValueFromHTML(testCaseCountryProperty.getValue());
                if (valueFromHTML != null) {
                    testCaseExecutionData.setValue(valueFromHTML);
                    res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_HTML);
                    res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseCountryProperty.getValue()));
                    res.setDescription(res.getDescription().replaceAll("%VALUE%", valueFromHTML));
                    testCaseExecutionData.setPropertyResultMessage(res);
                }
            } catch (NoSuchElementException exception) {
                MyLogger.log(PropertyService.class.getName(), Level.ERROR, exception.toString());
                res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_HTML_ELEMENTDONOTEXIST);
                res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseCountryProperty.getValue()));
                testCaseExecutionData.setPropertyResultMessage(res);
            }
        } else if (testCaseCountryProperty.getType().equals("getFromJS")) {
            try {
                String script = testCaseCountryProperty.getValue();
                String valueFromJS = this.seleniumService.getValueFromJS(script);
                if (script != null) {
                    res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_HTML);
                    res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseCountryProperty.getValue()));
                    res.setDescription(res.getDescription().replaceAll("%VALUE%", script));
                    testCaseExecutionData.setPropertyResultMessage(res);
                }
            } catch (NoSuchElementException exception) {
                MyLogger.log(PropertyService.class.getName(), Level.ERROR, exception.toString());
                res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_HTML_ELEMENTDONOTEXIST);
                res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseCountryProperty.getValue()));
                testCaseExecutionData.setPropertyResultMessage(res);
            }
        }else {
            res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_UNKNOWNPROPERTY);
            res.setDescription(res.getDescription().replaceAll("%PROPERTY%", testCaseCountryProperty.getType()));
        }

        testCaseExecutionData.setEnd(new Date().getTime());
        return testCaseExecutionData;
    }

    @Override
    public String decodeValue(String myString, List<TestCaseExecutionData> properties, TCExecution tCExecution) {

        /**
         * Trying to replace by system environment variables .
         */
        myString = StringUtil.replaceAllProperties(myString, "%ENV%", tCExecution.getEnvironmentData());
        myString = StringUtil.replaceAllProperties(myString, "%ENVGP%", tCExecution.getEnvironmentDataObj().getGp1());
        myString = StringUtil.replaceAllProperties(myString, "%COUNTRY%", tCExecution.getCountry());
        myString = StringUtil.replaceAllProperties(myString, "%COUNTRYGP1%", tCExecution.getCountryObj().getGp1());
        myString = StringUtil.replaceAllProperties(myString, "%SSIP%", tCExecution.getSeleniumIP());
        myString = StringUtil.replaceAllProperties(myString, "%SSPORT%", tCExecution.getSeleniumPort());
        myString = StringUtil.replaceAllProperties(myString, "%TAG%", tCExecution.getTag());

        /**
         * Trying to replace date variables .
         */
        myString = StringUtil.replaceAllProperties(myString, "%TODAY-yyyy%", DateUtil.getTodayFormat("yyyy"));
        myString = StringUtil.replaceAllProperties(myString, "%TODAY-MM%", DateUtil.getTodayFormat("MM"));
        myString = StringUtil.replaceAllProperties(myString, "%TODAY-dd%", DateUtil.getTodayFormat("dd"));
        myString = StringUtil.replaceAllProperties(myString, "%TODAY-HH%", DateUtil.getTodayFormat("HH"));
        myString = StringUtil.replaceAllProperties(myString, "%TODAY-mm%", DateUtil.getTodayFormat("mm"));
        myString = StringUtil.replaceAllProperties(myString, "%TODAY-ss%", DateUtil.getTodayFormat("ss"));
        myString = StringUtil.replaceAllProperties(myString, "%YESTERDAY-yyyy%", DateUtil.getYesterdayFormat("yyyy"));
        myString = StringUtil.replaceAllProperties(myString, "%YESTERDAY-MM%", DateUtil.getYesterdayFormat("MM"));
        myString = StringUtil.replaceAllProperties(myString, "%YESTERDAY-dd%", DateUtil.getYesterdayFormat("dd"));
        myString = StringUtil.replaceAllProperties(myString, "%YESTERDAY-HH%", DateUtil.getYesterdayFormat("HH"));
        myString = StringUtil.replaceAllProperties(myString, "%YESTERDAY-mm%", DateUtil.getYesterdayFormat("mm"));
        myString = StringUtil.replaceAllProperties(myString, "%YESTERDAY-ss%", DateUtil.getYesterdayFormat("ss"));

        /**
         * Trying to replace by property value already defined if not null.
         */
        if (properties != null) {
            for (TestCaseExecutionData prop : properties) {
                myString = StringUtil.replaceAllProperties(myString, "%" + prop.getProperty() + "%", ParameterParserUtil.securePassword(prop.getValue(), prop.getProperty()));
            }
        }

        return myString;
    }

    private TestCaseExecutionData calculateOnDatabase(TestCaseExecutionData testCaseExecutionData, TestCaseCountryProperties testCaseProperties, TCExecution tCExecution) {
        String sql = testCaseProperties.getValue();
        String db = testCaseProperties.getDatabase();

        String connectionName;
        CountryEnvironmentDatabase countryEnvironmentDatabase;

        try {
            countryEnvironmentDatabase = this.countryEnvironmentDatabaseService.findCountryEnvironmentDatabaseByKey(tCExecution.getApplication().getSystem(), testCaseProperties.getCountry(), tCExecution.getEnvironmentData(), db);
            connectionName = countryEnvironmentDatabase.getConnectionPoolName();

            if (!(StringUtil.isNullOrEmpty(connectionName))) {
                try {
                    List<String> list = this.connectionPoolDAO.queryDatabase(connectionName, sql, testCaseProperties.getRowLimit());

                    if (list != null) {
                        if (testCaseProperties.getNature().equalsIgnoreCase(Property.NATURE_STATIC)) {
                            if (list.size() > 0) {
                                MessageEvent mes = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_SQL);
                                mes.setDescription(mes.getDescription().replaceAll("%DB%", db));
                                mes.setDescription(mes.getDescription().replaceAll("%SQL%", sql));
                                mes.setDescription(mes.getDescription().replaceAll("%JDBCPOOLNAME%", connectionName));
                                testCaseExecutionData.setPropertyResultMessage(mes);
                                testCaseExecutionData.setValue(list.get(0));
                            } else {
                                MessageEvent mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_NODATA);
                                mes.setDescription(mes.getDescription().replaceAll("%DB%", db));
                                mes.setDescription(mes.getDescription().replaceAll("%SQL%", sql));
                                mes.setDescription(mes.getDescription().replaceAll("%JDBCPOOLNAME%", connectionName));
                                testCaseExecutionData.setPropertyResultMessage(mes);
                            }

                        } else if (testCaseProperties.getNature().equalsIgnoreCase(Property.NATURE_RANDOM)) {
                            testCaseExecutionData.setValue(this.calculateNatureRandom(list, testCaseProperties.getRowLimit()));
                            MessageEvent mes = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_SQL_RANDOM);
                            mes.setDescription(mes.getDescription().replaceAll("%DB%", db));
                            mes.setDescription(mes.getDescription().replaceAll("%SQL%", sql));
                            mes.setDescription(mes.getDescription().replaceAll("%JDBCPOOLNAME%", connectionName));
                            testCaseExecutionData.setPropertyResultMessage(mes);

                        } else if (testCaseProperties.getNature().equalsIgnoreCase(Property.NATURE_RANDOMNEW)) {
                            testCaseExecutionData.setValue(this.calculateNatureRandomNew(list, testCaseProperties.getProperty(), tCExecution));
                            MessageEvent mes = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_SQL_RANDOM_NEW);
                            mes.setDescription(mes.getDescription().replaceAll("%DB%", db));
                            mes.setDescription(mes.getDescription().replaceAll("%SQL%", sql));
                            mes.setDescription(mes.getDescription().replaceAll("%JDBCPOOLNAME%", connectionName));
                            testCaseExecutionData.setPropertyResultMessage(mes);

                        } else if (testCaseProperties.getNature().equalsIgnoreCase(Property.NATURE_NOTINUSE)) {
                            testCaseExecutionData.setValue(this.calculateNatureNotInUse(list, testCaseProperties.getProperty(), tCExecution));
                            MessageEvent mes = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_SQL_NOTINUSE);
                            mes.setDescription(mes.getDescription().replaceAll("%DB%", db));
                            mes.setDescription(mes.getDescription().replaceAll("%SQL%", sql));
                            mes.setDescription(mes.getDescription().replaceAll("%JDBCPOOLNAME%", connectionName));
                            testCaseExecutionData.setPropertyResultMessage(mes);

                        }
                    }
                } catch (CerberusEventException ex) {
                    MessageEvent mes = ex.getMessageError();
                    testCaseExecutionData.setPropertyResultMessage(mes);
                }

            } else {
                MessageEvent mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_EMPTYJDBCPOOL);
                mes.setDescription(mes.getDescription().replaceAll("%SYSTEM%", tCExecution.getApplication().getSystem()));
                mes.setDescription(mes.getDescription().replaceAll("%COUNTRY%", testCaseProperties.getCountry()));
                mes.setDescription(mes.getDescription().replaceAll("%ENV%", tCExecution.getEnvironmentData()));
                mes.setDescription(mes.getDescription().replaceAll("%DB%", db));
                testCaseExecutionData.setPropertyResultMessage(mes);
            }
        } catch (CerberusException ex) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_JDBCPOOLNOTCONFIGURED);
            mes.setDescription(mes.getDescription().replaceAll("%SYSTEM%", tCExecution.getApplication().getSystem()));
            mes.setDescription(mes.getDescription().replaceAll("%COUNTRY%", testCaseProperties.getCountry()));
            mes.setDescription(mes.getDescription().replaceAll("%ENV%", tCExecution.getEnvironmentData()));
            mes.setDescription(mes.getDescription().replaceAll("%DB%", db));
            testCaseExecutionData.setPropertyResultMessage(mes);
        }

        return testCaseExecutionData;
    }

    private String calculateNatureRandom(List<String> list, int rowLimit) {
        /* Limit | List Size  =>  Used in Random
         0   |    10      =>    10
         5   |    10      =>     5
         10  |     7      =>     7
         */
        Random random = new Random();
        if (list.size() > rowLimit && rowLimit == 0) {
            return list.get(random.nextInt(list.size()));
        } else if (list.size() > rowLimit && rowLimit > 0) {
            return list.get(random.nextInt(rowLimit));
        } else if (list.size() <= rowLimit) {
            return list.get(random.nextInt(list.size()));
        }
        return null;
    }

    private String calculateNatureRandomNew(List<String> list, String propName, TCExecution tCExecution) {
        //TODO clean code
        List<String> pastValues = this.testCaseExecutionDataDAO.getPastValuesOfProperty(propName, tCExecution.getTest(),
                tCExecution.getTestCase(), tCExecution.getCountryEnvParam().getBuild(), tCExecution.getEnvironmentData(),
                tCExecution.getCountry());

        if (pastValues.size() > 0) {
            for (String value : list) {
                if (!pastValues.contains(value)) {
                    return value;
                }
            }
        } else {
            return list.get(0);
        }
        return null;
    }

    private String calculateNatureNotInUse(List<String> list, String propName, TCExecution tCExecution) {
        try {
            List<TCExecution> exelist = this.testCaseExecutionService.findTCExecutionbyCriteria1(DateUtil.getMySQLTimestampTodayDeltaMinutes(10), "%", "%", "%", "%", "%", "PE", "%");
            // boucle sur list
            for (String value : list) {
                /**
                 * TODO
                 */
//        List<TestCaseExecutionData> pastValues = this.testCaseExecutionDataService.findTestCaseExecutionDataByCriteria1(propName, value, exelist);
            }
        } catch (CerberusException ex) {
            return list.get(0);
        }

        return null;
    }
}
