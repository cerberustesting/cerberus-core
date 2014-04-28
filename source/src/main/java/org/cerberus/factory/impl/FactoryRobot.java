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
package org.cerberus.factory.impl;

import org.cerberus.entity.Robot;
import org.cerberus.factory.IFactoryRobot;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryRobot implements IFactoryRobot {

    @Override
    public Robot create(Integer id, String name, String ip, Integer port, String platform ,
    String os, String browser, String version, String description) {
        Robot newRobot = new Robot();
        newRobot.setId(id);
        newRobot.setName(name);
        newRobot.setIp(ip);
        newRobot.setPort(port);
        newRobot.setPlatform(platform);
        newRobot.setOs(os);
        newRobot.setBrowser(browser);
        newRobot.setVersion(version);
        newRobot.setDescription(description);
        return newRobot;
    }


}
