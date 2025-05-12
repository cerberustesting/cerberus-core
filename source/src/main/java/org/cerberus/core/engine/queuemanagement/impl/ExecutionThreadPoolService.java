/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.engine.queuemanagement.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.Robot;
import org.cerberus.core.crud.entity.RobotExecutor;
import org.cerberus.core.crud.factory.IFactoryQueueStat;
import org.cerberus.core.crud.factory.IFactoryRobotExecutor;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.engine.execution.IRetriesService;
import org.cerberus.core.engine.queuemanagement.entity.TestCaseExecutionQueueToTreat;
import org.cerberus.core.crud.service.IMyVersionService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.IQueueStatService;
import org.cerberus.core.crud.service.IRobotExecutorService;
import org.cerberus.core.crud.service.IRobotService;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueDepService;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.core.engine.entity.ExecutionUUID;
import org.cerberus.core.engine.queuemanagement.IExecutionThreadPoolService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.service.authentification.impl.APIKeyService;
import org.cerberus.core.servlet.zzpublic.ManageV001;
import org.cerberus.core.session.SessionCounter;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.websocket.QueueStatus;
import org.cerberus.core.websocket.QueueStatusWebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * {@link IExecutionThreadPoolService} default implementation
 *
 * @author bcivel
 * @author abourdon
 */
@Service
public class ExecutionThreadPoolService implements IExecutionThreadPoolService {

    private static final Logger LOG = LogManager.getLogger(ExecutionThreadPoolService.class);

    private static final String CONST_SEPARATOR = "////";

    private boolean isInstanceActive = true;
    private boolean isSplashPageActive = false;

    @Autowired
    private ITestCaseExecutionQueueService tceiqService;
    @Autowired
    private IParameterService parameterService;
    @Autowired
    private SessionCounter sessionCounter;
    @Autowired
    private IInvariantService invariantService;
    @Autowired
    private IMyVersionService myVersionService;
    @Autowired
    private ExecutionUUID executionUUIDObject;
    @Autowired
    ExecutionQueueThreadPool threadQueuePool;
    @Autowired
    private ITestCaseExecutionQueueService queueService;
    @Autowired
    private APIKeyService apiKeyService;
    @Autowired
    private ITestCaseExecutionQueueDepService queueDepService;
    @Autowired
    private IRetriesService retriesService;
    @Autowired
    private ITagService tagService;
    @Autowired
    private IRobotExecutorService robotExecutorService;
    @Autowired
    private IRobotService robotService;
    @Autowired
    private IFactoryRobotExecutor factoryRobotExecutor;
    @Autowired
    private IFactoryQueueStat factoryQueueStat;
    @Autowired
    private IQueueStatService queueStatService;
    @Autowired
    private QueueStatusWebSocket queueStatusWebSocket;

    @Override
    public boolean isInstanceActive() {
        return isInstanceActive;
    }

    @Override
    public void setInstanceActive(boolean isInstanceActive) {
        this.isInstanceActive = isInstanceActive;
    }

    @Override
    public boolean isSplashPageActive() {
        return this.isSplashPageActive;
    }

    @Override
    public void setSplashPageActive(boolean isSplashPageActive) {
        this.isSplashPageActive = isSplashPageActive;
    }

