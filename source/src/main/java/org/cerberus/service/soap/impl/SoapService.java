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
package org.cerberus.service.soap.impl;

import org.cerberus.engine.execution.impl.RecorderService;
import com.mysql.jdbc.StringUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.activation.DataHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Level;
import org.cerberus.engine.entity.SOAPExecution;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.crud.entity.TestDataLib;
import org.cerberus.crud.service.ITestDataLibService;
import org.cerberus.engine.entity.TestDataLibResult;
import org.cerberus.engine.entity.TestDataLibResultSOAP;
import org.cerberus.engine.gwt.IPropertyService;
import org.cerberus.engine.gwt.impl.PropertyService;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.enums.TestDataLibTypeEnum;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.service.soap.ISoapService;
import org.cerberus.service.xmlunit.IXmlUnitService;
import org.cerberus.util.SoapUtil;
import org.cerberus.util.answer.AnswerItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author bcivel
 */
@Service
public class SoapService implements ISoapService {

    /**
     * The SOAP 1.2 namespace pattern
     */
    private static final Pattern SOAP_1_2_NAMESPACE_PATTERN = Pattern.compile(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE);

    @Autowired
    RecorderService recorderService;
    @Autowired
    private ITestDataLibService testDataLibService;
    @Autowired
    private IPropertyService propertyService;
    @Autowired
    private IXmlUnitService xmlUnitService;

    @Override
    public SOAPMessage createSoapRequest(String envelope, String method) throws SOAPException, IOException, SAXException, ParserConfigurationException {
        String unescapedEnvelope = StringEscapeUtils.unescapeXml(envelope);
        boolean is12SoapVersion = SOAP_1_2_NAMESPACE_PATTERN.matcher(unescapedEnvelope).matches();

        MimeHeaders headers = new MimeHeaders();
        headers.addHeader("SOAPAction", method);
        headers.addHeader("Content-Type", is12SoapVersion ? SOAPConstants.SOAP_1_2_CONTENT_TYPE : SOAPConstants.SOAP_1_1_CONTENT_TYPE);

        InputStream input = new ByteArrayInputStream(unescapedEnvelope.getBytes("UTF-8"));
        MessageFactory messageFactory = MessageFactory.newInstance(is12SoapVersion ? SOAPConstants.SOAP_1_2_PROTOCOL : SOAPConstants.SOAP_1_1_PROTOCOL);
        return messageFactory.createMessage(headers, input);
    }

