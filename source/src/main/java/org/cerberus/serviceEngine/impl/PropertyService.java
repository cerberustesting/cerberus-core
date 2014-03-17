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
package org.cerberus.serviceEngine.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.log4j.Level;
import org.cerberus.dao.ITestCaseExecutionDataDAO;
import org.cerberus.entity.CountryEnvironmentDatabase;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.Property;
import org.cerberus.entity.TestCaseCountryProperties;
import org.cerberus.entity.TestCaseExecution;
import org.cerberus.entity.TestCaseExecutionData;
import org.cerberus.entity.TestCaseStepActionExecution;
import org.cerberus.entity.SoapLibrary;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.service.ICountryEnvironmentDatabaseService;
import org.cerberus.service.ISoapLibraryService;
import org.cerberus.service.ISqlLibraryService;
import org.cerberus.service.ITestCaseExecutionService;
import org.cerberus.service.ITestDataService;
import org.cerberus.serviceEngine.IConnectionPoolDAO;
import org.cerberus.serviceEngine.IPropertyService;
import org.cerberus.serviceEngine.ISeleniumService;
import org.cerberus.util.DateUtil;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.openqa.selenium.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @since 0.9.0
 */
@Service
public class PropertyService implements IPropertyService {

    @Autowired
    private ISeleniumService seleniumService;
    @Autowired
    private ISqlLibraryService sqlLibraryService;
    @Autowired 
    private ISoapLibraryService soapLibraryService;
    @Autowired
    private IConnectionPoolDAO connectionPoolDAO;
    @Autowired
    private ICountryEnvironmentDatabaseService countryEnvironmentDatabaseService;
    @Autowired
    private ITestCaseExecutionDataDAO testCaseExecutionDataDAO;
    @Autowired
    private ITestCaseExecutionService testCaseExecutionService;
    @Autowired
    private ITestDataService testDataService;

    /** Format de date nécessaire pour interroger les Web services REDOUTE - le timeZone +01:00 est en dur car en java 6 le format par défaut est +0100 */
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+01:00");
    
