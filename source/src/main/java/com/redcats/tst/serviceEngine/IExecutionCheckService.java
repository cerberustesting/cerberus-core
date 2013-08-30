package com.redcats.tst.serviceEngine;

import com.redcats.tst.entity.MessageGeneral;
import com.redcats.tst.entity.TCExecution;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 15/01/2013
 * @since 2.0.0
 */
public interface IExecutionCheckService {

    MessageGeneral checkTestCaseExecution(TCExecution tCExecution);
}
