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
import java.util.ArrayList;
import java.util.List;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.cerberus.crud.entity.AppService;
import org.cerberus.crud.entity.AppServiceHeader;
import org.cerberus.crud.entity.AppServiceContent;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.service.rest.IRestService;
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

    private static final Logger LOG = Logger.getLogger(RestService.class);

    @Override
    public AnswerItem<AppService> callREST(String servicePath, String queryString, String method, List<AppServiceHeader> headerList, List<AppServiceContent> contentList) {
        AnswerItem result = new AnswerItem();
        AppService serviceREST = new AppService();
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
        try {

            String responseBody = "";
            int responseCode = 0;
            Response resp = null;
            Request req = null;
            switch (method) {
                case AppService.METHOD_HTTPGET:
                    // TODO Add the QueryString
                    req = Request.Get(servicePath);
                    // Header.
                    for (AppServiceHeader contentHeader : headerList) {
                        req.addHeader(contentHeader.getKey(), contentHeader.getValue());
                    }
                    // Call the REST
                    resp = req.execute();
                    break;
                case AppService.METHOD_HTTPPOST:
                    req = Request.Post(servicePath);
                    // Content.
                    List<BasicNameValuePair> requestData = new ArrayList<>();
                    for (AppServiceContent contentVal : contentList) {
                        requestData.add(new BasicNameValuePair(contentVal.getKey(), contentVal.getValue()));
                    }
                    if (!(contentList.isEmpty())) {
                        req.bodyForm(requestData);
                    }
                    // Header.
                    for (AppServiceHeader contentHeader : headerList) {
                        req.addHeader(contentHeader.getKey(), contentHeader.getValue());
                    }
                    // Call the REST
                    resp = req.execute();
                    break;
            }

            if (resp != null) {
                responseBody = resp.returnContent().asString();
                // TODO Manage the return Code
//                responseCode = resp.returnResponse().getStatusLine().getStatusCode();
            }

            serviceREST.setHTTPResponseBody(responseBody);
            serviceREST.setHTTPResponseCode(responseCode);
            serviceREST.setMethod(method);
            serviceREST.setServicePath(servicePath);
            serviceREST.setServiceRequest(queryString);
            serviceREST.setContentList(contentList);
            serviceREST.setHeaderList(headerList);
            result.setItem(serviceREST);
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSERVICE);
            message.setDescription(message.getDescription().replace("%SERVICEMETHOD%", method));
            message.setDescription(message.getDescription().replace("%SERVICEPATH%", servicePath));
            result.setResultMessage(message);

        } catch (IOException ex) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
            message.setDescription(message.getDescription().replace("%SERVICE%", servicePath));
            message.setDescription(message.getDescription().replace("%DESCRIPTION%", ex.toString()));
            result.setResultMessage(message);
            return result;
        }

        return result;
    }

}
