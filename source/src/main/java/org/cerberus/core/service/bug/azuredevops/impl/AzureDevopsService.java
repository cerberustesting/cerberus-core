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
package org.cerberus.core.service.bug.azuredevops.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.service.proxy.IProxyService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.service.bug.azuredevops.IAzureDevopsGenerationService;
import org.cerberus.core.service.bug.azuredevops.IAzureDevopsService;
import org.json.JSONArray;

/**
 *
 * @author vertigo17
 */
@Service
public class AzureDevopsService implements IAzureDevopsService {

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private IProxyService proxyService;
    @Autowired
    private IAzureDevopsGenerationService azureDevopsGenerationService;
    @Autowired
    private IApplicationService applicationService;
    @Autowired
    private ILogEventService logEventService;
    @Autowired
    private ITestCaseService testCaseService;

    private static final Logger LOG = LogManager.getLogger(AzureDevopsService.class);

    private static final boolean DEFAULT_PROXY_ACTIVATE = false;
    private static final String DEFAULT_PROXY_HOST = "proxy";
    private static final int DEFAULT_PROXY_PORT = 80;
    private static final boolean DEFAULT_PROXYAUTHENT_ACTIVATE = false;
    private static final String DEFAULT_PROXYAUTHENT_USER = "squid";
    private static final String DEFAULT_PROXYAUTHENT_PASSWORD = "squid";

    private static final String AZUREDEVOPS_ISSUECREATION_URL_DEFAULT = "https://dev.azure.com";
    private static final String AZUREDEVOPS_ISSUECREATION_URLPATH = "/_apis/wit/workitems/$Issue";
    private static final String AZUREDEVOPS_ISSUEGUI_URLPATH = "_workitems/edit/";
    private static final String AZUREDEVOPS_API_VERSION = "7.1";

    private static final int DEFAULT_XRAY_CACHE_DURATION = 300;

