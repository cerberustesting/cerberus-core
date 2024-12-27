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
package org.cerberus.core.service.xray.impl;

import java.io.IOException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
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
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.service.proxy.IProxyService;
import org.cerberus.core.service.xray.IXRayGenerationService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.cerberus.core.service.xray.IXRayService;
import org.cerberus.core.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.scheduling.annotation.Async;

/**
 *
 * @author vertigo17
 */
@Service
public class XRayService implements IXRayService {

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private IProxyService proxyService;
    @Autowired
    private IXRayGenerationService xRayGenerationService;
    @Autowired
    private ITagService tagService;
    @Autowired
    private ILogEventService logEventService;

    // Area to store JIRA XRay token in cache.
    private HashMap<String, JSONObject> cacheEntry = new HashMap<>();

    private static final Logger LOG = LogManager.getLogger(XRayService.class);

    private static final boolean DEFAULT_PROXY_ACTIVATE = false;
    private static final String DEFAULT_PROXY_HOST = "proxy";
    private static final int DEFAULT_PROXY_PORT = 80;
    private static final boolean DEFAULT_PROXYAUTHENT_ACTIVATE = false;
    private static final String DEFAULT_PROXYAUTHENT_USER = "squid";
    private static final String DEFAULT_PROXYAUTHENT_PASSWORD = "squid";

    private static final String XRAYCLOUD_AUTHENT_URL = "https://xray.cloud.getxray.app/api/v2/authenticate";
    private static final String XRAYCLOUD_TESTEXECUTIONCREATION_URL = "https://xray.cloud.getxray.app/api/v2/import/execution";
    private static final String XRAYDC_TESTEXECUTIONCREATION_URLPATH = "/rest/raven/2.0/api/import/execution";

    private static final int DEFAULT_XRAY_CACHE_DURATION = 300;

    private String getToken(String system, String origin) {
        try {
            if (cacheEntry.containsKey(getCacheKey(origin, system))) {
                return cacheEntry.get(getCacheKey(origin, system)).getString("value");
            }
        } catch (JSONException ex) {
            LOG.error(ex, ex);
        }
        return null;
    }

    private void putToken(String system, String origin, String value) {
        try {
            LocalDateTime currentTime = LocalDateTime.now();

            JSONObject entry = new JSONObject();
            entry.put("key", getCacheKey(origin, system));
            entry.put("value", value);
            entry.put("created", currentTime.toString());
            cacheEntry.put(getCacheKey(origin, system), entry);
        } catch (JSONException ex) {
            LOG.error(ex, ex);
        }
    }

    @Override
    public JSONArray getAllCacheEntries() {
        JSONArray arrayResult = new JSONArray();
        for (Map.Entry<String, JSONObject> entry : cacheEntry.entrySet()) {
            String key = entry.getKey();
            JSONObject val = entry.getValue();
            arrayResult.put(val);
        }
        return arrayResult;
    }

    @Override
    public void purgeAllCacheEntries() {
        cacheEntry.clear();
        LOG.info("All XRay config cache entries purged.");
    }

