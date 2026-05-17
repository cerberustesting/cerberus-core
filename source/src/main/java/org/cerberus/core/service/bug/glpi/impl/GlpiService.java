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
package org.cerberus.core.service.bug.glpi.impl;

import java.io.IOException;
import java.net.URL;
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
import org.cerberus.core.service.bug.glpi.IGlpiService;
import org.cerberus.core.service.bug.glpi.IGlpiGenerationService;
import org.cerberus.core.util.StringUtil;

/**
 *
 * @author vertigo17
 */
@Service
public class GlpiService implements IGlpiService {

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private IProxyService proxyService;
    @Autowired
    private IGlpiGenerationService glpiGenerationService;
    @Autowired
    private IApplicationService applicationService;
    @Autowired
    private ILogEventService logEventService;
    @Autowired
    private ITestCaseService testCaseService;

    private static final Logger LOG = LogManager.getLogger(GlpiService.class);

    private static final boolean DEFAULT_PROXY_ACTIVATE = false;
    private static final String DEFAULT_PROXY_HOST = "proxy";
    private static final int DEFAULT_PROXY_PORT = 80;
    private static final boolean DEFAULT_PROXYAUTHENT_ACTIVATE = false;
    private static final String DEFAULT_PROXYAUTHENT_USER = "squid";
    private static final String DEFAULT_PROXYAUTHENT_PASSWORD = "squid";

    private static final String GLPI_ISSUECREATION_URL_DEFAULT = "https://mon-glpi/apirest.php/Ticket";
    private static final String GLPI_ISSUECREATION_URLPATH = "issues";
    private static final String GLPI_API_VERSION = "2022-11-28";

    private static final int DEFAULT_XRAY_CACHE_DURATION = 300;

