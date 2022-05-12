/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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

import lombok.AllArgsConstructor;
import org.apache.commons.fileupload.FileItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.api.exceptions.EntityNotFoundException;
import org.cerberus.api.exceptions.FailedInsertOperationException;
import org.cerberus.api.exceptions.InvalidRequestException;
import org.cerberus.crud.dao.IAppServiceDAO;
import org.cerberus.crud.entity.AppService;
import org.cerberus.crud.entity.AppServiceContent;
import org.cerberus.crud.entity.AppServiceHeader;
import org.cerberus.crud.service.IAppServiceContentService;
import org.cerberus.crud.service.IAppServiceHeaderService;
import org.cerberus.crud.service.IAppServiceService;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.JSONUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.XmlUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
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
    IAppServiceDAO appServiceDao;
    private IAppServiceContentService appServiceContentService;
    private IAppServiceHeaderService appServiceHeaderService;

    @Override
    public AppService findAppServiceByKey(String name) throws CerberusException {
        return appServiceDao.findAppServiceByKey(name);
    }

    @Override
    public AnswerList<AppService> readByLikeName(String name, int limit) {
        return appServiceDao.findAppServiceByLikeName(name, limit);
    }

    @Override
    public AnswerList<AppService> readByCriteria(
            int startPosition, int length, String columnName, String sort,
            String searchParameter, Map<String, List<String>> individualSearch, List<String> systems) {
        return appServiceDao.readByCriteria(startPosition, length, columnName, sort, searchParameter, individualSearch, systems);
    }

    @Override
    public AnswerItem<AppService> readByKey(String key) {
        return appServiceDao.readByKey(key);
    }

    @Override
    public AnswerItem<AppService> readByKeyWithDependency(String key, boolean withActiveCriteria, boolean activeDetail) {
        AnswerItem<AppService> answerAppService = this.readByKey(key);
        AppService appService = answerAppService.getItem();

        try {
            if (appService != null) {
                AnswerList<AppServiceContent> content = appServiceContentService.readByVarious(key, withActiveCriteria, activeDetail);
                if (content != null) {
                    appService.setContentList(content.getDataList());
                }
                AnswerList<AppServiceHeader> header = appServiceHeaderService.readByVarious(key, withActiveCriteria, activeDetail);
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
    public Answer create(AppService object) {
        return appServiceDao.create(object);
    }

    @Override
    public AppService createAPI(AppService newAppService) {
        if (newAppService.getService() == null || newAppService.getService().isEmpty()) {
            throw new InvalidRequestException("service is required to create an ApplicationService");
        }

        if (newAppService.getType() == null || newAppService.getType().isEmpty()) {
            throw new InvalidRequestException("type is required to create an ApplicationService");
        }

        if (newAppService.getMethod() == null || newAppService.getMethod().isEmpty()) {
            throw new InvalidRequestException("method is required to create an ApplicationService");
        }

        Answer answer = this.create(newAppService);
        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            return this.readByKey(newAppService.getService()).getItem();
        } else {
            throw new FailedInsertOperationException("Failed to insert the new application service in the database");
        }
    }

    @Override
    public Answer update(String service, AppService object) {
        return appServiceDao.update(service, object);
    }

    @Override
    public AppService updateAPI(String service, AppService appServiceToUpdate) {
        if (service == null || service.isEmpty()) {
            throw new InvalidRequestException("service is required to update an ApplicationService");
        }

        AppService appServiceFromDb = this.readByKey(service).getItem();
        if (appServiceFromDb == null) {
            throw new EntityNotFoundException(AppService.class, "service", service);
        }

        appServiceToUpdate.setService(appServiceFromDb.getService());
        Answer answer = this.update(service, appServiceToUpdate);
        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            return this.readByKey(service).getItem();
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

        if (service == null || StringUtil.isNullOrEmpty(service.getResponseHTTPBody())) {
            // Service is null so we don't know the format.
            return AppService.RESPONSEHTTPBODYCONTENTTYPE_UNKNOWN;
        }

        for (AppServiceHeader object : service.getResponseHeaderList()) {
            if ((object != null) && (object.getKey().equalsIgnoreCase("Content-Type"))) {
                if (object.getValue().contains("application/json")) {
                    LOG.debug("JSON format guessed from header : {} : {}", object.getKey(), object.getValue());
                    return AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON;
                } else if (object.getValue().contains("application/xml")) {
                    LOG.debug("XML format guessed from header : {} : {}", object.getKey(), object.getValue());
                    return AppService.RESPONSEHTTPBODYCONTENTTYPE_XML;
                }
            }
        }

        if (XmlUtil.isXmlWellFormed(service.getResponseHTTPBody())) {
            LOG.debug("XML format guessed from successful parsing.");
            return AppService.RESPONSEHTTPBODYCONTENTTYPE_XML;
        } else if (JSONUtil.isJSONValid(service.getResponseHTTPBody())) {
            LOG.debug("JSON format guessed from successful parsing.");
            return AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON;
        }

        // Header did not define the format and could not guess from file content.
        if (StringUtil.isNullOrEmpty(defaultValue)) {
            return AppService.RESPONSEHTTPBODYCONTENTTYPE_TXT;
        }
        return defaultValue;
    }

    @Override
    public String convertContentListToQueryString(List<AppServiceContent> serviceContent) {
        StringBuilder result = new StringBuilder();
        if (serviceContent == null || serviceContent.isEmpty()) {
            return result.toString();
        }

        for (AppServiceContent object : serviceContent) {
            if (object.isActive()) {
                result.append(object.getKey());
                result.append("=");
                result.append(object.getValue());
                result.append("&");
            }
        }
        result = new StringBuilder(StringUtil.removeLastChar(result.toString(), 1));
        return result.toString();
    }

    @Override
    public Answer uploadFile(String service, FileItem file) {
        return appServiceDao.uploadFile(service, file);
    }
}
