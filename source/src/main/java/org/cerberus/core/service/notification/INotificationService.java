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
package org.cerberus.core.service.notification;

import org.cerberus.core.crud.entity.User;
import org.cerberus.core.engine.entity.MessageEvent;

/**
 *
 * @author bcivel
 */
public interface INotificationService {

    /**
     *
     * @param user
     * @return
     */
    public MessageEvent generateAndSendAccountCreationEmail(User user);

    /**
     *
     * @param user
     * @return
     */
    public MessageEvent generateAndSendForgotPasswordEmail(User user);

    /**
     *
     * @param system
     * @param country
     * @param env
     * @param build
     * @param revision
     * @return
     */
    public MessageEvent generateAndSendRevisionChangeEmail(String system, String country, String env, String build, String revision);

    /**
     *
     * @param system
     * @param country
     * @param env
     * @return
     */
    public MessageEvent generateAndSendDisableEnvEmail(String system, String country, String env);

    /**
     *
     * @param system
     * @param country
     * @param env
     * @param chain
     * @return
     */
    public MessageEvent generateAndSendNewChainEmail(String system, String country, String env, String chain);

}
