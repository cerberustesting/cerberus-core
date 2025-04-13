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
package org.cerberus.core.service.bug.gitlab.impl;

import java.io.IOException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
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
import org.cerberus.core.service.bug.gitlab.IGitlabService;
import org.cerberus.core.service.bug.gitlab.IGitlabGenerationService;

/**
 *
 * @author vertigo17
 */
@Service
public class GitlabService implements IGitlabService {

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private IProxyService proxyService;
    @Autowired
    private IGitlabGenerationService gitlabGenerationService;
    @Autowired
    private IApplicationService applicationService;
    @Autowired
    private ILogEventService logEventService;
    @Autowired
    private ITestCaseService testCaseService;

    private static final Logger LOG = LogManager.getLogger(GitlabService.class);

    private static final boolean DEFAULT_PROXY_ACTIVATE = false;
    private static final String DEFAULT_PROXY_HOST = "proxy";
    private static final int DEFAULT_PROXY_PORT = 80;
    private static final boolean DEFAULT_PROXYAUTHENT_ACTIVATE = false;
    private static final String DEFAULT_PROXYAUTHENT_USER = "squid";
    private static final String DEFAULT_PROXYAUTHENT_PASSWORD = "squid";

    private static final String GITLAB_ISSUECREATION_URL_DEFAULT = "https://gitlab.com/api/v4/projects";
    private static final String GITLAB_ISSUECREATION_URLPATH = "issues";

    @Override
    public JSONObject createGitlabIssue(TestCase tc, TestCaseExecution execution, String repoName, String issueType) {
        JSONObject newBugCreated = new JSONObject();

        try {

            LOG.debug("call GITLAB Issue creation following execution {}", execution.getId());

            // Build Gitlab URL.
            String gitlabUrl = GITLAB_ISSUECREATION_URL_DEFAULT + StringUtil.addPrefixIfNotAlready(StringUtil.addSuffixIfNotAlready(StringUtil.encodeURL(repoName), "/"), "/") + GITLAB_ISSUECREATION_URLPATH;

            CloseableHttpClient httpclient = null;
            HttpClientBuilder httpclientBuilder;

            if (proxyService.useProxy(gitlabUrl, "")) {

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

            gitlabUrl += "?title=" + StringUtil.encodeURL(gitlabGenerationService.generateGitlabTitleIssue(execution, issueType));
            gitlabUrl += "&labels=" + StringUtil.encodeURL(issueType);
            gitlabUrl += "&description=" + StringUtil.encodeURL(gitlabGenerationService.generateGitlabDescriptionIssue(execution, issueType));

//            gitlabUrl += "?title=title";
//            gitlabUrl += "&labels=cerberus";
//            gitlabUrl += "&description=description";
            HttpPost post = new HttpPost(gitlabUrl);

            post.setHeader("Accept", "application/json");
            post.setHeader("Content-type", "application/json");
            //
            // TODO Make auth based on parameters

            String bearerAuth = parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_gitlab_apitoken, "", "");
            post.setHeader("PRIVATE-TOKEN", bearerAuth);

            if (StringUtil.isEmptyOrNull(bearerAuth)) {
                newBugCreated.put("message", "Mandatory parameter value not defined for parameter : '" + Parameter.VALUE_cerberus_gitlab_apitoken + "'");
                newBugCreated.put("statusCode", 500);
            } else {

                try {

                    LOG.debug("Calling {} with Authent {}", gitlabUrl, bearerAuth);
                    HttpResponse response = httpclient.execute(post);

                    int rc = response.getStatusLine().getStatusCode();
                    if (rc >= 200 && rc < 300) {
                        LOG.debug("Gitlab Issue Creation request http return code : " + rc);
                        String responseString = EntityUtils.toString(response.getEntity());
                        LOG.debug("Response : {}", responseString);
                        JSONObject gitlabResponse = new JSONObject(responseString);
                        String newGitlabBugURL = "";
                        int gitlabIssueKey = 0;
                        if (gitlabResponse.has("iid")) {
                            gitlabIssueKey = gitlabResponse.getInt("iid");
                            if (gitlabResponse.has("web_url")) {
                                URL glURL = new URL(gitlabResponse.getString("web_url"));
                                newGitlabBugURL = glURL.toString();
                            }
                            // Update here the test case with new issue.
                            newBugCreated.put("bug", testCaseService.addNewBugEntry(tc, execution.getTest(), execution.getTestCase(), String.valueOf(gitlabIssueKey), newGitlabBugURL, "Created from Execution " + execution.getId()));
                            newBugCreated.put("message", "Bug '" + String.valueOf(gitlabIssueKey) + "' successfully created on Test case : '" + execution.getTest() + "' - '" + execution.getTestCase() + "' from execution : " + execution.getId());
                            newBugCreated.put("statusCode", 200);
                            LOG.debug("Setting new GITLAB Issue '{}' to test case '{} - {}'", gitlabResponse.getInt("iid"), execution.getTest() + execution.getTestCase());
                        } else {
                            LOG.warn("Gitlab Issue creation request http return code : {} is missing 'iid' entry.", rc);
                            String message = "Gitlab Issue creation request to '" + gitlabUrl + "' failed with http return code : " + rc + ". and no 'iid' entry. " + responseString;
                            logEventService.createForPrivateCalls("GITLAB", "APICALL", LogEvent.STATUS_WARN, message);
                            LOG.warn("Message sent to " + gitlabUrl + " :");
                            LOG.warn("Response : {}", responseString);
                            newBugCreated.put("message", message);
                            newBugCreated.put("statusCode", 500);
                        }
                    } else {
                        LOG.warn("Gitlab Issue creation request http return code : " + rc);
                        String responseString = EntityUtils.toString(response.getEntity());
                        String message = "Gitlab Issue creation request to '" + gitlabUrl + "' failed with http return code : " + rc + ". " + responseString;
                        logEventService.createForPrivateCalls("GITLAB", "APICALL", LogEvent.STATUS_WARN, message);
                        LOG.warn("Message sent to " + gitlabUrl + " :");
                        LOG.warn("Response : {}", responseString);
                        newBugCreated.put("message", message);
                        newBugCreated.put("statusCode", 500);
                    }

                } catch (IOException e) {
                    LOG.warn("Gitlab Issue creation request Exception : " + e, e);
                    logEventService.createForPrivateCalls("GITLAB", "APICALL", LogEvent.STATUS_WARN, "GITLAB Issue creation request to '" + gitlabUrl + "' failed : " + e.toString() + ".");
                    newBugCreated.put("message", "GITLAB Issue creation request Exception : " + e.toString());
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
