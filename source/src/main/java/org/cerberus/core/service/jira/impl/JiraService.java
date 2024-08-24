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
package org.cerberus.core.service.jira.impl;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
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
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.service.jira.IJiraGenerationService;
import org.cerberus.core.service.jira.IJiraService;
import org.cerberus.core.service.proxy.IProxyService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

/**
 *
 * @author vertigo17
 */
@Service
public class JiraService implements IJiraService {

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private IProxyService proxyService;
    @Autowired
    private IJiraGenerationService jiraGenerationService;
    @Autowired
    private IApplicationService applicationService;
    @Autowired
    private ILogEventService logEventService;
    @Autowired
    private ITestCaseService testCaseService;

    // Area to store JIRA XRay token in cache.
    private HashMap<String, JSONObject> cacheEntry = new HashMap<>();

    private static final Logger LOG = LogManager.getLogger(JiraService.class);

    private static final boolean DEFAULT_PROXY_ACTIVATE = false;
    private static final String DEFAULT_PROXY_HOST = "proxy";
    private static final int DEFAULT_PROXY_PORT = 80;
    private static final boolean DEFAULT_PROXYAUTHENT_ACTIVATE = false;
    private static final String DEFAULT_PROXYAUTHENT_USER = "squid";
    private static final String DEFAULT_PROXYAUTHENT_PASSWORD = "squid";

    private static final String JIRACLOUD_ISSUECREATION_URL = "";
    private static final String JIRACLOUD_ISSUECREATION_URLPATH = "/rest/api/3/issue";

    private static final int DEFAULT_XRAY_CACHE_DURATION = 300;

    @Override
    @Async
    public void createIssue(TestCaseExecution execution) {

        Application currentAppli = new Application();
        try {
            currentAppli = applicationService.convert(applicationService.readByKey(execution.getApplication()));
        } catch (CerberusException ex) {
            LOG.warn(ex, ex);
        }

        if (currentAppli != null && Application.BUGTRACKER_JIRA.equalsIgnoreCase(currentAppli.getBugTrackerConnector())) {
            createJiraIssue(execution, currentAppli.getBugTrackerParam1(), currentAppli.getBugTrackerParam2());
        }
    }

    @Override
    public void createJiraIssue(TestCaseExecution execution, String projectKey, String issueType) {

        try {

            JSONObject jiraRequest = new JSONObject();

            LOG.debug("call JIRA Issue creation following execution {}", execution.getId());

            jiraRequest = jiraGenerationService.generateJiraIssue(execution, projectKey, issueType);

            // TODO Make url to JIRA instance a parameter.
            String jiraUrl = JIRACLOUD_ISSUECREATION_URL + JIRACLOUD_ISSUECREATION_URLPATH;

            CloseableHttpClient httpclient = null;
            HttpClientBuilder httpclientBuilder;

            if (proxyService.useProxy(jiraUrl, "")) {

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

            HttpPost post = new HttpPost(jiraUrl);
            StringEntity entity = new StringEntity(jiraRequest.toString(), StandardCharsets.UTF_8);
            post.setEntity(entity);
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-type", "application/json");
            // TODO Make auth based on parameters
            post.setHeader("Authorization", "Basic ");

            try {

//                            LOG.debug("Calling {} with Bearer {}", xRayUrl, getToken(execution.getSystem(), execution.getTestCaseObj().getOrigine()));
                HttpResponse response = httpclient.execute(post);

                int rc = response.getStatusLine().getStatusCode();
                if (rc >= 200 && rc < 300) {
                    LOG.debug("Jira Issue Creation request http return code : " + rc);
                    String responseString = EntityUtils.toString(response.getEntity());
                    LOG.debug("Response : {}", responseString);
                    JSONObject jiraResponse = new JSONObject(responseString);
                    String xrayURL = "";
                    String jiraIssueKey = "";
                    if (jiraResponse.has("key")) {
                        jiraIssueKey = jiraResponse.getString("key");
                        if (jiraResponse.has("self")) {
                            URL jiURL = new URL(jiraResponse.getString("self"));
                            xrayURL = jiURL.getProtocol() + "://" + jiURL.getHost();
                        }
                        // Update here the test case with new issue.
                        testCaseService.addBugIfNotAlreadyOpen(execution.getTest(), execution.getTestCase(), jiraIssueKey, "Created from Cerberus Engine.");
                        LOG.debug("Setting new JIRA Issue '{}' to test case '{} - {}'", jiraResponse.getString("key"), execution.getTest() + execution.getTestCase());
                    } else {
                        LOG.warn("JIRA Issue creation request http return code : {} is missing 'key' entry.", rc);
                        String message = "JIRA Issue creation request to '" + jiraUrl + "' failed with http return code : " + rc + ". and no 'key' entry. " + responseString;
                        logEventService.createForPrivateCalls("JIRA", "APICALL", LogEvent.STATUS_WARN, message);
                        LOG.warn("Message sent to " + jiraUrl + " :");
                        LOG.warn(jiraRequest.toString(1));
                        LOG.warn("Response : {}", responseString);
                    }
                } else {
                    LOG.warn("JIRA Issue creation request http return code : " + rc);
                    String responseString = EntityUtils.toString(response.getEntity());
                    String message = "JIRA Issue creation request to '" + jiraUrl + "' failed with http return code : " + rc + ". " + responseString;
                    logEventService.createForPrivateCalls("JIRA", "APICALL", LogEvent.STATUS_WARN, message);
                    LOG.warn("Message sent to " + jiraUrl + " :");
                    LOG.warn(jiraRequest.toString(1));
                    LOG.warn("Response : {}", responseString);
                }

            } catch (IOException e) {
                LOG.warn("JIRA Issue creation request Exception : " + e, e);
                logEventService.createForPrivateCalls("JIRA", "APICALL", LogEvent.STATUS_WARN, "JIRA Issue creation request to '" + jiraUrl + "' failed : " + e.toString() + ".");
            }

//                    }
//                }
        } catch (Exception ex) {
            LOG.error(ex, ex);
        }

    }

}
