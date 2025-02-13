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
package org.cerberus.core.service.robotproxy.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.entity.RobotExecutor;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusEventException;
import org.cerberus.core.service.har.IHarService;
import org.cerberus.core.service.rest.IRestService;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.cerberus.core.service.robotproxy.IRobotProxyService;

/**
 *
 * @author vertigo17
 */
@Service
public class RobotProxyService implements IRobotProxyService {

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private IRestService restService;
    @Autowired
    private IHarService harService;
    @Autowired
    private ILogEventService logEventService;

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(RobotProxyService.class);

    @Override
    public void startRemoteProxy(TestCaseExecution tce) {

        String url = "http://" + tce.getRobotExecutorObj().getExecutorProxyServiceHost() + ":" + tce.getRobotExecutorObj().getExecutorProxyServicePort()
                + "/startProxy?timeout=" + String.valueOf(parameterService.getParameterIntegerByKey("cerberus_executorproxy_timeoutms", tce.getSystem(), 3600000));
        if (tce.getRobotExecutorObj().getExecutorBrowserProxyPort() != 0) {
            url += "&port=" + tce.getRobotExecutorObj().getExecutorBrowserProxyPort();
        }

        if (TestCaseExecution.ROBOTPROVIDER_BROWSERSTACK.equals(tce.getRobotProvider())) {
            url += "&bsLocalProxyActive=true";
            url += "&bsLocalProxyHost=" + tce.getRobotExecutorObj().getExecutorProxyServiceHost();
            url += "&bsKey=" + tce.getRobotExecutorObj().getHostPassword();
            url += "&bsLocalIdentifier=" + tce.getExecutionUUID();
        }
        LOG.debug("Starting Cerberus Robot Proxy calling : '{}'", url);

        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            String jsonText = sb.toString();

            JSONObject json = new JSONObject(jsonText);
            tce.setRemoteProxyPort(json.getInt("port"));
            tce.setRemoteProxyUUID(json.getString("uuid"));
            tce.setRemoteProxyStarted(true);

            LOG.debug("Cerberus Robot Proxy started on port : " + tce.getRemoteProxyPort() + " (uuid : " + tce.getRemoteProxyUUID() + ")");

        } catch (Exception ex) {
            logEventService.createForPrivateCalls("", "EXEC", LogEvent.STATUS_ERROR, "Error when trying to open a remote proxy on Cerberus Robot Proxy. " + ex.toString());
            LOG.error("Exception Starting Remote Proxy " + tce.getRobotExecutorObj().getExecutorProxyServiceHost() + ":" + tce.getRobotExecutorObj().getExecutorProxyServicePort() + " Exception :" + ex.toString(), ex);
        }

    }

    @Override
    public void stopRemoteProxy(TestCaseExecution tce) {

        if (tce.isRemoteProxyStarted()) {
            tce.setRemoteProxyStarted(false);
            /**
             * We Stop the Cerberus Robot Proxy.
             */
            try {
                // Ask the Proxy to stop.
                if (tce.getRobotExecutorObj() != null && RobotExecutor.PROXY_TYPE_NETWORKTRAFFIC.equals(tce.getRobotExecutorObj().getExecutorProxyType())) {

                    String urlStop = "http://" + tce.getRobotExecutorObj().getExecutorProxyServiceHost() + ":" + tce.getRobotExecutorObj().getExecutorProxyServicePort() + "/stopProxy?uuid=" + tce.getRemoteProxyUUID();

                    LOG.debug("Shutting down of Cerberus Robot Proxy calling : '{}'", urlStop);

                    InputStream is = new URL(urlStop).openStream();
                    is.close();

                    LOG.debug("Cerberus Robot Proxy shutdown done (uuid : " + tce.getRemoteProxyUUID() + ").");

                }

            } catch (Exception ex) {
                LOG.error("Exception when asking Cerberus Robot proxy to stop " + tce.getId(), ex);
            }
        }

    }

    @Override
    public MessageEvent waitForIdleNetwork(String exHost, Integer exPort, String exUuid, String system) throws CerberusEventException {
        // Generate URL to Cerberus executor with parameter to get the nb of hits so far.
        String url = "http://" + exHost + ":" + exPort + "/getStats?uuid=" + exUuid;

        try {

            Integer nbHits = 0;
            Integer nbHitsPrev = 0;

            Integer sleepPeriod = parameterService.getParameterIntegerByKey("cerberus_networkstatsave_idleperiod_ms", system, 5000);
            Integer maxLoop = parameterService.getParameterIntegerByKey("cerberus_networkstatsave_idlemaxloop_nb", system, 10);

            LOG.debug("Getting nb of Hits so far from URL : " + url);
            Integer i = 0;
            for (i = 0; i < maxLoop; i++) {
                AnswerItem<AppService> result = new AnswerItem<>();
                result = restService.callREST(url, "", AppService.METHOD_HTTPGET, AppService.SRVBODYTYPE_RAW, new ArrayList<>(), new ArrayList<>(), null, 10000, "", true, null, "", "", "", "", "");

                if (result.isCodeStringEquals("OK")) {

                    AppService appSrv = result.getItem();
                    JSONObject stats = new JSONObject(appSrv.getResponseHTTPBody());
                    nbHitsPrev = nbHits;
                    nbHits = stats.getInt("hits");

                    LOG.debug("Nb Hits so far : " + nbHits);

                    if (nbHits.equals(nbHitsPrev)) {
                        LOG.debug("Nb of hits (" + nbHits + ") is the same as before (" + nbHitsPrev + ") --> so network is idle.");
                        break;
                    }
                    Thread.sleep(sleepPeriod);

                } else {
                    LOG.warn("Failed getting nb of Hits from URL (Maybe cerberus-executor is not at the correct version) : '" + url + "'");
                    return new MessageEvent(MessageEventEnum.ACTION_FAILED_WAITNETWORKTRAFFICIDLE).resolveDescription("DETAIL", "Failed getting nb of Hits from URL (Maybe cerberus-executor is not reachable) : '" + url + "'");
                }
            }

            if (nbHits.equals(nbHitsPrev)) {
                return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_WAITNETWORKTRAFFICIDLE).resolveDescription("HITS", nbHits.toString())
                        .resolveDescription("NB", i.toString()).resolveDescription("TIME", sleepPeriod.toString());
            } else {
                return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_WAITNETWORKTRAFFICIDLE_TIMEOUT).resolveDescription("HITS", nbHits.toString())
                        .resolveDescription("NB", i.toString()).resolveDescription("TIME", sleepPeriod.toString());
            }

        } catch (InterruptedException ex) {
            LOG.warn("Exception when waiting for idle.", ex);
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_WAITNETWORKTRAFFICIDLE).resolveDescription("DETAIL", ex.toString());
        } catch (JSONException ex) {
            LOG.warn("Exception when waiting for idle (interpreting JSON answer from URL : '" + url + "').");
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_WAITNETWORKTRAFFICIDLE).resolveDescription("DETAIL", "Failed getting nb of Hits from URL (Maybe cerberus-executor is not at the correct version) : '" + url + "'");
        }
    }

    @Override
    public JSONObject getHar(String urlFilter, boolean withContent, String exHost, Integer exPort, String exUuid, String system, Integer indexFrom) {

        JSONObject har = new JSONObject();
        try {

            // Generate URL to Cerberus executor with parameter to reduce the answer size by removing response content.
            String url = getExecutorURL("", withContent, exHost, exPort, exUuid);

            LOG.debug("Getting Network Traffic content from URL : " + url);
            AnswerItem<AppService> result = new AnswerItem<>();
            result = restService.callREST(url, "", AppService.METHOD_HTTPGET, AppService.SRVBODYTYPE_RAW, new ArrayList<>(), new ArrayList<>(), null, 10000, "", true, null, "", "", "", "", "");

            AppService appSrv = result.getItem();
            har = new JSONObject(appSrv.getResponseHTTPBody());

            // remove the 1st entries and filter them based on the url.
            har = harService.removeFirstHitsandFilterURL(har, indexFrom, urlFilter);

            return har;

        } catch (JSONException ex) {
            LOG.error("Exception when parsing JSON.", ex);
        }
        return har;
    }

    @Override
    public String getExecutorURL(String urlFilter, boolean withContent, String exHost, Integer exPort, String exUuid) {
        LOG.debug("Building URL : " + exUuid);
        String url = "http://" + exHost + ":" + exPort
                + "/getHar?uuid=" + exUuid;
        if (!StringUtil.isEmptyOrNull(urlFilter)) {
            url += "&requestUrl=" + urlFilter;
        }
        if (!withContent) {
            url += "&emptyResponseContentText=true";
        }

        return url;
    }

    @Override
    public Integer getHitsNb(String exHost, Integer exPort, String exUuid) throws CerberusEventException {
        String url = "http://" + exHost + ":" + exPort + "/getStats?uuid=" + exUuid;
        Integer nbHits = 0;
        try {
            AnswerItem<AppService> result = new AnswerItem<>();
            result = restService.callREST(url, "", AppService.METHOD_HTTPGET, AppService.SRVBODYTYPE_RAW, new ArrayList<>(), new ArrayList<>(), null, 10000, "", true, null, "", "", "", "", "");

            if (result.isCodeStringEquals("OK")) {

                AppService appSrv = result.getItem();
                JSONObject stats = new JSONObject(appSrv.getResponseHTTPBody());
                nbHits = stats.getInt("hits");
                LOG.debug("Nb of Hits collected : " + nbHits);

                return nbHits;

            } else {
                LOG.warn("Failed getting nb of Hits from URL (Maybe cerberus-executor is not at the correct version) : '" + url + "'");
//                    return new MessageEvent(MessageEventEnum.ACTION_FAILED_WAITNETWORKTRAFFICIDLE).resolveDescription("DETAIL", "Failed getting nb of Hits from URL (Maybe cerberus-executor is not reachable) : '" + url + "'");
            }
        } catch (JSONException ex) {
            LOG.warn("Exception when getting nb of hits (interpreting JSON answer from URL : '" + url + "').");
//            return new MessageEvent(MessageEventEnum.ACTION_FAILED_WAITNETWORKTRAFFICIDLE).resolveDescription("DETAIL", "Failed getting nb of Hits from URL (Maybe cerberus-executor is not at the correct version) : '" + url + "'");
        }
        return nbHits;
    }
}
