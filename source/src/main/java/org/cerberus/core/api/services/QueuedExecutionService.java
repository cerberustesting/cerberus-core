/*
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
package org.cerberus.core.api.services;

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.entity.*;
import org.cerberus.core.api.exceptions.EntityNotFoundException;
import org.cerberus.core.api.exceptions.FailedInsertOperationException;
import org.cerberus.core.api.exceptions.InvalidRequestException;
import org.cerberus.core.crud.entity.*;
import org.cerberus.core.crud.factory.IFactoryRobot;
import org.cerberus.core.crud.factory.IFactoryTestCaseExecutionQueue;
import org.cerberus.core.crud.service.*;
import org.cerberus.core.engine.queuemanagement.IExecutionThreadPoolService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.exception.FactoryCreationException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author lucashimpens
 */
@AllArgsConstructor
@Service
public class QueuedExecutionService {

    private final IApplicationService applicationService;
    private final ICampaignService campaignService;
    private final ICampaignParameterService campaignParameterService;
    private final ICountryEnvParamService countryEnvParamService;
    private final ICountryEnvironmentParametersService countryEnvironmentParametersService;
    private final IExecutionThreadPoolService executionThreadService;
    private final IFactoryTestCaseExecutionQueue inQueueFactoryService;
    private final ITestCaseExecutionQueueService inQueueService;
    private final IParameterService parameterService;
    private final ITagService tagService;
    private final ITestCaseService testCaseService;
    private final ITestCaseCountryService testCaseCountryService;
    private final IRobotService robotService;
    private final IInvariantService invariantService;
    private final IFactoryRobot robotFactory;

    private static final String LOCAL_SEPARATOR = "_-_";
    private static final Logger LOG = LogManager.getLogger(QueuedExecutionService.class);