    @Override
    public HashMap<String, Integer> getCurrentlyRunning() throws CerberusException {
        AnswerList<TestCaseExecutionQueueToTreat> answer = new AnswerList<>();
        HashMap<String, Integer> constrains_current = new HashMap<>();

        // Getting all executions already running in the queue.
        answer = tceiqService.readQueueRunning();
        List<TestCaseExecutionQueueToTreat> executionsRunning = answer.getDataList();
        // Calculate constrain values.
        for (TestCaseExecutionQueueToTreat exe : executionsRunning) {
            String const01_key = TestCaseExecutionQueueToTreat.CONSTRAIN1_GLOBAL;
            String const02_key = TestCaseExecutionQueueToTreat.CONSTRAIN2_APPLIENV + CONST_SEPARATOR + exe.getSystem() + CONST_SEPARATOR + exe.getEnvironment() + CONST_SEPARATOR + exe.getCountry() + CONST_SEPARATOR + exe.getApplication();
            String const03_key = TestCaseExecutionQueueToTreat.CONSTRAIN3_APPLICATION + CONST_SEPARATOR + exe.getApplication();
            String const04_key = TestCaseExecutionQueueToTreat.CONSTRAIN4_ROBOT + CONST_SEPARATOR + exe.getSelectedRobotHost();
            String const05_key = TestCaseExecutionQueueToTreat.CONSTRAIN5_EXECUTOREXTENSION + CONST_SEPARATOR + exe.getSelectedRobotExtensionHost();

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
            if (constrains_current.containsKey(const04_key)) {
                constrains_current.put(const04_key, constrains_current.get(const04_key) + 1);
            } else {
                constrains_current.put(const04_key, 1);
            }
            if (constrains_current.containsKey(const05_key)) {
                constrains_current.put(const05_key, constrains_current.get(const05_key) + 1);
            } else {
                constrains_current.put(const05_key, 1);
            }
        }
        return constrains_current;

    }

    @Override
    public HashMap<String, Integer> getCurrentlyPoolSizes() throws CerberusException {
        AnswerList<TestCaseExecutionQueueToTreat> answer = new AnswerList<>();
        HashMap<String, Integer> constrains_current = new HashMap<>();

        String const01_key = TestCaseExecutionQueueToTreat.CONSTRAIN1_GLOBAL;
        int poolSizeGeneral = parameterService.getParameterIntegerByKey("cerberus_queueexecution_global_threadpoolsize", "", 12);
        int poolSizeRobot = parameterService.getParameterIntegerByKey("cerberus_queueexecution_defaultrobothost_threadpoolsize", "", 10);
        int poolSizeExecutorExt = parameterService.getParameterIntegerByKey("cerberus_queueexecution_defaultexecutorexthost_threadpoolsize", "", 2);
        constrains_current.put(const01_key, poolSizeGeneral);

        // Getting RobotHost PoolSize
        HashMap<String, Integer> robot_poolsize = new HashMap<>();
        robot_poolsize = invariantService.readToHashMapGp1IntegerByIdname("ROBOTHOST", poolSizeRobot);
        HashMap<String, Integer> robotext_poolsize = new HashMap<>();
        robotext_poolsize = invariantService.readToHashMapGp1IntegerByIdname("ROBOTPROXYHOST", poolSizeExecutorExt);

        // Getting all executions to be treated.
        answer = tceiqService.readQueueToTreatOrRunning();
        List<TestCaseExecutionQueueToTreat> executionsToTreat = answer.getDataList();
        // Calculate constrain values.
        for (TestCaseExecutionQueueToTreat exe : executionsToTreat) {
            String const02_key = TestCaseExecutionQueueToTreat.CONSTRAIN2_APPLIENV + CONST_SEPARATOR + exe.getSystem() + CONST_SEPARATOR + exe.getEnvironment() + CONST_SEPARATOR + exe.getCountry() + CONST_SEPARATOR + exe.getApplication();
            String const03_key = TestCaseExecutionQueueToTreat.CONSTRAIN3_APPLICATION + CONST_SEPARATOR + exe.getApplication();
            String const04_key = TestCaseExecutionQueueToTreat.CONSTRAIN4_ROBOT + CONST_SEPARATOR + exe.getSelectedRobotHost();
            String const05_key = TestCaseExecutionQueueToTreat.CONSTRAIN5_EXECUTOREXTENSION + CONST_SEPARATOR + exe.getSelectedRobotExtensionHost();

            constrains_current.put(const02_key, exe.getPoolSizeAppEnvironment());

            constrains_current.put(const03_key, exe.getPoolSizeApplication());

            // Getting Robot Host PoolSize from invariant hashmap.
            int robot_poolsize_final = 0;
            if (!StringUtil.isEmptyOrNull(exe.getSelectedRobotHost())) {
                if (robot_poolsize.containsKey(exe.getSelectedRobotHost())) {
                    robot_poolsize_final = ParameterParserUtil.parseIntegerParam(robot_poolsize.get(exe.getSelectedRobotHost()), poolSizeRobot);
                } else {
                    robot_poolsize_final = poolSizeRobot;
                }
            }
            constrains_current.put(const04_key, robot_poolsize_final);

            // Getting Robot Host PoolSize from invariant hashmap.
            int robotext_poolsize_final = 0;
            if (!StringUtil.isEmptyOrNull(exe.getSelectedRobotExtensionHost())) {
                if (robotext_poolsize.containsKey(exe.getSelectedRobotExtensionHost())) {
                    robotext_poolsize_final = ParameterParserUtil.parseIntegerParam(robotext_poolsize.get(exe.getSelectedRobotExtensionHost()), poolSizeExecutorExt);
                } else {
                    robotext_poolsize_final = poolSizeExecutorExt;
                }
            }
            constrains_current.put(const05_key, robotext_poolsize_final);

        }
        return constrains_current;

    }

