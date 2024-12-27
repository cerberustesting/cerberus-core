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
package org.cerberus.core.service.notifications.email;

import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.User;
import org.cerberus.core.service.notifications.email.entity.Email;

/**
 *
 * @author vertigo17
 */
public interface IEmailGenerationService {

    /**
     *
     * @param system
     * @param country
     * @param env
     * @param build
     * @param revision
     * @return
     * @throws Exception
     */
    public Email generateRevisionChangeEmail(String system, String country, String env, String build, String revision) throws Exception;

    /**
     *
     * @param system
     * @param country
     * @param env
     * @return
     * @throws Exception
     */
    public Email generateDisableEnvEmail(String system, String country, String env) throws Exception;

    /**
     *
     * @param system
     * @param country
     * @param env
     * @param chain
     * @return
     * @throws Exception
     */
    public Email generateNewChainEmail(String system, String country, String env, String chain) throws Exception;

    /**
     *
     * @param user
     * @return
     * @throws Exception
     */
    public Email generateAccountCreationEmail(User user) throws Exception;

    /**
     *
     * @param user
     * @return
     * @throws Exception
     */
    public Email generateForgotPasswordEmail(User user) throws Exception;
    
    /**
     *
     * @param Tag
     * @param to
     * @return
     * @throws Exception
     */
    public Email generateNotifyStartTagExecution(Tag Tag, String to) throws Exception;
    
    /**
     *
     * @param Tag
     * @param to
     * @return
     * @throws Exception
     */
    public Email generateNotifyEndTagExecution(Tag Tag, String to) throws Exception;
    /**
     *
     * @param exe
     * @param to
     * @return
     * @throws Exception
     */
    public Email generateNotifyStartExecution(TestCaseExecution exe, String to) throws Exception;
    
    /**
     *
     * @param exe
     * @param to
     * @return
     * @throws Exception
     */
    public Email generateNotifyEndExecution(TestCaseExecution exe, String to) throws Exception;
    /**
     *
     * @param testCase
     * @param to
     * @param eventReference
     * @return
     * @throws Exception
     */
    public Email generateNotifyTestCaseChange(TestCase testCase, String to, String eventReference) throws Exception;
}
