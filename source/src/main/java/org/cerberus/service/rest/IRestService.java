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
package org.cerberus.service.rest;

import java.util.List;
import org.cerberus.crud.entity.AppService;
import org.cerberus.crud.entity.AppServiceContent;
import org.cerberus.crud.entity.AppServiceHeader;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.util.answer.AnswerItem;

/**
 *
 * @author bcivel
 */
public interface IRestService {

    /**
     * Call Soap Message
     *
     * @param servicePath
     * @param queryString
     * @param method
     * @param headerList
     * @param contentList
     * @param token if != null the token will be added to http header 'cerberus-token'
     * @param timeOutMs
     * @param system
     * @param tcexecution
     * @return
     */
    AnswerItem<AppService> callREST(String servicePath, String queryString, String method,
                                    List<AppServiceHeader> headerList, List<AppServiceContent> contentList, String token, int timeOutMs, String system, TestCaseExecution tcexecution);

}
