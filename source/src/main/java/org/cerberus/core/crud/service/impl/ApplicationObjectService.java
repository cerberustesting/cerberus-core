/**
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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

import org.cerberus.core.api.exceptions.EntityNotFoundException;
import org.cerberus.core.api.exceptions.FailedInsertOperationException;
import org.cerberus.core.api.exceptions.InvalidRequestException;
import org.cerberus.core.util.StringUtil;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.Part;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.IApplicationObjectDAO;
import org.cerberus.core.crud.entity.ApplicationObject;
import org.cerberus.core.crud.service.IApplicationObjectService;
import org.cerberus.core.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.crud.service.ITestCaseStepActionControlService;
import org.cerberus.core.crud.service.ITestCaseStepActionService;
import org.cerberus.core.crud.service.ITestCaseStepService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author foudro
 */
@Service
public class ApplicationObjectService implements IApplicationObjectService {

    @Autowired
    private IApplicationObjectDAO applicationObjectDAO;
    @Autowired
    private ITestCaseStepActionService actionService;
    @Autowired
    private ITestCaseStepActionControlService controlService;
    @Autowired
    private ITestCaseStepService stepService;
    @Autowired
    private ITestCaseService testcaseService;
    @Autowired
    private ITestCaseCountryPropertiesService propertiesService;

    private static final Logger LOG = LogManager.getLogger("ApplicationObjectService");

    private final String OBJECT_NAME = "ApplicationObject";

    @Override
    public AnswerItem<ApplicationObject> readByKeyTech(int id) {
        return applicationObjectDAO.readByKeyTech(id);
    }

    @Override
    public AnswerItem<ApplicationObject> readByKey(String application, String object) {
        return applicationObjectDAO.readByKey(application, object);
    }

    @Override
    public AnswerList<ApplicationObject> readByApplication(String Application) {
        return applicationObjectDAO.readByApplication(Application);
    }

    @Override
    public Answer uploadFile(int id, Part filePart) {
        return applicationObjectDAO.uploadFile(id, filePart);
    }

    @Override
    public AnswerList<ApplicationObject> readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return applicationObjectDAO.readByCriteria(startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public AnswerList<ApplicationObject> readByApplicationByCriteria(String application, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch, List<String> systems) {
        return applicationObjectDAO.readByApplicationByCriteria(application, startPosition, length, columnName, sort, searchParameter, individualSearch, systems);
    }

    @Override
    public BufferedImage readImageByKey(String application, String object) {
        return applicationObjectDAO.readImageByKey(application, object);
    }

    @Override
    public Answer create(ApplicationObject object) {
        return applicationObjectDAO.create(object);
    }

    @Override
    public Answer delete(ApplicationObject object) {
        return applicationObjectDAO.delete(object);
    }

    @Override
    public Answer update(String originalApplication, String originalObject, ApplicationObject object) {
        Answer resp = applicationObjectDAO.update(originalApplication, originalObject, object);
        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            if (originalObject != null && !originalObject.equals(object.getObject())) {
                actionService.updateApplicationObject(originalApplication, originalObject, object.getObject());
                controlService.updateApplicationObject(originalApplication, originalObject, object.getObject());
                stepService.updateApplicationObject(originalApplication, originalObject, object.getObject());
                testcaseService.updateApplicationObject(originalApplication, originalObject, object.getObject());
                propertiesService.updateApplicationObject(originalApplication, originalObject, object.getObject());
            }
        }
        return resp;
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return applicationObjectDAO.readDistinctValuesByCriteria(searchParameter, individualSearch, columnName);
    }

    @Override
    public AnswerList<String> readDistinctValuesByApplicationByCriteria(String Application, String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return applicationObjectDAO.readDistinctValuesByApplicationByCriteria(Application, searchParameter, individualSearch, columnName);
    }

    // ---- Methods used by the public API



    @Override
    public List<ApplicationObject> readByApplicationAPI(String application) {
        AnswerList<ApplicationObject> answer = this.readByApplication(application);
        return answer.getDataList() == null ? List.of() : answer.getDataList();
    }

    @Override
    public ApplicationObject readByKeyAPI(String application, String object) {
        AnswerItem<ApplicationObject> answer = this.readByKey(application, object);
        if (answer.getItem() == null) {
            throw new EntityNotFoundException(ApplicationObject.class, "application", application, "object", object);
        }
        return answer.getItem();
    }

    @Override
    public ApplicationObject createAPI(String application, ApplicationObject newObject, String login) {
        if (StringUtil.isEmptyOrNull(newObject.getObject())) {
            throw new InvalidRequestException("Field 'object' is mandatory");
        }
        // Application in the URL wins, a body value pointing elsewhere is refused rather than ignored.
        if (!StringUtil.isEmptyOrNull(newObject.getApplication()) && !application.equals(newObject.getApplication())) {
            throw new InvalidRequestException("Field 'application' does not match the application of the URL");
        }
        if (this.readByKey(application, newObject.getObject()).getItem() != null) {
            throw new InvalidRequestException("Application object already exists: application=" + application + " object=" + newObject.getObject());
        }
        newObject.setApplication(application);
        newObject.setValue(nullToEmpty(newObject.getValue()));
        newObject.setScreenshotFilename(nullToEmpty(newObject.getScreenshotFilename()));
        newObject.setXOffset(nullToEmpty(newObject.getXOffset()));
        newObject.setYOffset(nullToEmpty(newObject.getYOffset()));
        newObject.setUsrCreated(login);
        newObject.setUsrModif(login);
        check(this.create(newObject));
        return readByKeyAPI(application, newObject.getObject());
    }

    @Override
    public ApplicationObject updateAPI(String application, String object, ApplicationObject incoming, String login, boolean patch) {
        ApplicationObject existing = readByKeyAPI(application, object);

        // The application is part of the key and cannot be changed, only the object name can be renamed.
        if (!StringUtil.isEmptyOrNull(incoming.getApplication()) && !application.equals(incoming.getApplication())) {
            throw new InvalidRequestException("Field 'application' cannot be changed");
        }
        String newName = StringUtil.isEmptyOrNull(incoming.getObject()) ? object : incoming.getObject();
        if (!newName.equals(object) && this.readByKey(application, newName).getItem() != null) {
            throw new InvalidRequestException("Application object already exists: application=" + application + " object=" + newName);
        }

        existing.setObject(newName);
        existing.setValue(pick(incoming.getValue(), existing.getValue(), patch));
        existing.setXOffset(pick(incoming.getXOffset(), existing.getXOffset(), patch));
        existing.setYOffset(pick(incoming.getYOffset(), existing.getYOffset(), patch));
        // The screenshot is managed by the upload, it is only touched when explicitly provided.
        if (incoming.getScreenshotFilename() != null) {
            existing.setScreenshotFilename(incoming.getScreenshotFilename());
        }
        existing.setUsrModif(login);

        check(this.update(application, object, existing));
        return readByKeyAPI(application, newName);
    }

    @Override
    public void deleteAPI(String application, String object) {
        check(this.delete(readByKeyAPI(application, object)));
    }

    private static String pick(String incoming, String existing, boolean patch) {
        if (incoming != null) {
            return incoming;
        }
        return patch ? existing : "";
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static void check(Answer answer) {
        if (!answer.isCodeStringEquals("OK")) {
            throw new FailedInsertOperationException(answer.getMessageDescription());
        }
    }
}
