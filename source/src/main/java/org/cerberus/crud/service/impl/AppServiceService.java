/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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

import java.util.List;
import java.util.Map;

import org.cerberus.crud.dao.impl.AppServiceDAO;
import org.cerberus.crud.entity.AppService;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.cerberus.crud.service.IAppServiceService;

/**
 *
 * @author cte
 */
@Service
public class AppServiceService implements IAppServiceService {

    @Autowired
    AppServiceDAO appServiceDao;

    @Override
    public AppService findAppServiceByKey(String name) throws CerberusException {
        return appServiceDao.findAppServiceByKey(name);
    }

    @Override
    public AnswerList readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return appServiceDao.readByCriteria(startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public AnswerItem readByKey(String key) {
        return appServiceDao.readByKey(key);
    }

    @Override
    public AnswerList readDistinctValuesByCriteria(String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return appServiceDao.readDistinctValuesByCriteria(searchParameter, individualSearch, columnName);
    }

    @Override
    public Answer create(AppService object) {
        return appServiceDao.create(object);
    }

    @Override
    public Answer update(AppService object) {
        return appServiceDao.update(object);
    }

    @Override
    public Answer delete(AppService object) {
        return appServiceDao.delete(object);
    }
}
