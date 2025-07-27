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
package org.cerberus.core.crud.entity;

import lombok.Data;
import org.apache.kafka.common.TopicPartition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.engine.entity.Selenium;
import org.cerberus.core.engine.entity.Session;
import org.cerberus.core.service.har.entity.NetworkTrafficIndex;
import org.cerberus.core.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.cerberus.core.engine.entity.ExecutionLog;

/**
 * @author bcivel
 */
@Data
public class TestCaseExecution {

    private static final Logger LOG = LogManager.getLogger(TestCaseExecution.class);

    private long id;
    private String system;
    private String test;
    private String testCase;
    private String description;
    private String build;
    private String revision;
    private String environment;
    private String environmentData;
    private String country;
    private String robot;
    private String robotExecutor;
    private String robotHost; // Host the Selenium IP
    private String robotPort; // host the Selenium Port
    private String robotDecli;
    private String robotSessionID;
    private String robotProvider;
    private String robotProviderSessionID;
    private String browser;
    private String version;
    private String platform;
    private long start;
    private long end;
    private long durationMs;
    private String controlStatus;
    private boolean falseNegative;
    private boolean isFlaky;
    private boolean isUseful;
    private String controlMessage;
    private String application;
    private String url;
    private String tag;
    private String status;
    private String crbVersion;
    private String executor;
    private String screenSize;
    private String conditionOperator;
    private String conditionVal1Init;
    private String conditionVal2Init;
    private String conditionVal3Init;
    private String conditionVal1;
    private String conditionVal2;
    private String conditionVal3;
    private String manualExecution;
    private String userAgent;
    private long queueID;
    private String UsrCreated;
    private Timestamp DateCreated;
    private String UsrModif;
    private Timestamp DateModif;
    private int testCaseVersion;
    private int testCasePriority;
    private boolean testCaseIsMuted;

    /**
     * From here are data outside database model.
     */
    // Execution Parameters
    private String queueState;
    private int verbose;
    private int screenshot;
    private int video;
    private String outputFormat;
    private int manualURL;
    private String myHost;
    private String myContextRoot;
    private String myLoginRelativeURL;
    private String seleniumIP;
    private String seleniumIPUser;
    private String seleniumIPPassword;
    private String seleniumPort;
    private Integer pageSource;
    private Integer robotLog;
    private Integer consoleLog;
    private Integer numberOfRetries;
    private boolean synchroneous;
    private String timeout;
    private JSONArray conditionOptions;
    // Objects.
    private TestCaseExecutionQueue testCaseExecutionQueue;
    private Application applicationObj;
    // App Type that is used by the engine to interpret the context. By defaut it is linked to the Type of the application but it can be temporary switch to a different type.
    private String appTypeEngine;
    private Invariant CountryObj;
    private Test testObj;
    private TestCase testCaseObj;
    private Tag tagObj;
    private CountryEnvParam countryEnvParam;
    private String currentApplication; // Allow to move the application environment context in case of a call to a service.
    private CountryEnvironmentParameters countryEnvApplicationParam; // Main value from application of the testcase.
    private HashMap<String, CountryEnvironmentParameters> countryEnvApplicationParams; // All applications values from all application existing on the same env/system and linked to main environement of the testcase.
    private Invariant environmentObj;
    private Invariant environmentDataObj;
    private Invariant priorityObj;
    // Host the list of the files stored at execution level
    private List<TestCaseExecutionFile> fileList;
    // Host the list of Steps that will be executed (both pre tests and main test)
    private List<TestCaseStepExecution> testCaseStepExecutionList;
    private TestCaseStepExecution testCaseStepInExecution;
    // Host the full list of data calculated during the execution.
    private TreeMap<String, TestCaseExecutionData> testCaseExecutionDataMap;
    // This is used to keep track of all property calculated within a step/action/control. It is reset each time we enter a step/action/control and the property name is added to the list each time it gets calculated. In case it was already asked for calculation, we stop the execution with FA message.
    private List<String> recursiveAlreadyCalculatedPropertiesList;
    private List<TestCaseCountryProperties> testCaseCountryPropertyList;