    public QueuedExecutionResult addCampaignToExecutionQueue(String campaignId, QueuedExecution queuedExecution, Principal principal) {
        Optional<Campaign> campaignOptional = Optional.ofNullable(campaignService.readByKey(campaignId).getItem());
        Campaign campaign;
        if (campaignOptional.isPresent()) {
            campaign = campaignOptional.get();
        } else {
            throw new EntityNotFoundException(Campaign.class, "campaignId", campaignId);
        }

        cleanStringAndListOfDefaultValueFromSwagger(queuedExecution);

        List<QueuedExecutionTestcase> selectedTestcases = CollectionUtils.isNotEmpty(queuedExecution.getTestcases()) ? queuedExecution.getTestcases() : new ArrayList<>();
        List<String> countries = CollectionUtils.isNotEmpty(queuedExecution.getCountries()) ? queuedExecution.getCountries() : new ArrayList<>();
        List<String> environments = CollectionUtils.isNotEmpty(queuedExecution.getEnvironments()) ? queuedExecution.getEnvironments() : new ArrayList<>();
        List<String> robots = CollectionUtils.isNotEmpty(queuedExecution.getRobots()) ? queuedExecution.getRobots() : new ArrayList<>();
        int manualUrl = queuedExecution.getManualUrl() != null
                ? queuedExecution.getManualUrl()
                : 0;
        ManualUrlParameters manualUrlParameters;
        if (queuedExecution.getManualUrlParameters() != null) {
            manualUrlParameters = ManualUrlParameters.builder()
                    .host(StringUtil.isNotEmptyOrNull(queuedExecution.getManualUrlParameters().getHost()) ? queuedExecution.getManualUrlParameters().getHost() : "")
                    .contextRoot(StringUtil.isNotEmptyOrNull(queuedExecution.getManualUrlParameters().getContextRoot()) ? queuedExecution.getManualUrlParameters().getContextRoot() : "")
                    .loginRelativeUrl(StringUtil.isNotEmptyOrNull(queuedExecution.getManualUrlParameters().getLoginRelativeUrl()) ? queuedExecution.getManualUrlParameters().getLoginRelativeUrl() : "")
                    .envData(StringUtil.isNotEmptyOrNull(queuedExecution.getManualUrlParameters().getEnvData()) ? queuedExecution.getManualUrlParameters().getEnvData() : "")
                    .build();
        } else {
            manualUrlParameters = ManualUrlParameters.builder()
                    .contextRoot("")
                    .envData("")
                    .host("")
                    .loginRelativeUrl("")
                    .build();
        }
        String tag = StringUtil.isNotEmptyOrNull(queuedExecution.getTag())
                ? queuedExecution.getTag()
                : campaign.getTag();
        int screenshot = queuedExecution.getScreenshot() != null
                ? queuedExecution.getScreenshot()
                : ParameterParserUtil.parseIntegerParamAndDecode(campaign.getScreenshot(), 1, "UTF-8");
        int video = queuedExecution.getVideo() != null
                ? queuedExecution.getVideo()
                : ParameterParserUtil.parseIntegerParamAndDecode(campaign.getVideo(), 1, "UTF-8");
        int verbose = queuedExecution.getVerbose() != null
                ? queuedExecution.getVerbose()
                : ParameterParserUtil.parseIntegerParamAndDecode(campaign.getVerbose(), 1, "UTF-8");
        String timeout = StringUtil.isNotEmptyOrNull(queuedExecution.getTimeout())
                ? queuedExecution.getTimeout()
                : ParameterParserUtil.parseStringParamAndDecode(campaign.getTimeout(), "30000", "UTF-8");
        int pageSource = queuedExecution.getPageSource() != null
                ? queuedExecution.getPageSource()
                : ParameterParserUtil.parseIntegerParamAndDecode(campaign.getPageSource(), 1, "UTF-8");
        int robotLog = queuedExecution.getRobotLog() != null
                ? queuedExecution.getRobotLog()
                : ParameterParserUtil.parseIntegerParamAndDecode(campaign.getRobotLog(), 1, "UTF-8");
        int consoleLog = queuedExecution.getConsoleLog() != null
                ? queuedExecution.getConsoleLog()
                : ParameterParserUtil.parseIntegerParamAndDecode(campaign.getConsoleLog(), 1, "UTF-8");
        String manualExecution = StringUtil.isNotEmptyOrNull(queuedExecution.getManualExecution())
                ? queuedExecution.getManualExecution()
                : ParameterParserUtil.parseStringParamAndDecode(campaign.getManualExecution(), "N", "UTF-8");
        int retries = queuedExecution.getRetries() != null
                ? queuedExecution.getRetries()
                : ParameterParserUtil.parseIntegerParamAndDecode(campaign.getRetries(), 0, "UTF-8");
        int priority = queuedExecution.getPriority() != null
                ? queuedExecution.getPriority()
                : ParameterParserUtil.parseIntegerParamAndDecode(campaign.getPriority(), 0, "UTF-8");
        String user = principal != null ? principal.getName() : "";

        screenshot = screenshotRetrocompatibility(screenshot);

        verifyTestcasesListConsistency(selectedTestcases);

        if (manualUrl == 1 && (StringUtil.isEmptyOrNull(manualUrlParameters.getHost()) || StringUtil.isEmptyOrNull(manualUrlParameters.getEnvData()))) {
            throw new InvalidRequestException("manualUrl is activated so host and envData of manualUrlParameters are required");
        }

        tag = generateTagAndControlTag(tag, campaign, principal);

        AnswerItem<Map<String, List<String>>> parsedCampaignParameters = campaignParameterService.parseParametersByCampaign(campaign.getCampaign());
        if (CollectionUtils.isEmpty(countries)) {
            countries = parsedCampaignParameters.getItem().get(CampaignParameter.COUNTRY_PARAMETER);
            if (CollectionUtils.isEmpty(countries)) {
                throw new InvalidRequestException("No countries found for this campaign");
            }
        }
        if (CollectionUtils.isEmpty(environments)) {
            environments = parsedCampaignParameters.getItem().get(CampaignParameter.ENVIRONMENT_PARAMETER);
            if (CollectionUtils.isEmpty(environments) && manualUrl != 1) {
                throw new InvalidRequestException("No environments found for this campaign");
            }
        }
        if (CollectionUtils.isEmpty(robots)) {
            robots = parsedCampaignParameters.getItem().get(CampaignParameter.ROBOT_PARAMETER);
        }

        String requestedCountries = StringUtil.convertToString(countries, parameterService.getParameterStringByKey("cerberus_tagvariable_separator", "", "-"));
        String requestedEnvironments = StringUtil.convertToString(environments, parameterService.getParameterStringByKey("cerberus_tagvariable_separator", "", "-"));

        if (CollectionUtils.isEmpty(selectedTestcases)) {
            AnswerList<TestCase> testcases = testCaseService.findTestCaseByCampaignNameAndCountries(campaign.getCampaign(), countries.toArray(new String[countries.size()]));
            if (testcases != null) {
                if (CollectionUtils.isNotEmpty(testcases.getDataList())) {
                    for (TestCase campaignTestCase : testcases.getDataList()) {
                        selectedTestcases.add(QueuedExecutionTestcase.builder()
                                .testFolderId(campaignTestCase.getTest())
                                .testcaseId(campaignTestCase.getTestcase())
                                .build());
                    }
                }
            } else {
                throw new InvalidRequestException("No testcases found for this campaign");
            }
        }

        tag = replaceTagParameters(tag, user, requestedCountries, requestedEnvironments);

        int nbRobots;
        Map<String, Robot> robotsMap = new HashMap<>();
        LOG.debug("ROBOTS {}", robots);
        LOG.debug("MANUALEXEC {}", manualExecution);
        if (CollectionUtils.isEmpty(robots)) {
            if (manualExecution.equalsIgnoreCase("Y") || manualExecution.equalsIgnoreCase("A")) {
                robotsMap.put("", robotFactory.create(0, "", "", "", "", true, "", "", "", "", "", 0, "", true, "", ""));
            } else {
                throw new InvalidRequestException("No robots found for this campaign");
            }
            nbRobots = 1;
        } else { // Not RobotIP defined but at least 1 robot has been found from servlet call or campaign definition.
            nbRobots = robots.size();
            try {
                // Load the map of robot from input.
                robotsMap = robotService.readToHashMapByRobotList(robots); // load Robots available for the campaign
                nbRobots = robotsMap.size();
            } catch (CerberusException ex) {
                LOG.warn(ex.toString(), ex);
            }
        }
        return addToQueue(campaign, selectedTestcases, environments, countries, robotsMap, manualUrlParameters, tag, user,
                manualUrl, screenshot, verbose, video, pageSource, robotLog, consoleLog, timeout, retries, manualExecution, priority, nbRobots, robots);
    }

