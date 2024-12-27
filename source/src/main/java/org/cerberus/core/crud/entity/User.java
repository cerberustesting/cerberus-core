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
package org.cerberus.core.crud.entity;

import java.sql.Timestamp;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author vertigo
 */
@Data
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private int userID;
    private String login;
    private String name;
    private String email;
    private String team;
    private String language;

    private String password;
    private String resetPasswordToken;
    private String request;

    private String attribute01;
    private String attribute02;
    private String attribute03;
    private String attribute04;
    private String attribute05;
    private String apiKey;
    private String comment;

    private String reportingFavorite;
    private String robotHost;
    private String robotPort;
    private String robotPlatform;
    private String robotBrowser;
    private String robotVersion;
    private String robot;
    private String defaultSystem;
    private String userPreferences;

    private String usrCreated;
    private Timestamp dateCreated;
    private String usrModif;
    private Timestamp dateModif;

    private List<UserSystem> userSystems;
    private List<UserRole> userRoles;

    /**
     * Invariant PROPERTY TYPE String.
     */
    public static final String CERBERUS_SERVICEACCOUNT_LOGIN = "srvaccount";

}
