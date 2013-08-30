/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service.impl;

import com.redcats.tst.dao.IInvariantDAO;
import com.redcats.tst.entity.Invariant;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.service.IInvariantService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class InvariantService implements IInvariantService {

    @Autowired
    IInvariantDAO invariantDao;

    @Override
    public Invariant findInvariantByIdValue(String idName, String value) throws CerberusException {
        return invariantDao.findInvariantByIdValue(idName, value);
    }

    @Override
    public List<Invariant> findListOfInvariantById(String idName) throws CerberusException {
        return invariantDao.findListOfInvariantById(idName);
    }

    @Override
    public List<Invariant> findInvariantByIdGp1(String idName, String gp) throws CerberusException {
        return invariantDao.findInvariantByIdGp1(idName, gp);
    }
}