    public QueuedExecutionResult addTestcasesToExecutionQueue(QueuedExecution queuedExecution, Principal principal) {

        cleanStringAndListOfDefaultValueFromSwagger(queuedExecution);
        List<QueuedExecutionTestcase> selectedTestcases = CollectionUtils.isNotEmpty(queuedExecution.getTestcases()) ? queuedExecution.getTestcases() : new ArrayList<>();
        List<String> countries = CollectionUtils.isNotEmpty(queuedExecution.getCountries()) ? queuedExecution.getCountries() : new ArrayList<>();
        List<String> environments = CollectionUtils.isNotEmpty(queuedExecution.getEnvironments()) ? queuedExecution.getEnvironments() : new ArrayList<>();
        List<String> robots = CollectionUtils.isNotEmpty(queuedExecution.getRobots()) ? queuedExecution.getRobots() : new ArrayList<>();
        int manualUrl = queuedExecution.getManualUrl() != null
                ? queuedExecution.getManualUrl()
                : 0;
        ManualUrlParameters manualUrlParameters;
        if (queuedExecution.getManualUrlParameters() != null) {
            manualUrlParameters = ManualUrlParameters.builder()
                    .host(StringUtil.isNotEmptyOrNull(queuedExecution.getManualUrlParameters().getHost()) ? queuedExecution.getManualUrlParameters().getHost() : "")
                    .contextRoot(StringUtil.isNotEmptyOrNull(queuedExecution.getManualUrlParameters().getContextRoot()) ? queuedExecution.getManualUrlParameters().getContextRoot() : "")
                    .loginRelativeUrl(StringUtil.isNotEmptyOrNull(queuedExecution.getManualUrlParameters().getLoginRelativeUrl()) ? queuedExecution.getManualUrlParameters().getLoginRelativeUrl() : "")
                    .envData(StringUtil.isNotEmptyOrNull(queuedExecution.getManualUrlParameters().getEnvData()) ? queuedExecution.getManualUrlParameters().getEnvData() : "")
                    .build();
        } else {
            manualUrlParameters = ManualUrlParameters.builder()
                    .contextRoot("")
                    .envData("")
                    .host("")
                    .loginRelativeUrl("")
                    .build();
        }
        String tag = queuedExecution.getTag();
        int screenshot = queuedExecution.getScreenshot() != null ? queuedExecution.getScreenshot() : 1;
        int video = queuedExecution.getVideo() != null ? queuedExecution.getVideo() : 1;
        int verbose = queuedExecution.getVerbose() != null ? queuedExecution.getVerbose() : 1;
        String timeout = StringUtil.isNotEmptyOrNull(queuedExecution.getTimeout()) ? queuedExecution.getTimeout() : "30000";
        int pageSource = queuedExecution.getPageSource() != null ? queuedExecution.getPageSource() : 1;
        int robotLog = queuedExecution.getRobotLog() != null ? queuedExecution.getRobotLog() : 1;
        int consoleLog = queuedExecution.getConsoleLog() != null ? queuedExecution.getConsoleLog() : 1;
        String manualExecution = StringUtil.isNotEmptyOrNull(queuedExecution.getManualExecution()) ? queuedExecution.getManualExecution() : "N";
        int retries = queuedExecution.getRetries() != null ? queuedExecution.getRetries() : 0;
        int priority = queuedExecution.getPriority() != null ? queuedExecution.getPriority() : 0;
        String user = principal != null ? principal.getName() : "";

        screenshot = screenshotRetrocompatibility(screenshot);

        if (CollectionUtils.isEmpty(selectedTestcases)) {
            throw new InvalidRequestException("testcases are required");
        }

        String requestedCountries = StringUtil.convertToString(countries, parameterService.getParameterStringByKey("cerberus_tagvariable_separator", "", "-"));
        String requestedEnvironments = StringUtil.convertToString(environments, parameterService.getParameterStringByKey("cerberus_tagvariable_separator", "", "-"));

        verifyTestcasesListConsistency(selectedTestcases);

        if (manualUrl == 1 && (StringUtil.isEmptyOrNull(manualUrlParameters.getHost()) || StringUtil.isEmptyOrNull(manualUrlParameters.getEnvData()))) {
            throw new InvalidRequestException("manualUrl is activated so host and envData of manualUrlParameters are required.");
        }

        tag = generateTagAndControlTag(tag, null, principal);

        tag = replaceTagParameters(tag, user, requestedCountries, requestedEnvironments);

        if (CollectionUtils.isEmpty(countries)) {
            throw new InvalidRequestException("countries are required.");
        }
        if (CollectionUtils.isEmpty(environments)) {
            throw new InvalidRequestException("environments are required.");
        }
        if (CollectionUtils.isEmpty(robots)) {
            throw new InvalidRequestException("robots are required.");
        }

        int nbRobots = robots.size();
        Map<String, Robot> robotsMap = new HashMap<>();
        try {
            // Load the map of robot from input.
            robotsMap = robotService.readToHashMapByRobotList(robots); // load Robots available for the campaign
            nbRobots = robotsMap.size();
        } catch (CerberusException ex) {
            LOG.warn(ex.toString(), ex);
        }

        return addToQueue(null, selectedTestcases, environments, countries, robotsMap, manualUrlParameters, tag, user,
                manualUrl, screenshot, verbose, video, pageSource, robotLog, consoleLog, timeout, retries, manualExecution, priority, nbRobots, robots);
    }

