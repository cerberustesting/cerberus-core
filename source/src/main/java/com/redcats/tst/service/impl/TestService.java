package com.redcats.tst.service.impl;

import com.redcats.tst.dao.ITestCaseStepActionDAO;
import com.redcats.tst.dao.ITestCaseStepBatchDAO;
import com.redcats.tst.dao.ITestCaseStepDAO;
import com.redcats.tst.dao.ITestDAO;
import com.redcats.tst.entity.Test;
import com.redcats.tst.service.ITestCaseCountryPropertiesService;
import com.redcats.tst.service.ITestCaseStepActionControlService;
import com.redcats.tst.service.ITestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 07/01/2013
 * @since 2.0.0
 */
@Service
public class TestService implements ITestService {

    @Autowired
    private ITestDAO testDAO;
    @Autowired
    private ITestCaseStepDAO testCaseStepDAO;
    @Autowired
    private ITestCaseStepBatchDAO testCaseStepBatchDAO;
    @Autowired
    private ITestCaseStepActionDAO testCaseStepActionDAO;
    @Autowired
    private ITestCaseStepActionControlService testCaseStepActionControlService;
    @Autowired
    private ITestCaseCountryPropertiesService testCaseCountryPropertiesService;


    @Override
    public List<String> getListOfTests() {
        List<String> result = new ArrayList<String>();
        List<Test> listOfTests = this.testDAO.findAllTest();

        for (Test lot : listOfTests) {
            result.add(lot.getTest());
        }

        return result;
    }

    @Override
    public List<Test> getListOfTest() {
        List<Test> result = new ArrayList();
        result = testDAO.findAllTest();
        return result;
    }


}
