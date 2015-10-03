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
package org.cerberus.crud.service.impl;

import org.cerberus.crud.dao.IBuildRevisionParametersDAO;
import org.cerberus.crud.entity.BuildRevisionParameters;
import org.cerberus.crud.service.IBuildRevisionParametersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

@Service
public class BuildRevisionParametersService implements IBuildRevisionParametersService {

    @Autowired
    IBuildRevisionParametersDAO buildRevisionParametersDAO;

    @Override
    public List<BuildRevisionParameters> findBuildRevisionParametersFromMaxRevision(String build, String revision, String lastBuild, String lastRevision) {
        return this.buildRevisionParametersDAO.findBuildRevisionParametersFromMaxRevision(build, revision, lastBuild, lastRevision);
    }

    @Override
    public List<BuildRevisionParameters> findBuildRevisionParametersByCriteria(String system, String build, String revision) {
        return this.buildRevisionParametersDAO.findBuildRevisionParametersByCriteria(system, build, revision);
    }

    @Override
    public String getMaxBuildBySystem(String system) {
        return this.buildRevisionParametersDAO.getMaxBuildBySystem(system);
    }

    @Override
    public String getMaxRevisionBySystemAndBuild(String system, String build) {
        return this.buildRevisionParametersDAO.getMaxRevisionBySystemAndBuild(system, build);
    }

    @Override
    public void insertBuildRevisionParameters(BuildRevisionParameters brp) {
        this.buildRevisionParametersDAO.insertBuildRevisionParameters(brp);
    }

    @Override
    public void deleteBuildRevisionParameters(int id) {
        this.buildRevisionParametersDAO.deleteBuildRevisionParameters(id);
    }

    @Override
    public void updateBuildRevisionParameters(BuildRevisionParameters brp) {
        this.buildRevisionParametersDAO.updateBuildRevisionParameters(brp);
    }

    @Override
    public BuildRevisionParameters findBuildRevisionParametersByKey(int id) {
        return this.buildRevisionParametersDAO.findBuildRevisionParametersByKey(id);
    }

    @Override
    public AnswerItem readByKeyTech(int id) {
        return this.buildRevisionParametersDAO.readByKeyTech(id);
    }

    @Override
    public AnswerItem readLastBySystem(String system) {
        return this.buildRevisionParametersDAO.readLastBySystem(system);
    }
    
    @Override
    public AnswerList readByVarious1ByCriteria(String system, String application, String build, String revision, int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        return this.buildRevisionParametersDAO.readByVarious1ByCriteria(system, application, build, revision, start, amount, column, dir, searchTerm, individualSearch);
    }

    @Override
    public Answer create(BuildRevisionParameters brp) {
        return buildRevisionParametersDAO.create(brp);
    }

    @Override
    public Answer delete(BuildRevisionParameters brp) {
        return buildRevisionParametersDAO.delete(brp);
    }

    @Override
    public Answer update(BuildRevisionParameters brp) {
        return buildRevisionParametersDAO.update(brp);
    }

    @Override
    public BuildRevisionParameters convert(AnswerItem answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (BuildRevisionParameters) answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<BuildRevisionParameters> convert(AnswerList answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (List<BuildRevisionParameters>) answerList.getDataList();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public void convert(Answer answer) throws CerberusException {
        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return;
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

}
