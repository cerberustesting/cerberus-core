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
package org.cerberus.service.soap.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
import org.cerberus.crud.entity.AppService;
import org.cerberus.crud.entity.AppServiceHeader;
import org.cerberus.crud.factory.IFactoryAppService;
import org.cerberus.crud.factory.IFactoryAppServiceHeader;
import org.cerberus.crud.service.IAppServiceService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.service.soap.ISoapService;
import org.cerberus.util.SoapUtil;
import org.cerberus.util.StringUtil;
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
    private IFactoryAppService factoryAppService;
    @Autowired
    private IAppServiceService appServiceService;
    @Autowired
    private IFactoryAppServiceHeader factoryAppServiceHeader;

    @Override
    public SOAPMessage createSoapRequest(String envelope, String method, List<AppServiceHeader> header, String token) throws SOAPException, IOException, SAXException, ParserConfigurationException {
        String unescapedEnvelope = StringEscapeUtils.unescapeXml(envelope);
        boolean is12SoapVersion = SOAP_1_2_NAMESPACE_PATTERN.matcher(unescapedEnvelope).matches();

        MimeHeaders headers = new MimeHeaders();
        for (AppServiceHeader appServiceHeader : header) {
            headers.addHeader(appServiceHeader.getKey(), appServiceHeader.getValue());
        }

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
    public AnswerItem<AppService> callSOAP(String envelope, String servicePath, String method, String attachmentUrl, List<AppServiceHeader> header, String token, int timeOutMs) {
        AnswerItem result = new AnswerItem();
        String unescapedEnvelope = StringEscapeUtils.unescapeXml(envelope);
        boolean is12SoapVersion = SOAP_1_2_NAMESPACE_PATTERN.matcher(unescapedEnvelope).matches();

        AppService serviceSOAP = factoryAppService.create("", "", "", "", "", envelope, "", servicePath, "", method, "", null, "", null);
        ByteArrayOutputStream out = null;
        MessageEvent message = null;

        if (StringUtil.isNullOrEmpty(servicePath)) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP_SERVICEPATHMISSING);
            result.setResultMessage(message);
            return result;
        }
        if (StringUtil.isNullOrEmpty(method)) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP_METHODMISSING);
            result.setResultMessage(message);
            return result;
        }
        if (StringUtil.isNullOrEmpty(envelope)) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP_ENVELOPEMISSING);
            result.setResultMessage(message);
            return result;
        }

        // If header is null we create the list empty.
        if (header == null) {
            header = new ArrayList<AppServiceHeader>();
        }
        // We feed the header with token + Standard SOAP header.
        if (token != null) {
            header.add(factoryAppServiceHeader.create(null, "cerberus-token", token, "Y", 0, "", "", null, "", null));
        }
        header.add(factoryAppServiceHeader.create(null, "SOAPAction", "\"" + method + "\"", "Y", 0, "", "", null, "", null));
        header.add(factoryAppServiceHeader.create(null, "Content-Type", is12SoapVersion ? SOAPConstants.SOAP_1_2_CONTENT_TYPE : SOAPConstants.SOAP_1_1_CONTENT_TYPE, "Y", 0, "", "", null, "", null));
        serviceSOAP.setHeaderList(header);

        SOAPConnectionFactory soapConnectionFactory;
        SOAPConnection soapConnection = null;
        try {
            //Initialize SOAP Connection
            soapConnectionFactory = SOAPConnectionFactory.newInstance();
            soapConnection = soapConnectionFactory.createConnection();
            MyLogger.log(SoapService.class.getName(), Level.DEBUG, "Connection opened");

            // Create SOAP Request
            MyLogger.log(SoapService.class.getName(), Level.DEBUG, "Create request");
            SOAPMessage input = createSoapRequest(envelope, method, header, token);

            //Add attachment File if specified
            //TODO: this feature is not implemented yet therefore is always empty!
            if (!StringUtil.isNullOrEmpty(attachmentUrl)) {
                this.addAttachmentPart(input, attachmentUrl);
            }

            // Store the SOAP Call
            out = new ByteArrayOutputStream();
            input.writeTo(out);
            MyLogger.log(SoapService.class.getName(), Level.DEBUG, "WS call : " + out.toString());
            // We already set the item in order to keep the request message in case of failure of SOAP calls.
            result.setItem(serviceSOAP);

            // Call the WS
            MyLogger.log(SoapService.class.getName(), Level.DEBUG, "Calling WS");
            SOAPMessage soapResponse = soapConnection.call(input, servicePath);
            MyLogger.log(SoapService.class.getName(), Level.DEBUG, "Called WS");
            out = new ByteArrayOutputStream();

            // Store the response
            soapResponse.writeTo(out);
            MyLogger.log(SoapService.class.getName(), Level.DEBUG, "WS response received");
            MyLogger.log(SoapService.class.getName(), Level.DEBUG, "WS response : " + out.toString());

            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSOAP);
            message.setDescription(message.getDescription()
                    .replace("%SERVICEPATH%", servicePath)
                    .replace("%SOAPMETHOD%", method));
            result.setResultMessage(message);

            // We save convert to string the final response from SOAP request.
            serviceSOAP.setResponseHTTPBody(SoapUtil.convertSoapMessageToString(soapResponse));

            // Get result Content Type.
            serviceSOAP.setResponseHTTPBodyContentType(appServiceService.guessContentType(serviceSOAP, AppService.RESPONSEHTTPBODYCONTENTTYPE_XML));

            result.setItem(serviceSOAP);

        } catch (SOAPException | UnsupportedOperationException | IOException | SAXException | ParserConfigurationException | CerberusException e) {
            MyLogger.log(SoapService.class.getName(), Level.ERROR, e.toString());
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP);
            message.setDescription(message.getDescription()
                    .replace("%SERVICEPATH%", servicePath)
                    .replace("%SOAPMETHOD%", method)
                    .replace("%DESCRIPTION%", e.getMessage()));
            result.setResultMessage(message);
            return result;
        } finally {
            try {
                if (soapConnection != null) {
                    soapConnection.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (SOAPException | IOException ex) {
                Logger.getLogger(SoapService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } finally {
                result.setResultMessage(message);
            }
        }

        return result;
    }

}
