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
package org.cerberus.crud.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.cerberus.crud.entity.AppService;
import org.cerberus.crud.entity.AppServiceContent;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author cte
 */
public interface IAppServiceService {

    /**
     * Get the {@link AppService} List with the given Criteria
     *
     * @param name the name of the service
     * @param limit the number of {@link AppService} to get
     * @return
     */
    AnswerList<AppService> readByLikeName(String name, int limit);

    AppService findAppServiceByKey(String name) throws CerberusException;

    /**
     * Get the {@link AppService} List of the given {@link System} with the
     * given Criteria
     *
     * @param startPosition the start index to look for
     * @param length the number of {@link AppService} to get
     * @param columnName the Column name to sort
     * @param sort
     * @param searchParameter the string to search in the {@link AppService}
     * @param individualSearch the string to search for each column
     * @param systems
     * @return
     */
    AnswerList<AppService> readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch, List<String> systems);

    /**
     * Get the {@link AppService} of the given key
     *
     * @param key the key of the {@link AppService} to get
     * @return
     */
    AnswerItem<AppService> readByKey(String key);

    /**
     * Get the {@link AppService} of the given key
     *
     * @param key the key of the {@link AppService} to get
     * @param activeDetail Y will load detail only with Active data on header
     * and content. null wil load all data.
     * @return
     */
    AnswerItem<AppService> readByKeyWithDependency(String key, String activeDetail);

    /**
     * Get the distinctValue of the column
     *
     * @param columnName the Column name to get
     * @param searchParameter the string to search in the {@link AppService}
     * @param individualSearch the string to search for each column
     * @return
     */
    AnswerList<String> readDistinctValuesByCriteria(String searchParameter, Map<String, List<String>> individualSearch, String columnName);

    /**
     * @param object the {@link AppService} to Create
     * @return {@link AnswerItem}
     */
    Answer create(AppService object);

    /**
     * @param service
     * @param object the {@link AppService} to Update
     * @return {@link AnswerItem}
     */
    Answer update(String service, AppService object);

    /**
     * @param object the {@link AppService} to Delete
     * @return {@link AnswerItem}
     */
    Answer delete(AppService object);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    AppService convert(AnswerItem<AppService> answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<AppService> convert(AnswerList<AppService> answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

    /**
     * This method return the Content-type of the service. Content Type is
     * calculated from the responseHeaderList or responseHTTPBody itself.
     *
     * @param service
     * @param defaultValue
     * @return
     */
    String guessContentType(AppService service, String defaultValue);

    /**
     * This method convert the list of active content service (key/value) to
     * query string (ex : key1=value1&key2=value2&key3=value3
     *
     * @param serviceContent
     * @return
     */
    String convertContentListToQueryString(List<AppServiceContent> serviceContent);

    /**
     * this method will store local file into application server
     *
     * @param service
     * @param file
     * @return
     */
    Answer uploadFile(String service, FileItem file);

}
