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
package org.cerberus.core.service.rest.impl;

import java.io.EOFException;
import java.io.File;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.entity.AppServiceContent;
import org.cerberus.core.crud.entity.AppServiceHeader;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.factory.IFactoryAppService;
import org.cerberus.core.crud.factory.IFactoryAppServiceHeader;
import org.cerberus.core.crud.service.IAppServiceService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.execution.IRecorderService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.service.proxy.IProxyService;
import org.cerberus.core.service.rest.IRestService;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.concurrent.NotThreadSafe;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.cerberus.core.crud.service.IAppServiceHeaderService;
import org.cerberus.core.engine.gwt.IVariableService;
import org.cerberus.core.exception.CerberusEventException;

/**
 * @author bcivel
 */
@Service
public class RestService implements IRestService {

    @Autowired
    IRecorderService recorderService;
    @Autowired
    IFactoryAppServiceHeader factoryAppServiceHeader;
    @Autowired
    IParameterService parameterService;
    @Autowired
    IFactoryAppService factoryAppService;
    @Autowired
    IAppServiceService AppServiceService;
    @Autowired
    IAppServiceHeaderService AppServiceHeaderService;
    @Autowired
    IProxyService proxyService;
    @Autowired
    private IVariableService variableService;

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

    private static final Logger LOG = LogManager.getLogger(RestService.class);

    @NotThreadSafe
    class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {

        public static final String METHOD_NAME = "DELETE";

        @Override
        public String getMethod() {
            return METHOD_NAME;
        }

        public HttpDeleteWithBody(final String uri) {
            super();
            setURI(URI.create(uri));
        }

        public HttpDeleteWithBody(final URI uri) {
            super();
            setURI(uri);
        }

        public HttpDeleteWithBody() {
            super();
        }
    }

    private AppService executeHTTPCall(CloseableHttpClient httpclient, HttpRequestBase httpget) throws Exception {
        try {
            // Create a custom response handler
            ResponseHandler<AppService> responseHandler = (final HttpResponse response) -> {
                AppService myResponse = factoryAppService.create("", AppService.TYPE_REST,
                        AppService.METHOD_HTTPGET, "", "", "", "", "", "", "", "", "", "", "", "", true, "", "", false, "", false, "", false, "", null, "", null, "", null, null);
                int responseCode = response.getStatusLine().getStatusCode();
                myResponse.setResponseHTTPCode(responseCode);
                myResponse.setResponseHTTPVersion(response.getProtocolVersion().toString());
                LOG.info(String.valueOf(responseCode) + " " + response.getProtocolVersion().toString());
                Header[] allHeaderList = response.getAllHeaders();
                for (Header header : allHeaderList) {
                    myResponse.addResponseHeaderList(factoryAppServiceHeader.create(null, header.getName(),
                            header.getValue(), true, 0, "", "", null, "", null));
                }
                try {
                    HttpEntity entity = response.getEntity();
                    myResponse.setResponseHTTPBody(entity != null ? EntityUtils.toString(entity) : null);
                } catch (EOFException ex) {
                    myResponse.setResponseHTTPBody(null);
                }
                return myResponse;
            };
            return httpclient.execute(httpget, responseHandler);

        } catch (Exception ex) {
            LOG.error(ex.toString(), ex);
            throw ex;
        } finally {
            httpclient.close();
        }
    }

