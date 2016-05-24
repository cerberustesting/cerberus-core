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
package org.cerberus.service.engine.impl;

import com.mysql.jdbc.StringUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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
import org.cerberus.crud.entity.SOAPExecution;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.service.engine.ISoapService;
import org.cerberus.util.answer.AnswerItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    public AnswerItem callSOAP(String envelope, String servicePath, String method, String attachmentUrl) {
        AnswerItem result = new AnswerItem();
        SOAPExecution executionSOAP = new SOAPExecution();
        ByteArrayOutputStream out = null;
        MessageEvent message = null;
        if (envelope != null && servicePath != null && method != null) {

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

            } catch (Exception e) {
                MyLogger.log(SoapService.class.getName(), Level.ERROR, e.toString());
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP);
                message.setDescription(message.getDescription().replaceAll("%SOAPNAME%", method));
                message.setDescription(message.getDescription().replaceAll("%DESCRIPTION%", e.getMessage()));
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
                } finally {
                    result.setResultMessage(message);
                }
            }
        }

        return result;
    }

}