    private HashMap<String, Application> updateMapWithApplication(String application, HashMap<String, Application> appMap) throws CerberusException {
        if (!appMap.containsKey(application)) {
            Application app = applicationService.convert(applicationService.readByKey(application));
            appMap.put(application, app);
        }
        return appMap;
    }

    /**
     * @param manualHost
     * @param application
     * @return
     */
    private String getManualHostForThisApplication(Map<String, String> manualHost, String application) {

        if (manualHost.containsKey("")) {
            return manualHost.get("");
        }
        if (manualHost.containsKey(application)) {
            return manualHost.get(application);
        }
        return "";
    }

    /**
     * Convert manualhost parameter to MAP. manual host can be just
     * 'manualHost1' (case 1) or
     * `applicationname1:manualhost1;applicationname2:manualhost2;...` (cases 2)
     * or a json string in format : { "applicationname1" : "manualhost1",
     * "applicationname2" : "manualhost2" } (case 3)
     *
     * @param manualHost
     * @return a Map of application : url
     */
    private Map<String, String> getManualHostMap(String manualHost) {
        Map<String, String> myHostMap = new HashMap<>();
        if (StringUtil.isEmptyOrNull(manualHost)) {
            LOG.debug("Converting from empty.");
            myHostMap.put("", "");
            return myHostMap;
        }
        try {
            JSONObject myJSONObj = new JSONObject(manualHost);
            Iterator<?> nameItr = myJSONObj.keys();
            LOG.debug("Converting from JSON.");
            while (nameItr.hasNext()) {
                String name = (String) nameItr.next();
                myHostMap.put(name, myJSONObj.getString(name));
            }
            return myHostMap;
        } catch (JSONException ex) {
            // parameter could not be converted to JSON Array so we try with the : and ; separators.
            String newManualHost = "";
            // Remove the http:// and https:// in order to avoid conflict with : split that will be done
            if (!StringUtil.isEmptyOrNull(manualHost)) {
                newManualHost = manualHost.replace("http://", "|ZZZHTTPZZZ|");
                newManualHost = newManualHost.replace("https://", "|ZZZHTTPSZZZ|");
            }
            if (!StringUtil.isEmptyOrNull(manualHost) && !newManualHost.contains(":")) {
                LOG.debug("Converting from string.");
                myHostMap.put("", manualHost);
                return myHostMap; // if no :, just return manual host (case 1)
            }
            // (case 2)
            if (!StringUtil.isEmptyOrNull(manualHost)) {
                LOG.debug("Converting from separator.");
                String[] manualHostByApp = newManualHost.split(";");
                for (String appManualHost : manualHostByApp) {
                    String[] appAndHost = appManualHost.split(":");
                    myHostMap.put(appAndHost[0], appAndHost[1].replace("|ZZZHTTPZZZ|", "http://").replace("|ZZZHTTPSZZZ|", "https://"));
                }
                return myHostMap;
            }
        }
        return myHostMap;
    }

