/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.refactor;


import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TCEwwwSumServiceImpl implements ITCEwwwSumService {

    @Autowired
    ITCEwwwSumDAO tCEwwwSumDAO;

    @Override
    public List<TestcaseExecutionwwwSum> getAllDetailsFromTCEwwwSum(int id) {
        return tCEwwwSumDAO.getAllDetailsFromTCEwwwSum(id);
    }
}
