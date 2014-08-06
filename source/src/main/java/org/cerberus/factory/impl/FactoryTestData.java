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

package org.cerberus.factory.impl;

import org.cerberus.entity.TestData;
import org.cerberus.factory.IFactoryTestData;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class FactoryTestData implements IFactoryTestData{

    @Override
    public TestData create(String key, String value, String description,
            String application, String environment, String country) {
        TestData newData = new TestData();
        newData.setKey(key);
        newData.setValue(value);
        newData.setDescription(description);
        newData.setApplication(application);
        newData.setCountry(country);
        newData.setEnvironment(environment);
        return newData;
    }
    
}
