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
package org.cerberus.service.rest.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.cerberus.crud.entity.AppService;
import org.cerberus.crud.entity.AppServiceContent;
import org.cerberus.crud.entity.AppServiceHeader;
import org.cerberus.crud.factory.IFactoryAppService;
import org.cerberus.crud.factory.IFactoryAppServiceHeader;
import org.cerberus.crud.service.IAppServiceService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.execution.IRecorderService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.service.proxy.IProxyService;
import org.cerberus.service.rest.IRestService;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
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
    IProxyService proxyService;

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

    private static final Logger LOG = Logger.getLogger(RestService.class);

    private AppService executeHTTPCall(CloseableHttpClient httpclient, HttpRequestBase httpget) throws Exception {
        try {
            // Create a custom response handler
            ResponseHandler<AppService> responseHandler = new ResponseHandler<AppService>() {

                @Override
                public AppService handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    AppService myResponse = factoryAppService.create("", AppService.TYPE_REST, AppService.METHOD_HTTPGET, "", "", "", "", "", "", "", "", null, "", null);
                    int responseCode = response.getStatusLine().getStatusCode();
                    myResponse.setResponseHTTPCode(responseCode);
                    myResponse.setResponseHTTPVersion(response.getProtocolVersion().toString());
                    LOG.info(String.valueOf(responseCode) + " " + response.getProtocolVersion().toString());
                    Header[] allHeaderList = response.getAllHeaders();
                    for (Header header : allHeaderList) {
                        myResponse.addResponseHeaderList(factoryAppServiceHeader.create(null, header.getName(), header.getValue(), "Y", 0, "", "", null, "", null));
                    }
                    HttpEntity entity = response.getEntity();
                    myResponse.setResponseHTTPBody(entity != null ? EntityUtils.toString(entity) : null);
                    return myResponse;
                }

            };
            return httpclient.execute(httpget, responseHandler);

        } catch (Exception ex) {
            LOG.error(ex.toString());
            throw ex;
        } finally {
            httpclient.close();
        }
    }

    @Override
    public AnswerItem<AppService> callREST(String servicePath, String requestString, String method, List<AppServiceHeader> headerList,
            List<AppServiceContent> contentList, String token, int timeOutMs, String system) {
        AnswerItem result = new AnswerItem();
        AppService serviceREST = factoryAppService.create("", AppService.TYPE_REST, method, "", "", "", "", "", "", "", "", null, "", null);
        serviceREST.setProxy(false);
        serviceREST.setProxyHost(null);
        serviceREST.setProxyPort(0);
        serviceREST.setProxyWithCredential(false);
        serviceREST.setProxyUser(null);
        serviceREST.setTimeoutms(timeOutMs);
        MessageEvent message = null;

        if (StringUtil.isNullOrEmpty(servicePath)) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_SERVICEPATHMISSING);
            result.setResultMessage(message);
            return result;
        }
        if (StringUtil.isNullOrEmpty(method)) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_METHODMISSING);
            result.setResultMessage(message);
            return result;
        }
        // If token is defined, we add 'cerberus-token' on the http header.
        if (token != null) {
            headerList.add(factoryAppServiceHeader.create(null, "cerberus-token", token, "Y", 0, "", "", null, "", null));
        }

        CloseableHttpClient httpclient;
        if (proxyService.useProxy(servicePath, system)) {

            String proxyHost = parameterService.getParameterStringByKey("cerberus_proxy_host", system, DEFAULT_PROXY_HOST);
            int proxyPort = parameterService.getParameterIntegerByKey("cerberus_proxy_port", system, DEFAULT_PROXY_PORT);

            serviceREST.setProxy(true);
            serviceREST.setProxyHost(proxyHost);
            serviceREST.setProxyPort(proxyPort);

            HttpHost proxyHostObject = new HttpHost(proxyHost, proxyPort);

            if (parameterService.getParameterBooleanByKey("cerberus_proxyauthentification_active", system, DEFAULT_PROXYAUTHENT_ACTIVATE)) {

                String proxyUser = parameterService.getParameterStringByKey("cerberus_proxyauthentification_user", system, DEFAULT_PROXYAUTHENT_USER);
                String proxyPassword = parameterService.getParameterStringByKey("cerberus_proxyauthentification_password", system, DEFAULT_PROXYAUTHENT_PASSWORD);

                serviceREST.setProxyWithCredential(true);
                serviceREST.setProxyUser(proxyUser);

                CredentialsProvider credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(
                        new AuthScope(proxyHost, proxyPort),
                        new UsernamePasswordCredentials(proxyUser, proxyPassword));

                LOG.debug("Activating Proxy With Authentification.");
                httpclient = HttpClientBuilder.create().setProxy(proxyHostObject).setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy()).setDefaultCredentialsProvider(credsProvider).build();

            } else {

                LOG.debug("Activating Proxy (No Authentification).");
                httpclient = HttpClientBuilder.create().setProxy(proxyHostObject).build();

            }

        } else {

            httpclient = HttpClients.createDefault();

        }

        try {

            RequestConfig requestConfig;
            // Timeout setup.
            requestConfig = RequestConfig.custom().setConnectTimeout(timeOutMs).setConnectionRequestTimeout(timeOutMs).setSocketTimeout(timeOutMs).build();

            AppService responseHttp = null;

            switch (method) {
                case AppService.METHOD_HTTPGET:

                    LOG.info("Start preparing the REST Call (GET). " + servicePath + " - " + requestString);

                    servicePath = StringUtil.addQueryString(servicePath, requestString);
                    serviceREST.setServicePath(servicePath);
                    HttpGet httpGet = new HttpGet(servicePath);

                    // Timeout setup.
                    httpGet.setConfig(requestConfig);

                    // Header.
                    for (AppServiceHeader contentHeader : headerList) {
                        httpGet.addHeader(contentHeader.getKey(), contentHeader.getValue());
                    }
                    serviceREST.setHeaderList(headerList);

                    // Saving the service before the call Just in case it goes wrong (ex : timeout).
                    result.setItem(serviceREST);

                    LOG.info("Executing request " + httpGet.getRequestLine());
                    responseHttp = executeHTTPCall(httpclient, httpGet);

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

                    // Content
                    if (!(StringUtil.isNullOrEmpty(requestString))) {
                        // If requestString is defined, we POST it.
                        InputStream stream = new ByteArrayInputStream(requestString.getBytes(StandardCharsets.UTF_8));
                        InputStreamEntity reqEntity = new InputStreamEntity(stream);
                        reqEntity.setChunked(true);
                        httpPost.setEntity(reqEntity);
                        serviceREST.setServiceRequest(requestString);
                    } else {
                        // If requestString is not defined, we POST the list of key/value request.
                        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                        for (AppServiceContent contentVal : contentList) {
                            nvps.add(new BasicNameValuePair(contentVal.getKey(), contentVal.getValue()));
                        }
                        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
                        serviceREST.setContentList(contentList);
                    }

                    // Header.
                    for (AppServiceHeader contentHeader : headerList) {
                        httpPost.addHeader(contentHeader.getKey(), contentHeader.getValue());
                    }
                    serviceREST.setHeaderList(headerList);

                    // Saving the service before the call Just in case it goes wrong (ex : timeout).
                    result.setItem(serviceREST);

                    LOG.info("Executing request " + httpPost.getRequestLine());
                    responseHttp = executeHTTPCall(httpclient, httpPost);

                    if (responseHttp != null) {
                        serviceREST.setResponseHTTPBody(responseHttp.getResponseHTTPBody());
                        serviceREST.setResponseHTTPCode(responseHttp.getResponseHTTPCode());
                        serviceREST.setResponseHTTPVersion(responseHttp.getResponseHTTPVersion());
                        serviceREST.setResponseHeaderList(responseHttp.getResponseHeaderList());
                    } else {
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
                        message.setDescription(message.getDescription().replace("%SERVICE%", servicePath));
                        message.setDescription(message.getDescription().replace("%DESCRIPTION%", "Any issue was found when calling the service. Coud be a reached timeout during the call (." + timeOutMs + ")"));
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
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSERVICE);
            message.setDescription(message.getDescription().replace("%SERVICEMETHOD%", method));
            message.setDescription(message.getDescription().replace("%SERVICEPATH%", servicePath));
            result.setResultMessage(message);

        } catch (SocketTimeoutException ex) {
            LOG.info("Exception when performing the REST Call. " + ex.toString());
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_TIMEOUT);
            message.setDescription(message.getDescription().replace("%SERVICEURL%", servicePath));
            message.setDescription(message.getDescription().replace("%TIMEOUT%", String.valueOf(timeOutMs)));
            result.setResultMessage(message);
            return result;
        } catch (Exception ex) {
            LOG.error("Exception when performing the REST Call. " + ex.toString());
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
            message.setDescription(message.getDescription().replace("%SERVICE%", servicePath));
            message.setDescription(message.getDescription().replace("%DESCRIPTION%", "Error on CallREST : " + ex.toString()));
            result.setResultMessage(message);
            return result;
        } finally {
            try {
                httpclient.close();
            } catch (IOException ex) {
                LOG.error(ex.toString());
            }
        }

        return result;
    }

}