    @Override
    public AnswerItem<AppService> callREST(String servicePath, String requestString, String method, String bodyType,
            List<AppServiceHeader> headerList, List<AppServiceContent> contentList, String token, int timeOutMs,
            String system, boolean isFollowRedir, TestCaseExecution tcexecution, String description,
            String authType, String authUser, String authPassword, String authAddTo) {

        AnswerItem<AppService> result = new AnswerItem<>();
        AppService serviceREST = factoryAppService.create("", AppService.TYPE_REST, method, "", "", "", "", "", "", "", "", "", "", "", "", true, "", "", false, "", false, "", false, "", null,
                "", null, "", null, null);
        serviceREST.setProxy(false);
        serviceREST.setProxyHost(null);
        serviceREST.setProxyPort(0);
        serviceREST.setProxyWithCredential(false);
        serviceREST.setProxyUser(null);
        serviceREST.setTimeoutms(timeOutMs);
        MessageEvent message = null;

        if (StringUtil.isEmptyOrNull(servicePath)) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_SERVICEPATHMISSING);
            result.setResultMessage(message);
            return result;
        }
        if (StringUtil.isEmptyOrNull(method)) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_METHODMISSING);
            result.setResultMessage(message);
            return result;
        }
        // If token is defined, we add 'cerberus-token' on the http header.
        if (!StringUtil.isEmptyOrNull(token)) {
            headerList.add(factoryAppServiceHeader.create(null, "cerberus-token", token, true, 0, "", "", null, "", null));
        }

        CloseableHttpClient httpclient = null;
        HttpClientBuilder httpclientBuilder;
        if (proxyService.useProxy(servicePath, system)) {

            String proxyHost = parameterService.getParameterStringByKey("cerberus_proxy_host", system, DEFAULT_PROXY_HOST);
            int proxyPort = parameterService.getParameterIntegerByKey("cerberus_proxy_port", system, DEFAULT_PROXY_PORT);

            serviceREST.setProxy(true);
            serviceREST.setProxyHost(proxyHost);
            serviceREST.setProxyPort(proxyPort);

            HttpHost proxyHostObject = new HttpHost(proxyHost, proxyPort);

            if (parameterService.getParameterBooleanByKey("cerberus_proxyauthentification_active", system,
                    DEFAULT_PROXYAUTHENT_ACTIVATE)) {

                String proxyUser = parameterService.getParameterStringByKey("cerberus_proxyauthentification_user", system, DEFAULT_PROXYAUTHENT_USER);
                String proxyPassword = parameterService.getParameterStringByKey("cerberus_proxyauthentification_password", system, DEFAULT_PROXYAUTHENT_PASSWORD);

                serviceREST.setProxyWithCredential(true);
                serviceREST.setProxyUser(proxyUser);

                CredentialsProvider credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(new AuthScope(proxyHost, proxyPort), new UsernamePasswordCredentials(proxyUser, proxyPassword));

                LOG.debug("Activating Proxy With Authentification.");
                httpclientBuilder = HttpClientBuilder.create().setProxy(proxyHostObject)
                        .setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy())
                        .setDefaultCredentialsProvider(credsProvider);

            } else {

                LOG.debug("Activating Proxy (No Authentification).");
                httpclientBuilder = HttpClientBuilder.create().setProxy(proxyHostObject);
            }
        } else {
            httpclientBuilder = HttpClientBuilder.create();
        }

        // if it is an GUI REST, share the GUI context with api call
        if ((tcexecution != null) && (tcexecution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI))) {
            // When performing a simulation service call, the session may be null, 
            if (tcexecution.getSession() != null) {
                WebDriver driver = tcexecution.getSession().getDriver();

                BasicCookieStore cookieStore = new BasicCookieStore();

                driver.manage().getCookies().forEach(cookieSelenium -> {
                    BasicClientCookie cookie = new BasicClientCookie(cookieSelenium.getName(), cookieSelenium.getValue());
                    cookie.setDomain(cookieSelenium.getDomain());
                    cookie.setPath(cookieSelenium.getPath());
                    cookie.setExpiryDate(cookieSelenium.getExpiry());
                    cookieStore.addCookie(cookie);
                });

                httpclientBuilder.setDefaultCookieStore(cookieStore);
            }
        }

        try {

            boolean acceptUnsignedSsl = parameterService.getParameterBooleanByKey("cerberus_accept_unsigned_ssl_certificate", system, true);

            if (acceptUnsignedSsl) {
                LOG.debug("Trusting all SSL Certificates.");
                // authorize non valide certificat ssl
                SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy() {
                    @Override
                    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        return true;
                    }
                }).build();

                httpclientBuilder
                        .setSSLContext(sslContext)
                        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
            }

            // Disable redir if required. 
            if (isFollowRedir) {
                serviceREST.setFollowRedir(true);
            } else {
                httpclientBuilder.disableRedirectHandling();
                serviceREST.setFollowRedir(false);
            }
            serviceREST.setBodyType(bodyType);

            httpclient = httpclientBuilder.build();

            RequestConfig requestConfig;
            // Timeout setup.
            requestConfig = RequestConfig.custom().setConnectTimeout(timeOutMs).setConnectionRequestTimeout(timeOutMs)
                    .setSocketTimeout(timeOutMs).build();

            // Decode authPassword.
            AnswerItem<String> answerDecode = new AnswerItem<>();
            answerDecode = variableService.decodeStringCompletly(authPassword, tcexecution, null, false);
            authPassword = answerDecode.getItem();
            if (!(answerDecode.isCodeStringEquals("OK"))) {
                // If anything wrong with the decode --> we stop here with decode message in the action result.
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                        .resolveDescription("SERVICENAME", "")
                        .resolveDescription("DESCRIPTION", answerDecode.getResultMessage().resolveDescription("FIELD", "Authentification Password").getDescription());
                LOG.debug("Service Call interupted due to decode 'Authentification Password'.");
                result.setResultMessage(message);
                return result;
            }

            //Decode authUser
            if (AppService.AUTHTYPE_APIKEY.equals(authType) || AppService.AUTHTYPE_BASICAUTH.equals(authType)) {
                // Decode authPassword.
                answerDecode = variableService.decodeStringCompletly(authUser, tcexecution, null, false);
                authUser = answerDecode.getItem();
                if (!(answerDecode.isCodeStringEquals("OK"))) {
                    // If anything wrong with the decode --> we stop here with decode message in the action result.
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                            .resolveDescription("SERVICENAME", "")
                            .resolveDescription("DESCRIPTION", answerDecode.getResultMessage().resolveDescription("FIELD", "Authentification User").getDescription());
                    LOG.debug("Service Call interupted due to decode 'Authentification User'.");
                    result.setResultMessage(message);
                    return result;
                }
            }

            AppService responseHttp = null;
            HttpEntity entity = null;

            // Enrich Query String with auth parameters when necessary
            if (AppService.AUTHTYPE_APIKEY.equals(authType) && AppService.AUTHADDTO_QUERYSTRING.equals(authAddTo)) {
                servicePath = StringUtil.addQueryString(servicePath, authUser + "=" + authPassword);
            }
            // Enrich Headers with auth parameters when necessary
            switch (authType) {
                case AppService.AUTHTYPE_APIKEY:
                    if (!StringUtil.isEmptyOrNull(authPassword) && !StringUtil.isEmptyOrNull(authUser) && AppService.AUTHADDTO_HEADERS.equals(authAddTo)) {
                        headerList.add(factoryAppServiceHeader.create(null, authUser, authPassword, true, 0, "", "", null, "", null));
                    }
                    break;
                case AppService.AUTHTYPE_BASICAUTH:
                    if (!StringUtil.isEmptyOrNull(authPassword) || !StringUtil.isEmptyOrNull(authUser)) {
                        String authHeader = authUser + ":" + authPassword;
                        headerList.add(factoryAppServiceHeader.create(null, "authorization", "Basic " + Base64.getEncoder().encodeToString(authHeader.getBytes()), true, 0, "", "", null, "", null));
                    }
                    break;
                case AppService.AUTHTYPE_BEARERTOKEN:
                    if (!StringUtil.isEmptyOrNull(authPassword)) {
                        headerList.add(factoryAppServiceHeader.create(null, "authorization", "Bearer " + authPassword, true, 0, "", "", null, "", null));
                    }
                    break;
                default:
            }

            // Adding Accept */* automaticaly if no Accept header has been defined already.
            headerList = AppServiceHeaderService.addIfNotExist(headerList, factoryAppServiceHeader.create(null, "Accept", "*/*", true, 0, "Added by engine", "", null, "", null));

            switch (method) {
                case AppService.METHOD_HTTPGET:

                    LOG.info("Start preparing the REST Call (GET). " + servicePath);

                    if (AppService.SRVBODYTYPE_FORMDATA.equals(bodyType)) {
                        // Adding query string from requestString
//                    servicePath = StringUtil.addQueryString(servicePath, requestString);

                        // Adding query string from contentList
                        String newRequestString = AppServiceService.convertContentListToQueryString(contentList, false);
                        servicePath = StringUtil.addQueryString(servicePath, newRequestString);

                    } else if (AppService.SRVBODYTYPE_FORMURLENCODED.equals(bodyType)) {

                        String newRequestString = AppServiceService.convertContentListToQueryString(contentList, true);
                        servicePath = StringUtil.addQueryString(servicePath, newRequestString);

                    }

                    serviceREST.setServicePath(servicePath);
                    HttpGet httpGet = new HttpGet(servicePath);

                    // Timeout setup.
                    httpGet.setConfig(requestConfig);

                    // Header.
                    if (headerList != null) {
                        for (AppServiceHeader contentHeader : headerList) {
                            httpGet.addHeader(contentHeader.getKey(), contentHeader.getValue());
                        }
                    }
                    serviceREST.setHeaderList(headerList);

                    // Saving the service before the call Just in case it goes wrong (ex : timeout).
                    result.setItem(serviceREST);

                    LOG.info("Executing request " + httpGet.getRequestLine());
                    serviceREST.setStart(new Timestamp(new Date().getTime()));
                    responseHttp = executeHTTPCall(httpclient, httpGet);
                    serviceREST.setEnd(new Timestamp(new Date().getTime()));

                    if (responseHttp != null) {
                        serviceREST.setResponseHTTPBody(responseHttp.getResponseHTTPBody());
                        serviceREST.setResponseHTTPCode(responseHttp.getResponseHTTPCode());
                        serviceREST.setResponseHTTPVersion(responseHttp.getResponseHTTPVersion());
                        serviceREST.setResponseHeaderList(responseHttp.getResponseHeaderList());
                    }

                    break;

                case AppService.METHOD_HTTPPOST:

                    LOG.info("Start preparing the REST Call (POST). " + servicePath);

                    serviceREST.setServicePath(servicePath);
                    HttpPost httpPost = new HttpPost(servicePath);

                    // Timeout setup.
                    httpPost.setConfig(requestConfig);

                    // Calculate entity.
                    entity = getEntity(bodyType, requestString, contentList, serviceREST);

                    // Setting entity when defined.
                    if (entity != null) {
                        httpPost.setEntity(entity);
                    }

                    // Header.
                    for (AppServiceHeader contentHeader : headerList) {
                        httpPost.addHeader(contentHeader.getKey(), contentHeader.getValue());
                    }
                    serviceREST.setHeaderList(headerList);

                    // Saving the service before the call Just in case it goes wrong (ex : timeout).
                    result.setItem(serviceREST);

                    LOG.info("Executing request " + httpPost.getRequestLine());
                    serviceREST.setStart(new Timestamp(new Date().getTime()));
                    responseHttp = executeHTTPCall(httpclient, httpPost);
                    serviceREST.setEnd(new Timestamp(new Date().getTime()));

                    if (responseHttp != null) {
                        serviceREST.setResponseHTTPBody(responseHttp.getResponseHTTPBody());
                        serviceREST.setResponseHTTPCode(responseHttp.getResponseHTTPCode());
                        serviceREST.setResponseHTTPVersion(responseHttp.getResponseHTTPVersion());
                        serviceREST.setResponseHeaderList(responseHttp.getResponseHeaderList());
                    } else {
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
                        message.setDescription(message.getDescription().replace("%DESCRIPTION%",
                                "Any issue was found when calling the service. Coud be a reached timeout during the call (."
                                + timeOutMs + ")"));
                        result.setResultMessage(message);
                        return result;

                    }
                    break;

                case AppService.METHOD_HTTPDELETE:

                    LOG.info("Start preparing the REST Call (DELETE). " + servicePath);

                    serviceREST.setServicePath(servicePath);
                    HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(servicePath);

                    // Timeout setup.
                    httpDelete.setConfig(requestConfig);

                    // Calculate entity.
                    entity = getEntity(bodyType, requestString, contentList, serviceREST);

                    // Setting entity when defined.
                    if (entity != null) {
                        httpDelete.setEntity(entity);
                    }

                    // Header.
                    for (AppServiceHeader contentHeader : headerList) {
                        httpDelete.addHeader(contentHeader.getKey(), contentHeader.getValue());
                    }
                    serviceREST.setHeaderList(headerList);

                    // Saving the service before the call Just in case it goes wrong (ex : timeout).
                    result.setItem(serviceREST);

                    LOG.info("Executing request " + httpDelete.getRequestLine());
                    serviceREST.setStart(new Timestamp(new Date().getTime()));
                    responseHttp = executeHTTPCall(httpclient, httpDelete);
                    serviceREST.setEnd(new Timestamp(new Date().getTime()));

                    if (responseHttp != null) {
                        serviceREST.setResponseHTTPBody(responseHttp.getResponseHTTPBody());
                        serviceREST.setResponseHTTPCode(responseHttp.getResponseHTTPCode());
                        serviceREST.setResponseHTTPVersion(responseHttp.getResponseHTTPVersion());
                        serviceREST.setResponseHeaderList(responseHttp.getResponseHeaderList());
                    }

                    break;

                case AppService.METHOD_HTTPPUT:
                    LOG.info("Start preparing the REST Call (PUT). " + servicePath);

                    serviceREST.setServicePath(servicePath);
                    HttpPut httpPut = new HttpPut(servicePath);

                    // Timeout setup.
                    httpPut.setConfig(requestConfig);

                    // Calculate entity.
                    entity = getEntity(bodyType, requestString, contentList, serviceREST);

                    // Setting entity when defined.
                    if (entity != null) {
                        httpPut.setEntity(entity);
                    }

                    // Header.
                    for (AppServiceHeader contentHeader : headerList) {
                        httpPut.addHeader(contentHeader.getKey(), contentHeader.getValue());
                    }
                    serviceREST.setHeaderList(headerList);

                    // Saving the service before the call Just in case it goes wrong (ex : timeout).
                    result.setItem(serviceREST);

                    LOG.info("Executing request " + httpPut.getRequestLine());
                    serviceREST.setStart(new Timestamp(new Date().getTime()));
                    responseHttp = executeHTTPCall(httpclient, httpPut);
                    serviceREST.setEnd(new Timestamp(new Date().getTime()));

                    if (responseHttp != null) {
                        serviceREST.setResponseHTTPBody(responseHttp.getResponseHTTPBody());
                        serviceREST.setResponseHTTPCode(responseHttp.getResponseHTTPCode());
                        serviceREST.setResponseHTTPVersion(responseHttp.getResponseHTTPVersion());
                        serviceREST.setResponseHeaderList(responseHttp.getResponseHeaderList());
                    } else {
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
                        message.setDescription(message.getDescription().replace("%SERVICE%", servicePath));
                        message.setDescription(message.getDescription().replace("%DESCRIPTION%",
                                "Any issue was found when calling the service. Coud be a reached timeout during the call (."
                                + timeOutMs + ")"));
                        result.setResultMessage(message);
                        return result;

                    }
                    break;

                case AppService.METHOD_HTTPPATCH:
                    LOG.info("Start preparing the REST Call (PATCH). " + servicePath);

                    serviceREST.setServicePath(servicePath);
                    HttpPatch httpPatch = new HttpPatch(servicePath);

                    // Timeout setup.
                    httpPatch.setConfig(requestConfig);

                    // Calculate entity.
                    entity = getEntity(bodyType, requestString, contentList, serviceREST);

                    // Setting entity when defined.
                    if (entity != null) {
                        httpPatch.setEntity(entity);
                    }

                    // Header.
                    for (AppServiceHeader contentHeader : headerList) {
                        httpPatch.addHeader(contentHeader.getKey(), contentHeader.getValue());
                    }
                    serviceREST.setHeaderList(headerList);

                    // Saving the service before the call Just in case it goes wrong (ex : timeout).
                    result.setItem(serviceREST);

                    LOG.info("Executing request " + httpPatch.getRequestLine());
                    serviceREST.setStart(new Timestamp(new Date().getTime()));
                    responseHttp = executeHTTPCall(httpclient, httpPatch);
                    serviceREST.setEnd(new Timestamp(new Date().getTime()));

                    if (responseHttp != null) {
                        serviceREST.setResponseHTTPBody(responseHttp.getResponseHTTPBody());
                        serviceREST.setResponseHTTPCode(responseHttp.getResponseHTTPCode());
                        serviceREST.setResponseHTTPVersion(responseHttp.getResponseHTTPVersion());
                        serviceREST.setResponseHeaderList(responseHttp.getResponseHeaderList());
                    } else {
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
                        message.setDescription(message.getDescription().replace("%SERVICE%", servicePath));
                        message.setDescription(message.getDescription().replace("%DESCRIPTION%",
                                "Any issue was found when calling the service. Coud be a reached timeout during the call (."
                                + timeOutMs + ")"));
                        result.setResultMessage(message);
                        return result;

                    }
                    break;

            }

            // Get result Content Type.
            if (responseHttp != null) {
                serviceREST.setResponseHTTPBodyContentType(AppServiceService.guessContentType(serviceREST, AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON));
            }

            result.setItem(serviceREST);
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSERVICE)
                    .resolveDescription("SERVICEMETHOD", method)
                    .resolveDescription("SERVICEPATH", servicePath);
            result.setResultMessage(message);

        } catch (CerberusEventException ex) {
            result.setResultMessage(ex.getMessageError());
            return result;

        } catch (SocketTimeoutException ex) {
            LOG.info("Exception when performing the REST Call. " + ex.toString());
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_TIMEOUT)
                    .resolveDescription("TIMEOUT", String.valueOf(timeOutMs))
                    .resolveDescription("SERVICEURL", servicePath);
            result.setResultMessage(message);
            return result;

        } catch (Exception ex) {
            LOG.error("Exception when performing the REST Call. " + ex.toString(), ex);
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                    .resolveDescription("DESCRIPTION", "Error on CallREST : " + ex.toString())
                    .resolveDescription("SERVICE", servicePath);
            result.setResultMessage(message);
            return result;

        } finally {
            try {
                if (httpclient != null) {
                    httpclient.close();
                }
            } catch (IOException ex) {
                LOG.error(ex.toString(), ex);
            }
        }

        return result;
    }

    private HttpEntity getEntity(String bodyType, String requestString, List<AppServiceContent> contentList, AppService serviceREST) throws CerberusEventException, UnsupportedEncodingException {
        MultipartEntityBuilder builder;
        List<NameValuePair> nvps = new ArrayList<>();
        HttpEntity entity = null;
        MessageEvent message = null;

        switch (bodyType) {

            case AppService.SRVBODYTYPE_RAW:
                serviceREST.setServiceRequest(requestString);
                entity = new StringEntity(requestString, StandardCharsets.UTF_8);
                break;

            case AppService.SRVBODYTYPE_FORMDATA:
                serviceREST.setContentList(contentList);
                builder = MultipartEntityBuilder.create();
                for (AppServiceContent contentVal : contentList) {
                    if (contentVal.getValue().length() > 0 && contentVal.getValue().charAt(0) == '@') {
                        String filePath = contentVal.getValue().substring(1);
                        if (StringUtil.isEmptyOrNULLString(filePath)) {
                            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
                            message.resolveDescription("DESCRIPTION", "file path '" + filePath + "' is empty for key '" + contentVal.getKey() + "' in request details");
                            throw new CerberusEventException(message);
                        } else {
                            File file = new File(filePath);
                            if (file.exists()) {
                                final FileBody fileBody = new FileBody(file);
                                builder.addPart(contentVal.getKey(), fileBody);
                            } else {
                                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
                                message.resolveDescription("DESCRIPTION", "file path '" + filePath + "' does not exist for key '" + contentVal.getKey() + "' in request details");
                                throw new CerberusEventException(message);
                            }
                        }
                    } else {
                        builder.addPart(contentVal.getKey(), new StringBody(contentVal.getValue(), ContentType.TEXT_PLAIN));
                    }
                }
                entity = builder.build();
                break;

            case AppService.SRVBODYTYPE_FORMURLENCODED:
                serviceREST.setContentList(contentList);
                for (AppServiceContent contentVal : contentList) {
                    nvps.add(new BasicNameValuePair(contentVal.getKey(), contentVal.getValue()));
                }
                entity = new UrlEncodedFormEntity(nvps);
                break;

            case AppService.SRVBODYTYPE_NONE:
                break;

            default:
                if (!(StringUtil.isEmptyOrNull(requestString))) {
                    // If requestString is defined, we POST it.
                    serviceREST.setServiceRequest(requestString);
                    entity = new StringEntity(requestString, StandardCharsets.UTF_8);
//                        httpPost.setEntity(new StringEntity(requestString, StandardCharsets.UTF_8));
                } else {
                    serviceREST.setContentList(contentList);
                    // If requestString is not defined, we POST the list of key/value request.
                    for (AppServiceContent contentVal : contentList) {
                        nvps.add(new BasicNameValuePair(contentVal.getKey(), contentVal.getValue()));
                    }
                    entity = new UrlEncodedFormEntity(nvps);
                }
        }
        return entity;
    }

}
