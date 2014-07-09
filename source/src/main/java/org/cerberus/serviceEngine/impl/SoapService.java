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
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.apache.log4j.Level;
import org.cerberus.entity.ExecutionSOAPResponse;
import org.cerberus.entity.ExecutionUUID;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.entity.TestCaseExecution;
import org.cerberus.log.MyLogger;
import org.cerberus.service.ICountryEnvironmentDatabaseService;
import org.cerberus.serviceEngine.ISoapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author bcivel
 */
@Service
public class SoapService implements ISoapService {

    @Autowired
    ExecutionSOAPResponse executionSOAPResponse;

    @Override
    public SOAPMessage createSoapRequest(String pBody, String method) throws SOAPException, IOException, SAXException, ParserConfigurationException {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);

        SOAPMessage soapMessage = messageFactory.createMessage();

        MimeHeaders headers = soapMessage.getMimeHeaders();

        // WSDL Method to call
        headers.addHeader("SOAPAction", method);
        // Encode UTF-8
        headers.addHeader("Content-Type", "text/xml;charset=UTF-8");

        final SOAPBody soapBody = soapMessage.getSOAPBody();

        // convert String into InputStream
        String unescaped = HtmlUtils.htmlUnescape(pBody);

        InputStream is = new ByteArrayInputStream(unescaped.getBytes());
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = null;

        builderFactory.setNamespaceAware(true);
        try {
            builder = builderFactory.newDocumentBuilder();

            Document document = builder.parse(is);

            soapBody.addDocument(document);
        } catch (ParserConfigurationException e) {
            MyLogger.log(SoapService.class.getName(), Level.ERROR, e.toString());
        } finally {
            is.close();
            if (builder != null) {
                builder.reset();
            }
        }
        soapMessage.saveChanges();

        return soapMessage;
    }

    @Override
    public MessageEvent callSOAPAndStoreResponseInMemory(String uuid, String envelope, String servicePath, String method) {
        String result = null;
        ByteArrayOutputStream out = null;
        MessageEvent message = null;
        if (envelope != null && servicePath != null && method != null) {

            SOAPConnectionFactory soapConnectionFactory;
            SOAPConnection soapConnection = null;
            try {
                //Initialize SOAP Connection
                soapConnectionFactory = SOAPConnectionFactory.newInstance();
                soapConnection = soapConnectionFactory.createConnection();
                MyLogger.log(SoapService.class.getName(), Level.INFO, "Connection opened");

                // Create SOAP Request
                MyLogger.log(SoapService.class.getName(), Level.INFO, "Create request");
                SOAPMessage input = createSoapRequest(envelope, method);

                // Call the WS
                MyLogger.log(SoapService.class.getName(), Level.INFO, "Calling WS");
                MyLogger.log(SoapService.class.getName(), Level.INFO, "Input :" + input);
                SOAPMessage soapResponse = soapConnection.call(input, servicePath);

                out = new ByteArrayOutputStream();

                // Store the response in memory (Using the persistent ExecutionSOAPResponse object)
                soapResponse.writeTo(out);
                MyLogger.log(SoapService.class.getName(), Level.INFO, "WS response received");
                MyLogger.log(SoapService.class.getName(), Level.DEBUG, "WS response : " + out.toString());
                result = out.toString();
                executionSOAPResponse.setExecutionSOAPResponse(uuid, result);
                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSOAP);
                message.setDescription(message.getDescription().replaceAll("%SOAPNAME%", method));
                return message;

            } catch (SOAPException e) {
                MyLogger.log(SoapService.class.getName(), Level.ERROR, e.toString());
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP);
                message.setDescription(message.getDescription().replaceAll("%SOAPNAME%", method));
            } catch (IOException e) {
                MyLogger.log(SoapService.class.getName(), Level.ERROR, e.toString());
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP);
                message.setDescription(message.getDescription().replaceAll("%SOAPNAME%", method));
            } catch (ParserConfigurationException e) {
                MyLogger.log(SoapService.class.getName(), Level.ERROR, e.toString());
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP);
                message.setDescription(message.getDescription().replaceAll("%SOAPNAME%", method));
            } catch (SAXException e) {
                MyLogger.log(SoapService.class.getName(), Level.ERROR, e.toString());
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP);
                message.setDescription(message.getDescription().replaceAll("%SOAPNAME%", method));
            } finally {
                try {
                    if (soapConnection != null) {
                        soapConnection.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                    MyLogger.log(SoapService.class.getName(), Level.INFO, "Connection and ByteArray closed");
                } catch (SOAPException ex) {
                    Logger.getLogger(SoapService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(SoapService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
            }
        }

        return message;
    }

}
