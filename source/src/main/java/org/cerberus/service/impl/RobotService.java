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
package org.cerberus.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.cerberus.dao.IProjectDAO;
import org.cerberus.dao.IRobotDAO;
import org.cerberus.entity.Robot;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.IRobotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class RobotService implements IRobotService {

    @Autowired
    private IRobotDAO robotDao;

    @Override
    public Robot findRobotByKey(Integer id) throws CerberusException {
        return robotDao.findRobotByKey(id);
    }

    @Override
    public List<Robot> findAllRobot() throws CerberusException {
        return robotDao.findAllRobot();
    }

    @Override
    public void updateRobot(Robot robot) throws CerberusException {
        robotDao.updateRobot(robot);
    }

    @Override
    public void createRobot(Robot robot) throws CerberusException {
        robotDao.createRobot(robot);
    }

    @Override
    public void deleteRobot(Robot robot) throws CerberusException {
        robotDao.deleteRobot(robot);
    }

    @Override
    public List<Robot> findRobotListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        return robotDao.findRobotListByCriteria(start, amount, column, dir, searchTerm, individualSearch);
    }

    @Override
    public Integer getNumberOfRobotPerCriteria(String searchTerm, String inds) {
        return robotDao.getNumberOfRobotPerCriteria(searchTerm, inds);
    }

    @Override
    public List<String> getDistinctValues(String columnName, String PlatformChosen,
            String BrowserChosen, String VersionChosen) throws CerberusException {
        List<Robot> robots = this.findAllRobot();
        List<String> result = new ArrayList();
        for (Robot robot : robots) {
            if (columnName.equals("Platform")) {
                if (robot.getBrowser().equals(BrowserChosen)
                        && robot.getVersion().equals(VersionChosen)) {
                    result.add(robot.getPlatform());
                } else if (robot.getBrowser().equals(BrowserChosen)
                        && VersionChosen.equals("")) {
                    result.add(robot.getPlatform());
                } else if (robot.getVersion().equals(VersionChosen)
                        && BrowserChosen.equals("")) {
                    result.add(robot.getPlatform());
                } else if (VersionChosen.equals("")
                        && BrowserChosen.equals("")) {
                    result.add(robot.getPlatform());
                }
            }
            if (columnName.equals("Browser")) {
                if (robot.getVersion().equals(VersionChosen)
                        && robot.getPlatform().equals(PlatformChosen)) {
                    result.add(robot.getBrowser());
                } else if (robot.getPlatform().equals(PlatformChosen)
                        && VersionChosen.equals("")) {
                    result.add(robot.getBrowser());
                } else if (robot.getVersion().equals(VersionChosen)
                        && PlatformChosen.equals("")) {
                    result.add(robot.getBrowser());
                } else if (VersionChosen.equals("")
                        && PlatformChosen.equals("")) {
                    result.add(robot.getBrowser());
                }
            }
            if (columnName.equals("Version")) {
                if (robot.getBrowser().equals(BrowserChosen)
                        && robot.getPlatform().equals(PlatformChosen)) {
                    result.add(robot.getVersion());
                } else if (robot.getBrowser().equals(BrowserChosen)
                        && PlatformChosen.equals("")) {
                    result.add(robot.getVersion());
                } else if (robot.getPlatform().equals(PlatformChosen)
                        && BrowserChosen.equals("")) {
                    result.add(robot.getVersion());
                } else if (PlatformChosen.equals("")
                        && BrowserChosen.equals("")) {
                    result.add(robot.getVersion());
                }
            }
        }
        Set<String> uniqueResult = new HashSet<String>(result);
        result = new ArrayList();
        result.addAll(uniqueResult);
        if (columnName.equals("Version")){
        java.util.Collections.sort(result);
        }else{
        java.util.Collections.sort(result);
        }
        return result;
    }

}
