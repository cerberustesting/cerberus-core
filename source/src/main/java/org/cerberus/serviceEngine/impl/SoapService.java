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
import java.io.StringReader;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.log4j.Level;
import org.cerberus.entity.ExecutionSOAPResponse;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.log.MyLogger;
import org.cerberus.serviceEngine.ISoapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

/**
 *
 * @author bcivel
 */
@Service
public class SoapService implements ISoapService {
	
	/** The SOAP 1.2 namespace pattern */
	private static final Pattern SOAP_1_2_NAMESPACE_PATTERN = Pattern.compile(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE);

    @Autowired
    ExecutionSOAPResponse executionSOAPResponse;

    @Override
    public SOAPMessage createSoapRequest(String envelope, String method) throws SOAPException, IOException, SAXException, ParserConfigurationException {
    	MimeHeaders headers = new MimeHeaders();
		headers.addHeader("SOAPAction", method);
		headers.addHeader("Content-Type", SOAP_1_2_NAMESPACE_PATTERN.matcher(envelope).matches() ? SOAPConstants.SOAP_1_2_CONTENT_TYPE : SOAPConstants.SOAP_1_1_CONTENT_TYPE);

		InputStream input = new ByteArrayInputStream(envelope.getBytes("UTF-8"));
		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.DYNAMIC_SOAP_PROTOCOL);
        return messageFactory.createMessage(headers, input);
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
