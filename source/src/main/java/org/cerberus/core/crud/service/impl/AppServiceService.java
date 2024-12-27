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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.exceptions.EntityNotFoundException;
import org.cerberus.core.api.exceptions.FailedInsertOperationException;
import org.cerberus.core.api.exceptions.InvalidRequestException;
import org.cerberus.core.crud.dao.IAppServiceDAO;
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.entity.AppServiceContent;
import org.cerberus.core.crud.entity.AppServiceHeader;
import org.cerberus.core.crud.service.IAppServiceContentService;
import org.cerberus.core.crud.service.IAppServiceHeaderService;
import org.cerberus.core.crud.service.IAppServiceService;
import org.cerberus.core.crud.service.ITestCaseStepActionService;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.JSONUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.XmlUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author cte
 */
@AllArgsConstructor
@Service
public class AppServiceService implements IAppServiceService {

    private static final Logger LOG = LogManager.getLogger(AppServiceService.class);
    private IAppServiceDAO appServiceDao;
    private IAppServiceContentService appServiceContentService;
    private IAppServiceHeaderService appServiceHeaderService;
    private ITestCaseStepActionService actionService;

    @Override
    public AppService findAppServiceByKey(String name) throws CerberusException {
        return appServiceDao.findAppServiceByKey(name);
    }

    @Override
    public AnswerList<AppService> readByLikeName(String name, int limit) {
        return appServiceDao.findAppServiceByLikeName(name, limit);
    }

