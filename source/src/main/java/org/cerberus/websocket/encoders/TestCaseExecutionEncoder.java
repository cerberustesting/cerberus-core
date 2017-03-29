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
package org.cerberus.websocket.encoders;

import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.service.ITestCaseStepExecutionService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

/**
 * Created by corentin on 31/10/16.
 */
public class TestCaseExecutionEncoder  implements Encoder.Text<TestCaseExecution>  {

    @Autowired
    ITestCaseStepExecutionService testCaseStepExecutionService;

    @Override
    public String encode(TestCaseExecution testCaseExecution) throws EncodeException {
        return  testCaseExecution.toJson(true).toString();
    }

    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    @Override
    public void destroy() {

    }
}
