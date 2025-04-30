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
package org.cerberus.core.service.bug.github.impl;

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
import org.cerberus.core.service.bug.github.IGithubService;
import org.cerberus.core.service.bug.github.IGithubGenerationService;
import org.cerberus.core.util.StringUtil;

/**
 *
 * @author vertigo17
 */
@Service
public class GithubService implements IGithubService {

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private IProxyService proxyService;
    @Autowired
    private IGithubGenerationService githubGenerationService;
    @Autowired
    private IApplicationService applicationService;
    @Autowired
    private ILogEventService logEventService;
    @Autowired
    private ITestCaseService testCaseService;

    private static final Logger LOG = LogManager.getLogger(GithubService.class);

    private static final boolean DEFAULT_PROXY_ACTIVATE = false;
    private static final String DEFAULT_PROXY_HOST = "proxy";
    private static final int DEFAULT_PROXY_PORT = 80;
    private static final boolean DEFAULT_PROXYAUTHENT_ACTIVATE = false;
    private static final String DEFAULT_PROXYAUTHENT_USER = "squid";
    private static final String DEFAULT_PROXYAUTHENT_PASSWORD = "squid";

    private static final String GITHUB_ISSUECREATION_URL_DEFAULT = "https://api.github.com/repos";
    private static final String GITHUB_ISSUECREATION_URLPATH = "issues";
    private static final String GITHUB_API_VERSION = "2022-11-28";

    private static final int DEFAULT_XRAY_CACHE_DURATION = 300;

    @Override
    public JSONObject createGithubIssue(TestCase tc, TestCaseExecution execution, String repoName, String issueType) {
        JSONObject newBugCreated = new JSONObject();

        try {

            JSONObject githubRequest = new JSONObject();

            LOG.debug("call GITHUB Issue creation following execution {}", execution.getId());

            githubRequest = githubGenerationService.generateGithubIssue(execution, issueType);

            // Builf Github URL.
            String githubUrl = GITHUB_ISSUECREATION_URL_DEFAULT + StringUtil.addPrefixIfNotAlready(StringUtil.addSuffixIfNotAlready(repoName, "/"), "/") + GITHUB_ISSUECREATION_URLPATH;

            CloseableHttpClient httpclient = null;
            HttpClientBuilder httpclientBuilder;

            if (proxyService.useProxy(githubUrl, "")) {

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

            HttpPost post = new HttpPost(githubUrl);
            StringEntity entity = new StringEntity(githubRequest.toString(), StandardCharsets.UTF_8);
            post.setEntity(entity);
            post.setHeader("Accept", "application/vnd.github+json");
            post.setHeader("Content-type", "application/json");
            post.setHeader("X-GitHub-Api-Version", GITHUB_API_VERSION);
            //
            // TODO Make auth based on parameters

            String bearerAuth = parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_github_apitoken, "", "");
            post.setHeader("Authorization", "Bearer " + bearerAuth);

            if (StringUtil.isEmptyOrNull(bearerAuth)) {
                newBugCreated.put("message", "Mandatory parameter value not defined for parameter : '" + Parameter.VALUE_cerberus_github_apitoken + "'");
                newBugCreated.put("statusCode", 500);
            } else {

                try {

                    LOG.debug("Calling {} with Authent {}", githubUrl, bearerAuth);
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
                            newBugCreated.put("bug", testCaseService.addNewBugEntry(tc, execution.getTest(), execution.getTestCase(), String.valueOf(githubIssueKey), newGithubBugURL, "Created from Execution " + execution.getId()));
                            newBugCreated.put("message", "Bug '" + String.valueOf(githubIssueKey) + "' successfully created on Test case : '" + execution.getTest() + "' - '" + execution.getTestCase() + "' from execution : " + execution.getId());
                            newBugCreated.put("statusCode", 200);
                            LOG.debug("Setting new GITHUB Issue '{}' to test case '{} - {}'", githubResponse.getInt("number"), execution.getTest() + execution.getTestCase());
                        } else {
                            LOG.warn("Github Issue creation request http return code : {} is missing 'number' entry.", rc);
                            String message = "Github Issue creation request to '" + githubUrl + "' failed with http return code : " + rc + ". and no 'number' entry. " + responseString;
                            logEventService.createForPrivateCalls("GITHUB", "APICALL", LogEvent.STATUS_WARN, message);
                            LOG.warn("Message sent to " + githubUrl + " :");
                            LOG.warn(githubRequest.toString(1));
                            LOG.warn("Response : {}", responseString);
                            newBugCreated.put("message", message);
                            newBugCreated.put("statusCode", 500);
                        }
                    } else {
                        LOG.warn("Github Issue creation request http return code : " + rc);
                        String responseString = EntityUtils.toString(response.getEntity());
                        String message = "Github Issue creation request to '" + githubUrl + "' failed with http return code : " + rc + ". " + responseString;
                        logEventService.createForPrivateCalls("GITHUB", "APICALL", LogEvent.STATUS_WARN, message);
                        LOG.warn("Message sent to " + githubUrl + " :");
                        LOG.warn(githubRequest.toString(1));
                        LOG.warn("Response : {}", responseString);
                        newBugCreated.put("message", message);
                        newBugCreated.put("statusCode", 500);
                    }

                } catch (IOException e) {
                    LOG.warn("Github Issue creation request Exception : " + e, e);
                    logEventService.createForPrivateCalls("GITHUB", "APICALL", LogEvent.STATUS_WARN, "GITHUB Issue creation request to '" + githubUrl + "' failed : " + e.toString() + ".");
                    newBugCreated.put("message", "GITHUB Issue creation request Exception : " + e.toString());
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