    private QueuedExecution cleanStringAndListOfDefaultValueFromSwagger(QueuedExecution queuedExecution) {
        if (CollectionUtils.isNotEmpty(queuedExecution.getTestcases()) && queuedExecution.getTestcases().size() == 1) {
            if (StringUtil.isNotEmptyOrNull(queuedExecution.getTestcases().get(0).getTestcaseId()) && queuedExecution.getTestcases().get(0).getTestcaseId().equals("string")
                    || StringUtil.isNotEmptyOrNull(queuedExecution.getTestcases().get(0).getTestFolderId()) && queuedExecution.getTestcases().get(0).getTestFolderId().equals("string")) {
                queuedExecution.getTestcases().clear();
            }
        }
        if (CollectionUtils.isNotEmpty(queuedExecution.getCountries()) && queuedExecution.getCountries().contains("string") && queuedExecution.getCountries().size() == 1) {
            queuedExecution.getCountries().clear();
        }
        if (CollectionUtils.isNotEmpty(queuedExecution.getEnvironments()) && queuedExecution.getEnvironments().contains("string") && queuedExecution.getEnvironments().size() == 1) {
            queuedExecution.getEnvironments().clear();
        }
        if (CollectionUtils.isNotEmpty(queuedExecution.getRobots()) && queuedExecution.getRobots().contains("string") && queuedExecution.getRobots().size() == 1) {
            queuedExecution.getRobots().clear();
        }
        if (StringUtil.isNotEmptyOrNull(queuedExecution.getTag()) && queuedExecution.getTag().equals("string")) {
            queuedExecution.setTag("");
        }
        if (StringUtil.isNotEmptyOrNull(queuedExecution.getManualExecution()) && queuedExecution.getManualExecution().equals("string")) {
            queuedExecution.setManualExecution("");
        }
        if (queuedExecution.getManualUrlParameters() != null) {
            if (StringUtil.isNotEmptyOrNull(queuedExecution.getManualUrlParameters().getHost()) && queuedExecution.getManualUrlParameters().getHost().equals("string")) {
                queuedExecution.getManualUrlParameters().setHost("");
            }
            if (StringUtil.isNotEmptyOrNull(queuedExecution.getManualUrlParameters().getContextRoot()) && queuedExecution.getManualUrlParameters().getContextRoot().equals("string")) {
                queuedExecution.getManualUrlParameters().setContextRoot("");
            }
            if (StringUtil.isNotEmptyOrNull(queuedExecution.getManualUrlParameters().getLoginRelativeUrl()) && queuedExecution.getManualUrlParameters().getLoginRelativeUrl().equals("string")) {
                queuedExecution.getManualUrlParameters().setLoginRelativeUrl("");
            }
            if (StringUtil.isNotEmptyOrNull(queuedExecution.getManualUrlParameters().getEnvData()) && queuedExecution.getManualUrlParameters().getEnvData().equals("string")) {
                queuedExecution.getManualUrlParameters().setEnvData("");
            }
        }

        return queuedExecution;
    }

