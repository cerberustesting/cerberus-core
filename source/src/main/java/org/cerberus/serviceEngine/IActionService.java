/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cerberus.serviceEngine;

import org.cerberus.entity.TestCaseStepActionExecution;

/**
 *
 * @author bcivel
 */
public interface IActionService {
    
    TestCaseStepActionExecution doAction(TestCaseStepActionExecution testCaseStepActionExecution);
}