    @Override
    public HashMap<String, Integer> getCurrentlyToTreat() throws CerberusException {
        AnswerList<TestCaseExecutionQueueToTreat> answer = new AnswerList<>();
        HashMap<String, Integer> constrains_current = new HashMap<>();

        // Getting all executions to be treated.
        answer = tceiqService.readQueueToTreat();
        List<TestCaseExecutionQueueToTreat> executionsToTreat = answer.getDataList();

        // Calculate constrain values.
        for (TestCaseExecutionQueueToTreat exe : executionsToTreat) {
            String const01_key = TestCaseExecutionQueueToTreat.CONSTRAIN1_GLOBAL;
            String const02_key = TestCaseExecutionQueueToTreat.CONSTRAIN2_APPLIENV + CONST_SEPARATOR + exe.getSystem() + CONST_SEPARATOR + exe.getEnvironment() + CONST_SEPARATOR + exe.getCountry() + CONST_SEPARATOR + exe.getApplication();
            String const03_key = TestCaseExecutionQueueToTreat.CONSTRAIN3_APPLICATION + CONST_SEPARATOR + exe.getApplication();
            String const04_key = TestCaseExecutionQueueToTreat.CONSTRAIN4_ROBOT + CONST_SEPARATOR + exe.getQueueRobotHost();
            String const05_key = TestCaseExecutionQueueToTreat.CONSTRAIN5_EXECUTOREXTENSION + CONST_SEPARATOR + "";

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
            if (constrains_current.containsKey(const04_key)) {
                constrains_current.put(const04_key, constrains_current.get(const04_key) + 1);
            } else {
                constrains_current.put(const04_key, 1);
            }
        }
        return constrains_current;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeNextInQueue(boolean forceExecution) throws CerberusException {

        if (!isInstanceActive) {
            LOG.warn("Queue execution disable on that JVM instance.");
            return;
        }

        // Job can be desactivated by parameter.
        if (!(parameterService.getParameterBooleanByKey("cerberus_queueexecution_enable", "", true))) {
            LOG.debug("Queue_Processing_Job disabled by parameter : 'cerberus_queueexecution_enable'.");
            return;
        }

        // Flag in database that job is already running.
        if (myVersionService.flagMyVersionString("queueprocessingjobrunning") || forceExecution) {

            // Saving the timestamps when the job start in database.
            myVersionService.updateMyVersionString("queueprocessingjobstart", String.valueOf(new Date()));

            if (forceExecution) {
                LOG.debug("Forcing Start of Queue_Processing_Job.");
            }

            int nbqueuedexe = 0;

            // We try to submit new jobs until the job does not trigger any new execution.
            // In Other Words : As long as the job trigger new execution, we execute it.
            do {

                if (!(parameterService.getParameterBooleanByKey("cerberus_queueexecution_enable", "", true))) {
                    LOG.debug("Queue_Processing_Job disabled by parameter : 'cerberus_queueexecution_enable'.");
                    // Flag in database that job is finished.
                    myVersionService.updateMyVersionString("queueprocessingjobrunning", "N");
                    return;
                }

                nbqueuedexe = 0;
                // Job is not already running, we can trigger it.

                LOG.debug("Starting Queue_Processing_Job.");

                // Getting all executions to be treated.
                AnswerList<TestCaseExecutionQueueToTreat> answer = new AnswerList<>();
                answer = tceiqService.readQueueToTreat();
                List<TestCaseExecutionQueueToTreat> executionsInQueue = answer.getDataList();

                int poolSizeGeneral = 12;
                int poolSizeRobot = 10;
                int poolSizeExecutorExt = 2;
                int queueTimeout = 600000;

                // Init constrain counter (from list of already running execution.).
                int const01_current = 0;
                int const02_current = 0;
                int const03_current = 0;
                int const04_current = 0;
                int const05_current = 0;
                HashMap<String, Integer> constrains_current = new HashMap<>();
                HashMap<String, Integer> robothost_poolsize = new HashMap<>();
                HashMap<String, Integer> executorexthost_poolsize = new HashMap<>();
                HashMap<String, List<RobotExecutor>> robot_executor = new HashMap<>();
                HashMap<String, Robot> robot_header = new HashMap<>();

                poolSizeGeneral = parameterService.getParameterIntegerByKey("cerberus_queueexecution_global_threadpoolsize", "", 12);

                constrains_current = getCurrentlyRunning();
                LOG.debug("Current Constrains : " + constrains_current);

                if (!executionsInQueue.isEmpty()) {

                    poolSizeRobot = parameterService.getParameterIntegerByKey("cerberus_queueexecution_defaultrobothost_threadpoolsize", "", 10);
                    poolSizeExecutorExt = parameterService.getParameterIntegerByKey("cerberus_queueexecution_defaultexecutorexthost_threadpoolsize", "", 2);
                    queueTimeout = parameterService.getParameterIntegerByKey("cerberus_queueexecution_timeout", "", 600000);

                    // Init constrain counter (from list of already running execution.).
                    const01_current = 0;
                    const02_current = 0;
                    const03_current = 0;
                    const04_current = 0;
                    const05_current = 0;

                    // Getting RobotHost PoolSize
                    robothost_poolsize = invariantService.readToHashMapGp1IntegerByIdname("ROBOTHOST", poolSizeRobot);

                    // Getting CerberusExecutorHost PoolSize
                    executorexthost_poolsize = invariantService.readToHashMapGp1IntegerByIdname("ROBOTPROXYHOST", poolSizeExecutorExt);

                    // Getting the list of robot in scope of the queue entries. This is to avoid getting all robots from database.
                    LOG.debug("Getting List of Robot Executor.");
                    for (TestCaseExecutionQueueToTreat exe : executionsInQueue) {
                        if (!StringUtil.isEmptyOrNull(exe.getQueueRobot())) {
                            robot_executor.put(exe.getQueueRobot(), new ArrayList<>());
                        }
                    }
                    LOG.debug("List of Robot from Queue entries : " + robot_executor);
                    robot_executor = robotExecutorService.getExecutorListFromRobotHash(robot_executor);
                    LOG.debug("Robot Executor List : " + robot_executor);

                    LOG.debug("Getting List of Robot (Header).");
                    List<String> listRobotS = new ArrayList<>();
                    for (Map.Entry<String, List<RobotExecutor>> entry : robot_executor.entrySet()) {
                        String key = entry.getKey();
                        listRobotS.add(key);
                    }
                    robot_header = robotService.readToHashMapByRobotList(listRobotS);
                    LOG.debug("Robot Header List : " + robot_header);

                }

                String robot = "";
                String robotExecutor = "";
                String robotHost = "";
                String robotExtHost = "";
                String robotPort = "";
                String appType = "";
                List<RobotExecutor> tmpExelist = new ArrayList<>();
                List<RobotExecutor> newTmpExelist = new ArrayList<>();

                // Analysing each execution in the database queue.
                for (TestCaseExecutionQueueToTreat exe : executionsInQueue) {

                    LOG.debug("Starting analysing : " + exe.getId());

                    String notTriggeredExeMessage = "";
                    boolean triggerExe = false;
                    robot = exe.getQueueRobot();

                    // Getting here the list of possible executor sorted by prio.
                    List<RobotExecutor> robotExelist = new ArrayList<>();
                    appType = exe.getAppType();
                    if ((appType.equals(Application.TYPE_APK)) || (appType.equals(Application.TYPE_GUI)) || (appType.equals(Application.TYPE_FAT)) || (appType.equals(Application.TYPE_IPA))) {
                        // Application require a robot so we can get the list of executors.
                        if (StringUtil.isEmptyOrNull(robot)) {
                            robotExelist = new ArrayList<>();
                            robotExelist.add(factoryRobotExecutor.create(0, "", "", true, 1, exe.getQueueRobotHost(), exe.getQueueRobotPort(), "", "", 0, "", "", null, false, "", 0, "", 0, 0,"", "", "", null, "", null));
                        } else {
                            robotExelist = robot_executor.get(robot);
                            if (robotExelist == null || robotExelist.isEmpty()) {
                                robotExelist = new ArrayList<>();
                                robotExelist.add(factoryRobotExecutor.create(0, "", "", true, 1, "", "", "", "", 0, "", "", null, false, "", 0, "", 0, 0, "", "", "", null, "", null));
                            }
                        }
                    } else {
                        // Application does not require a robot so we create a fake one with empty data.
                        robotExelist = new ArrayList<>();
                        robotExelist.add(factoryRobotExecutor.create(0, "", "", true, 1, "", "", "", "", 0, "", "", null, false, "", 0, "", 0, 0,"", "", "", null, "", null));
                    }

                    // Looping over every potential executor on the corresponding robot.
                    for (RobotExecutor robotExecutor1 : robotExelist) {

                        if (RobotExecutor.PROXY_TYPE_NETWORKTRAFFIC.equalsIgnoreCase(robotExecutor1.getExecutorProxyType())) {
                            robotExtHost = robotExecutor1.getExecutorProxyServiceHost();
                            if (StringUtil.isEmptyOrNull(robotExtHost)) {
                                robotExtHost = robotExecutor1.getHost();
                            }
                        } else {
                            robotExtHost = "";
                        }

                        robotHost = robotExecutor1.getHost();
                        robotPort = robotExecutor1.getPort();
                        robotExecutor = robotExecutor1.getExecutor();
                        LOG.debug("Trying with : " + robotHost + " Port : " + robotPort + " From Robot/Executor : " + robotExecutor1.getRobot() + "/" + robotExecutor1.getExecutor() + " Extension : " + robotExtHost);

                        // RobotHost PoolSize if retreived from invariant hashmap.
                        int robothost_poolsize_final = 0;
                        if (!StringUtil.isEmptyOrNull(robotHost)) {
                            if (robothost_poolsize.containsKey(robotHost)) {
                                robothost_poolsize_final = ParameterParserUtil.parseIntegerParam(robothost_poolsize.get(robotHost), poolSizeRobot);
                            } else {
                                robothost_poolsize_final = poolSizeRobot;
                            }
                        }

                        // RobotExtensionHost PoolSize if retreived from invariant hashmap.
                        int robotexthost_poolsize_final = 0;
                        if (!StringUtil.isEmptyOrNull(robotExtHost)) {
                            if (executorexthost_poolsize.containsKey(robotExtHost)) {
                                robotexthost_poolsize_final = ParameterParserUtil.parseIntegerParam(executorexthost_poolsize.get(robotExtHost), poolSizeExecutorExt);
                            } else {
                                robotexthost_poolsize_final = poolSizeExecutorExt;
                            }
                        }

                        LOG.debug("Pool Values : poolGen " + poolSizeGeneral + " poolAppEnv " + exe.getPoolSizeAppEnvironment() + " poolApp " + exe.getPoolSizeApplication() + " poolRobotHost " + robothost_poolsize_final + " poolRobotExtHost " + robotexthost_poolsize_final);

                        String const01_key = TestCaseExecutionQueueToTreat.CONSTRAIN1_GLOBAL;
                        String const02_key = TestCaseExecutionQueueToTreat.CONSTRAIN2_APPLIENV + CONST_SEPARATOR + exe.getSystem() + CONST_SEPARATOR + exe.getEnvironment() + CONST_SEPARATOR + exe.getCountry() + CONST_SEPARATOR + exe.getApplication();
                        String const03_key = TestCaseExecutionQueueToTreat.CONSTRAIN3_APPLICATION + CONST_SEPARATOR + exe.getApplication();
                        String const04_key = TestCaseExecutionQueueToTreat.CONSTRAIN4_ROBOT + CONST_SEPARATOR + robotHost;
                        String const05_key = TestCaseExecutionQueueToTreat.CONSTRAIN5_EXECUTOREXTENSION + CONST_SEPARATOR + robotExtHost;

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
                        if (exe.getPoolSizeAppEnvironment() == 0) {
                            // if poolsize == 0, this means no constrain specified.
                            constMatch02 = false;
                        } else {
                            constMatch02 = (const02_current >= exe.getPoolSizeAppEnvironment());
                        }

                        // Eval Constrain 3
                        if (constrains_current.containsKey(const03_key)) {
                            const03_current = constrains_current.get(const03_key);
                        } else {
                            const03_current = 0;
                        }
                        // Eval Constrain 3
                        boolean constMatch03;
                        if (exe.getPoolSizeApplication() == 0) {
                            // if poolsize == 0, this means no constrain specified.
                            constMatch03 = false;
                        } else {
                            constMatch03 = (const03_current >= exe.getPoolSizeApplication());
                        }

                        // Eval Constrain 4
                        if (constrains_current.containsKey(const04_key)) {
                            const04_current = constrains_current.get(const04_key);
                        } else {
                            const04_current = 0;
                        }
                        // Eval Constrain 4
                        boolean constMatch04;
                        if (robothost_poolsize_final == 0) {
                            // if poolsize == 0, this means no constrain specified.
                            constMatch04 = false;
                        } else {
                            constMatch04 = (const04_current >= robothost_poolsize_final);
                        }

                        // Eval Constrain 5
                        if (constrains_current.containsKey(const05_key)) {
                            const05_current = constrains_current.get(const05_key);
                        } else {
                            const05_current = 0;
                        }
                        // Eval Constrain 5
                        boolean constMatch05;
                        if (robotexthost_poolsize_final == 0) {
                            // if poolsize == 0, this means no constrain specified.
                            constMatch05 = false;
                        } else {
                            constMatch05 = (const05_current >= robotexthost_poolsize_final);
                        }

                        if ((!constMatch01 && !constMatch02 && !constMatch03 && !constMatch04 && !constMatch05)
                                || (!constMatch01 && exe.getManualExecution().equals("Y"))) {
                            // None of the constrains match or exe is manual so we can trigger the execution.

                            // Execution could already been triggered on a different executor.
                            if (triggerExe == false) {

                                // Adding execution to queue.
                                if (queueService.updateToWaiting(exe.getId())) {
                                    try {
                                        ExecutionQueueWorkerThread task = new ExecutionQueueWorkerThread();
                                        // Flag on database that execution has been selected.
                                        robotExecutorService.updateLastExe(robot, robotExecutor);
                                        // Update robot_executor HasMap for next queued executions in the current batch. If Algo is based on Ranking, nothing needs to be changed.
                                        if ((robot_header.get(robot) != null)
                                                && (Robot.LOADBALANCINGEXECUTORMETHOD_ROUNDROBIN.equals(robot_header.get(robot).getLbexemethod()))
                                                && (robotExelist.size() > 1)) {
                                            tmpExelist = robot_executor.get(robot);
                                            newTmpExelist = new ArrayList<>();
                                            RobotExecutor lastRobotExecutor = null;
                                            for (RobotExecutor robotExecutor2 : tmpExelist) {
                                                // Update new List with RobotExecutor.LOADBALANCINGMETHOD_ROUNDROBIN Algo puting the Executor that has just been inserted at the end.
                                                if (robotExecutor2.getExecutor().equals(robotExecutor)) {
                                                    lastRobotExecutor = robotExecutor2;
                                                } else {
                                                    newTmpExelist.add(robotExecutor2);
                                                }
                                            }
                                            newTmpExelist.add(lastRobotExecutor);
                                            robot_executor.put(robot, newTmpExelist);
                                        }
                                        // Flag the queue entry to STARTING
                                        queueService.updateToStarting(exe.getId(), robotHost, robotExtHost);

                                        task.setCerberusExecutionUrl(StringUtil.addSuffixIfNotAlready(parameterService.getParameterStringByKey("cerberus_url", exe.getSystem(), ""), "/"));
                                        task.setCerberusTriggerQueueJobUrl(StringUtil.addSuffixIfNotAlready(parameterService.getParameterStringByKey("cerberus_url", exe.getSystem(), ""), "/")
                                                + ManageV001.SERVLETNAME + "?apikey=" + apiKeyService.getServiceAccountAPIKey() + "&action=" + ManageV001.ACTIONRUNQUEUEJOB);

                                        task.setQueueId(exe.getId());
                                        task.setRobotExecutor(robotExecutor);
                                        task.setSelectedRobotHost(robotHost);
                                        task.setSelectedRobotExtHost(robotExtHost);
                                        task.setToExecuteTimeout(queueTimeout);
                                        task.setQueueService(queueService);
                                        task.setQueueService(queueService);
                                        task.setParameterService(parameterService);
                                        task.setApiKeyService(apiKeyService);
                                        task.setSessionCounter(sessionCounter);
                                        task.setRetriesService(retriesService);
                                        task.setTagService(tagService);
                                        task.setExecThreadPool(threadQueuePool);
                                        Future<?> future = threadQueuePool.getExecutor().submit(task);
                                        task.setFuture(future);

                                        triggerExe = true;
                                        nbqueuedexe++;

                                        // Debug messages.
                                        LOG.debug("RESULT : Execution triggered. Const1 " + constMatch01 + " Const2 " + constMatch02 + " Const3 " + constMatch03 + " Const4 " + constMatch04 + " Const5 " + constMatch05 + " Manual " + exe.getManualExecution());
                                        LOG.debug(" CurConst1 " + const01_current + " CurConst2 " + const02_current + " CurConst3 " + const03_current + " CurConst4 " + const04_current + " CurConst5 " + const05_current);

                                        // Constrains Counter increase
                                        constrains_current.put(const01_key, const01_current + 1);
                                        if (!exe.getManualExecution().equals("Y")) {
                                            // Specific increment only if automatic execution.
                                            constrains_current.put(const02_key, const02_current + 1);
                                            constrains_current.put(const03_key, const03_current + 1);
                                            constrains_current.put(const04_key, const04_current + 1);
                                            constrains_current.put(const05_key, const05_current + 1);
                                        }

                                    } catch (Exception e) {
                                        LOG.error("Failed to add Queueid : " + exe.getId() + " into the queue : " + e.getMessage(), e);
                                    }

                                }
                            } else {
                                LOG.debug("RESULT : Execution Not triggered. Queueid : " + exe.getId() + " already inserted (on a previous Executor).");
                            }

                        } else {
                            if (constMatch05) {
                                notTriggeredExeMessage += "Robot Extension Host contrain on '" + const05_key + "' reached. " + robotexthost_poolsize_final + " Execution(s) already in pool. ";
                            }
                            if (constMatch04) {
                                notTriggeredExeMessage += "Robot Host contrain on '" + const04_key + "' reached. " + robothost_poolsize_final + " Execution(s) already in pool. ";
                            }
                            if (constMatch03) {
                                notTriggeredExeMessage += "Application contrain on '" + const03_key + "' reached . " + exe.getPoolSizeApplication() + " Execution(s) already in pool. ";
                            }
                            if (constMatch02) {
                                notTriggeredExeMessage += "Application Environment contrain on '" + const02_key + "' reached . " + exe.getPoolSizeAppEnvironment() + " Execution(s) already in pool. ";
                            }
                            if (constMatch01) {
                                notTriggeredExeMessage += "Global contrain reached. " + poolSizeGeneral + " Execution(s) already in pool. ";
                            }
                            LOG.debug("RESULT : Execution not triggered. Const1 " + constMatch01 + " Const2 " + constMatch02 + " Const3 " + constMatch03 + " Const4 " + constMatch04 + " Const5 " + constMatch05 + " Manual " + exe.getManualExecution());
                            LOG.debug(" CurConst1 " + const01_current + " CurConst2 " + const02_current + " CurConst3 " + const03_current + " CurConst4 " + const04_current + " CurConst5 " + const05_current);
                        }
                    }

//                  End of Queue entry analysis accross all Executors.
                    if ((exe.getDebugFlag() != null) && (exe.getDebugFlag().equalsIgnoreCase("Y"))) {
                        if (triggerExe == false) {
                            queueService.updateComment(exe.getId(), notTriggeredExeMessage);
                        }
                        LOG.debug("Debug Message : " + notTriggeredExeMessage);

                    }
                }

                LOG.debug("Stopping Queue_Processing_Job - TOTAL Released execution(s) : " + nbqueuedexe);

                if (constrains_current.containsKey(TestCaseExecutionQueueToTreat.CONSTRAIN1_GLOBAL)) {
                    const01_current = constrains_current.get(TestCaseExecutionQueueToTreat.CONSTRAIN1_GLOBAL);
                } else {
                    const01_current = 0;
                }
                LOG.debug("Stats : GlobalContrain=" + poolSizeGeneral + " - nbRunning=" + const01_current + " - NbQueued=" + executionsInQueue.size());

                if (nbqueuedexe == 0) { // Websocket of queue status is sent only if no new execution was submitted. In case a new execution is submitted, the websocket is refreshed only when execution has been created on database.
                    executionUUIDObject.setQueueCounters(poolSizeGeneral, const01_current, executionsInQueue.size());
                    QueueStatus queueS = QueueStatus.builder()
                            .executionHashMap(executionUUIDObject.getExecutionUUIDList())
                            .globalLimit(poolSizeGeneral)
                            .running(const01_current)
                            .queueSize(executionsInQueue.size()).build();
                    queueStatusWebSocket.send(queueS, true);
                }

                queueStatService.create(factoryQueueStat.create(0, poolSizeGeneral, const01_current, executionsInQueue.size(), "", null, null, null));

            } while (nbqueuedexe > 0);

            // Flag in database that job is finished.
            myVersionService.updateMyVersionString("queueprocessingjobrunning", "N");

        } else {
            LOG.debug("Queue_Processing_Job not triggered (already running.)");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Async
    public void executeNextInQueueAsynchroneously(boolean forceExecution) throws CerberusException {
        this.executeNextInQueue(forceExecution);
    }

}
