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
package org.cerberus.core.service.robotproviders.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.service.proxy.IProxyService;
import org.cerberus.core.service.robotproviders.IBrowserstackService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author vertigo17
 */
@Service
public class BrowserstackService implements IBrowserstackService {

    @Autowired
    private IProxyService proxyService;

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(BrowserstackService.class);

    @Override
    public void setSessionStatus(String system, String sessionId, String status, String reason, String user, String pass) {
        URI uri;
        HttpPut putRequest;
        LOG.debug("Notify BrowserStack on target status about the end of the execution : " + status);
        try {

            String bsStatus = "failed";
            if (TestCaseExecution.CONTROLSTATUS_OK.equals(status)) {
                bsStatus = "passed";
            }

            String url = "https://" + user + ":" + pass + "@api.browserstack.com/automate/sessions/" + sessionId + ".json";

            HttpClient httpclient = null;
            HttpClientBuilder httpclientBuilder;

            httpclientBuilder = proxyService.getBuilderWithProxy(system, url);
            httpclient = httpclientBuilder.build();
            HttpPut httpPut = new HttpPut(url);

            RequestConfig requestConfig;
            // Timeout setup.
            requestConfig = RequestConfig.custom().setConnectTimeout(10000).setConnectionRequestTimeout(10000)
                    .setSocketTimeout(10000).build();

            // Timeout setup.
            httpPut.setConfig(requestConfig);

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add((new BasicNameValuePair("status", bsStatus)));
            nameValuePairs.add((new BasicNameValuePair("reason", reason)));
            httpPut.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = httpclient.execute(httpPut);
            int responseCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            LOG.debug("Calling : " + url);
            LOG.debug("with request : " + nameValuePairs.toString());
            LOG.debug("ResponseCode : " + responseCode + " Response : " + EntityUtils.toString(entity));

        } catch (UnsupportedEncodingException ex) {
            LOG.error("Exception when notify Execution status to Browserstack.", ex);
        } catch (IOException ex) {
            LOG.error("Exception when notify Execution status to Browserstack.", ex);
        } catch (Exception ex) {
            LOG.error("Exception when notify Execution status to Browserstack.", ex);
        }
    }

    @Override
    public String getBrowserStackBuildHashFromEndpoint(String system, String tag, String user, String pass, String endPoint) {
        LOG.debug("Calling Browserstack to get build referential and find the tag.");

        try {

            String url = "https://" + user + ":" + pass + "@" + endPoint;

            HttpClient httpclient = null;
            HttpClientBuilder httpclientBuilder;

            httpclientBuilder = proxyService.getBuilderWithProxy(system, url);
            httpclient = httpclientBuilder.build();
            HttpGet httpGet = new HttpGet(url);

            RequestConfig requestConfig;
            // Timeout setup.
            requestConfig = RequestConfig.custom().setConnectTimeout(10000).setConnectionRequestTimeout(10000)
                    .setSocketTimeout(10000).build();

            // Timeout setup.
            httpGet.setConfig(requestConfig);

            HttpResponse response = httpclient.execute(httpGet);
            int responseCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String resultresp = EntityUtils.toString(entity);
            LOG.debug("Calling : " + url);
            LOG.debug("ResponseCode : " + responseCode + " Response : " + resultresp);

            JSONArray respJSON = new JSONArray(resultresp);

            for (int i = 0; i < respJSON.length(); i++) {
                if (((JSONObject) ((JSONObject) respJSON.get(i)).get("automation_build")).get("name").equals(tag)) {
                    return ((JSONObject) ((JSONObject) respJSON.get(i)).get("automation_build")).get("hashed_id").toString();
                }
            }

        } catch (UnsupportedEncodingException ex) {
            LOG.error("Exception when getting build referential and find the tag from BrowserStack.", ex);
        } catch (IOException ex) {
            LOG.error("Exception when getting build referential and find the tag from BrowserStack.", ex);
        } catch (Exception ex) {
            LOG.error("Exception when getting build referential and find the tag from BrowserStack.", ex);
        }

        return "BSHash";
    }

    @Override
    public List<String> getSeleniumLogs(String sessionId, String user, String pass) {
        // Not Implemented yet.

//[{
//        "automation_build": {
//            "name": "bdumont.20190812-122746",
//            "duration": 36,
//            "status": "failed",
//            "hashed_id": "ca80efb5668ebe6a51339b36ba62144bcdcd9f98"
//        }
//    }, {
//        "automation_build": {
//            "name": "bdumont.20190809-151554",
//            "duration": 149239,
//            "status": "failed",
//            "hashed_id": "a3f2201061fb607f6384b2d30fa783fa498e01b6"
        return new ArrayList<>();
    }

    @Override
    public JSONObject getHarLogs(String sessionId, String user, String pass) {
        // Not Implemented yet.
        return new JSONObject();
    }

}