    private QueuedExecutionResult addToQueue(Campaign campaign, List<QueuedExecutionTestcase> selectedTestcases,
            List<String> environments, List<String> countries, Map<String, Robot> robotsMap,
            ManualUrlParameters manualUrlParameters, String tag, String user, int manualUrl,
            int screenshot, int verbose, int video, int pageSource, int robotLog, int consoleLog,
            String timeout, int retries, String manualExecution, int priority, int nbRobots, List<String> robots) {
        int nbExe = 0;
        int nbTestcaseNotActive = 0;
        int nbTestcaseNotExist = 0;
        int nbTestcaseEnvGroupNotAllowed = 0;
        int nbEnvNotExist = 0;
        int nbRobotMissing = 0;
        boolean tagAlreadyAdded = false;
        int nbEnvironments = environments.size();
        int nbCountries = countries.size();

        HashMap<String, Application> appMap = new HashMap<>();

        Map<String, String> invariantEnvMap = invariantService.readToHashMapGp1StringByIdname("ENVIRONMENT", "");
        invariantEnvMap.put("MANUAL", "");

        Map<String, String> myHostMap;
        myHostMap = getManualHostMap(manualUrlParameters.getHost());

        List<String> messages = new ArrayList<>();

        // Part 1: Getting all possible Execution from test cases + countries + environments + browsers which have been sent to this servlet.
        List<TestCaseExecutionQueue> toInserts = new ArrayList<>();
        try {

            HashMap<String, CountryEnvParam> envMap = new HashMap<>();
            LOG.debug("Loading all environments.");
            for (CountryEnvParam envParam : countryEnvParamService.convert(countryEnvParamService.readActiveBySystem(null))) {
                envMap.put(envParam.getSystem() + LOCAL_SEPARATOR + envParam.getCountry() + LOCAL_SEPARATOR + envParam.getEnvironment(), envParam);
            }

            HashMap<String, CountryEnvironmentParameters> envAppMap = new HashMap<>();
            LOG.debug("Loading all environments.");
            for (CountryEnvironmentParameters envAppParam : countryEnvironmentParametersService.convert(countryEnvironmentParametersService.readByVarious(null, null, null, null))) {
                envAppMap.put(envAppParam.getSystem() + LOCAL_SEPARATOR + envAppParam.getCountry() + LOCAL_SEPARATOR + envAppParam.getEnvironment() + LOCAL_SEPARATOR + envAppParam.getApplication(), envAppParam);
            }

            if (campaign != null && !StringUtil.isEmptyOrNull(campaign.getCampaign())) {
                LOG.info("Campaign [{}] asked to triggered.", campaign);
                LOG.info("[{}] testcases: {}", campaign, selectedTestcases);
                LOG.info("[{}] environments: {}", campaign, environments);
                LOG.info("[{}] countries: {}", campaign, countries);
                LOG.info("[{}] robots: {}", campaign, robotsMap);
            }
            LOG.debug("Nb of Testcases: {}", selectedTestcases.size());
            for (QueuedExecutionTestcase selectedTestcase : selectedTestcases) {
                String test = selectedTestcase.getTestFolderId();
                String testCase = selectedTestcase.getTestcaseId();
                TestCase tc = testCaseService.convert(testCaseService.readByKey(test, testCase));
                // TestCases that are not active are not inserted into queue.
                if (tc != null) {
                    if (tc.isActive()) {
                        // We only insert testcase that exist for the given country.
                        for (TestCaseCountry country : testCaseCountryService.convert(testCaseCountryService.readByTestTestCase(null, test, testCase, null))) {
                            if (countries.contains(country.getCountry())) {
                                // for each environment we test that correspondng gp1 is compatible with testcase environment flag activation.
                                for (String environment : environments) {
                                    String envGp1 = invariantEnvMap.get(environment);
                                    if (StringUtil.isEmptyOrNull(envGp1)) {
                                        envGp1 = "";
                                    }
                                    if (((envGp1.equals("PROD")) && tc.isActivePROD())
                                            || ((envGp1.equals("UAT")) && tc.isActiveUAT())
                                            || ((envGp1.equals("QA")) && tc.isActiveQA())
                                            || (envGp1.equals("DEV"))
                                            || (envGp1.isEmpty())) {
                                        // Getting Application in order to check application type against browser.
                                        appMap = updateMapWithApplication(tc.getApplication(), appMap);
                                        Application app = appMap.get(tc.getApplication());
                                        if ((envMap.containsKey(app.getSystem() + LOCAL_SEPARATOR + country.getCountry() + LOCAL_SEPARATOR + environment))
                                                || (environment.equals("MANUAL"))) {

                                            if ((envAppMap.containsKey(app.getSystem() + LOCAL_SEPARATOR + country.getCountry() + LOCAL_SEPARATOR + environment + LOCAL_SEPARATOR + app.getApplication()))
                                                    || (environment.equals("MANUAL"))) {

                                                // Create Tag only if not already done and defined.
                                                if (!StringUtil.isEmptyOrNull(tag) && !tagAlreadyAdded) {
                                                    // We create or update it.
                                                    if (campaign != null) {
                                                        tagService.createAuto(tag, campaign.getCampaign(), user, new JSONArray(environments), new JSONArray(countries));
                                                    } else {
                                                        tagService.createAuto(tag, "", user, new JSONArray(environments), new JSONArray(countries));
                                                    }
                                                    tagAlreadyAdded = true;
                                                }

                                                // manage manual host for this execution
                                                String manualHostforThisApplication = getManualHostForThisApplication(myHostMap, app.getApplication());

                                                if ((app != null)
                                                        && (app.getType() != null)
                                                        && (app.getType().equalsIgnoreCase(Application.TYPE_GUI) || app.getType().equalsIgnoreCase(Application.TYPE_APK)
                                                        || app.getType().equalsIgnoreCase(Application.TYPE_IPA) || app.getType().equalsIgnoreCase(Application.TYPE_FAT))) {

                                                    for (Map.Entry<String, Robot> entry : robotsMap.entrySet()) {
                                                        String key = entry.getKey();
                                                        Robot robot = entry.getValue();

                                                        try {
                                                            if ("".equals(robot.getType()) || app.getType().equals(robot.getType())) {
                                                                // Robot type is not feeded (not attached to any techno) or robot type match the one of the application.
                                                                LOG.debug("Insert Queue Entry.");
                                                                // We get here the corresponding robotDecli value from robot.
                                                                String robotDecli = robot.getRobotDecli();
                                                                if (StringUtil.isEmptyOrNull(robotDecli)) {
                                                                    robotDecli = robot.getRobot();
                                                                }
                                                                if ("".equals(robot.getRobot()) && !(manualExecution.equalsIgnoreCase("Y") || manualExecution.equalsIgnoreCase("A"))) {
                                                                    // We don't insert the execution for robot application that have no robot and if it's not manual execution
                                                                    nbRobotMissing++;
                                                                } else {
                                                                    toInserts.add(inQueueFactoryService.create(app.getSystem(),
                                                                            test, testCase, country.getCountry(), environment,
                                                                            robot.getRobot(), robotDecli, "", "", robot.getBrowser(),
                                                                            robot.getVersion(), robot.getPlatform(), robot.getScreenSize(), manualUrl,
                                                                            manualHostforThisApplication, manualUrlParameters.getContextRoot(),
                                                                            manualUrlParameters.getLoginRelativeUrl(), manualUrlParameters.getEnvData(), tag,
                                                                            screenshot, video, verbose, timeout, pageSource,
                                                                            robotLog, consoleLog, 0, retries, manualExecution, priority,
                                                                            user, null, null, null));
                                                                }
                                                            } else {
                                                                LOG.debug("Not inserted because app type {} does not match robot type {}", app.getType(), robot.getType());
                                                                messages.add(String.format("%s-%s for %s-%s not inserted because app type '%s' doesn't match robot type '%s'", test, testCase, environment, country.getCountry(), app.getType(), robot.getType()));
                                                            }
                                                        } catch (FactoryCreationException e) {
                                                            LOG.error("Unable to insert record due to: {}", e.toString());
                                                            LOG.error("test: {}-{}-{}-{}-{}", test, testCase, country.getCountry(), environment, robots);
                                                            messages.add(String.format("Unable to insert %s-%s-%s-%s-%s due to: %s", test, testCase, country.getCountry(), environment, robots, e));
                                                        }
                                                    }

                                                } else {
                                                    // Application does not need any robot so we force an empty value.
                                                    LOG.debug("Forcing Robot to empty value. Application type={}", app.getType());
                                                    try {
                                                        LOG.debug("Insert Queue Entry.");
                                                        toInserts.add(inQueueFactoryService.create(app.getSystem(), test,
                                                                testCase, country.getCountry(), environment, "", "", "", "",
                                                                "", "", "", "", manualUrl, manualHostforThisApplication, manualUrlParameters.getContextRoot(),
                                                                manualUrlParameters.getLoginRelativeUrl(), manualUrlParameters.getEnvData(), tag, screenshot, video,
                                                                verbose, timeout, pageSource, robotLog, consoleLog, 0, retries,
                                                                manualExecution, priority, user, null, null, null));
                                                    } catch (FactoryCreationException e) {
                                                        LOG.error("Unable to insert record due to: {}", e.toString());
                                                        LOG.error("test: {}-{}-{}-{}-{}", test, testCase, country.getCountry(), environment, robots);
                                                        messages.add(String.format("Unable to insert %s-%s-%s-%s-%s due to: %s", test, testCase, country.getCountry(), environment, robots, e));
                                                    }
                                                }
                                            } else {
                                                LOG.debug("Env Application does not exist.");
                                                messages.add(String.format("Environment '%s' and Country %s doesn't exist for application '%s'", environment, country.getCountry(), tc.getApplication()));
                                                nbEnvNotExist += nbRobots;
                                            }
                                        } else {
                                            LOG.debug("Env does not exist or is not active.");
                                            messages.add(String.format("Environment '%s' doesn't exist or isn't active for country '%s'", environment, country.getCountry()));
                                            nbEnvNotExist += nbRobots;
                                        }

                                    } else {
                                        LOG.debug("Env group not active for testcase: {}", environment);
                                        messages.add(String.format("Environment group not active for %s - %s - %s: %s", test, testCase, country.getCountry(), environment));
                                        nbTestcaseEnvGroupNotAllowed += nbRobots;
                                    }
                                }
                            } else {
                                LOG.debug("Country does not match. {} {}", countries, country.getCountry());
                            }
                        }
                    } else {
                        LOG.debug("TestCase not Active.");
                        nbTestcaseNotActive += (nbCountries * nbEnvironments * nbRobots);
                        messages.add(String.format("Testcase %s - %s isn't active", test, testCase));
                    }
                } else {
                    LOG.debug("TestCase not exist.");
                    nbTestcaseNotExist += (nbCountries * nbEnvironments * nbRobots);
                    messages.add(String.format("Testcase %s - %s doesn't exist", test, testCase));
                    LOG.warn("Testcase {} - {} doesn't exist.", test, testCase);
                }
            }
        } catch (CerberusException ex) {
            LOG.warn(ex);
        }

        Map<String, TestCaseExecutionQueue> testCasesInserted = new HashMap<>();
        for (TestCaseExecutionQueue toInsert : toInserts) {
            if (!testCasesInserted.containsKey(inQueueService.getUniqKey(toInsert.getTest(), toInsert.getTestCase(), toInsert.getCountry(), toInsert.getEnvironment()))) {
                testCasesInserted.put(inQueueService.getUniqKey(toInsert.getTest(), toInsert.getTestCase(), toInsert.getCountry(), toInsert.getEnvironment()), toInsert);
            }
        }

        // Part 2a: Try to insert all these test cases to the execution queue.
        List<Long> queueInsertedIds = new ArrayList<>();
        List<QueuedEntry> queuedEntries = new ArrayList<>();
        for (TestCaseExecutionQueue toInsert : toInserts) {
            try {
                inQueueService.convert(inQueueService.create(toInsert, true, 0, TestCaseExecutionQueue.State.QUTEMP, testCasesInserted));
                nbExe++;
                queuedEntries.add(
                        QueuedEntry.builder()
                                .queueId(toInsert.getId())
                                .testFolderId(toInsert.getTest())
                                .testcaseId(toInsert.getTestCase())
                                .country(toInsert.getCountry())
                                .environment(toInsert.getEnvironment())
                                .build());
                queueInsertedIds.add(toInsert.getId());
            } catch (CerberusException exception) {
                LOG.warn(String.format("Unable to insert %s due to %s", toInsert.toString(), exception.getMessage()));
                throw new FailedInsertOperationException(String.format("Unable to insert %s due to %s", toInsert, exception.getMessage()));
            }
        }

        // Part 2b: move all the execution queue from tag to QUEUE state.
        inQueueService.updateAllTagToQueuedFromQuTemp(tag, queueInsertedIds);

        // Part 3 : Trigger JobQueue
        try {
            executionThreadService.executeNextInQueueAsynchroneously(false);
        } catch (CerberusException ex) {
            LOG.warn(String.format("Unable to feed the execution queue due to {%s}", ex.getMessage()));
            throw new FailedInsertOperationException(String.format("Unable to feed the execution queue due to {%s}", ex.getMessage()));
        }

        messages.add(String.format("%s execution(s) successfully inserted to queue.", nbExe));

        return QueuedExecutionResult.builder()
                .tag(tag)
                .nbExecutions(nbExe)
                .environmentsNotExistOrNotActive(nbEnvNotExist)
                .testcasesNotAllowedOnEnvironment(nbTestcaseEnvGroupNotAllowed)
                .testcasesNotActive(nbTestcaseNotActive)
                .testcasesNotExist(nbTestcaseNotExist)
                .robotsMissing(nbRobotMissing)
                .messages(messages)
                .queuedEntries(queuedEntries)
                .build();
    }