    @Override
    @Async
    public void createXRayTestExecution(TestCaseExecution execution) {

        try {
            Tag currentTag = new Tag();

            if ((TestCase.TESTCASE_ORIGIN_JIRAXRAYCLOUD.equalsIgnoreCase(execution.getTestCaseObj().getOrigine()))
                    || TestCase.TESTCASE_ORIGIN_JIRAXRAYDC.equalsIgnoreCase(execution.getTestCaseObj().getOrigine())) {

                getXRayAuthenticationToken(execution.getTestCaseObj().getOrigine(), execution.getSystem());

                JSONObject xRayRequest = new JSONObject();

                LOG.debug("Checking call JIRA XRay TestExecution creation. {}", execution.getId());

                if (!StringUtil.isEmptyOrNull(execution.getTag())) {
                    currentTag = tagService.convert(tagService.readByKey(execution.getTag()));

                    if ((currentTag != null)) {

                        int lock = 0;

                        // We lock the tag updating it to PENDING when empty.
                        if (StringUtil.isEmptyOrNull(currentTag.getXRayTestExecution())) {
                            lock = tagService.lockXRayTestExecution(currentTag.getTag(), currentTag);
                            LOG.debug("Lock attempt : {} on {}", lock, execution.getId());
                        }

                        if (lock == 0) {
                            currentTag.setXRayTestExecution("PENDING");
                            int maxIteration = 0;
                            // We wait that JIRA provide the Epic and Cerberus update it.
                            while ("PENDING".equals(currentTag.getXRayTestExecution()) && maxIteration++ < 20) {
                                LOG.debug("Loop Until Tag is no longuer PENDING {}/20 - {}", maxIteration, execution.getId());
                                currentTag = tagService.convert(tagService.readByKey(execution.getTag()));
                                Thread.sleep(5000);
                            }

                        }

                        xRayRequest = xRayGenerationService.generateCreateTestExecution(currentTag, execution);

                        String xRayUrl = XRAYCLOUD_TESTEXECUTIONCREATION_URL;
                        if (TestCase.TESTCASE_ORIGIN_JIRAXRAYDC.equalsIgnoreCase(execution.getTestCaseObj().getOrigine())) {
                            xRayUrl = parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_xraydc_url, execution.getSystem(), "");
                            xRayUrl += XRAYDC_TESTEXECUTIONCREATION_URLPATH;
                        }

                        CloseableHttpClient httpclient = null;
                        HttpClientBuilder httpclientBuilder;

                        if (proxyService.useProxy(xRayUrl, "")) {

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

                        HttpPost post = new HttpPost(xRayUrl);
                        StringEntity entity = new StringEntity(xRayRequest.toString());
                        post.setEntity(entity);
                        post.setHeader("Accept", "application/json");
                        post.setHeader("Content-type", "application/json");
                        post.setHeader("Authorization", "Bearer " + getToken(execution.getSystem(), execution.getTestCaseObj().getOrigine()));

                        try {

                            LOG.debug("Calling {} with Bearer {}", xRayUrl, getToken(execution.getSystem(), execution.getTestCaseObj().getOrigine()));
                            HttpResponse response = httpclient.execute(post);

                            int rc = response.getStatusLine().getStatusCode();
                            if (rc >= 200 && rc < 300) {
                                LOG.debug("XRay Test Execution request http return code : " + rc);
                                String responseString = EntityUtils.toString(response.getEntity());
                                LOG.debug("Response : {}", responseString);
                                JSONObject xRayResponse = getXRayResponseJSON(execution, responseString);
                                String xrayURL = "";
                                String xrayTestExecution = "";
                                if (xRayResponse.has("key")) {
                                    xrayTestExecution = xRayResponse.getString("key");
                                    if (xRayResponse.has("self")) {
                                        URL xrURL = new URL(xRayResponse.getString("self"));
                                        xrayURL = xrURL.getProtocol() + "://" + xrURL.getHost();
                                    }
                                    if (!xrayURL.equals(currentTag.getXRayURL()) || !xrayTestExecution.equals(currentTag.getXRayTestExecution())) {
                                        // We avoid updating is the data did not change.
                                        currentTag.setXRayURL(xrayURL);
                                        currentTag.setXRayTestExecution(xrayTestExecution);
                                        tagService.updateXRayTestExecution(currentTag.getTag(), currentTag);
                                    }
                                    LOG.debug("Setting new XRay TestExecution '{}' to tag '{}'", xRayResponse.getString("key"), currentTag.getTag());
                                } else {
                                    LOG.warn("XRay Test Execution request http return code : {} is missing 'key' entry.", rc);
                                    String message = "Xray Execution creation request to '" + xRayUrl + "' failed with http return code : " + rc + ". and no 'key' entry. " + responseString;
                                    logEventService.createForPrivateCalls("XRAY", "APICALL", LogEvent.STATUS_WARN, message);
                                    currentTag.setXRayURL("");
                                    currentTag.setXRayTestExecution("ERROR");
                                    currentTag.setXRayMessage(message);
                                    tagService.updateXRayTestExecution(currentTag.getTag(), currentTag);
                                    LOG.warn("Message sent to " + xRayUrl + " :");
                                    LOG.warn(xRayRequest.toString(1));
                                    LOG.warn("Response : {}", responseString);
                                }
                            } else {
                                LOG.warn("XRay Test Execution request http return code : " + rc);
                                String responseString = EntityUtils.toString(response.getEntity());
                                String message = "Xray Execution creation request to '" + xRayUrl + "' failed with http return code : " + rc + ". " + responseString;
                                logEventService.createForPrivateCalls("XRAY", "APICALL", LogEvent.STATUS_WARN, message);
                                currentTag.setXRayURL("");
                                currentTag.setXRayTestExecution("ERROR");
                                currentTag.setXRayMessage(message);
                                tagService.updateXRayTestExecution(currentTag.getTag(), currentTag);
                                LOG.warn("Message sent to " + xRayUrl + " :");
                                LOG.warn(xRayRequest.toString(1));
                                LOG.warn("Response : {}", responseString);
                            }

                        } catch (IOException e) {
                            LOG.warn("XRay Test Execution request Exception : " + e, e);
                            logEventService.createForPrivateCalls("XRAY", "APICALL", LogEvent.STATUS_WARN, "Xray Execution creation request to '" + xRayUrl + "' failed : " + e.toString() + ".");
                        }

                    }
                }
            }

        } catch (Exception ex) {
            LOG.error(ex, ex);
        }

    }

