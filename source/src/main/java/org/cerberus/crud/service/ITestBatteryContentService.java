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
package org.cerberus.crud.service;

import org.cerberus.crud.entity.TestBattery;
import org.cerberus.crud.entity.TestBatteryContent;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

import java.util.List;
import java.util.Map;

/**
 * @author cerberus
 */
public interface ITestBatteryContentService {

    AnswerList readByTestBatteryByCriteria(String testBattery, int startPosition, int length, String columnName, String sort, String searchParameter, String string);

    public AnswerList readByCampaignByCriteria(String campaign, int start, int amount, String columnName, String sortInformation, String searchTerm, Map<String, List<String>> individualSearch);

    public AnswerList readByCriteria(int start, int amount, String columnName, String sortInformation, String searchTerm, Map<String, List<String>> individualSearch);

    public AnswerList<String> readDistinctValuesByCampaignByCriteria(String campaign, String searchParameter, Map<String, List<String>> individualSearch, String columnName);

    AnswerList readByTestBattery(String key);

    Answer deleteByTestBattery(String key);

    public Answer createList(List<TestBatteryContent> objectList);

    public Answer deleteList(List<TestBatteryContent> objectList);

    Answer compareListAndUpdateInsertDeleteElements(String tb, List<TestBatteryContent> tbc);

    TestBatteryContent convert(AnswerItem answerItem) throws CerberusException;

    List<TestBatteryContent> convert(AnswerList answerList) throws CerberusException;
}
