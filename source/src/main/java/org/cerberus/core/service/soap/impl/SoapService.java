/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.service.soap.impl;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.entity.AppServiceHeader;
import org.cerberus.core.crud.factory.IFactoryAppService;
import org.cerberus.core.crud.factory.IFactoryAppServiceHeader;
import org.cerberus.core.crud.service.IAppServiceService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.service.proxy.IProxyService;
import org.cerberus.core.service.soap.ISoapService;
import org.cerberus.core.util.SoapUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author bcivel
 */
@Service
public class SoapService implements ISoapService {

    /**
     * The SOAP 1.2 namespace pattern
     */
    private static final Pattern SOAP_1_2_NAMESPACE_PATTERN = Pattern.compile(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE);
    /**
     * Proxy default config. (Should never be used as default config is inserted
     * into database)
     */
    private static final boolean DEFAULT_PROXY_ACTIVATE = false;
    private static final String DEFAULT_PROXY_HOST = "proxy";
    private static final int DEFAULT_PROXY_PORT = 80;
    private static final boolean DEFAULT_PROXYAUTHENT_ACTIVATE = false;
    private static final String DEFAULT_PROXYAUTHENT_USER = "squid";
    private static final String DEFAULT_PROXYAUTHENT_PASSWORD = "squid";

    private static final Logger LOG = LogManager.getLogger(SoapService.class);