    @Override
    public void addAttachmentPart(SOAPMessage input, String path) throws CerberusException {
        URL url;
        try {
            url = new URL(path);
            DataHandler handler = new DataHandler(url);
            //TODO: verify if this code is necessary
            /*String str = "";
             StringBuilder sb = new StringBuilder();
             BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
             while (null != (str = br.readLine())) {
             sb.append(str);
             }*/
            AttachmentPart attachPart = input.createAttachmentPart(handler);
            input.addAttachmentPart(attachPart);
        } catch (MalformedURLException ex) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.SOAPLIB_MALFORMED_URL));
        } catch (IOException ex) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.SOAPLIB_MALFORMED_URL));
        }

    }

    @Override
    public AnswerItem<SOAPExecution> callSOAP(String envelope, String servicePath, String method, String attachmentUrl) {
        AnswerItem result = new AnswerItem();
        SOAPExecution executionSOAP = new SOAPExecution();
        ByteArrayOutputStream out = null;
        MessageEvent message = null;

        if (StringUtils.isNullOrEmpty(servicePath)) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP_SERVICEPATHMISSING);
            result.setResultMessage(message);
            return result;
        }
        if (StringUtils.isNullOrEmpty(method)) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP_METHODMISSING);
            result.setResultMessage(message);
            return result;
        }
        if (StringUtils.isNullOrEmpty(envelope)) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP_ENVELOPEMISSING);
            result.setResultMessage(message);
            return result;
        }

        SOAPConnectionFactory soapConnectionFactory;
        SOAPConnection soapConnection = null;
        try {
            //Initialize SOAP Connection
            soapConnectionFactory = SOAPConnectionFactory.newInstance();
            soapConnection = soapConnectionFactory.createConnection();
            MyLogger.log(SoapService.class.getName(), Level.DEBUG, "Connection opened");

            // Create SOAP Request
            MyLogger.log(SoapService.class.getName(), Level.DEBUG, "Create request");
            SOAPMessage input = createSoapRequest(envelope, method);

            //Add attachment File if specified
            //TODO: this feature is not implemented yet therefore is always empty!
            if (!StringUtils.isNullOrEmpty(attachmentUrl)) {
                this.addAttachmentPart(input, attachmentUrl);
            }

            // Store the SOAP Call
            out = new ByteArrayOutputStream();
            input.writeTo(out);
            MyLogger.log(SoapService.class.getName(), Level.DEBUG, "WS call : " + out.toString());
            executionSOAP.setSOAPRequest(input);

            // Call the WS
            MyLogger.log(SoapService.class.getName(), Level.DEBUG, "Calling WS");
            SOAPMessage soapResponse = soapConnection.call(input, servicePath);
            MyLogger.log(SoapService.class.getName(), Level.DEBUG, "Called WS");
            out = new ByteArrayOutputStream();

            // Store the response
            soapResponse.writeTo(out);
            MyLogger.log(SoapService.class.getName(), Level.DEBUG, "WS response received");
            MyLogger.log(SoapService.class.getName(), Level.DEBUG, "WS response : " + out.toString());
            executionSOAP.setSOAPResponse(soapResponse);

            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSOAP);
            message.setDescription(message.getDescription().replaceAll("%SOAPNAME%", method));
            result.setItem(executionSOAP);

        } catch (SOAPException | UnsupportedOperationException | IOException | SAXException | ParserConfigurationException | CerberusException e) {
            MyLogger.log(SoapService.class.getName(), Level.ERROR, e.toString());
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP);
            message.setDescription(message.getDescription()
                    .replaceAll("%SERVICEPATH%", servicePath)
                    .replaceAll("%SOAPNAME%", method)
                    .replaceAll("%DESCRIPTION%", e.getMessage()));
        } finally {
            try {
                if (soapConnection != null) {
                    soapConnection.close();
                }
                if (out != null) {
                    out.close();
                }
                MyLogger.log(SoapService.class.getName(), Level.INFO, "Connection and ByteArray closed");
            } catch (SOAPException | IOException ex) {
                Logger.getLogger(SoapService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } finally {
                result.setResultMessage(message);
            }
        }

        return result;
    }

    @Override
    public AnswerItem callSoapFromDataLib(TestCaseStepActionExecution testCaseStepActionExecution, String propertyName) {
        AnswerItem answerItem = new AnswerItem();
        TestCaseExecution testCaseExecution = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution();
        MessageEvent message;

        //property exists
        String libName = testCaseStepActionExecution.getObject();
        String system = testCaseExecution.getApplication().getSystem();
        String environment = testCaseExecution.getEnvironment();
        String country = testCaseExecution.getCountry();

        AnswerItem answerLib = testDataLibService.readByNameBySystemByEnvironmentByCountry(libName, system, environment, country);

        if (answerLib.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && answerLib.getItem() != null) {
            TestDataLib lib = (TestDataLib) answerLib.getItem();
            //unescape all attributes that were escaped: description, script, method, servicepath and envelope
            this.unescapeTestDataLibrary(lib);
            if (lib.getType().equals(TestDataLibTypeEnum.SOAP.getCode())) {
                //now we can call the soap entry
                this.calculateInnerProperties(lib, testCaseStepActionExecution);
                AnswerItem callAnswer = this.fetchDataSOAP(lib);
                //store request and response
                testCaseExecution.setLastSOAPCalled(((TestDataLibResultSOAP) callAnswer.getItem()).getSoapExecution());

                //updates the result data
                if (callAnswer.isCodeEquals(MessageEventEnum.PROPERTY_SUCCESS.getCode())) {
                    //new need to update the results list and the execution operation
                    TestDataLibResultSOAP resultSoap = (TestDataLibResultSOAP) callAnswer.getItem();

                    HashMap<String, TestDataLibResult> currentListResults = testCaseExecution.getDataLibraryExecutionDataList();
                    if (currentListResults == null) {
                        currentListResults = new HashMap<String, TestDataLibResult>();
                    }
                    if (currentListResults.get(propertyName) != null) {
                        currentListResults.remove(propertyName);
                    }
                    currentListResults.put(propertyName, resultSoap);
                    //updates the execution data list
                    testCaseExecution.setDataLibraryExecutionDataList(currentListResults);
                }
                message = callAnswer.getResultMessage();
            } else {
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAPBETA_NOTSOAP);
                message.setDescription(message.getDescription().replace("%ENTRY%", libName));
            }
        } else {
            //library entry is not defined for the specified: name + country + system + environment
            message = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_NOT_FOUND_ERROR);
            message.setDescription(message.getDescription().replace("%ITEM%", libName).
                    replace("%COUNTRY%", country).
                    replace("%ENVIRONMENT%", environment).
                    replace("%SYSTEM%", system));
        }

        answerItem.setResultMessage(message);
        return answerItem;
    }

    private void unescapeTestDataLibrary(TestDataLib lib) {
        //unescape all data  
        lib.setDescription(StringEscapeUtils.unescapeHtml4(lib.getDescription()));
        //SQL
        lib.setScript(StringEscapeUtils.unescapeHtml4(lib.getScript()));

        //SOAP
        lib.setServicePath(StringEscapeUtils.unescapeHtml4(lib.getServicePath()));
        lib.setMethod(StringEscapeUtils.unescapeHtml4(lib.getMethod()));
        lib.setEnvelope(StringEscapeUtils.unescapeXml(lib.getEnvelope()));
    }

    private AnswerItem fetchDataSOAP(TestDataLib lib) {
        AnswerItem answer = new AnswerItem();
        MessageEvent msg;
        TestDataLibResult result = null;
        SOAPExecution executionSoap = new SOAPExecution();

        //soap data needs to get the soap response
        String key = TestDataLibTypeEnum.SOAP.getCode() + lib.getTestDataLibID();
        AnswerItem ai = this.callSOAP(lib.getEnvelope(), lib.getServicePath(),
                lib.getMethod(), null);
        executionSoap = (SOAPExecution) ai.getItem();
        msg = ai.getResultMessage();

        //if the call returns success then we can process the soap ressponse
        if (msg.getCode() == MessageEventEnum.ACTION_SUCCESS_CALLSOAP.getCode()) {
            result = new TestDataLibResultSOAP();
            ((TestDataLibResultSOAP) result).setSoapExecution(ai);
            ((TestDataLibResultSOAP) result).setSoapResponseKey(key);
            result.setTestDataLibID(lib.getTestDataLibID());
            Document xmlDocument = xmlUnitService.getXmlDocument(SoapUtil.convertSoapMessageToString(executionSoap.getSOAPResponse()));
            ((TestDataLibResultSOAP) result).setData(xmlDocument);
            //the code for action success call soap is different from the
            //code return from the property success soap
            //if the action succeeds then, we can assume that the SOAP request was performed with success
            msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_SOAP);

            // saving the raw data to the result.
            result.setDataLibRawData(null);

        }
        answer.setItem(result);
        answer.setResultMessage(msg);
        return answer;
    }

    /**
     * Auxiliary method that calculates the inner properties that are defined in
     * a test data library entry
     *
     * @param lib - test data library entry
     * @param testCaseStepActionExecution step action execution
     */
    private void calculateInnerProperties(TestDataLib lib, TestCaseStepActionExecution testCaseStepActionExecution) {
        try {
            if (lib.getType().equals(TestDataLibTypeEnum.SOAP.getCode())) {
                //check if the servicepath contains properties that needs to be calculated
                String decodedServicePath = propertyService.getValue(lib.getServicePath(), testCaseStepActionExecution, false);
                lib.setServicePath(decodedServicePath);
                //check if the method contains properties that needs to be calculated
                String decodedMethod = propertyService.getValue(lib.getMethod(), testCaseStepActionExecution, false);
                lib.setMethod(decodedMethod);
                //check if the envelope contains properties that needs to be calculated
                String decodedEnvelope = propertyService.getValue(lib.getEnvelope(), testCaseStepActionExecution, false);
                lib.setEnvelope(decodedEnvelope);

            } else if (lib.getType().equals(TestDataLibTypeEnum.SQL.getCode())) {
                //check if the script contains properties that needs to be calculated
                String decodedScript = propertyService.getValue(lib.getScript(), testCaseStepActionExecution, false);
                lib.setScript(decodedScript);

            }
        } catch (CerberusEventException cex) {
            Logger.getLogger(PropertyService.class.getName()).log(java.util.logging.Level.SEVERE, "calculateInnerProperties", cex);
        }
    }

}
