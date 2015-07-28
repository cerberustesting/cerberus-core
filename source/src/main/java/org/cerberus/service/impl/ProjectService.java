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
import java.util.List;

import org.cerberus.dao.IProjectDAO;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.Project;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.IProjectService;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class ProjectService implements IProjectService {

    @Autowired
    private IProjectDAO projectDao;

    @Override
    public List<String> findListOfProjectDescription() {
        List<String> result = new ArrayList<String>();
        List<Project> listOfProject = this.projectDao.findAllProject();
        for (Project project : listOfProject) {
            result.add(project.getIdProject().concat(project.getCode()).concat(project.getDescription()));
        }

        return result;
    }

    @Override
    public Project findProjectByKey(String project) throws CerberusException {
        Project myProject = projectDao.findProjectByKey(project);
        if (myProject == null) {
            //TODO define message => error occur trying to find user
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return myProject;
    }

    @Override
    public List<Project> findAllProject() throws CerberusException {
        return projectDao.findAllProject();
    }

    @Override
    public boolean isProjectExist(String project) {
        try {
            findProjectByKey(project);
            return true;
        } catch (CerberusException e) {
            return false;
        }
    }

    @Override
    public void createProject(Project project) throws CerberusException {
        projectDao.createProject(project);
    }

    @Override
    public void deleteProject(Project project) throws CerberusException {
        projectDao.deleteProject(project);
    }

    @Override
    public void updateProject(Project project) throws CerberusException {
        projectDao.updateProject(project);
    }

    @Override
    public AnswerList findProjectListByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, String string) {
        return projectDao.findProjectListByCriteria(startPosition, length, columnName, sort, searchParameter, string);
    }

}
