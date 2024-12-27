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

import lombok.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.Timestamp;

/**
 * @author bcivel
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TestGenericObject {

    private String object;
    private String test;
    private String testcase;
    private String active;
    private String country;
    private String application;
    private String status;
    private String system;
    private int stepId;
    private int actionId;
    private int controlId;
    private String loop;
    private String conditionOperator;
    private String conditionValue1;
    private String conditionValue2;
    private String conditionValue3;
    private String property;
    private String actionControl;
    private String value1;
    private String value2;
    private String value3;
    private String isFatal;
    private String doScreenshotBefore;
    private String doScreenshotAfter;
    private int waitBefore;
    private int waitAfter;
    private String description;
    private String usrCreated;
    private Timestamp dateCreated;
    private String usrModif;
    private Timestamp dateModif;

    private static final Logger LOG = LogManager.getLogger(TestGenericObject.class);

}