    @Override
    public TestCaseExecutionData calculateProperty(TestCaseExecutionData testCaseExecutionData, TestCaseStepActionExecution testCaseStepActionExecution, TestCaseCountryProperties testCaseCountryProperty) {
        testCaseExecutionData.setStart(new Date().getTime());
        MessageEvent res;

        TestCaseExecution tCExecution = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution();

        if (testCaseCountryProperty.getValue1().contains("%")) {
            String decodedValue = this.decodeValue(testCaseCountryProperty.getValue1(), testCaseStepActionExecution.getTestCaseExecutionDataList(), tCExecution);
            testCaseExecutionData.setValue(decodedValue);
            testCaseCountryProperty.setValue1(decodedValue);
        }
        
        if ((testCaseCountryProperty.getType().equals("executeSqlFromLib")) || (testCaseCountryProperty.getType().equals("executeSql"))) {
            if (testCaseCountryProperty.getType().equals("executeSqlFromLib")) {
                try {
                    String script = this.sqlLibraryService.findSqlLibraryByKey(testCaseCountryProperty.getValue1()).getScript();
                    testCaseExecutionData.setValue(script);
                } catch (CerberusException ex) {
                    Logger.getLogger(PropertyService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_SQLLIB_NOTEXIT);
                    res.setDescription(res.getDescription().replaceAll("%SQLLIB%", testCaseCountryProperty.getValue1()));
                    testCaseExecutionData.setPropertyResultMessage(res);
                    testCaseExecutionData.setEnd(new Date().getTime());
                    return testCaseExecutionData;
                }
            }

            testCaseExecutionData = this.calculateOnDatabase(testCaseExecutionData, testCaseCountryProperty, tCExecution);

        } else if (testCaseCountryProperty.getType().equals("text")) {
            if ((testCaseCountryProperty.getNature().equals("RANDOM"))
                    || (testCaseCountryProperty.getNature().equals("RANDOM_NEW"))) {
                if (testCaseCountryProperty.getLength() == 0) {
                    res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_TEXTRANDOMLENGHT0);
                    testCaseExecutionData.setPropertyResultMessage(res);
                } else {
                    String charset;
                    if (testCaseCountryProperty.getValue1() != null && !"".equals(testCaseCountryProperty.getValue1().trim())) {
                        charset = testCaseCountryProperty.getValue1();
                    } else {
                        charset = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
                    }
                    String value = StringUtil.getRandomString(testCaseCountryProperty.getLength(), charset);
                    testCaseExecutionData.setValue(value);
                    res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_RANDOM);
                    res.setDescription(res.getDescription().replaceAll("%VALUE%", ParameterParserUtil.securePassword(value, testCaseCountryProperty.getProperty())));
                    testCaseExecutionData.setPropertyResultMessage(res);
//                    if (testCaseCountryProperty.getNature().equals("RANDOM_NEW")) {
//                        //TODO check if value exist on DB ( used in another test case of the revision )
//                    }
                }
            } else {
                MyLogger.log(PropertyService.class.getName(), Level.DEBUG, "Setting value : " + testCaseCountryProperty.getValue1());
                String value = testCaseCountryProperty.getValue1();
                testCaseExecutionData.setValue(value);
                res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_TEXT);
                res.setDescription(res.getDescription().replaceAll("%VALUE%", ParameterParserUtil.securePassword(value, testCaseCountryProperty.getProperty())));
                testCaseExecutionData.setPropertyResultMessage(res);
            }

        } else if (testCaseCountryProperty.getType().equals("getFromHtmlVisible")) {
            try {
                String valueFromHTML = this.seleniumService.getValueFromHTMLVisible(testCaseCountryProperty.getValue1());
                if (valueFromHTML != null) {
                    testCaseExecutionData.setValue(valueFromHTML);
                    res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_HTMLVISIBLE);
                    res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseCountryProperty.getValue1()));
                    res.setDescription(res.getDescription().replaceAll("%VALUE%", valueFromHTML));
                    testCaseExecutionData.setPropertyResultMessage(res);
                }
            } catch (NoSuchElementException exception) {
                MyLogger.log(PropertyService.class.getName(), Level.ERROR, exception.toString());
                res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_HTMLVISIBLE_ELEMENTDONOTEXIST);
                res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseCountryProperty.getValue1()));
                testCaseExecutionData.setPropertyResultMessage(res);
            }
        } else if (testCaseCountryProperty.getType().equals("getFromHtml")) {
            try {
                String valueFromHTML = this.seleniumService.getValueFromHTML(testCaseCountryProperty.getValue1());
                if (valueFromHTML != null) {
                    testCaseExecutionData.setValue(valueFromHTML);
                    res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_HTML);
                    res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseCountryProperty.getValue1()));
                    res.setDescription(res.getDescription().replaceAll("%VALUE%", valueFromHTML));
                    testCaseExecutionData.setPropertyResultMessage(res);
                }
            } catch (NoSuchElementException exception) {
                MyLogger.log(PropertyService.class.getName(), Level.ERROR, exception.toString());
                res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_HTML_ELEMENTDONOTEXIST);
                res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseCountryProperty.getValue1()));
                testCaseExecutionData.setPropertyResultMessage(res);
            }
        } else if (testCaseCountryProperty.getType().equals("getFromJS")) {
            try {
                String script = testCaseCountryProperty.getValue1();
                String valueFromJS = this.seleniumService.getValueFromJS(script);
                if (valueFromJS != null) {
                    testCaseExecutionData.setValue(valueFromJS);
                    res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_HTML);
                    res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseCountryProperty.getValue1()));
                    res.setDescription(res.getDescription().replaceAll("%VALUE%", script));
                    testCaseExecutionData.setPropertyResultMessage(res);
                }
            } catch (NoSuchElementException exception) {
                MyLogger.log(PropertyService.class.getName(), Level.ERROR, exception.toString());
                res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_HTML_ELEMENTDONOTEXIST);
                res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseCountryProperty.getValue1()));
                testCaseExecutionData.setPropertyResultMessage(res);
            }
        }else if (testCaseCountryProperty.getType().equals("getFromTestData")) {
            try {
                String propertyValue = testCaseCountryProperty.getValue1();
                String valueFromTestData = testDataService.findTestDataByKey(propertyValue).getValue();
                if (valueFromTestData != null) {
                    testCaseExecutionData.setValue(valueFromTestData);
                    res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_TESTDATA);
                    res.setDescription(res.getDescription().replaceAll("%PROPERTY%", propertyValue));
                    res.setDescription(res.getDescription().replaceAll("%VALUE%", valueFromTestData));
                    testCaseExecutionData.setPropertyResultMessage(res);
                }
            } catch (CerberusException exception) {
                MyLogger.log(PropertyService.class.getName(), Level.ERROR, exception.toString());
                res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_TESTDATA_PROPERTYDONOTEXIST);
                res.setDescription(res.getDescription().replaceAll("%PROPERTY%", testCaseCountryProperty.getValue1()));
                testCaseExecutionData.setPropertyResultMessage(res);
            }
        } else if (testCaseCountryProperty.getType().equals("getAttributeFromHTMLElement")) {
            try {
                String valueFromHTML = this.seleniumService.getAttributeFromHTMLElement(testCaseCountryProperty.getValue1(), testCaseCountryProperty.getValue2());
                if (valueFromHTML != null) {
                    testCaseExecutionData.setValue(valueFromHTML);
                    res = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETATTRIBUTEFROMHTML);
                    res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseCountryProperty.getValue1()));
                    res.setDescription(res.getDescription().replaceAll("%ATTRIBUTE%", testCaseCountryProperty.getValue2()));
                    res.setDescription(res.getDescription().replaceAll("%VALUE%", valueFromHTML));
                    testCaseExecutionData.setPropertyResultMessage(res);
                }
            } catch (NoSuchElementException exception) {
                MyLogger.log(PropertyService.class.getName(), Level.ERROR, exception.toString());
                res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_HTMLVISIBLE_ELEMENTDONOTEXIST);
                res.setDescription(res.getDescription().replaceAll("%ELEMENT%", testCaseCountryProperty.getValue1()));
                testCaseExecutionData.setPropertyResultMessage(res);
            }
        } else if ("executeSoapFromLib".equals(testCaseCountryProperty.getType())) {
            try
            {
                SoapLibrary soapLib = this.soapLibraryService.findSoapLibraryByKey(testCaseCountryProperty.getValue1());
                if (soapLib != null) {
                     String result = calculatePropertyFromSOAPResponse(soapLib.getEnvelope(), soapLib.getServicePath(), soapLib.getParsingAnswer(), soapLib.getMethod());
                     if(result != null) {
                         testCaseExecutionData.setValue(result);
                         testCaseExecutionData.setPropertyResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_SOAP));
                     }
                }
            } catch (CerberusException exception) {                
                MyLogger.log(PropertyService.class.getName(), Level.ERROR, exception.toString());
                res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_TESTDATA_PROPERTYDONOTEXIST);
                res.setDescription(res.getDescription().replaceAll("%PROPERTY%", testCaseCountryProperty.getValue1()));
                testCaseExecutionData.setPropertyResultMessage(res);
            }
        }
        

    else {
            res = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_UNKNOWNPROPERTY);
            res.setDescription(res.getDescription().replaceAll("%PROPERTY%", testCaseCountryProperty.getType()));
        }

        testCaseExecutionData.setEnd(new Date().getTime());
        return testCaseExecutionData;
    }

    @Override
    public String decodeValue(String myString, List<TestCaseExecutionData> properties, TestCaseExecution tCExecution) {

        /**
         * Trying to replace by system environment variables .
         */
        myString = StringUtil.replaceAllProperties(myString, "%SYS_SYSTEM%", tCExecution.getApplication().getSystem());
        myString = StringUtil.replaceAllProperties(myString, "%SYS_APPLI%", tCExecution.getApplication().getApplication());
        myString = StringUtil.replaceAllProperties(myString, "%SYS_ENV%", tCExecution.getEnvironmentData());
        myString = StringUtil.replaceAllProperties(myString, "%SYS_ENVGP%", tCExecution.getEnvironmentDataObj().getGp1());
        myString = StringUtil.replaceAllProperties(myString, "%SYS_COUNTRY%", tCExecution.getCountry());
        myString = StringUtil.replaceAllProperties(myString, "%SYS_COUNTRYGP1%", tCExecution.getCountryObj().getGp1());
        myString = StringUtil.replaceAllProperties(myString, "%SYS_SSIP%", tCExecution.getSeleniumIP());
        myString = StringUtil.replaceAllProperties(myString, "%SYS_SSPORT%", tCExecution.getSeleniumPort());
        myString = StringUtil.replaceAllProperties(myString, "%SYS_TAG%", tCExecution.getTag());

        /**
         * Trying to replace date variables .
         */
        myString = StringUtil.replaceAllProperties(myString, "%SYS_TODAY-yyyy%", DateUtil.getTodayFormat("yyyy"));
        myString = StringUtil.replaceAllProperties(myString, "%SYS_TODAY-MM%", DateUtil.getTodayFormat("MM"));
        myString = StringUtil.replaceAllProperties(myString, "%SYS_TODAY-dd%", DateUtil.getTodayFormat("dd"));
        myString = StringUtil.replaceAllProperties(myString, "%SYS_TODAY-HH%", DateUtil.getTodayFormat("HH"));
        myString = StringUtil.replaceAllProperties(myString, "%SYS_TODAY-mm%", DateUtil.getTodayFormat("mm"));
        myString = StringUtil.replaceAllProperties(myString, "%SYS_TODAY-ss%", DateUtil.getTodayFormat("ss"));
        myString = StringUtil.replaceAllProperties(myString, "%SYS_YESTERDAY-yyyy%", DateUtil.getYesterdayFormat("yyyy"));
        myString = StringUtil.replaceAllProperties(myString, "%SYS_YESTERDAY-MM%", DateUtil.getYesterdayFormat("MM"));
        myString = StringUtil.replaceAllProperties(myString, "%SYS_YESTERDAY-dd%", DateUtil.getYesterdayFormat("dd"));
        myString = StringUtil.replaceAllProperties(myString, "%SYS_YESTERDAY-HH%", DateUtil.getYesterdayFormat("HH"));
        myString = StringUtil.replaceAllProperties(myString, "%SYS_YESTERDAY-mm%", DateUtil.getYesterdayFormat("mm"));
        myString = StringUtil.replaceAllProperties(myString, "%SYS_YESTERDAY-ss%", DateUtil.getYesterdayFormat("ss"));

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

    private TestCaseExecutionData calculateOnDatabase(TestCaseExecutionData testCaseExecutionData, TestCaseCountryProperties testCaseProperties, TestCaseExecution tCExecution) {
        String sql = testCaseProperties.getValue1();
        String db = testCaseProperties.getDatabase();

        String connectionName;
        CountryEnvironmentDatabase countryEnvironmentDatabase;

        try {
            countryEnvironmentDatabase = this.countryEnvironmentDatabaseService.findCountryEnvironmentDatabaseByKey(tCExecution.getApplication().getSystem(), testCaseProperties.getCountry(), tCExecution.getEnvironmentData(), db);
            connectionName = countryEnvironmentDatabase.getConnectionPoolName();

            if (!(StringUtil.isNullOrEmpty(connectionName))) {
                try {
                    List<String> list = this.connectionPoolDAO.queryDatabase(connectionName, sql, testCaseProperties.getRowLimit());

                    if (list != null && !list.isEmpty()) {
                        if (testCaseProperties.getNature().equalsIgnoreCase(Property.NATURE_STATIC)) {
                            MessageEvent mes = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_SQL);
                            mes.setDescription(mes.getDescription().replaceAll("%DB%", db));
                            mes.setDescription(mes.getDescription().replaceAll("%SQL%", sql));
                            mes.setDescription(mes.getDescription().replaceAll("%JDBCPOOLNAME%", connectionName));
                            testCaseExecutionData.setPropertyResultMessage(mes);
                            testCaseExecutionData.setValue(list.get(0));

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
                    } else {
                        MessageEvent mes = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_SQL_NODATA);
                        mes.setDescription(mes.getDescription().replaceAll("%DB%", db));
                        mes.setDescription(mes.getDescription().replaceAll("%SQL%", sql));
                        mes.setDescription(mes.getDescription().replaceAll("%JDBCPOOLNAME%", connectionName));
                        testCaseExecutionData.setPropertyResultMessage(mes);
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
        if (!list.isEmpty()) {
            if (rowLimit == 0) {
                return list.get(random.nextInt(list.size()));
            } else {
                int index = Math.min(list.size(), rowLimit);
                return list.get(random.nextInt(index));
            }
        }
        return null;
    }

    private String calculateNatureRandomNew(List<String> list, String propName, TestCaseExecution tCExecution) {
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

    private String calculateNatureNotInUse(List<String> list, String propName, TestCaseExecution tCExecution) {
        try {
//            List<TCExecution> exelist = this.testCaseExecutionService.findTCExecutionbyCriteria1(DateUtil.getMySQLTimestampTodayDeltaMinutes(10), "%", "%", "%", "%", "%", "PE", "%");
            this.testCaseExecutionService.findTCExecutionbyCriteria1(DateUtil.getMySQLTimestampTodayDeltaMinutes(10), "%", "%", "%", "%", "%", "PE", "%");
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
    
    /**
     * Calcule d'une propriété depuis une requête SOAP.
     */
    private String calculatePropertyFromSOAPResponse(String envelope, String servicePath, String parsingAnswer, String method) throws CerberusException {
        String result = null;
        // Test des inputs nécessaires.
        if(envelope != null && servicePath != null && parsingAnswer != null && method != null) {
            SOAPConnectionFactory soapConnectionFactory;
    
            SOAPConnection soapConnection;
            try {
                soapConnectionFactory = SOAPConnectionFactory
					.newInstance();
                soapConnection = soapConnectionFactory.createConnection();

                // Création de la requete SOAP
                SOAPMessage input = createSOAPRequest(envelope, method);

                // Appel du WS
                SOAPMessage soapResponse = soapConnection.call(input, servicePath);

                // Traitement de la r the SOAP Response
                result = parseSOAPResponse(soapResponse, parsingAnswer);

                soapConnection.close();
            
            } catch (SOAPException e)
            {
                MyLogger.log(PropertyService.class.getName(), Level.ERROR, e.toString());
                 throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA));
            } catch (IOException e)
            {
                MyLogger.log(PropertyService.class.getName(), Level.ERROR, e.toString());
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA));
            }
        }
        return result;
    }
    
    /**
     * Création d'une requête SOAP
     * @param envelope Envelope complète de la requête SOAP exécutée depuis SOAPUI (table SOAPLIBRARY.ENVELOPE)
     * @param method Nom de la méthode du WSDL à interroger(table SOAPLIBRARY.METHOD)
     * @return SOAPMessage
     * @throws SOAPException
     * @throws IOException 
     */
    private SOAPMessage createSOAPRequest(String envelope, String method) throws SOAPException, IOException {

        MessageFactory messageFactory = MessageFactory.newInstance();

	SOAPMessage soapMessage = messageFactory.createMessage();

	SOAPPart soapPart = soapMessage.getSOAPPart();

	Reader reader = new StringReader(envelope);

        String date = format.format(new Date());
        StringBuilder soapRequest = new StringBuilder();
        soapRequest.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns=\"http://RedouteFrance/Technical/CERBERUS/Technical/2.0/ExecuteSQLRequest/1.0\">"
                        + "<soapenv:Header/><soapenv:Body><ns:ExecuteSQLRequestRequest_1.0><Header><Application>CERBERUS</Application><Channel>Local</Channel><UserDisplayLanguage>fr</UserDisplayLanguage><UserDisplayCountry>FR</UserDisplayCountry>"
                        + "<Environment>Development</Environment><Timestamp>"+date+"</Timestamp></Header><Request><SQLRequest><![CDATA[");
        soapRequest.append(envelope);
        
        soapRequest.append("]]></SQLRequest>"
                        + "</Request>"
                        + "</ns:ExecuteSQLRequestRequest_1.0>"
                        + "</soapenv:Body>" + "</soapenv:Envelope>");
          
        String tmp = soapRequest.toString();
        System.out.println(tmp);
	reader.read(tmp.toCharArray(), 0, tmp.length());
	
        // CTE à faire pour éviter une SAXParseException premature end of file
	reader.reset();

	StreamSource prepMsg = new StreamSource(reader);
        
        soapPart.setContent(prepMsg);

	MimeHeaders headers = soapMessage.getMimeHeaders();

        // Précise la méthode du WSDL à interroger
        headers.addHeader("SOAPAction", method);
        // Encodage UTF-8
	headers.addHeader("Content-Type", "text/xml;charset=UTF-8");

	soapMessage.saveChanges();
        
	return soapMessage;
    }
    
    /**
     * Parse la réponse de la requête SOAP en fonction de la règle XPATH (rule).
     * @param soapResponse Réponse de la requête SOAP
     * @param rule règle de parsing XPATH (table SOAPLIBRARY.PARSINGANSWER)
     * @return String
     */
    private String parseSOAPResponse(SOAPMessage soapResponse, String rule)
    {
	String result = null;
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory
				.newInstance();
	DocumentBuilder builder = null;
	try {
            builder = builderFactory.newDocumentBuilder();	
	
            ByteArrayOutputStream out = new ByteArrayOutputStream(); 
            soapResponse.writeTo(out); 
            InputStream is = new ByteArrayInputStream( out.toByteArray() ); 
            Document xmlDocument = builder.parse( is );

            XPath xPath = XPathFactory.newInstance().newXPath();
		
            NodeList nodeList2 = (NodeList) xPath.compile(rule)
					.evaluate(xmlDocument, XPathConstants.NODESET);
            
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < nodeList2.getLength(); i++) {
                // On retourne le premier noeud non null trouvé 
                if(!StringUtil.isNullOrEmpty(nodeList2.item(i).getFirstChild()
						.getNodeValue())){
                    s.append(nodeList2.item(i).getFirstChild()
						.getNodeValue());
                }
            }
            result = s.toString();
        } catch (SOAPException e){
            MyLogger.log(PropertyService.class.getName(), Level.ERROR, e.toString());
        } catch (SAXParseException e) {
            MyLogger.log(PropertyService.class.getName(), Level.ERROR, e.toString());
	} catch (SAXException e) {
            MyLogger.log(PropertyService.class.getName(), Level.ERROR, e.toString());
	} catch (IOException e) {
            MyLogger.log(PropertyService.class.getName(), Level.ERROR, e.toString());
	} catch (XPathExpressionException e) {
            MyLogger.log(PropertyService.class.getName(), Level.ERROR, e.toString());
        } catch (ParserConfigurationException e) {
            MyLogger.log(PropertyService.class.getName(), Level.ERROR, e.toString());
	}
		
        return result;
    }
}