    @Override
    public JSONObject createGlpiIssue(TestCase tc, TestCaseExecution execution, String param1, String param2) {
        JSONObject newBugCreated = new JSONObject();

        try {
            String appToken = parameterService.getParameterStringByKey("cerberus_glpi_apptoken", "", "");
            String sessionToken = getGlpiSession(parameterService.getParameterStringByKey("cerberus_glpi_usertoken", "", ""),
                     appToken);

            if (sessionToken == null) {
//                LOG.warn("Glpi Issue creation request Exception : " + e, e);
//                logEventService.createForPrivateCalls("GLPI", "APICALL", LogEvent.STATUS_WARN, "GLPI Issue creation request to '" + glpiUrl + "' failed : " + e.toString() + ".");
//                newBugCreated.put("message", "GLPI Issue creation request Exception : " + e.toString());
                newBugCreated.put("statusCode", 500);
                return newBugCreated;
            }

            JSONObject glpiRequest = new JSONObject();

            LOG.debug("call GLPI Issue creation following execution {}", execution.getId());

            glpiRequest = glpiGenerationService.generateGlpiIssue(execution);

            // Build Glpi URL.
            String glpiUrl = GLPI_ISSUECREATION_URL_DEFAULT;

            CloseableHttpClient httpclient = null;
            HttpClientBuilder httpclientBuilder;

            if (proxyService.useProxy(glpiUrl, "")) {

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

            HttpPost post = new HttpPost(glpiUrl);
            StringEntity entity = new StringEntity(glpiRequest.toString(), StandardCharsets.UTF_8);
            post.setEntity(entity);
            post.setHeader("Content-type", "application/json");
            post.setHeader("App-Token", appToken);
            post.setHeader("Session-Token", sessionToken);
            //
            // TODO Make auth based on parameters

            try {

                LOG.debug("Calling {} with Authent {}", glpiUrl, sessionToken);
                HttpResponse response = httpclient.execute(post);

                int rc = response.getStatusLine().getStatusCode();
                if (rc >= 200 && rc < 300) {
                    LOG.debug("Glpi Issue Creation request http return code : " + rc);
                    String responseString = EntityUtils.toString(response.getEntity());
                    LOG.debug("Response : {}", responseString);
                    JSONObject glpiResponse = new JSONObject(responseString);
                    String newGlpiBugURL = "";
                    int glpiIssueKey = 0;
                    if (glpiResponse.has("id")) {
                        glpiIssueKey = glpiResponse.getInt("id");
                        if (glpiResponse.has("html_url")) {
                            URL gURL = new URL(glpiResponse.getString("html_url"));
                            newGlpiBugURL = gURL.toString();
                        }
                        // Update here the test case with new issue.
                        newBugCreated.put("bug", testCaseService.addNewBugEntry(tc, execution.getTest(), execution.getTestCase(), String.valueOf(glpiIssueKey), newGlpiBugURL, "Created from Execution " + execution.getId()));
                        newBugCreated.put("message", "Bug '" + String.valueOf(glpiIssueKey) + "' successfully created on Test case : '" + execution.getTest() + "' - '" + execution.getTestCase() + "' from execution : " + execution.getId());
                        newBugCreated.put("statusCode", 200);
                        LOG.debug("Setting new GLPI Issue '{}' to test case '{} - {}'", glpiResponse.getInt("id"), execution.getTest() + execution.getTestCase());
                    } else {
                        LOG.warn("Glpi Issue creation request http return code : {} is missing 'id' entry.", rc);
                        String message = "Glpi Issue creation request to '" + glpiUrl + "' failed with http return code : " + rc + ". and no 'id' entry. " + responseString;
                        logEventService.createForPrivateCalls("GLPI", "APICALL", LogEvent.STATUS_WARN, message);
                        LOG.warn("Message sent to " + glpiUrl + " :");
                        LOG.warn(glpiRequest.toString(1));
                        LOG.warn("Response : {}", responseString);
                        newBugCreated.put("message", message);
                        newBugCreated.put("statusCode", 500);
                    }
                } else {
                    LOG.warn("Glpi Issue creation request http return code : " + rc);
                    String responseString = EntityUtils.toString(response.getEntity());
                    String message = "Glpi Issue creation request to '" + glpiUrl + "' failed with http return code : " + rc + ". " + responseString;
                    logEventService.createForPrivateCalls("GLPI", "APICALL", LogEvent.STATUS_WARN, message);
                    LOG.warn("Message sent to " + glpiUrl + " :");
                    LOG.warn(glpiRequest.toString(1));
                    LOG.warn("Response : {}", responseString);
                    newBugCreated.put("message", message);
                    newBugCreated.put("statusCode", 500);
                }

            } catch (IOException e) {
                LOG.warn("Glpi Issue creation request Exception : " + e, e);
                logEventService.createForPrivateCalls("GLPI", "APICALL", LogEvent.STATUS_WARN, "GLPI Issue creation request to '" + glpiUrl + "' failed : " + e.toString() + ".");
                newBugCreated.put("message", "GLPI Issue creation request Exception : " + e.toString());
                newBugCreated.put("statusCode", 500);
            }

        } catch (Exception ex) {
            newBugCreated.put("message", ex.toString());
            newBugCreated.put("statusCode", 500);
            LOG.error(ex, ex);
        }
        return newBugCreated;
    }

    private String getGlpiSession(String userToken, String appToken) {
        JSONObject newBugCreated = new JSONObject();

        try {

            JSONObject glpiRequest = new JSONObject();

//            LOG.debug("call GLPI Issue creation following execution {}", execution.getId());
//            glpiRequest = glpiGenerationService.generateGlpiIssue(execution);
            // Build Github URL.
            String glpiUrl = GLPI_ISSUECREATION_URL_DEFAULT;

            CloseableHttpClient httpclient = null;
            HttpClientBuilder httpclientBuilder;

            if (proxyService.useProxy(glpiUrl, "")) {

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

            HttpPost post = new HttpPost(glpiUrl);
            StringEntity entity = new StringEntity(glpiRequest.toString(), StandardCharsets.UTF_8);
            post.setEntity(entity);
            post.setHeader("Content-type", "application/json");
            post.setHeader("App-Token", appToken);
//            post.setHeader("Session-Token", sessionToken);
            //
            // TODO Make auth based on parameters

            String bearerAuth = parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_github_apitoken, "", "");
            post.setHeader("Authorization", "Bearer " + bearerAuth);

            if (StringUtil.isEmptyOrNull(bearerAuth)) {
                newBugCreated.put("message", "Mandatory parameter value not defined for parameter : '" + Parameter.VALUE_cerberus_github_apitoken + "'");
                newBugCreated.put("statusCode", 500);
            } else {

                try {

                    LOG.debug("Calling {} with Authent {}", glpiUrl, bearerAuth);
                    HttpResponse response = httpclient.execute(post);

                    int rc = response.getStatusLine().getStatusCode();
                    if (rc >= 200 && rc < 300) {
                        LOG.debug("Github Issue Creation request http return code : " + rc);
                        String responseString = EntityUtils.toString(response.getEntity());
                        LOG.debug("Response : {}", responseString);
                        JSONObject githubResponse = new JSONObject(responseString);
                        String newGithubBugURL = "";
                        int githubIssueKey = 0;
                        if (githubResponse.has("number")) {
                            githubIssueKey = githubResponse.getInt("number");
                            if (githubResponse.has("html_url")) {
                                URL ghURL = new URL(githubResponse.getString("html_url"));
                                newGithubBugURL = ghURL.toString();
                            }
                            // Update here the test case with new issue.
//                            newBugCreated.put("bug", testCaseService.addNewBugEntry(tc, execution.getTest(), execution.getTestCase(), String.valueOf(githubIssueKey), newGithubBugURL, "Created from Execution " + execution.getId()));
//                            newBugCreated.put("message", "Bug '" + String.valueOf(githubIssueKey) + "' successfully created on Test case : '" + execution.getTest() + "' - '" + execution.getTestCase() + "' from execution : " + execution.getId());
                            newBugCreated.put("statusCode", 200);
//                            LOG.debug("Setting new GITHUB Issue '{}' to test case '{} - {}'", githubResponse.getInt("number"), execution.getTest() + execution.getTestCase());
                        } else {
                            LOG.warn("Github Issue creation request http return code : {} is missing 'number' entry.", rc);
                            String message = "Github Issue creation request to '" + glpiUrl + "' failed with http return code : " + rc + ". and no 'number' entry. " + responseString;
                            logEventService.createForPrivateCalls("GITHUB", "APICALL", LogEvent.STATUS_WARN, message);
                            LOG.warn("Message sent to " + glpiUrl + " :");
                            LOG.warn(glpiRequest.toString(1));
                            LOG.warn("Response : {}", responseString);
                            newBugCreated.put("message", message);
                            newBugCreated.put("statusCode", 500);
                        }
                    } else {
                        LOG.warn("Github Issue creation request http return code : " + rc);
                        String responseString = EntityUtils.toString(response.getEntity());
                        String message = "Github Issue creation request to '" + glpiUrl + "' failed with http return code : " + rc + ". " + responseString;
                        logEventService.createForPrivateCalls("GITHUB", "APICALL", LogEvent.STATUS_WARN, message);
                        LOG.warn("Message sent to " + glpiUrl + " :");
                        LOG.warn(glpiRequest.toString(1));
                        LOG.warn("Response : {}", responseString);
                        newBugCreated.put("message", message);
                        newBugCreated.put("statusCode", 500);
                    }

                } catch (IOException e) {
                    LOG.warn("Github Issue creation request Exception : " + e, e);
                    logEventService.createForPrivateCalls("GITHUB", "APICALL", LogEvent.STATUS_WARN, "GITHUB Issue creation request to '" + glpiUrl + "' failed : " + e.toString() + ".");
                    newBugCreated.put("message", "GITHUB Issue creation request Exception : " + e.toString());
                    newBugCreated.put("statusCode", 500);
                }
            }

        } catch (Exception ex) {
            newBugCreated.put("message", ex.toString());
            newBugCreated.put("statusCode", 500);
            LOG.error(ex, ex);
        }
        return "";
    }

}
