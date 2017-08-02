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
package org.cerberus.engine.threadpool.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import org.apache.log4j.Logger;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.engine.threadpool.entity.TestCaseExecutionQueueToTreat;
import org.cerberus.crud.service.IMyVersionService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.engine.threadpool.IExecutionThreadPoolService;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * {@link IExecutionThreadPoolService} default implementation
 *
 * @author bcivel
 * @author abourdon
 */
@Service
public class ExecutionThreadPoolService implements IExecutionThreadPoolService {

    private static final Logger LOG = Logger.getLogger(ExecutionThreadPoolService.class);

    private static final String CONST_SEPARATOR = "//";

    @Autowired
    private ITestCaseExecutionQueueService tceiqService;
    @Autowired
    private IParameterService parameterService;
    @Autowired
    private IInvariantService invariantService;
    @Autowired
    private IMyVersionService myVersionService;
    @Autowired
    ExecutionQueueThreadPool threadQueuePool;
    @Autowired
    private ITestCaseExecutionQueueService queueService;

    @Override
    public HashMap<String, Integer> getCurrentlyRunning() throws CerberusException {
        AnswerList answer = new AnswerList();
        HashMap<String, Integer> constrains_current = new HashMap<String, Integer>();

        // Getting all executions already running in the queue.
        answer = tceiqService.readQueueRunning();
        List<TestCaseExecutionQueueToTreat> executionsRunning = (List<TestCaseExecutionQueueToTreat>) answer.getDataList();
        // Calculate constrain values.
        for (TestCaseExecutionQueueToTreat exe : executionsRunning) {
            String const01_key = TestCaseExecutionQueueToTreat.CONSTRAIN1_GLOBAL;
            String const02_key = TestCaseExecutionQueueToTreat.CONSTRAIN2_APPLICATION + CONST_SEPARATOR + exe.getSystem() + CONST_SEPARATOR + exe.getEnvironment() + CONST_SEPARATOR + exe.getCountry() + CONST_SEPARATOR + exe.getApplication();
            String const03_key = TestCaseExecutionQueueToTreat.CONSTRAIN3_ROBOT + CONST_SEPARATOR + exe.getRobotHost();

            if (constrains_current.containsKey(const01_key)) {
                constrains_current.put(const01_key, constrains_current.get(const01_key) + 1);
            } else {
                constrains_current.put(const01_key, 1);
            }
            if (constrains_current.containsKey(const02_key)) {
                constrains_current.put(const02_key, constrains_current.get(const02_key) + 1);
            } else {
                constrains_current.put(const02_key, 1);
            }
            if (constrains_current.containsKey(const03_key)) {
                constrains_current.put(const03_key, constrains_current.get(const03_key) + 1);
            } else {
                constrains_current.put(const03_key, 1);
            }
        }
        return constrains_current;

    }

    @Override
    public HashMap<String, Integer> getCurrentlyPoolSizes() throws CerberusException {
        AnswerList answer = new AnswerList();
        HashMap<String, Integer> constrains_current = new HashMap<String, Integer>();

        String const01_key = TestCaseExecutionQueueToTreat.CONSTRAIN1_GLOBAL;
        int poolSizeGeneral = parameterService.getParameterIntegerByKey("cerberus_queueexecution_global_threadpoolsize", "", 12);
        int poolSizeRobot = parameterService.getParameterIntegerByKey("cerberus_queueexecution_defaultrobothost_threadpoolsize", "", 10);
        constrains_current.put(const01_key, poolSizeGeneral);

        // Getting RobotHost PoolSize
        HashMap<String, Integer> robot_poolsize = new HashMap<String, Integer>();
        robot_poolsize = invariantService.readToHashMapByIdname("ROBOTHOST", poolSizeRobot);

        // Getting all executions to be treated.
        answer = tceiqService.readQueueToTreat();
        List<TestCaseExecutionQueueToTreat> executionsToTreat = (List<TestCaseExecutionQueueToTreat>) answer.getDataList();
        // Calculate constrain values.
        for (TestCaseExecutionQueueToTreat exe : executionsToTreat) {
            String const02_key = TestCaseExecutionQueueToTreat.CONSTRAIN2_APPLICATION + CONST_SEPARATOR + exe.getSystem() + CONST_SEPARATOR + exe.getEnvironment() + CONST_SEPARATOR + exe.getCountry() + CONST_SEPARATOR + exe.getApplication();
            String const03_key = TestCaseExecutionQueueToTreat.CONSTRAIN3_ROBOT + CONST_SEPARATOR + exe.getRobotHost();

            constrains_current.put(const02_key, exe.getPoolSizeApplication());

            // Getting Robot Host PoolSize from invariant hashmap.
            int robot_poolsize_final = 0;
            if (robot_poolsize.containsKey(exe.getRobotHost())) {
                robot_poolsize_final = ParameterParserUtil.parseIntegerParam(robot_poolsize.get(exe.getRobotHost()), poolSizeRobot);
            } else {
                robot_poolsize_final = 0;
            }
            constrains_current.put(const03_key, robot_poolsize_final);
        }
        return constrains_current;

    }