    private void verifyTestcasesListConsistency(List<QueuedExecutionTestcase> selectedTestcases) {
        if (CollectionUtils.isNotEmpty(selectedTestcases)) {
            for (QueuedExecutionTestcase queuedExecutionTestcase : selectedTestcases) {
                if (StringUtil.isEmptyOrNull(queuedExecutionTestcase.getTestFolderId())) {
                    throw new InvalidRequestException("testFolderId is required for each testcase object");
                }
                if (StringUtil.isEmptyOrNull(queuedExecutionTestcase.getTestcaseId())) {
                    throw new InvalidRequestException("testCaseId is required for each testcase object");
                }
            }
        }
    }

    private String replaceTagParameters(String tag, String user, String requestedCountries, String requestedEnvironments) {
        return tag
                .replace("%TIMESTAMP%", new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Timestamp(System.currentTimeMillis())))
                .replace("%USER%", user)
                .replace("%REQCOUNTRYLIST%", requestedCountries)
                .replace("%REQENVIRONMENTLIST%", requestedEnvironments);
    }

    private int screenshotRetrocompatibility(int screenshot) {
        if (screenshot == 3) {
            screenshot = 1;
        }
        if (screenshot == 4) {
            screenshot = 2;
        }
        return screenshot;
    }

    private String generateTagAndControlTag(String tag, Campaign campaign, Principal principal) {
        if (StringUtil.isEmptyOrNull(tag)) {
            if (principal != null && StringUtil.isNotEmptyOrNull(principal.getName())) {
                tag = principal.getName() + ".";
            }
            if (campaign != null) {
                tag += campaign.getCampaign() + ".";
            }
            tag += new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Timestamp(System.currentTimeMillis()));
        } else {
            if (tag.length() > 255) {
                throw new InvalidRequestException(String.format("Tag length is too big. Maximum size is 255. Current size is %s.", tag.length()));
            }
        }
        return tag;
    }
}
