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
package org.cerberus.service.rest.impl;

import org.cerberus.engine.execution.impl.RecorderService;
import com.mysql.jdbc.StringUtils;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.cerberus.crud.entity.AppService;
import org.cerberus.crud.entity.AppServiceHeader;
import org.cerberus.crud.entity.AppServiceContent;
import org.cerberus.crud.factory.impl.FactoryAppService;
import org.cerberus.crud.factory.impl.FactoryAppServiceHeader;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
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
    RecorderService recorderService;
    @Autowired
    FactoryAppServiceHeader factoryAppServiceHeader;
    @Autowired
    FactoryAppService factoryAppService;

    private static final Logger LOG = Logger.getLogger(RestService.class);

    @Override
    public AnswerItem<AppService> callREST(String servicePath, String queryString, String method, List<AppServiceHeader> headerList, List<AppServiceContent> contentList, String token, int timeOutMs) {
        AnswerItem result = new AnswerItem();
        AppService serviceREST = factoryAppService.create("", AppService.TYPE_REST, method, "", "", "", "", "", "", "", "", null, "", null);
        MessageEvent message = null;

        if (StringUtils.isNullOrEmpty(servicePath)) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_SERVICEPATHMISSING);
            result.setResultMessage(message);
            return result;
        }
        if (StringUtils.isNullOrEmpty(method)) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_METHODMISSING);
            result.setResultMessage(message);
            return result;
        }
        // If token is defined, we add 'cerberus-token' on the http header.
        if (token != null) {
            headerList.add(factoryAppServiceHeader.create(null, "cerberus-token", token, "Y", 0, "", "", null, "", null));
        }
        try {

            String responseBody = "";
            int responseCode = 0;
            Response resp = null;
            Request req = null;
            switch (method) {
                case AppService.METHOD_HTTPGET:

                    // TODO Add the QueryString
//                    req = Request.Get(servicePath);
                    // Call the REST
//                    resp = req.connectTimeout(timeOutMs).socketTimeout(timeOutMs).execute();
                    LOG.info("Start preparing the REST Call (GET). " + servicePath + " - " + queryString);
                    CloseableHttpClient httpclient = HttpClients.createDefault();
                    try {
                        servicePath = StringUtil.addQueryString(servicePath, queryString);
                        serviceREST.setServicePath(servicePath);
                        HttpGet httpget = new HttpGet(servicePath);
                        // Header.
                        for (AppServiceHeader contentHeader : headerList) {
                            httpget.addHeader(contentHeader.getKey(), contentHeader.getValue());
//                        req.addHeader(contentHeader.getKey(), contentHeader.getValue());
                        }
                        serviceREST.setHeaderList(headerList);
                        result.setItem(serviceREST);

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
                        LOG.info("Executing request " + httpget.getRequestLine());
                        AppService responseHttp;
                        responseHttp = httpclient.execute(httpget, responseHandler);
                        serviceREST.setResponseHTTPBody(responseHttp.getResponseHTTPBody());
                        serviceREST.setResponseHTTPCode(responseHttp.getResponseHTTPCode());
                        serviceREST.setResponseHTTPVersion(responseHttp.getResponseHTTPVersion());
                        serviceREST.setResponseHeaderList(responseHttp.getResponseHeaderList());

//                responseBody = resp.returnContent().asString();
                    } finally {
                        httpclient.close();
                    }

                    break;
                case AppService.METHOD_HTTPPOST:
                    LOG.info("Start preparing the REST Call (POST). " + servicePath);
                    req = Request.Post(servicePath);
                    serviceREST.setServicePath(servicePath);
                    // Content.
                    List<BasicNameValuePair> requestData = new ArrayList<>();
                    for (AppServiceContent contentVal : contentList) {
                        requestData.add(new BasicNameValuePair(contentVal.getKey(), contentVal.getValue()));
                    }
                    if (!(contentList.isEmpty())) {
                        req.bodyForm(requestData);
                    }
                    serviceREST.setContentList(contentList);
                    // Header.
                    for (AppServiceHeader contentHeader : headerList) {
                        req.addHeader(contentHeader.getKey(), contentHeader.getValue());
                    }
                    serviceREST.setHeaderList(headerList);
                    result.setItem(serviceREST);
                    // Call the REST
                    resp = req.connectTimeout(timeOutMs).socketTimeout(timeOutMs).execute();
                    LOG.info("REST Call performed (POST). " + servicePath);
                    break;
            }

//            if (resp != null) {
//                responseBody = resp.returnContent().asString();
            // TODO Manage the return Code + Content Type.
//                responseCode = resp.returnResponse().getStatusLine().getStatusCode();
//            }
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
        }

        return result;
    }

}