    @Autowired
    private IFactoryAppService factoryAppService;
    @Autowired
    private IAppServiceService appServiceService;
    @Autowired
    private IFactoryAppServiceHeader factoryAppServiceHeader;
    @Autowired
    private IParameterService parameterService;
    @Autowired
    IProxyService proxyService;

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
            LOG.debug("Adding Attachement to SOAP request : " + path);
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
        } catch (Exception ex) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.SOAPLIB_MALFORMED_URL));
        }

    }

    private static SOAPMessage sendSOAPMessage(SOAPMessage message, String url, final Proxy p, final int timeoutms) throws SOAPException, MalformedURLException {
        SOAPConnectionFactory factory = SOAPConnectionFactory.newInstance();
        SOAPConnection connection = factory.createConnection();

        URL endpoint = new URL(null, url, new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL url) throws IOException {
                // The url is the parent of this stream handler, so must
                // create clone
                URL clone = new URL(url.toString());

                URLConnection connection = null;
                if (p == null) {
                    connection = clone.openConnection();

                } else if (p.address().toString().equals("0.0.0.0/0.0.0.0:80")) {
                    connection = clone.openConnection();
                } else {
                    connection = clone.openConnection(p);
                }
                // Set Timeout
                connection.setConnectTimeout(timeoutms);
                connection.setReadTimeout(timeoutms);
                // Custom header
//                connection.addRequestProperty("Developer-Mood", "Happy");
                return connection;
            }
        });

        try {
            SOAPMessage response = connection.call(message, endpoint);
            connection.close();
            return response;
        } catch (Exception e) {
            // Re-try if the connection failed
            SOAPMessage response = connection.call(message, endpoint);
            connection.close();
            return response;
        }
    }

    private void initializeProxyAuthenticator(final String proxyUser, final String proxyPassword) {

        if (proxyUser != null && proxyPassword != null) {
            Authenticator.setDefault(
                    new Authenticator() {
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                            proxyUser, proxyPassword.toCharArray()
                    );
                }
            }
            );
            System.setProperty("http.proxyUser", proxyUser);
            System.setProperty("http.proxyPassword", proxyPassword);
        }
    }

    @Override
    public AnswerItem<AppService> callSOAP(String envelope, String servicePath, String soapOperation, String attachmentUrl, List<AppServiceHeader> header, String token, int timeOutMs, String system) {
        AnswerItem<AppService> result = new AnswerItem<>();
        String unescapedEnvelope = StringEscapeUtils.unescapeXml(envelope);
        boolean is12SoapVersion = SOAP_1_2_NAMESPACE_PATTERN.matcher(unescapedEnvelope).matches();

        AppService serviceSOAP = factoryAppService.create("", AppService.TYPE_SOAP, null, "", "", "", envelope, "", "", "", "", "", "", "", servicePath, true, "", soapOperation, false, "", false, "", false, "", null, "", null, "", null, null);
        serviceSOAP.setTimeoutms(timeOutMs);
        ByteArrayOutputStream out = null;
        MessageEvent message = null;

        if (StringUtil.isEmptyOrNull(servicePath)) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP_SERVICEPATHMISSING);
            result.setResultMessage(message);
            return result;
        }
        if (StringUtil.isEmptyOrNull(envelope)) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP_ENVELOPEMISSING);
            result.setResultMessage(message);
            return result;
        }

        // If header is null we create the list empty.
        if (header == null) {
            header = new ArrayList<>();
        }
        // We feed the header with token + Standard SOAP header.
        if (token != null) {
            header.add(factoryAppServiceHeader.create(null, "cerberus-token", token, true, 0, "", "", null, "", null));
        }
        if (StringUtil.isEmptyOrNull(soapOperation)) {
            header.add(factoryAppServiceHeader.create(null, "SOAPAction", "", true, 0, "", "", null, "", null));
        } else {
            header.add(factoryAppServiceHeader.create(null, "SOAPAction", "\"" + soapOperation + "\"", true, 0, "", "", null, "", null));
        }
        header.add(factoryAppServiceHeader.create(null, "Content-Type", is12SoapVersion ? SOAPConstants.SOAP_1_2_CONTENT_TYPE : SOAPConstants.SOAP_1_1_CONTENT_TYPE, true, 0, "", "", null, "", null));
        serviceSOAP.setHeaderList(header);

        SOAPConnectionFactory soapConnectionFactory;
        SOAPConnection soapConnection = null;
        try {
            //Initialize SOAP Connection
            soapConnectionFactory = SOAPConnectionFactory.newInstance();
            soapConnection = soapConnectionFactory.createConnection();

            LOG.debug("Connection opened");

            // Create SOAP Request
            LOG.debug("Create request");
            SOAPMessage input = createSoapRequest(envelope, soapOperation, header, token);

            //Add attachment File if specified
            if (!StringUtil.isEmptyOrNull(attachmentUrl)) {
                this.addAttachmentPart(input, attachmentUrl);
                // Store the SOAP Call
                out = new ByteArrayOutputStream();
                input.writeTo(out);
                LOG.debug("WS call with attachement : " + out.toString());
                serviceSOAP.setServiceRequest(out.toString());
            } else {
                // Store the SOAP Call
                out = new ByteArrayOutputStream();
                input.writeTo(out);
                LOG.debug("WS call : " + out.toString());
            }

            // We already set the item in order to keep the request message in case of failure of SOAP calls.
            serviceSOAP.setService(servicePath);

            result.setItem(serviceSOAP);

            // Call the WS
            LOG.debug("Calling WS.");

            // Reset previous Authentification.
            Authenticator.setDefault(null);
            serviceSOAP.setProxyWithCredential(false);
            serviceSOAP.setProxyUser(null);

            SOAPMessage soapResponse = null;
            if (proxyService.useProxy(servicePath, system)) {

                // Get Proxy host and port from parameters.
                String proxyHost = parameterService.getParameterStringByKey("cerberus_proxy_host", system, DEFAULT_PROXY_HOST);
                int proxyPort = parameterService.getParameterIntegerByKey("cerberus_proxy_port", system, DEFAULT_PROXY_PORT);

                serviceSOAP.setProxy(true);
                serviceSOAP.setProxyHost(proxyHost);
                serviceSOAP.setProxyPort(proxyPort);

                // Create the Proxy.
                SocketAddress sockaddr = new InetSocketAddress(proxyHost, proxyPort);
                try (Socket socket = new Socket();) {
                    socket.connect(sockaddr, 10000);
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(socket.getInetAddress(), proxyPort));

                    if (parameterService.getParameterBooleanByKey("cerberus_proxyauthentification_active", system, DEFAULT_PROXYAUTHENT_ACTIVATE)) {

                        // Get the credentials from parameters.
                        String proxyUser = parameterService.getParameterStringByKey("cerberus_proxyauthentification_user", system, DEFAULT_PROXYAUTHENT_USER);
                        String proxyPass = parameterService.getParameterStringByKey("cerberus_proxyauthentification_password", system, DEFAULT_PROXYAUTHENT_PASSWORD);
                        serviceSOAP.setProxyWithCredential(true);
                        serviceSOAP.setProxyUser(proxyUser);
                        // Define the credential to the proxy.
                        initializeProxyAuthenticator(proxyUser, proxyPass);

                    }
                    // Call with Proxy.
                    serviceSOAP.setStart(new Timestamp(new Date().getTime()));
                    soapResponse = sendSOAPMessage(input, servicePath, proxy, timeOutMs);
                    serviceSOAP.setEnd(new Timestamp(new Date().getTime()));
                } catch (Exception e) {
                    LOG.error("Exception when trying to callSOAP on URL : '" + servicePath + "' for operation : '" + soapOperation + "'", e);
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP);
                    message.setDescription(message.getDescription()
                            .replace("%SERVICEPATH%", servicePath)
                            .replace("%SOAPMETHOD%", soapOperation)
                            .replace("%DESCRIPTION%", e.getMessage()));
                    result.setResultMessage(message);
                    return result;
                }
            } else {

                serviceSOAP.setProxy(false);
                serviceSOAP.setProxyHost(null);
                serviceSOAP.setProxyPort(0);

                // Call without proxy.
                serviceSOAP.setStart(new Timestamp(new Date().getTime()));
                soapResponse = sendSOAPMessage(input, servicePath, null, timeOutMs);
                serviceSOAP.setEnd(new Timestamp(new Date().getTime()));
//                soapResponse = soapConnection.call(input, servicePath);

            }

            LOG.debug("Called WS.");
            out = new ByteArrayOutputStream();

            // Store the response
            soapResponse.writeTo(out);
            LOG.debug("WS response received.");
            LOG.debug("WS response : " + out.toString());

            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSOAP);
            message.setDescription(message.getDescription()
                    .replace("%SERVICEPATH%", servicePath)
                    .replace("%SOAPMETHOD%", soapOperation));
            result.setResultMessage(message);

            //soapResponse.getSOAPPart().getEnvelope().getBody().getFault().getFaultCode();
            // We save convert to string the final response from SOAP request.
            serviceSOAP.setResponseHTTPBody(SoapUtil.convertSoapMessageToString(soapResponse));

            // Get result Content Type.
            serviceSOAP.setResponseHTTPBodyContentType(appServiceService.guessContentType(serviceSOAP, AppService.RESPONSEHTTPBODYCONTENTTYPE_XML));

            result.setItem(serviceSOAP);

        } catch (SOAPException | UnsupportedOperationException | IOException | SAXException
                | ParserConfigurationException | CerberusException e) {
            LOG.error("Exception when trying to callSOAP on URL : '" + servicePath + "' for operation : '" + soapOperation + "'", e);
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP);
            message.setDescription(message.getDescription()
                    .replace("%SERVICEPATH%", servicePath)
                    .replace("%SOAPMETHOD%", soapOperation)
                    .replace("%DESCRIPTION%", StringUtil.getExceptionCauseFromString(e)));
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
                LOG.error(ex, ex);
            } finally {
                result.setResultMessage(message);
            }
        }

        return result;
    }

}
