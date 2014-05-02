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
package org.cerberus.factory;

import org.cerberus.entity.Robot;

/**
 * @author bcivel
 */
public interface IFactoryRobot {

    /**
     * 
     * @param id Autoincrement Id
     * @param name Name of the Robot
     * @param ip IP of the Robot
     * @param port port of the Robot
     * @param platform platform of the Robot
     * @param browser browser of the Robot
     * @param version version of the Robot
     * @param description description of the Robot
     * @return 
     */
    Robot create(Integer id, String name, String ip, Integer port, String platform ,
    String browser, String version, String description);

}