    @Override
    public AnswerList<AppService> readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch, List<String> systems) {
        return appServiceDao.readByCriteria(startPosition, length, columnName, sort, searchParameter, individualSearch, systems);
    }

    @Override
    public AnswerItem<AppService> readByKey(String key) {
        return appServiceDao.readByKey(key);
    }

    @Override
    public AnswerItem<AppService> readByKeyWithDependency(String key) {
        AnswerItem<AppService> answerAppService = this.readByKey(key);
        AppService appService = answerAppService.getItem();

        try {
            if (appService != null) {
                AnswerList<AppServiceContent> content;
                // Add first the inherited values.
                if (!StringUtil.isEmptyOrNull(appService.getParentContentService())) {
                    content = appServiceContentService.readByVarious(appService.getParentContentService());
                    if (content != null) {
                        List<AppServiceContent> contentList = content.getDataList();
                        for (AppServiceContent appServiceContent : contentList) {
                            appServiceContent.setInherited(true);
                        }
                        appService.setContentList(content.getDataList());
                    }
                }
                // Add then the normal values.
                content = appServiceContentService.readByVarious(key);
                if (content != null) {
                    appService.addContentList(content.getDataList());
                }
                // Header List
                AnswerList<AppServiceHeader> header = appServiceHeaderService.readByVarious(key);
                if (header != null) {
                    appService.setHeaderList(header.getDataList());
                }
                answerAppService.setItem(appService);
            }
        } catch (Exception e) {
            LOG.error(e, e);
        }
        return answerAppService;
    }

    @Override
    public AnswerItem<AppService> readByKeyWithDependency(String key, boolean activeDetail) {
        AnswerItem<AppService> answerAppService = this.readByKey(key);
        AppService appService = answerAppService.getItem();

        try {
            if (appService != null) {
                AnswerList<AppServiceContent> content;
                // Add first the inherited values.
                if (StringUtil.isNotEmptyOrNull(appService.getParentContentService())) {
                    content = appServiceContentService.readByVarious(appService.getParentContentService(), activeDetail);
                    if (content != null) {
                        List<AppServiceContent> contentList = content.getDataList();
                        for (AppServiceContent appServiceContent : contentList) {
                            appServiceContent.setInherited(true);
                        }
                        appService.setContentList(content.getDataList());
                    }
                }
                // Add then the normal values.
                content = appServiceContentService.readByVarious(key, activeDetail);
                if (content != null) {
                    appService.addContentList(content.getDataList());
                }

                // Header List
                AnswerList<AppServiceHeader> header = appServiceHeaderService.readByVarious(key, activeDetail);
                if (header != null) {
                    appService.setHeaderList(header.getDataList());
                }
                answerAppService.setItem(appService);
            }
        } catch (Exception e) {
            LOG.error(e, e);
        }
        return answerAppService;
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return appServiceDao.readDistinctValuesByCriteria(searchParameter, individualSearch, columnName);
    }

    @Override
    public Integer getNbServices(List<String> systems) {
        return appServiceDao.getNbServices(systems);
    }
    
    @Override
    public Answer create(AppService object) {
        return appServiceDao.create(object);
    }

    @Override
    public AppService createAPI(AppService newAppService, String login) {
        if (newAppService.getService() == null || newAppService.getService().isEmpty()) {
            throw new InvalidRequestException("service is required to create an ApplicationService");
        }

        if (newAppService.getType() == null || newAppService.getType().isEmpty()) {
            throw new InvalidRequestException("type is required to create an ApplicationService");
        }

        if (newAppService.getMethod() == null || newAppService.getMethod().isEmpty()) {
            throw new InvalidRequestException("method is required to create an ApplicationService");
        }
        newAppService.setUsrCreated(login);
        Answer answer = this.create(newAppService);

        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            if (CollectionUtils.isNotEmpty(newAppService.getContentList())) {
                newAppService.getContentList().forEach(appServiceContent -> {
                    if (StringUtil.isEmptyOrNull(appServiceContent.getKey())) {
                        throw new InvalidRequestException("A key is required for each ServiceContent");
                    }
                    appServiceContent.setUsrCreated(newAppService.getUsrCreated() == null ? "" : newAppService.getUsrCreated());
                    appServiceContent.setService(newAppService.getService());
                });

                Answer answerContent = this.appServiceContentService.createList(newAppService.getContentList());
                if (!answerContent.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    throw new FailedInsertOperationException("Failed to insert the content list in the database");
                }
            }

            if (CollectionUtils.isNotEmpty(newAppService.getHeaderList())) {
                newAppService.getHeaderList().forEach(appServiceHeader -> {
                    if (StringUtil.isEmptyOrNull(appServiceHeader.getKey())) {
                        throw new InvalidRequestException("A key is required for each ServiceHeader");
                    }
                    appServiceHeader.setUsrCreated(newAppService.getUsrCreated() == null ? "" : newAppService.getUsrCreated());
                    appServiceHeader.setService(newAppService.getService());
                });

                Answer answerHeader = this.appServiceHeaderService.createList(newAppService.getHeaderList());
                if (!answerHeader.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    throw new FailedInsertOperationException("Failed to insert the content list in the database");
                }
            }

            return this.readByKeyWithDependency(newAppService.getService()).getItem();
        } else {
            throw new FailedInsertOperationException("Failed to insert the new application service in the database");
        }

    }

    @Override
    public Answer update(String originalService, AppService object) {
        Answer resp = appServiceDao.update(originalService, object);
        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())
                && originalService != null
                && !originalService.equals(object.getService())) {
            try {
                // Key is modified, we update all testcase actions that call that service
                actionService.updateService(originalService, object.getService());
            } catch (CerberusException ex) {
                LOG.warn(ex, ex);
            }
        }
        return resp;
    }

    @Override
    public AppService updateAPI(String service, AppService appServiceToUpdate, String login) {
        if (service == null || service.isEmpty()) {
            throw new InvalidRequestException("service is required to update an ApplicationService");
        }

        AppService appServiceFromDb = this.readByKey(service).getItem();
        if (appServiceFromDb == null) {
            throw new EntityNotFoundException(AppService.class, "service", service);
        }

        appServiceToUpdate.setService(appServiceFromDb.getService());
        appServiceToUpdate.setUsrModif(login);
        if (appServiceToUpdate.getUsrModif() == null) {
            appServiceToUpdate.setUsrModif("");
        }
        Answer answerService = this.update(service, appServiceToUpdate);
        if (answerService.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            appServiceToUpdate.getHeaderList().forEach(appServiceHeader -> appServiceHeader.setUsrModif(appServiceToUpdate.getUsrModif()));
            appServiceToUpdate.getHeaderList().forEach(appServiceHeader -> appServiceHeader.setUsrCreated(appServiceToUpdate.getUsrModif()));
            Answer answerHeader = this.appServiceHeaderService.compareListAndUpdateInsertDeleteElements(service, appServiceToUpdate.getHeaderList());
            if (!answerHeader.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                throw new FailedInsertOperationException("Unable to update service headers for service=" + service);
            }
            appServiceToUpdate.getContentList().forEach(appServiceContent -> appServiceContent.setUsrModif(appServiceToUpdate.getUsrModif()));
            appServiceToUpdate.getContentList().forEach(appServiceContent -> appServiceContent.setUsrCreated(appServiceToUpdate.getUsrModif()));
            Answer answerContent = this.appServiceContentService.compareListAndUpdateInsertDeleteElements(service, appServiceToUpdate.getContentList());
            if (!answerContent.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                throw new FailedInsertOperationException("Unable to update service contents for service=" + service);
            }
            return this.readByKeyWithDependency(service).getItem();
        } else {
            throw new FailedInsertOperationException("Unable to update service for service=" + service);
        }
    }

    @Override
    public Answer delete(AppService object) {
        return appServiceDao.delete(object);
    }

    @Override
    public AppService convert(AnswerItem<AppService> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<AppService> convert(AnswerList<AppService> answerList) throws CerberusException {
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
    public String guessContentType(AppService service, String defaultValue) {

        // If service is null, Type is not defined.
        if (service == null || StringUtil.isEmptyOrNull(service.getResponseHTTPBody())) {
            // Service is null so we don't know the format.
            return AppService.RESPONSEHTTPBODYCONTENTTYPE_UNKNOWN;
        }

        // We guess type from Header first.
        for (AppServiceHeader object : service.getResponseHeaderList()) {
            if ((object != null) && (object.getKey().equalsIgnoreCase("Content-Type"))) {
                if (object.getValue().contains("application/json")) {
                    LOG.debug("JSON format guessed from header : {} : {}", object.getKey(), object.getValue());
                    return AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON;
                } else if (object.getValue().contains("application/xml")) {
                    LOG.debug("XML format guessed from header : {} : {}", object.getKey(), object.getValue());
                    return AppService.RESPONSEHTTPBODYCONTENTTYPE_XML;
                } else if (object.getValue().contains("text/html")) {
                    LOG.debug("HTML format guessed from header : {} : {}", object.getKey(), object.getValue());
                    return AppService.RESPONSEHTTPBODYCONTENTTYPE_HTML;
                } else if (object.getValue().contains("text/csv")) {
                    LOG.debug("CSV format guessed from header : {} : {}", object.getKey(), object.getValue());
                    return AppService.RESPONSEHTTPBODYCONTENTTYPE_CSV;
                }
            }
        }

        // We guess type from the content.
        if (JSONUtil.isJSONValid(service.getResponseHTTPBody())) {
            LOG.debug("JSON format guessed from successful parsing.");
            return AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON;
        } else if (XmlUtil.isXmlWellFormed(service.getResponseHTTPBody())) {
            LOG.debug("XML format guessed from successful parsing.");
            return AppService.RESPONSEHTTPBODYCONTENTTYPE_XML;
        }

        // Header did not define the format and could not guess from file content.
        if (StringUtil.isEmptyOrNull(defaultValue)) {
            LOG.debug("Format guessed to value : " + AppService.RESPONSEHTTPBODYCONTENTTYPE_TXT + " (No default value defined)");
            return AppService.RESPONSEHTTPBODYCONTENTTYPE_TXT;
        }

        LOG.debug("Format guessed returned to default value : " + defaultValue);
        return defaultValue;
    }

    @Override
    public String guessContentType(String content) {
        if (StringUtil.isEmptyOrNull(content)) {
            // Service is null so we don't know the format.
            return null;
        }

        if (JSONUtil.isJSONValid(content)) {
            LOG.debug("JSON format guessed from successful parsing.");
            return AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON;
        } else if (XmlUtil.isXmlWellFormed(content)) {
            LOG.debug("XML format guessed from successful parsing.");
            return AppService.RESPONSEHTTPBODYCONTENTTYPE_XML;
        }
        return null;
    }

    @Override
    public String convertContentListToQueryString(List<AppServiceContent> serviceContent, boolean encodeParameters) {
        StringBuilder result = new StringBuilder();
        if (serviceContent == null || serviceContent.isEmpty()) {
            return result.toString();
        }

        try {

            for (AppServiceContent object : serviceContent) {
                if (object.isActive()) {
                    if (encodeParameters) {
                        result.append(URLEncoder.encode(object.getKey(), StandardCharsets.UTF_8.toString()));
                    } else {
                        result.append(object.getKey());
                    }

                    result.append("=");

                    if (encodeParameters) {
                        result.append(URLEncoder.encode(object.getValue(), StandardCharsets.UTF_8.toString()));
                    } else {
                        result.append(object.getValue());
                    }

                    result.append("&");
                }
            }

        } catch (UnsupportedEncodingException ex) {
            LOG.error(ex, ex);
        }
        result = new StringBuilder(StringUtil.removeLastChar(result.toString()));
        return result.toString();
    }

    @Override
    public Answer uploadFile(String service, FileItem file) {
        return appServiceDao.uploadFile(service, file);
    }
}
