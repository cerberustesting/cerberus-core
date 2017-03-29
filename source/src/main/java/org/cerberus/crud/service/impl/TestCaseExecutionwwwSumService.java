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
package org.cerberus.crud.service.impl;

import java.util.List;
import org.cerberus.crud.dao.ITestCaseExecutionwwwDetDAO;
import org.cerberus.crud.dao.ITestCaseExecutionwwwSumDAO;
import org.cerberus.crud.entity.StatisticDetail;
import org.cerberus.crud.entity.StatisticSummary;
import org.cerberus.crud.entity.TestCaseExecutionwwwSum;
import org.cerberus.crud.service.ITestCaseExecutionwwwSumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestCaseExecutionwwwSumService implements ITestCaseExecutionwwwSumService {

    @Autowired
    private ITestCaseExecutionwwwSumDAO testCaseExecutionWWWSumDAO;
    @Autowired
    private ITestCaseExecutionwwwDetDAO testCaseExecutionWWWDetDAO;

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

    @Override
    public List<TestCaseExecutionwwwSum> getAllDetailsFromTCEwwwSum(int id) {
        return testCaseExecutionWWWSumDAO.getAllDetailsFromTCEwwwSum(id);
    }
}
