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
package org.cerberus.crud.service.impl;

import org.cerberus.crud.dao.ITestBatteryContentDAO;
import org.cerberus.crud.service.ITestBatteryContentService;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author cerberus
 */
@Service
public class TestBatteryContentService implements ITestBatteryContentService {

    @Autowired
    ITestBatteryContentDAO testBatteryContentDAO;

    @Override
    public AnswerList readByTestBatteryByCriteria(String testBattery, int startPosition, int length, String columnName, String sort, String searchParameter, String string) {
        return testBatteryContentDAO.readByTestBatteryByCriteria(testBattery, startPosition, length, columnName, sort, searchParameter, string);
    }

    @Override
    public AnswerList readByCampaignByCriteria(String campaign, int start, int amount, String columnName, String sortInformation, String searchTerm, Map<String, List<String>> individualSearch) {
        return testBatteryContentDAO.readByCampaignByCriteria(campaign, start, amount, columnName, sortInformation, searchTerm, individualSearch);
    }

    @Override
    public AnswerList<String> readDistinctValuesByCampaignByCriteria(String campaign, String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return testBatteryContentDAO.readDistinctValuesByCampaignByCriteria(campaign, searchParameter, individualSearch, columnName);
    }

}
