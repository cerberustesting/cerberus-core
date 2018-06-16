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
package org.cerberus.service.email;

import org.cerberus.crud.entity.User;
import org.cerberus.service.email.entity.Email;

/**
 *
 * @author bcivel
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
     * @param campaign
     * @param to
     * @return
     * @throws Exception
     */
    public Email generateNotifyStartTagExecution(String Tag, String campaign, String to) throws Exception;
    
    /**
     *
     * @param Tag
     * @param campaign
     * @param to
     * @return
     * @throws Exception
     */
    public Email generateNotifyEndTagExecution(String Tag, String campaign, String to) throws Exception;
}