    @Override
    public HashMap<String, Integer> getCurrentlyToTreat() throws CerberusException {
        AnswerList answer = new AnswerList();
        HashMap<String, Integer> constrains_current = new HashMap<String, Integer>();

        // Getting all executions to be treated.
        answer = tceiqService.readQueueToTreat();
        List<TestCaseExecutionQueueToTreat> executionsToTreat = (List<TestCaseExecutionQueueToTreat>) answer.getDataList();

        // Calculate constrain values.
        for (TestCaseExecutionQueueToTreat exe : executionsToTreat) {
            String const01_key = TestCaseExecutionQueueToTreat.CONSTRAIN1_GLOBAL;
            String const02_key = TestCaseExecutionQueueToTreat.CONSTRAIN2_APPLICATION + CONST_SEPARATOR + exe.getSystem() + CONST_SEPARATOR + exe.getEnvironment() + CONST_SEPARATOR + exe.getCountry() + CONST_SEPARATOR + exe.getApplication();
            String const03_key = TestCaseExecutionQueueToTreat.CONSTRAIN3_ROBOT + CONST_SEPARATOR + exe.getRobotHost();

            if (constrains_current.containsKey(const01_key)) {
                constrains_current.put(const01_key, constrains_current.get(const01_key) + 1);
            } else {
                constrains_current.put(const01_key, 1);
            }
            if (constrains_current.containsKey(const02_key)) {
                constrains_current.put(const02_key, constrains_current.get(const02_key) + 1);
            } else {
                constrains_current.put(const02_key, 1);
            }
            if (constrains_current.containsKey(const03_key)) {
                constrains_current.put(const03_key, constrains_current.get(const03_key) + 1);
            } else {
                constrains_current.put(const03_key, 1);
            }
        }
        return constrains_current;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeNextInQueue(boolean forceExecution) throws CerberusException {
        // We first check that another thread of Cerberus already trigger the job. Only 1 instance of the job is necessary.
        if (!(myVersionService.getMyVersionStringByKey("queueprocessingjobrunning", "N").equals("Y"))
                || forceExecution) {
            if (forceExecution) {
                LOG.debug("Forcing Start of Queue_Processing_Job.");
            }
            // Job is not already running, we can trigger it.

            LOG.debug("Starting Queue_Processing_Job.");

            // Flag in database that job is already running.
            myVersionService.UpdateMyVersionString("queueprocessingjobrunning", "Y");
            myVersionService.UpdateMyVersionString("queueprocessingjobstart", String.valueOf(new Date()));

            String cerberus_url = parameterService.getParameterStringByKey("cerberus_url", "", "");
            AnswerList answer = new AnswerList();

            // Getting all executions to be treated.
            answer = tceiqService.readQueueToTreat();
            List<TestCaseExecutionQueueToTreat> executionsInQueue = (List<TestCaseExecutionQueueToTreat>) answer.getDataList();

            int poolSizeGeneral = parameterService.getParameterIntegerByKey("cerberus_queueexecution_global_threadpoolsize", "", 12);
            int poolSizeRobot = parameterService.getParameterIntegerByKey("cerberus_queueexecution_defaultrobothost_threadpoolsize", "", 10);

            // Init constrain counter.
            int const01_current = 0;
            int const02_current = 0;
            int const03_current = 0;
            HashMap<String, Integer> constrains_current = new HashMap<String, Integer>();
            constrains_current = getCurrentlyRunning();

            // Getting RobotHost PoolSize
            HashMap<String, Integer> robot_poolsize = new HashMap<String, Integer>();
            robot_poolsize = invariantService.readToHashMapByIdname("ROBOTHOST", poolSizeRobot);

            // Analysing each execution in the database queue.
            int nbqueuedexe = 0;
            for (TestCaseExecutionQueueToTreat exe : executionsInQueue) {
                // Robot PoolSize if retreived from hashmap.
                int robot_poolsize_final = 0;
                if (robot_poolsize.containsKey(exe.getRobotHost())) {
                    robot_poolsize_final = ParameterParserUtil.parseIntegerParam(robot_poolsize.get(exe.getRobotHost()), poolSizeRobot);
                } else {
                    robot_poolsize_final = 0;
                }

                LOG.debug("Analysing Queue : " + exe.getId() + " poolGen " + poolSizeGeneral + " poolApp " + exe.getPoolSizeApplication() + " poolRobot " + robot_poolsize_final);

                String const01_key = TestCaseExecutionQueueToTreat.CONSTRAIN1_GLOBAL;
                String const02_key = TestCaseExecutionQueueToTreat.CONSTRAIN2_APPLICATION + CONST_SEPARATOR + exe.getSystem() + CONST_SEPARATOR + exe.getEnvironment() + CONST_SEPARATOR + exe.getCountry() + CONST_SEPARATOR + exe.getApplication();
                String const03_key = TestCaseExecutionQueueToTreat.CONSTRAIN3_ROBOT + CONST_SEPARATOR + exe.getRobotHost();

                // Eval Constrain 1
                if (constrains_current.containsKey(const01_key)) {
                    const01_current = constrains_current.get(const01_key);
                } else {
                    const01_current = 0;
                }
                // Eval Constrain 1
                boolean constMatch01;
                if (poolSizeGeneral == 0) {
                    // if poolsize == 0, this means no constrain specified.
                    constMatch01 = false;
                } else {
                    constMatch01 = (const01_current >= poolSizeGeneral);
                }

                // Eval Constrain 2
                if (constrains_current.containsKey(const02_key)) {
                    const02_current = constrains_current.get(const02_key);
                } else {
                    const02_current = 0;
                }
                // Eval Constrain 2
                boolean constMatch02;
                if (exe.getPoolSizeApplication() == 0) {
                    // if poolsize == 0, this means no constrain specified.
                    constMatch02 = false;
                } else {
                    constMatch02 = (const02_current >= exe.getPoolSizeApplication());
                }

                // Eval Constrain 3
                if (constrains_current.containsKey(const03_key)) {
                    const03_current = constrains_current.get(const03_key);
                } else {
                    const03_current = 0;
                }
                // Eval Constrain 3
                boolean constMatch03;
                if (robot_poolsize_final == 0) {
                    // if poolsize == 0, this means no constrain specified.
                    constMatch03 = false;
                } else {
                    constMatch03 = (const03_current >= robot_poolsize_final);
                }

                String notTriggeredExeMessage = "";
                boolean triggerExe = false;
                if ((!constMatch01 && !constMatch02 && !constMatch03)
                        || (!constMatch01 && exe.getManualExecution().equals("Y"))) {
                    // None of the constrains match or exe is manual so we can trigger the execution.

                    // Adding execution to queue.
                    if (queueService.updateToWaiting(exe.getId())) {
                        ExecutionQueueWorkerThread task = new ExecutionQueueWorkerThread();
                        task.setCerberusExecutionUrl(cerberus_url);
                        task.setQueueId(exe.getId());
                        task.setQueueService(queueService);
                        task.setExecThreadPool(threadQueuePool);
                        try {
                            Future<?> future = threadQueuePool.getExecutor().submit(task);
                            task.setFuture(future);
                        } catch (RejectedExecutionException e) {
                            System.out.println("RejectedExecutionException :" + e);
                        }

                        triggerExe = true;
                        nbqueuedexe++;

                        // Debug messages.
                        LOG.debug("result : " + triggerExe + " Const1 " + constMatch01 + " Const2 " + constMatch01 + " Const3 " + constMatch01 + " Manual " + exe.getManualExecution());
                        LOG.debug(" CurConst1 " + const01_current + " CurConst2 " + const02_current + " CurConst3 " + const03_current);

                        // Counter increase
                        constrains_current.put(const01_key, const01_current + 1);
                        if (!exe.getManualExecution().equals("Y")) {
                            // Specific increment only if automatic execution.
                            constrains_current.put(const02_key, const02_current + 1);
                            constrains_current.put(const03_key, const03_current + 1);
                        }

                    }

                } else {
                    if (constMatch03) {
                        notTriggeredExeMessage = "Robot contrain on '" + const03_key + "' reached. " + robot_poolsize_final + " Execution(s) poolsize reached";
                    }
                    if (constMatch02) {
                        notTriggeredExeMessage = "Application Environment contrain on '" + const02_key + "' reached . " + exe.getPoolSizeApplication() + " Execution(s) poolsize reached";
                    }
                    if (constMatch01) {
                        notTriggeredExeMessage = "Global contrain reached. " + poolSizeGeneral + " Execution(s) poolsize reached";
                    }
                    LOG.debug("result : " + triggerExe + " Const1 " + constMatch01 + " Const2 " + constMatch01 + " Const3 " + constMatch01 + " Manual " + exe.getManualExecution());
                    LOG.debug(" CurConst1 " + const01_current + " CurConst2 " + const02_current + " CurConst3 " + const03_current);
                    LOG.debug(" " + notTriggeredExeMessage);
                }
            }

            // Flag in database that job is finished.
            myVersionService.UpdateMyVersionString("queueprocessingjobrunning", "N");

            LOG.debug("Stoping Queue_Processing_Job - TOTAL Released execution(s) : " + nbqueuedexe);

        } else {
            LOG.debug("Queue_Processing_Job not triggered (already running.)");
        }
    }

}
