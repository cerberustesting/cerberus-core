/* Cerberus Copyright (C) 2013 - 2017 cerberustesting
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.

This file is part of Cerberus.

Cerberus is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Cerberus is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.cerberus.crud.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.cerberus.crud.entity.ScheduleEntry;
import org.cerberus.util.answer.AnswerItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.cerberus.crud.dao.IScheduleEntryDAO;
import org.cerberus.crud.service.IScheduleEntryService;

/**
 *
 * @author cdelage
 */

@Service
public class ScheduleEntryService implements IScheduleEntryService{
    
    @Autowired
    IScheduleEntryDAO schedulerDao;
    
    @Override
    public AnswerItem<ScheduleEntry> readbykey (String name){
    AnswerItem<ScheduleEntry> ans = new AnswerItem();
    ans = schedulerDao.readByKey(name);
    return ans;
    }
    
    @Override
    public AnswerItem<List> readAllActive (){
    AnswerItem<List> ans = new AnswerItem();
    ans = schedulerDao.readAllActive();
    return ans;
    }
    /*
    public boolean createScheduleEntry (ScheduleEntry scheduleentry){
        boolean response = true;
        return response;
    }*/
    
}