    private JSONObject getXRayResponseJSON(TestCaseExecution execution, String responseString) throws JSONException {
        if (TestCase.TESTCASE_ORIGIN_JIRAXRAYDC.equalsIgnoreCase(execution.getTestCaseObj().getOrigine())) {
            return new JSONObject(responseString).getJSONObject("testExecIssue");
        } else {
            return new JSONObject(responseString);
        }
    }

    private void getXRayAuthenticationToken(String origin, String system) throws Exception {
        String xRayUrl = XRAYCLOUD_AUTHENT_URL;

        if (getToken(system, origin) == null || (isTokenExpired(origin, system))) {

            if (TestCase.TESTCASE_ORIGIN_JIRAXRAYCLOUD.equals(origin)) {

                LOG.debug("Getting new XRay Token for {} and {}.", system, origin);
                String clientID = parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_xraycloud_clientid, system, "");
                String clientSecret = parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_xraycloud_clientsecret, system, "");

                if (StringUtil.isEmptyOrNull(clientID) || StringUtil.isEmptyOrNull(clientSecret)) {
                    throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
                }

                JSONObject authenMessage = xRayGenerationService.generateAuthenticationRequest(clientID, clientSecret);

                // curl -H "Content-Type: application/json" -X POST --data '{ "client_id": "E5A6F0FC4A8941C88CF4D1CAACFFAA81","client_secret": "2625a68b1953e66fff5b64642f6a9f59c6885db83fb3a9f9a73b34170513ad3f" }'  https://xray.cloud.getxray.app/api/v2/authenticate
                CloseableHttpClient httpclient = null;
                HttpClientBuilder httpclientBuilder;

                if (proxyService.useProxy(xRayUrl, "")) {

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

                HttpPost post = new HttpPost(xRayUrl);

                StringEntity entity = new StringEntity(authenMessage.toString());
                post.setEntity(entity);
                post.setHeader("Accept", "application/json");
                post.setHeader("Content-type", "application/json");

                try {

                    HttpResponse response = httpclient.execute(post);

                    int rc = response.getStatusLine().getStatusCode();
                    if (rc >= 200 && rc < 300) {
                        LOG.debug("XRay Authent request http return code : " + rc);
                        String responseString = EntityUtils.toString(response.getEntity());
                        LOG.debug("Response : {}", responseString);
                        // Token is surounded with " (double quotes. We need to remove them.
                        putToken(system, origin, responseString.substring(0, responseString.length() - 1).substring(1));
                        LOG.debug("Setting new XRay Cloud Token : {}", getToken(system, origin));
                    } else {
                        logEventService.createForPrivateCalls("XRAY", "APICALL", LogEvent.STATUS_WARN, "Xray Authent request to '" + xRayUrl + "' failed with http return code : " + rc + ".");
                        LOG.warn("XRay Authent request http return code : " + rc);
                        LOG.warn("Message sent to " + xRayUrl + ":");
                        LOG.debug(authenMessage.toString(1));
                    }

                } catch (Exception e) {
                    LOG.warn("XRay Authent request http Exception : " + e, e);
                    logEventService.createForPrivateCalls("XRAY", "APICALL", LogEvent.STATUS_WARN, "Xray Authent request to '" + xRayUrl + "' failed : " + e.toString() + ".");
                }

            } else if (TestCase.TESTCASE_ORIGIN_JIRAXRAYDC.equals(origin)) {

                putToken(system, origin, parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_xraydc_token, system, ""));
                LOG.debug("Setting new XRay DC Token : {}", getToken(system, origin));

            }

        } else {

            LOG.debug("Token in cache : {}", getToken(system, origin));

        }

    }

    private boolean isTokenExpired(String origin, String system) {
        try {
            LOG.debug("Checking Token Cache validity.");
            LocalDateTime currentTime = LocalDateTime.now();
            int cacheDuration = parameterService.getParameterIntegerByKey(Parameter.VALUE_cerberus_xray_tokencache_duration, system, DEFAULT_XRAY_CACHE_DURATION);
            if (cacheEntry.containsKey(getCacheKey(origin, system))) {
                LOG.debug("Cache entry creation timestamp {}", cacheEntry.get(getCacheKey(origin, system)).getString("created"));
                LocalDateTime cacheDateTime = LocalDateTime.parse(cacheEntry.get(getCacheKey(origin, system)).getString("created"));
                LOG.debug("isTokenExpired : {}", cacheDateTime.plusSeconds(cacheDuration).isBefore(currentTime));
                return cacheDateTime.plusSeconds(cacheDuration).isBefore(currentTime);
            } else {
                LOG.debug("isTokenExpired (no entry found) : {}", true);
                return true;
            }
        } catch (JSONException ex) {
            LOG.error(ex, ex);
        }

        return true;
    }

    private String getCacheKey(String origin, String system) {
        return "TOKEN-" + origin + "#" + system;
    }

}
