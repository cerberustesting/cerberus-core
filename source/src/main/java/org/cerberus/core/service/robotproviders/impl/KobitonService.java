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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.service.proxy.IProxyService;
import org.cerberus.core.service.robotproviders.IKobitonService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author vertigo17
 */
@Service
public class KobitonService implements IKobitonService {

    @Autowired
    private IProxyService proxyService;

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(KobitonService.class);

    @Override
    public void setSessionStatus(String system, String sessionId, String status, String reason, String user, String pass) {

        URI uri;
        HttpPut putRequest;
        LOG.debug("Notify Kobiton on target status about the end of the execution : " + status);
        try {

            String bsStatus = "Failed";
            if (TestCaseExecution.CONTROLSTATUS_OK.equals(status)) {
                bsStatus = "Passed";
            }

            String url = "https://api.kobiton.com/v1/sessions/" + sessionId;

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
            JSONObject requestString = new JSONObject();
            requestString.put("state", bsStatus);
//            requestString.put("name", "newName");
//            requestString.put("description", "newDescription");
            InputStream stream = new ByteArrayInputStream(requestString.toString().getBytes(StandardCharsets.UTF_8));
            InputStreamEntity reqEntity = new InputStreamEntity(stream);
            reqEntity.setChunked(true);
            httpPut.setEntity(reqEntity);
            httpPut.addHeader("Authorization", generateBasicAuth(user, pass));
            HttpResponse response = httpclient.execute(httpPut);
            int responseCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            LOG.debug("Calling : " + url);
            LOG.debug("with request : " + requestString.toString());
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
    public List<String> getSeleniumLogs(String sessionId, String user, String pass) {
        // Not implemented yet.
        return new ArrayList<>();
    }

    @Override
    public JSONObject getHarLogs(String sessionId, String user, String pass) {
        // Not implemented yet.
        return new JSONObject();
    }

    static String generateBasicAuth(String username, String apiKey) {
        String authString = username + ":" + apiKey;
        byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
        String authStringEnc = new String(authEncBytes);
        return "Basic " + authStringEnc;
    }

}
