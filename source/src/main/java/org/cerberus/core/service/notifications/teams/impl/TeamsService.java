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
package org.cerberus.core.service.notifications.teams.impl;

import java.nio.charset.StandardCharsets;
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
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.service.proxy.IProxyService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.cerberus.core.service.notifications.teams.ITeamsService;

/**
 *
 * @author vertigo17
 */
@Service
public class TeamsService implements ITeamsService {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(TeamsService.class);

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private IProxyService proxyService;

    private static final boolean DEFAULT_PROXY_ACTIVATE = false;
    private static final String DEFAULT_PROXY_HOST = "proxy";
    private static final int DEFAULT_PROXY_PORT = 80;
    private static final boolean DEFAULT_PROXYAUTHENT_ACTIVATE = false;
    private static final String DEFAULT_PROXYAUTHENT_USER = "squid";
    private static final String DEFAULT_PROXYAUTHENT_PASSWORD = "squid";

    @Override
    public void sendTeamsMessage(JSONObject cerberusMessage, String webHook) throws Exception {

        CloseableHttpClient httpclient = null;
        HttpClientBuilder httpclientBuilder;

        if (proxyService.useProxy(webHook, "")) {

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

        HttpPost post = new HttpPost(webHook);

        post.setEntity(new StringEntity(cerberusMessage.toString(), StandardCharsets.UTF_8));

        post.setHeader("Content-Type", "application/json");

        HttpResponse response = httpclient.execute(post);

        int rc = response.getStatusLine().getStatusCode();
        if (rc >= 200 && rc < 300) {
            LOG.debug("Teams request http return code : " + rc);
        } else {
            LOG.warn("Teams request http return code : " + rc);
            LOG.warn("Message sent to " + webHook + ":");
            LOG.warn(cerberusMessage.toString(1));
        }

    }

}