    // List of strings that needs to be secured and hidden from end users.
    private HashMap<String, String> secrets;

    private List<TestCaseExecutionQueueDep> testCaseExecutionQueueDepList;

    private List<String> videos;

    // Used in reporting page to report the previous executions from the same tag.
    private long previousExeId;
    private String previousExeStatus;
    private long firstExeStart;
    private long lastExeStart;
    private long lastExeEnd;

    // Others
    private MessageGeneral resultMessage;
    private String executionUUID;
    private Selenium selenium;
    private Session session;
    private Robot robotObj;
    private RobotExecutor robotExecutorObj;
    private AppService lastServiceCalled;
    private String originalLastServiceCalled; // Used in order to save the last call when using the action setServiceCallContent.
    private String originalLastServiceCalledContent; // Used in order to save the last call when using the action setServiceCallContent.
    private Integer nbExecutions; // Has the nb of execution that was necessary to execute the testcase.
    // Global parameters.
    private Integer cerberus_action_wait_default;
    // Websocket management parameters
    private boolean cerberus_featureflipping_activatewebsocketpush;
    private long cerberus_featureflipping_websocketpushperiod;
    private long lastWebsocketPush;
    // Remote Proxy data.
    private boolean remoteProxyStarted;
    private Integer remoteProxyPort;
    private String remoteProxyUUID;
    private String remoteProxyLastHarMD5;
    // Kafka Consumers
    private HashMap<String, Map<TopicPartition, Long>> kafkaLatestOffset;
    // Http Stats
    private TestCaseExecutionHttpStat httpStat;
    private List<NetworkTrafficIndex> networkTrafficIndexList;
    private List<ExecutionLog> executionLog;

    /**
     * Invariant PROPERTY TYPE String.
     */
    public static final String CONTROLSTATUS_OK = "OK"; // Test executed and everything was fine.
    public static final String CONTROLSTATUS_OK_COL = "var(--crb-green-color)"; // Test executed and everything was fine.
    public static final String CONTROLSTATUS_OK_COL_EXT = "#00d27a"; // Test executed and everything was fine.
    public static final String CONTROLSTATUS_KO = "KO"; // Test executed and 1 control has reported a bug. --> Ticket to be open for dev team.
    public static final String CONTROLSTATUS_KO_COL = "var(--crb-red-color)"; // Test executed and 1 control has reported a bug. --> Ticket to be open for dev team.
    public static final String CONTROLSTATUS_KO_COL_EXT = "#e63757"; // Test executed and 1 control has reported a bug. --> Ticket to be open for dev team.
    public static final String CONTROLSTATUS_FA = "FA"; // Test failed to be executed. --> Problem is in the test itself.
    public static final String CONTROLSTATUS_FA_COL = "var(--crb-orange-color)"; // Test failed to be executed. --> Problem is in the test itself.
    public static final String CONTROLSTATUS_FA_COL_EXT = "#f5803e"; // Test failed to be executed. --> Problem is in the test itself.
    public static final String CONTROLSTATUS_NA = "NA"; // Test could not be executed because no data could be retreive for testing.
    public static final String CONTROLSTATUS_NA_COL = "var(--crb-yellow-color)"; // Test could not be executed because no data could be retreive for testing.
    public static final String CONTROLSTATUS_NA_COL_EXT = "#f1c40f"; // Test could not be executed because no data could be retreive for testing.
    public static final String CONTROLSTATUS_NE = "NE"; // Test was not executed.
    public static final String CONTROLSTATUS_NE_COL = "var(--crb-light-color)"; // Test was not executed.
    public static final String CONTROLSTATUS_NE_COL_EXT = "#ffffff"; // Test was not executed.
    public static final String CONTROLSTATUS_WE = "WE"; // Test is waiting for a manual testing.
    public static final String CONTROLSTATUS_WE_COL = "var(--crb-dark-color)"; // Test is waiting for a manual testing.
    public static final String CONTROLSTATUS_WE_COL_EXT = "#34495e"; // Test is waiting for a manual testing.
    public static final String CONTROLSTATUS_PE = "PE"; // Test is currently beeing executed.
    public static final String CONTROLSTATUS_PE_COL = "var(--crb-blue-color)"; // Test is currently beeing executed.
    public static final String CONTROLSTATUS_PE_COL_EXT = "#2c7be5"; // Test is currently beeing executed.
    public static final String CONTROLSTATUS_CA = "CA"; // Test has been cancelled by user.
    public static final String CONTROLSTATUS_CA_COL = "var(--crb-yellow-dark-color)"; // Test has been cancelled by user.
    public static final String CONTROLSTATUS_CA_COL_EXT = "#c6a20d"; // Test has been cancelled by user.
    public static final String CONTROLSTATUS_QU = "QU"; // Test is still waiting in queue.
    public static final String CONTROLSTATUS_QU_COL = "var(--crb-purple-color)"; // Test is still waiting in queue.
    public static final String CONTROLSTATUS_QU_COL_EXT = "#BF00BF"; // Test is still waiting in queue.
    public static final String CONTROLSTATUS_QE = "QE"; // Test is stuck in Queue.
    public static final String CONTROLSTATUS_QE_COL = "var(--crb-purple-dark-color)"; // Test is stuck in Queue.
    public static final String CONTROLSTATUS_QE_COL_EXT = "#5C025C"; // Test is stuck in Queue.
    public static final String CONTROLSTATUS_PA = "PA"; // Test is still waiting in queue.
    public static final String CONTROLSTATUS_PA_COL = "var(--crb-thistle-color)"; // Test is still waiting in queue.
    public static final String CONTROLSTATUS_PA_COL_EXT = "#D8BFD8"; // Test is still waiting in queue.

