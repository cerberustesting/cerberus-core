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

import org.cerberus.crud.dao.impl.SoapLibraryDAO;
import org.cerberus.crud.entity.SoapLibrary;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ISoapLibraryService;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author cte
 */
@Service
public class SoapLibraryService implements ISoapLibraryService {

    @Autowired
    SoapLibraryDAO soapLibraryDao;

    @Override
    public SoapLibrary findSoapLibraryByKey(String name) throws CerberusException {
        return soapLibraryDao.findSoapLibraryByKey(name);
    }

    @Override
    public void createSoapLibrary(SoapLibrary soapLibrary) throws CerberusException {
        soapLibraryDao.createSoapLibrary(soapLibrary);
    }

    @Override
    public void updateSoapLibrary(SoapLibrary soapLibrary) throws CerberusException {
        soapLibraryDao.updateSoapLibrary(soapLibrary);
    }

    @Override
    public void deleteSoapLibrary(SoapLibrary soapLibrary) throws CerberusException {
        soapLibraryDao.deleteSoapLibrary(soapLibrary);
    }

    @Override
    public List<SoapLibrary> findAllSoapLibrary() {
        return soapLibraryDao.findAllSoapLibrary();
    }

    @Override
    public List<SoapLibrary> findSoapLibraryListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        return soapLibraryDao.findSoapLibraryListByCriteria(start, amount, column, dir, searchTerm, individualSearch);
    }

    @Override
    public void updateSoapLibrary(String name, String columnName, String value) throws CerberusException {
        soapLibraryDao.updateSoapLibrary(name, columnName, value);
    }

    @Override
    public Integer getNumberOfSoapLibraryPerCrtiteria(String searchTerm, String inds) {
        return soapLibraryDao.getNumberOfSoapLibraryPerCrtiteria(searchTerm, inds);
    }

    @Override
    public AnswerList readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return soapLibraryDao.readByCriteria(startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public AnswerItem readByKey(String key) {
        return soapLibraryDao.readByKey(key);
    }

    @Override
    public AnswerList readDistinctValuesByCriteria(String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return soapLibraryDao.readDistinctValuesByCriteria(searchParameter, individualSearch, columnName);
    }

    @Override
    public Answer create(SoapLibrary object) {
        return soapLibraryDao.create(object);
    }

    @Override
    public Answer update(SoapLibrary object) {
        return soapLibraryDao.update(object);
    }

    @Override
    public Answer delete(SoapLibrary object) {
        return soapLibraryDao.delete(object);
    }
}
