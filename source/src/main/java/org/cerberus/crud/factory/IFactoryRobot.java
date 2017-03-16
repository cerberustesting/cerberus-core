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
package org.cerberus.crud.factory;

import java.util.List;
import org.cerberus.crud.entity.Robot;
import org.cerberus.crud.entity.RobotCapability;

/**
 * @author bcivel
 */
public interface IFactoryRobot {

    /**
     * 
     * @param robotID Autoincrement Id
     * @param robot Name of the Robot
     * @param host Host of the Robot
     * @param port port of the Robot
     * @param platform platform of the Robot
     * @param browser browser of the Robot
     * @param version version of the Robot
     * @param active Robot active
     * @param description description of the Robot
     * @param userAgent userAgent to Use
     * @return 
     */
    Robot create(Integer robotID, String robot, String host, String port, String platform ,
    String browser, String version, String active, String description, String userAgent, String screenSize);
    
    Robot create(Integer robotID, String robot, String host, String port, String platform ,
    String browser, String version, String active, String description, String userAgent, String screenSize, List<RobotCapability> capabilities);

    Robot create(String robot, String host, String port, String platform ,
                 String browser, String version, String active, String description, String userAgent, String screenSize);

}
