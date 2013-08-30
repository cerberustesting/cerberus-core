/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.TCExecution;
import com.redcats.tst.entity.TCase;
import com.redcats.tst.entity.TestCaseStep;
import java.util.List;

/**
 * @author bcivel
 */
public interface ILoadTestCaseService {

    void loadTestCase(TCExecution tCExecution);

    List<TestCaseStep> loadTestCaseStep(TCase testCase);
        
    
}
