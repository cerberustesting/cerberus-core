/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.service.impl;

import java.io.IOException;

import org.apache.log4j.Level;
import org.cerberus.dao.ITestCaseExecutionWWWDetDAO;
import org.cerberus.dao.ITestCaseExecutionWWWSumDAO;
import org.cerberus.entity.StatisticDetail;
import org.cerberus.entity.StatisticSummary;
import org.cerberus.log.MyLogger;
import org.cerberus.service.ITestCaseExecutionWWWService;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.umass.cs.benchlab.har.HarEntry;
import edu.umass.cs.benchlab.har.HarLog;
import edu.umass.cs.benchlab.har.tools.HarFileReader;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 04/03/2013
 * @since 2.0.0
 */
@Service
public class TestCaseExecutionWWWService implements ITestCaseExecutionWWWService {

    @Autowired
    private ITestCaseExecutionWWWDetDAO testCaseExecutionWWWDetDAO;

    @Autowired
    private ITestCaseExecutionWWWSumDAO testCaseExecutionWWWSumDAO;

    @Override
    public void registerDetail(long runId, String file, String page) {
        try {
            JsonFactory f = new JsonFactory();
            JsonParser jp = f.createJsonParser(file);

            HarFileReader r = new HarFileReader();
            HarLog log = r.readHarFile(jp, null);
            StatisticDetail detail;

            for (HarEntry entry : log.getEntries().getEntries()) {
                detail = new StatisticDetail();
                detail.setStart(entry.getStartedDateTime().getTime());
                detail.setTime(entry.getTime());
                detail.setEnd(entry.getStartedDateTime().getTime() + entry.getTime());
                detail.setPageRes(page);
                detail.setUrl(entry.getRequest().getUrl());
                detail.setExt(entry.getResponse().getContent().getMimeType().split("/")[1]);
                detail.setStatus(entry.getResponse().getStatus());
                detail.setMethod(entry.getRequest().getMethod());
                detail.setBytes(entry.getRequest().getHeadersSize() + entry.getRequest().getBodySize());
                detail.setHostReq(entry.getRequest().getUrl().split("/")[2]);
                detail.setContentType();

                this.testCaseExecutionWWWDetDAO.register(runId, detail);
            }
        } catch (IOException exception) {
            MyLogger.log(TestCaseExecutionWWWService.class.getName(), Level.WARN, exception.toString() + "\nrunID: " + runId);
        }
    }

    @Override
    public void registerSummary(long runId) {
        StatisticSummary summary = new StatisticSummary();

        for (StatisticDetail detail : this.testCaseExecutionWWWDetDAO.getStatistics(runId)) {
            summary.addTotNbHits();
            summary.addTotTps((int) detail.getTime());
            summary.addTotSize((int) detail.getBytes());
            switch (detail.getStatus() / 100) {
                case 2:
                    summary.addNbRc2xx();
                    break;

                case 3:
                    summary.addNbRc3xx();
                    break;

                case 4:
                    summary.addNbRc4xx();
                    break;

                case 5:
                    summary.addNbRc5xx();
                    break;
            }
            if (detail.isImage()) {
                this.treatImage(detail, summary);
            } else if (detail.isScript()) {
                this.treatScript(detail, summary);
            } else if (detail.isStyle()) {
                this.treatStyle(detail, summary);
            }
        }

        this.testCaseExecutionWWWSumDAO.register(runId, summary);
    }

    private void treatImage(StatisticDetail detail, StatisticSummary summary) {
        int size = (int) detail.getBytes();

        summary.addImgNb();
        summary.addImgTps((int) detail.getTime());
        summary.addImgSizeTot(size);
        if (size > summary.getImgSizeMax()) {
            summary.setImgSizeMax(size);
            summary.setImgSizeMaxUrl(detail.getUrl());
        }
    }

    private void treatScript(StatisticDetail detail, StatisticSummary summary) {
        int size = (int) detail.getBytes();

        summary.addJsNb();
        summary.addJsTps((int) detail.getTime());
        summary.addJsSizeTot(size);
        if (size > summary.getJsSizeMax()) {
            summary.setJsSizeMax(size);
            summary.setJsSizeMaxUrl(detail.getUrl());
        }
    }

    private void treatStyle(StatisticDetail detail, StatisticSummary summary) {
        int size = (int) detail.getBytes();

        summary.addCssNb();
        summary.addCssTps((int) detail.getTime());
        summary.addCssSizeTot(size);
        if (size > summary.getCssSizeMax()) {
            summary.setCssSizeMax(size);
            summary.setCssSizeMaxUrl(detail.getUrl());
        }
    }
}