    @Override
    public JSONObject createAzureDevopsWorkItem(TestCase tc, TestCaseExecution execution, String repoName) {
        JSONObject newBugCreated = new JSONObject();

        try {

            JSONArray azureDevopsRequest = new JSONArray();

            LOG.debug("call AZUREDEVOPS WorkItem creation following execution {}", execution.getId());

            azureDevopsRequest = azureDevopsGenerationService.generateAzureDevopsWorkItem(execution);

            // Build Azure Devops URL.
            String azureDevopsUrl = AZUREDEVOPS_ISSUECREATION_URL_DEFAULT + StringUtil.addPrefixIfNotAlready(StringUtil.addSuffixIfNotAlready(repoName, "/"), "/") + AZUREDEVOPS_ISSUECREATION_URLPATH
                    + "?api-version=" + AZUREDEVOPS_API_VERSION;
            String azureDevopsGuiUrl = AZUREDEVOPS_ISSUECREATION_URL_DEFAULT + StringUtil.addPrefixIfNotAlready(StringUtil.addSuffixIfNotAlready(repoName, "/"), "/") + AZUREDEVOPS_ISSUEGUI_URLPATH;

            CloseableHttpClient httpclient = null;
            HttpClientBuilder httpclientBuilder;

            if (proxyService.useProxy(azureDevopsUrl, "")) {

                String proxyHost = parameterService.getParameterStringByKey("cerberus_proxy_host", "", DEFAULT_PROXY_HOST);
                int proxyPort = parameterService.getParameterIntegerByKey("cerberus_proxy_port", "", DEFAULT_PROXY_PORT);

                HttpHost proxyHostObject = new HttpHost(proxyHost, proxyPort);

                if (parameterService.getParameterBooleanByKey("cerberus_proxyauthentification_active", "", DEFAULT_PROXYAUTHENT_ACTIVATE)) {

                    String proxyUser = parameterService.getParameterStringByKey("cerberus_proxyauthentification_user", "", DEFAULT_PROXYAUTHENT_USER);
                    String proxyPassword = parameterService.getParameterStringByKey("cerberus_proxyauthentification_password", "", DEFAULT_PROXYAUTHENT_PASSWORD);

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

            boolean acceptUnsignedSsl = parameterService.getParameterBooleanByKey("cerberus_accept_unsigned_ssl_certificate", "", true);

            if (acceptUnsignedSsl) {
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

            httpclient = httpclientBuilder.build();

            HttpPost post = new HttpPost(azureDevopsUrl);
            StringEntity entity = new StringEntity(azureDevopsRequest.toString(), StandardCharsets.UTF_8);
            post.setEntity(entity);
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-type", "application/json-patch+json");
            //
            // TODO Make auth based on parameters

            String accessToken = parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_azuredevops_accesstoken, "", "");
            String authAccessToken = ":" + accessToken;

            post.setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(authAccessToken.getBytes()));

            if (StringUtil.isEmptyOrNull(accessToken)) {
                newBugCreated.put("message", "Mandatory parameter value not defined for parameter : '" + Parameter.VALUE_cerberus_azuredevops_accesstoken + "'");
                newBugCreated.put("statusCode", 500);
            } else {

                try {

                    LOG.debug("Calling {} with Authent {}", azureDevopsUrl, accessToken);
                    HttpResponse response = httpclient.execute(post);

                    int rc = response.getStatusLine().getStatusCode();
                    if (rc >= 200 && rc < 300) {
                        LOG.debug("Azure Devops WorkItem Creation request http return code : " + rc);
                        String responseString = EntityUtils.toString(response.getEntity());
                        LOG.debug("Response : {}", responseString);
                        JSONObject azureResponse = new JSONObject(responseString);
                        String newAzureDevopsBugURL = "";
                        int azureDevopsWorkItemId = 0;
                        if (azureResponse.has("id")) {
                            azureDevopsWorkItemId = azureResponse.getInt("id");
                            newAzureDevopsBugURL = azureDevopsGuiUrl + azureDevopsWorkItemId + "/";
                            // Update here the test case with new issue.
                            newBugCreated.put("bug", testCaseService.addNewBugEntry(tc, execution.getTest(), execution.getTestCase(), String.valueOf(azureDevopsWorkItemId), newAzureDevopsBugURL, "Created from Execution " + execution.getId()));
                            newBugCreated.put("message", "Bug '" + String.valueOf(azureDevopsWorkItemId) + "' successfully created on Test case : '" + execution.getTest() + "' - '" + execution.getTestCase() + "' from execution : " + execution.getId());
                            newBugCreated.put("statusCode", 200);
                            LOG.debug("Setting new AZUREDEVOPS Issue '{}' to test case '{} - {}'", azureResponse.getInt("id"), execution.getTest() + execution.getTestCase());
                        } else {
                            LOG.warn("Azure Devops WorkItem creation request http return code : {} is missing 'id' entry.", rc);
                            String message = "Azure Devops WorkItem creation request to '" + azureDevopsUrl + "' failed with http return code : " + rc + ". and no 'id' entry. " + responseString;
                            logEventService.createForPrivateCalls("AZUREDEVOPS", "APICALL", LogEvent.STATUS_WARN, message);
                            LOG.warn("Message sent to " + azureDevopsUrl + " :");
                            LOG.warn(azureDevopsRequest.toString(1));
                            LOG.warn("Response : {}", responseString);
                            newBugCreated.put("message", message);
                            newBugCreated.put("statusCode", 500);
                        }
                    } else {
                        LOG.warn("Azure Devops WorkItem creation request http return code : " + rc);
                        String responseString = EntityUtils.toString(response.getEntity());
                        String message = "Azure Devops WorkItem creation request to '" + azureDevopsUrl + "' failed with http return code : " + rc + ". " + responseString;
                        logEventService.createForPrivateCalls("AZUREDEVOPS", "APICALL", LogEvent.STATUS_WARN, message);
                        LOG.warn("Message sent to " + azureDevopsUrl + " :");
                        LOG.warn(azureDevopsRequest.toString(1));
                        LOG.warn("Response : {}", responseString);
                        newBugCreated.put("message", message);
                        newBugCreated.put("statusCode", 500);
                    }

                } catch (IOException e) {
                    LOG.warn("Azure Devops WorkItem creation request Exception : " + e, e);
                    logEventService.createForPrivateCalls("AZUREDEVOPS", "APICALL", LogEvent.STATUS_WARN, "AZUREDEVOPS Issue creation request to '" + azureDevopsUrl + "' failed : " + e.toString() + ".");
                    newBugCreated.put("message", "AZUREDEVOPS WorkItem creation request Exception : " + e.toString());
                    newBugCreated.put("statusCode", 500);
                }
            }

        } catch (Exception ex) {
            newBugCreated.put("message", ex.toString());
            newBugCreated.put("statusCode", 500);
            LOG.error(ex, ex);
        }
        return newBugCreated;
    }

}