    public enum ControlStatus {
        OK, KO, FA, NA, NE, WE, PE, CA, QU, QE
    };

    public static final String MANUAL_Y = "Y";
    public static final String MANUAL_N = "N";
    public static final String MANUAL_A = "A";

    public static final String ROBOTPROVIDER_BROWSERSTACK = "BROWSERSTACK";
    public static final String ROBOTPROVIDER_KOBITON = "KOBITON";
    public static final String ROBOTPROVIDER_LAMBDATEST = "LAMBDATEST";
    public static final String ROBOTPROVIDER_NONE = "NONE";

    public void setResultMessage(MessageGeneral resultMessage) {
        this.resultMessage = resultMessage;
        if (resultMessage != null) {
            this.setControlMessage(resultMessage.getDescription());
            this.setControlStatus(resultMessage.getCodeString());
        }
    }

    public void addExecutionLog(String status, String message) {
        if (executionLog == null) {
            executionLog = new ArrayList<>();
        }
        if (executionLog != null) {
            this.executionLog.add(ExecutionLog.builder().message(StringUtil.secureFromSecrets(message, this.getSecrets())).status(status).datetime(new Timestamp(System.currentTimeMillis())).build());
        }
    }

    public void addFileList(TestCaseExecutionFile file) {
        if (file != null) {
            this.fileList.add(file);
        }
    }

    public void addFileList(List<TestCaseExecutionFile> fileList) {
        if (fileList != null) {
            for (TestCaseExecutionFile testCaseExecutionFile : fileList) {
                this.fileList.add(testCaseExecutionFile);
            }
        }
    }

    public void addStepExecutionList(TestCaseStepExecution stepExecution) {
        if (stepExecution != null) {
            this.testCaseStepExecutionList.add(stepExecution);
        }
    }

