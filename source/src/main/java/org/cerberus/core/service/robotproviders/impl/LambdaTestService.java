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
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.engine.entity.Session;
import org.cerberus.core.service.proxy.IProxyService;
import org.cerberus.core.service.robotproviders.ILambdaTestService;
import org.json.JSONObject;
import org.openqa.selenium.JavascriptExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author vertigo17
 */
@Service
public class LambdaTestService implements ILambdaTestService {

    @Autowired
    private IProxyService proxyService;

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(LambdaTestService.class);

    @Override
    public void setSessionStatus(Session session, String status) {

        URI uri;
        HttpPut putRequest;
        LOG.debug("Notify LambdaTest on target status about the end of the execution : " + status);
        try {

            String bsStatus = "Failed";
            if (TestCaseExecution.CONTROLSTATUS_OK.equals(status)) {
                ((JavascriptExecutor) session.getDriver()).executeScript("lambda-status=passed");
            } else {
                ((JavascriptExecutor) session.getDriver()).executeScript("lambda-status=failed");
            }

        } catch (Exception ex) {
            LOG.error("Exception when notify Execution status to Browserstack.", ex);
        }
    }

    @Override
    public List<String> getSeleniumLogs(String sessionId, String user, String pass) {
        // Not implemented yet.
        return new ArrayList<>();
    }

    @Override
    public JSONObject getHarLogs(String sessionId, String user, String pass) {
        // Not implemented yet.
        return new JSONObject();
    }

    @Override
    public String getBuildValue(String tag, String user, String pass, String system) {
        LOG.debug("Get LambdaTest internal Build value from tag.");

        String bsStatus = "";
        try {

            String url = "https://api.lambdatest.com/automation/api/v1/builds?limit=50&sort=desc.build_id";

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
            httpGet.addHeader("Authorization", generateBasicAuth(user, pass));
            HttpResponse response = httpclient.execute(httpGet);
            int responseCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            LOG.debug("Calling : " + url);
            String resultService = EntityUtils.toString(entity);
            LOG.debug("ResponseCode : " + responseCode + " Response : " + resultService);

            JSONObject responseString = new JSONObject(resultService);

            LOG.debug("return JSON : " + responseString.toString(1));
            // Looping against 50 latest build until we find the one that match the tag value.
            if (responseString.has("data")) {
                LOG.debug("has data");
                for (int i = 0; i < responseString.getJSONArray("data").length(); i++) {
                    LOG.debug(" " + i);
                    if (responseString.getJSONArray("data").getJSONObject(i).has("name")) {
                        LOG.debug("has name");
                        if (tag.equals(responseString.getJSONArray("data").getJSONObject(i).getString("name"))) {
                            LOG.debug("found build_id");

                            return String.valueOf(responseString.getJSONArray("data").getJSONObject(i).getInt("build_id"));
                        }
                    }
                }
            }

        } catch (UnsupportedEncodingException ex) {
            LOG.error("Exception when notify Execution status to LambdaTest.", ex);
        } catch (IOException ex) {
            LOG.error("Exception when notify Execution status to LambdaTest.", ex);
        } catch (Exception ex) {
            LOG.error("Exception when notify Execution status to LambdaTest.", ex);
        }
        return bsStatus;
    }

    @Override
    public String getTestID(String build, String seleniumsession, String user, String pass, String system) {
        LOG.debug("Get LambdaTest internal exeid value from seleniumsession.");

        String bsStatus = "";
        try {

            String url = "https://api.lambdatest.com/automation/api/v1/sessions?build_id=" + build;

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
            httpGet.addHeader("Authorization", generateBasicAuth(user, pass));
            HttpResponse response = httpclient.execute(httpGet);
            int responseCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            LOG.debug("Calling : " + url);
            String resultService = EntityUtils.toString(entity);
            LOG.debug("ResponseCode : " + responseCode + " Response : " + resultService);

            JSONObject responseString = new JSONObject(resultService);

            LOG.debug("return JSON : " + responseString.toString(1));
            // Looping against 50 latest build until we find the one that match the tag value.
            if (responseString.has("data")) {
                LOG.debug("has data");
                for (int i = 0; i < responseString.getJSONArray("data").length(); i++) {
                    LOG.debug(" " + i);
                    if (responseString.getJSONArray("data").getJSONObject(i).has("session_id")) {
                        LOG.debug("has name");
                        if (seleniumsession.equals(responseString.getJSONArray("data").getJSONObject(i).getString("session_id"))) {
                            LOG.debug("found session_id");

                            return responseString.getJSONArray("data").getJSONObject(i).getString("test_id");
                        }
                    }
                }
            }

        } catch (UnsupportedEncodingException ex) {
            LOG.error("Exception when notify Execution status to LambdaTest.", ex);
        } catch (IOException ex) {
            LOG.error("Exception when notify Execution status to LambdaTest.", ex);
        } catch (Exception ex) {
            LOG.error("Exception when notify Execution status to LambdaTest.", ex);
        }
        return bsStatus;
    }

    static String generateBasicAuth(String username, String apiKey) {
        String authString = username + ":" + apiKey;
        byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
        String authStringEnc = new String(authEncBytes);
        return "Basic " + authStringEnc;
    }

}
