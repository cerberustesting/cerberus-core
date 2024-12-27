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
package org.cerberus.core.service.appservice;

import java.util.List;
import org.cerberus.core.api.dto.appservice.AppServiceCallPropertyDTO;
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.util.answer.AnswerItem;

/**
 *
 * @author bcivel
 */
public interface IServiceService {

    /**
     * Perform a service call and feed the AppService object in return.If URL
     * coming from service object is enriched from the context of either the
     * database or the tCExecution.service is defined, If database is defined
     * the URL is enriched from context is coming from database if not, context
     * will be taken from tCExecution.
     *
     * @param service
     * @param targetNbEvents
     * @param targetNbSec
     * @param database
     * @param manualRequest Used when service not defined.
     * @param manualServicePath Used when service not defined.
     * @param manualOperation Used when service not defined.
     * @param execution
     * @param timeoutMs
     * @return
     */
    AnswerItem<AppService> callService(String service, String targetNbEvents, String targetNbSec, String database, String manualRequest, String manualServicePath, String manualOperation, TestCaseExecution execution, int timeoutMs);

    /**
     * Simulate a call from outside a normal testcase execution.
     *
     * @param service
     * @param country
     * @param environment
     * @param application
     * @param system
     * @param timeout
     * @param kafkaNb
     * @param kafkaTime
     * @param props
     * @param login
     * @return
     */
    AnswerItem<AppService> callAPI(String service, String country, String environment, String application, String system, int timeout, String kafkaNb, String kafkaTime, List<AppServiceCallPropertyDTO> props, String login);

}