    public void addStepExecutionList(List<TestCaseStepExecution> stepExecutionList) {
        if (stepExecutionList != null) {
            for (TestCaseStepExecution stepExecution : stepExecutionList) {
                this.testCaseStepExecutionList.add(stepExecution);
            }
        }
    }

    public void addTestCaseCountryPropertyList(TestCaseCountryProperties property) {
        if (testCaseCountryPropertyList == null) {
            testCaseCountryPropertyList = new ArrayList<>();
        }
        if (property != null) {
            this.testCaseCountryPropertyList.add(property);
        }
    }

    public void addTestCaseCountryPropertyList(List<TestCaseCountryProperties> propertyList) {
        if (testCaseCountryPropertyList == null) {
            testCaseCountryPropertyList = new ArrayList<>();
        }
        if (propertyList != null) {
            for (TestCaseCountryProperties property : propertyList) {
                this.testCaseCountryPropertyList.add(property);
            }
        }
    }

    public void addNetworkTrafficIndexList(NetworkTrafficIndex newIndex) {
        this.networkTrafficIndexList.add(newIndex);
    }

    public void addSecret(String secret) {
        if (secret != null && (!"".equals(secret))) {
            this.secrets.put(secret, "");
        }
    }

    public void addSecrets(List<String> secrets) {
        secrets.forEach(secret -> {
            this.secrets.put(secret, "");
        });
    }

    public void addcountryEnvApplicationParam(CountryEnvironmentParameters countryEnvApplication) {
        if (countryEnvApplication != null) {
            this.countryEnvApplicationParams.put(countryEnvApplication.getApplication(), countryEnvApplication);
        }
    }

    public void addcountryEnvApplicationParams(List<CountryEnvironmentParameters> countryEnvApplications) {
        LOG.debug(countryEnvApplications);
        countryEnvApplications.forEach(countryEnvApplication -> {
            LOG.debug(countryEnvApplication);
            this.countryEnvApplicationParams.put(countryEnvApplication.getApplication(), countryEnvApplication);
        });
    }

    public TestCaseStepExecution getTestCaseStepExecutionBySortId(int sortID) {
        for (TestCaseStepExecution tcse : this.testCaseStepExecutionList) {
            if (sortID == tcse.getTestCaseStep().getSort()) {
                return tcse;
            }
        }
        return null;
    }

    public TestCaseStepExecution getTestCaseStepExecutionByStepId(int stepId) {
        TestCaseStepExecution tcsee = this.getTestCaseStepInExecution();
        //If step executing if from library, return the step from the library instead
        if (tcsee.isUsingLibraryStep()) {
            for (TestCaseStepExecution tcse : this.testCaseStepExecutionList) {
                if (stepId == tcse.getTestCaseStep().getLibraryStepStepId()) {
                    return tcse;
                }
            }
            return tcsee;
        } else {
            for (TestCaseStepExecution tcse : this.testCaseStepExecutionList) {
                if (stepId == tcse.getTestCaseStep().getStepId()) {
                    return tcse;
                }
            }
        }
        return null;
    }

    public TestCaseStepExecution getTestCaseStepExecutionExecuting() {
        for (TestCaseStepExecution tcse : this.testCaseStepExecutionList) {
            if ("PE".equals(tcse.getReturnCode())) {
                return tcse;
            }
        }
        return null;
    }

