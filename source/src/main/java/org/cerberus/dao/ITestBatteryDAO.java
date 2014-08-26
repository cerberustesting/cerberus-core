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
package org.cerberus.dao;

import java.util.List;
import org.cerberus.entity.TestBattery;
import org.cerberus.exception.CerberusException;

/**
 *
 * @author memiks
 */
public interface ITestBatteryDAO {


    List<TestBattery> findAll() throws CerberusException;

    TestBattery findTestBatteryByKey(Integer testBatteryID) throws CerberusException;

    TestBattery findTestBatteryByTestBatteryName(String testBattery) throws CerberusException;

    List<TestBattery> findTestBatteriesByDescription(String description) throws CerberusException;

    boolean updateTestBattery(TestBattery testBattery);

    boolean createTestBattery(TestBattery testBattery);

    boolean deleteTestBattery(TestBattery testBattery);

    List<TestBattery> findTestBatteryByCriteria(Integer testBatteryID, String testBattery, String Description) throws CerberusException;

    List<TestBattery> findTestBatteriesByTestCase(String test, String testCase) throws CerberusException;
}
