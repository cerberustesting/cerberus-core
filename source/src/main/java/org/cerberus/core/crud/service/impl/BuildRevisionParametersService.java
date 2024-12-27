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
package org.cerberus.core.crud.service.impl;

import org.cerberus.core.crud.dao.IBuildRevisionParametersDAO;
import org.cerberus.core.crud.entity.BuildRevisionParameters;
import org.cerberus.core.crud.service.IBuildRevisionParametersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.ICountryEnvParam_logService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

@Service
public class BuildRevisionParametersService implements IBuildRevisionParametersService {

    private final String OBJECT_NAME = "BuildRevisionParameters";

    private static final Logger LOG = LogManager.getLogger("BuildRevisionParametersService");

    @Autowired
    IBuildRevisionParametersDAO buildRevisionParametersDAO;
    @Autowired
    IApplicationService applicationService;
    @Autowired
    ICountryEnvParam_logService countryEnvParamLogService;

    @Override
    public AnswerItem readByKeyTech(int id) {
        return this.buildRevisionParametersDAO.readByKeyTech(id);
    }

    @Override
    public AnswerItem readLastBySystem(String system) {
        return this.buildRevisionParametersDAO.readLastBySystem(system);
    }

    @Override
    public AnswerItem readByVarious2(String build, String revision, String release, String application) {
        return buildRevisionParametersDAO.readByVarious2(build, revision, release, application);
    }

    @Override
    public AnswerList<BuildRevisionParameters> readByVarious1ByCriteria(String system, String application, String build, String revision, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        return this.buildRevisionParametersDAO.readByVarious1ByCriteria(system, application, build, revision, start, amount, column, dir, searchTerm, individualSearch);
    }

    @Override
    public AnswerList<BuildRevisionParameters> readMaxSVNReleasePerApplication(String system, String build, String revision, String lastBuild, String lastRevision) {
        return this.buildRevisionParametersDAO.readMaxSVNReleasePerApplication(system, build, revision, lastBuild, lastRevision);
    }

    @Override
    public AnswerList<BuildRevisionParameters> readNonSVNRelease(String system, String build, String revision, String lastBuild, String lastRevision) {
        return this.buildRevisionParametersDAO.readNonSVNRelease(system, build, revision, lastBuild, lastRevision);
    }

    @Override
    public Answer create(BuildRevisionParameters brp) {
        Answer ans = new Answer();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);

        /**
         * Checking if the build Revision has already been deployed. If so the
         * Create cannot be performed
         */
        if (check_buildRevisionAlreadyUsed(brp.getApplication(), brp.getBuild(), brp.getRevision())) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "Could not create this release as corresponding build " + brp.getBuild() + " revision " + brp.getRevision() + " has already been deployed in an environment."));
            ans.setResultMessage(msg);
            return ans;
        }

        return buildRevisionParametersDAO.create(brp);
    }

    @Override
    public Answer delete(BuildRevisionParameters brp) {
        Answer ans = new Answer();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);

        /**
         * Checking if the build Revision has already been deployed. If so the
         * delete cannot be performed
         */
        if (check_buildRevisionAlreadyUsed(brp.getApplication(), brp.getBuild(), brp.getRevision())) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                    .replace("%OPERATION%", "Delete")
                    .replace("%REASON%", "Could not delete this release as corresponding build " + brp.getBuild() + " revision " + brp.getRevision() + " has already been deployed in an environment."));
            ans.setResultMessage(msg);
            return ans;
        }

        return buildRevisionParametersDAO.delete(brp);
    }

    @Override
    public Answer update(BuildRevisionParameters brp) {
        Answer ans = new Answer();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);

        /**
         * Checking if the build Revision has already been deployed. If so the
         * update cannot be performed
         */
        if (check_buildRevisionAlreadyUsed(brp.getApplication(), brp.getBuild(), brp.getRevision())) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME)
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "Could not update this release as corresponding build " + brp.getBuild() + " revision " + brp.getRevision() + " has already been deployed in an environment."));
            // "Could not update this release to this new build revision values as it has already been deployed in an environment."
            ans.setResultMessage(msg);
            return ans;
        }

        return buildRevisionParametersDAO.update(brp);
    }

    @Override
    public boolean check_buildRevisionAlreadyUsed(String application, String build, String revision) {
        try {
            // First set is to get the system value
            String system = "";
            system = applicationService.convert(applicationService.readByKey(application)).getSystem();

            // Then we check here inside countryenvparam_log table is the build revision has already been used.
            AnswerList resp = countryEnvParamLogService.readByVariousByCriteria(system, null, null, build, revision, 0, 0, "id", "asc", null, null);
            if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getTotalRows() > 0) {
                return true;
            } else {
                return false;
            }
        } catch (CerberusException ex) {
            LOG.error(ex, ex);
        }
        return true;
    }

    @Override
    public BuildRevisionParameters convert(AnswerItem<BuildRevisionParameters> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<BuildRevisionParameters> convert(AnswerList<BuildRevisionParameters> answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerList.getDataList();
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

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String system, String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return buildRevisionParametersDAO.readDistinctValuesByCriteria(system, searchParameter, individualSearch, columnName);
    }

}