    /**
     * Convert the current TestCaseExecution into JSON format
     *
     * @param withChildren boolean that define if childs should be included
     * @return TestCaseExecution in JSONObject format
     */
    public JSONObject toJson(boolean withChildren) {
        JSONObject result = new JSONObject();
        try {
            result.put("type", "testCaseExecution");
            result.put("id", this.getId());
            result.put("test", this.getTest());
            result.put("testcase", this.getTestCase());
            result.put("description", this.getDescription());
            result.put("build", this.getBuild());
            result.put("revision", this.getRevision());
            result.put("environment", this.getEnvironment());
            result.put("environmentData", this.getEnvironmentData());
            result.put("country", this.getCountry());
            result.put("browser", this.getBrowser());
            result.put("version", this.getVersion());
            result.put("platform", this.getPlatform());
            result.put("start", this.getStart());
            result.put("end", this.getEnd());
            result.put("controlStatus", this.getControlStatus());
            result.put("falseNegative", this.isFalseNegative());
            result.put("controlMessage", StringUtil.secureFromSecrets(this.getControlMessage(), this.getSecrets()));
            result.put("application", this.getApplication());
            result.put("robot", this.getRobot());
            result.put("robotExecutor", this.getRobotExecutor());
            result.put("robotHost", StringUtil.secureFromSecrets(this.getRobotHost(), this.getSecrets()));
            result.put("robotPort", this.getRobotPort());
            result.put("url", StringUtil.secureFromSecrets(this.getUrl(), this.getSecrets()));
            result.put("tag", this.getTag());
            result.put("verbose", this.getVerbose());
            result.put("status", this.getStatus());
            result.put("crbVersion", this.getCrbVersion());
            result.put("executor", this.getExecutor());
            result.put("screenSize", this.getScreenSize());

            result.put("conditionOperator", this.getConditionOperator());
            result.put("conditionVal1Init", StringUtil.secureFromSecrets(this.getConditionVal1Init(), this.getSecrets()));
            result.put("conditionVal2Init", StringUtil.secureFromSecrets(this.getConditionVal2Init(), this.getSecrets()));
            result.put("conditionVal3Init", StringUtil.secureFromSecrets(this.getConditionVal3Init(), this.getSecrets()));
            result.put("conditionVal1", StringUtil.secureFromSecrets(this.getConditionVal1(), this.getSecrets()));
            result.put("conditionVal2", StringUtil.secureFromSecrets(this.getConditionVal2(), this.getSecrets()));
            result.put("conditionVal3", StringUtil.secureFromSecrets(this.getConditionVal3(), this.getSecrets()));
            result.put("userAgent", this.getUserAgent());
            result.put("queueId", this.getQueueID());
            result.put("manualExecution", this.getManualExecution());
            result.put("testCaseVersion", this.getTestCaseVersion());
            result.put("testCasePriority", this.getTestCasePriority());
            result.put("testCaseIsMuted", this.isTestCaseIsMuted());
            result.put("system", this.getSystem());
            result.put("robotDecli", this.getRobotDecli());
            result.put("robotProvider", this.getRobotProvider());
            result.put("robotSessionId", this.getRobotSessionID());
            result.put("robotProviderSessionId", this.getRobotProviderSessionID());
            result.put("videos", this.getVideos());
            result.put("previousExeId", this.getPreviousExeId());
            result.put("previousExeStatus", this.getPreviousExeStatus());

            result.put("isUseful", this.isUseful());
            result.put("isFlaky", this.isFlaky());
            result.put("durationMs", this.getDurationMs());

            result.put("usrCreated", this.getUsrCreated());
            result.put("dateCreated", this.getDateCreated());
            result.put("usrModif", this.getUsrModif());
            result.put("dateModif", this.getDateModif());

            if (withChildren) {
                // Looping on ** Step **
                JSONArray array = new JSONArray();
                if (this.getTestCaseStepExecutionList() != null) {
                    for (TestCaseStepExecution testCaseStepExecution : this.getTestCaseStepExecutionList()) {
                        array.put(testCaseStepExecution.toJson(true, false, this.getSecrets()));
                    }
                }
                result.put("testCaseStepExecutionList", array);

                array = new JSONArray();
                if (this.getTestCaseExecutionQueueDepList() != null) {
                    for (TestCaseExecutionQueueDep tceQDep : this.getTestCaseExecutionQueueDepList()) {
                        array.put(tceQDep.toJson());
                    }
                }
                result.put("testCaseExecutionQueueDepList", array);

                // ** TestCase **
                if (this.getTestCaseObj() != null) {
                    TestCase tc = this.getTestCaseObj();
                    result.put("testCaseObj", tc.toJson());
                }

                // ** Tag **
                if (this.getTagObj() != null) {
                    Tag tagO = this.getTagObj();
                    result.put("tagObj", tagO.toJsonLight());
                }

                // Looping on ** Execution Data **
                array = new JSONArray();
                for (String key1 : this.getTestCaseExecutionDataMap().keySet()) {
                    TestCaseExecutionData tced = this.getTestCaseExecutionDataMap().get(key1);
                    array.put((tced).toJson(true, false, this.getSecrets()));
                }
                result.put("testCaseExecutionDataList", array);

                // Looping on ** Media File Execution **
                array = new JSONArray();
                if (this.getFileList() != null) {
                    for (TestCaseExecutionFile testCaseFileExecution : this.getFileList()) {
                        array.put(testCaseFileExecution.toJson());
                    }
                }
                result.put("fileList", array);

                if (this.getHttpStat() != null) {
                    result.put("httpStat", this.getHttpStat().toJson());
                }

            }

        } catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }
        return result;
    }

    /**
     * Convert the current TestCaseExecution into a public JSON format.
     *
     * @param cerberusURL
     * @param prioritiesList : send the invariant list of priorities to the
     * method (this is to avoid getting value from database for every entries)
     * @param countriesList : send the invariant list of countries to the method
     * (this is to avoid getting value from database for every entries)
     * @param environmentsList : send the invariant list of environments to the
     * method (this is to avoid getting value from database for every entries)
     * @return TestCaseExecution in JSONObject format
     */
    public JSONObject toJsonV001(String cerberusURL, List<Invariant> prioritiesList, List<Invariant> countriesList, List<Invariant> environmentsList) {
        JSONObject result = new JSONObject();
        try {
            result.put("JSONVersion", "001");
            result.put("link", cerberusURL + "TestCaseExecution.jsp?executionId=" + this.id);
            result.put("id", this.getId());

            result.put("testcase", this.getTestCaseObj().toJsonV001(cerberusURL, prioritiesList));

            result.put("testcaseVersion", this.getTestCaseVersion());

            result.put("description", this.getDescription());
            result.put("build", this.getBuild());
            result.put("revision", this.getRevision());

            // ENVIRONMENT
            if (this.getEnvironmentObj() != null) {
                result.put("environment", this.getEnvironmentObj().toJsonV001());
            } else {
                result.put("environment", this.getEnvironment());
            }
            if (environmentsList != null) {
                Invariant environmentLocal = environmentsList.stream().filter(inv -> this.getEnvironment().equals(inv.getValue())).findAny().orElse(null);
                if (environmentLocal != null) {
                    result.put("environment", environmentLocal.toJsonV001());
                }
            }
            // ENVIRONMENTDATA
            if (this.getEnvironmentDataObj() != null) {
                result.put("environmentData", this.getEnvironmentDataObj().toJsonV001());
            } else {
                result.put("environmentData", this.getEnvironmentData());
            }
            if (environmentsList != null) {
                Invariant environmentDataLocal = environmentsList.stream().filter(inv -> this.getEnvironmentData().equals(inv.getValue())).findAny().orElse(null);
                if (environmentDataLocal != null) {
                    result.put("environmentData", environmentDataLocal.toJsonV001());
                }
            }

            // COUNTRY
            if (this.getCountryObj() != null) {
                result.put("country", this.getCountryObj().toJsonV001());
            } else {
                result.put("country", this.getCountry());
            }
            if (countriesList != null) {
                Invariant countryLocal = countriesList.stream().filter(inv -> this.getCountry().equals(inv.getValue())).findAny().orElse(null);
                if (countryLocal != null) {
                    result.put("country", countryLocal.toJsonV001());
                }
            }

            // PRIORITY
            if (this.getTestCaseObj() != null) {
                if (this.getPriorityObj() != null) {
                    result.put("priority", this.getPriorityObj().toJsonV001());
                }
                if (prioritiesList != null) {
                    Invariant priorityLocal = prioritiesList.stream().filter(inv -> Integer.toString(this.getTestCaseObj().getPriority()).equals(inv.getValue())).findAny().orElse(null);
                    if (priorityLocal != null) {
                        result.put("priority", priorityLocal.toJsonV001());
                    }
                }
            }

            result.put("start", new Timestamp(this.getStart()));
            result.put("end", new Timestamp(this.getEnd()));
            result.put("durationInMs", this.getEnd() - this.getStart());
            result.put("controlStatus", this.getControlStatus());
            result.put("controlMessage", StringUtil.secureFromSecrets(this.getControlMessage(), this.getSecrets()));
            result.put("falseNegative", this.isFalseNegative());
            result.put("application", this.getApplication());
            JSONObject robotLocal = new JSONObject();

            robotLocal.put("name", this.getRobot());
            robotLocal.put("executor", this.getRobotExecutor());
            robotLocal.put("host", StringUtil.secureFromSecrets(this.getRobotHost(), this.getSecrets()));
            robotLocal.put("port", this.getRobotPort());
            robotLocal.put("declination", this.getRobotDecli());
            robotLocal.put("provider", this.getRobotProvider());
            robotLocal.put("sessionId", this.getRobotSessionID());
            robotLocal.put("providerSessionId", this.getRobotProviderSessionID());
            robotLocal.put("browser", this.getBrowser());
            robotLocal.put("version", this.getVersion());
            robotLocal.put("platform", this.getPlatform());
            robotLocal.put("screenSize", this.getScreenSize());
            robotLocal.put("userAgent", this.getUserAgent());
            result.put("robot", robotLocal);

            result.put("url", StringUtil.secureFromSecrets(this.getUrl(), this.getSecrets()));
            result.put("tag", this.getTag());
            result.put("status", this.getStatus());
            result.put("executor", this.getExecutor());
            result.put("queueId", this.getQueueID());
            result.put("manualExecution", this.getManualExecution());
            result.put("system", this.getSystem());

            result.put("usrCreated", this.getUsrCreated());
            result.put("dateCreated", this.getDateCreated());
            result.put("usrModif", this.getUsrModif());
            result.put("dateModif", this.getDateModif());

        } catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }
        return result;
    }

    public String getColor(String controlStatus) {
        String color = null;

        if ("OK".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_OK_COL_EXT;
        } else if ("KO".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_KO_COL_EXT;
        } else if ("FA".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_FA_COL_EXT;
        } else if ("CA".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_CA_COL_EXT;
        } else if ("NA".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_NA_COL_EXT;
        } else if ("NE".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_NE_COL_EXT;
        } else if ("WE".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_WE_COL_EXT;
        } else if ("PE".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_PE_COL_EXT;
        } else if ("QU".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_QU_COL_EXT;
        } else if ("PA".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_PA_COL_EXT;
        } else if ("QE".equals(controlStatus)) {
            color = TestCaseExecution.CONTROLSTATUS_QE_COL_EXT;
        } else {
            color = "#000000";
        }
        return color;
    }

    public TestCaseExecutionLight toLight() {
        TestCaseExecutionLight result;
        result = TestCaseExecutionLight.builder()
                .id(this.getId())
                .system(this.getSystem())
                .test(this.getTest())
                .testCase(this.getTestCase())
                .description(this.getDescription())
                .application(this.getApplication())
                .environment(this.getEnvironment())
                .environmentData(this.getEnvironmentData())
                .country(this.getCountry())
                .robot(this.getRobot())
                .tag(this.getTag())
                .controlStatus(this.getControlStatus())
                .controlMessage(this.getControlMessage())
                .start(this.getStart())
                .end(this.getEnd())
                .build();

        if (this.getTagObj() != null) {
            result.setCampaign(this.getTagObj().getCampaign());
        }

        return result;
    }

}
