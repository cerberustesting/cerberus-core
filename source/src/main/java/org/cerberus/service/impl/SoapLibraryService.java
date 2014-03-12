/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cerberus.service.impl;

import java.util.List;
import org.cerberus.dao.impl.SoapLibraryDAO;
import org.cerberus.entity.SoapLibrary;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.ISoapLibraryService;
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
}
