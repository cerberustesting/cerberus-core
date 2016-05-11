/*
 * Cerberus  Copyright (C) 2016  vertigo17
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
package org.cerberus.crud.dao;

import org.cerberus.crud.entity.Robot;
import org.cerberus.crud.entity.RobotCapabilities;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * {@link RobotCapabilities} DAO
 *
 * @author Aurelien Bourdon
 */
public interface IRobotCapabilitiesDAO {

    Answer create(RobotCapabilities capabilities);

    Answer update(RobotCapabilities capabilities);

    Answer delete(RobotCapabilities capabilities);

    AnswerItem<RobotCapabilities> findFromRobot(Robot robot);

}
