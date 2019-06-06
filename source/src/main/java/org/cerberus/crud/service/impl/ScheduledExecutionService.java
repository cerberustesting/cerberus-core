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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.dao.IScheduledExecutionDAO;
import org.cerberus.crud.dao.impl.ScheduledExecutionDAO;
import org.cerberus.crud.entity.ScheduledExecution;
import org.cerberus.crud.service.IScheduledExecutionService;
import org.cerberus.util.answer.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author cdelage
 */
@Service
public class ScheduledExecutionService implements IScheduledExecutionService {
    private static final Logger LOG = LogManager.getLogger(ScheduledExecutionService.class);

    @Autowired
    IScheduledExecutionDAO scheduledExecutionDAO = new ScheduledExecutionDAO();

    @Override
    public Answer create(ScheduledExecution scheduledExecution) {
        Answer response = new Answer();
        response = scheduledExecutionDAO.create(scheduledExecution);
        return response;
    }
    
    @Override
    public Answer update(ScheduledExecution scheduledExecution) {
        Answer response = new Answer();
        response = scheduledExecutionDAO.update(scheduledExecution);
        return response;
    }    
    
}
