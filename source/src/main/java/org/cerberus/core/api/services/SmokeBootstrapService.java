/*
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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

import java.net.URI;
import java.security.Principal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.dto.bootstrap.SmokeBootstrapRequestDTO;
import org.cerberus.core.api.entity.QueuedExecution;
import org.cerberus.core.api.entity.QueuedExecutionResult;
import org.cerberus.core.api.exceptions.InvalidRequestException;
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.Campaign;
import org.cerberus.core.crud.entity.CampaignLabel;
import org.cerberus.core.crud.entity.CampaignParameter;
import org.cerberus.core.crud.entity.CountryEnvParam;
import org.cerberus.core.crud.entity.CountryEnvironmentParameters;
import org.cerberus.core.crud.entity.EventHook;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.entity.Label;
import org.cerberus.core.crud.entity.Test;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseLabel;
import org.cerberus.core.crud.entity.TestDataLib;
import org.cerberus.core.crud.entity.TestDataLibData;
import org.cerberus.core.crud.entity.User;
import org.cerberus.core.crud.entity.UserRole;
import org.cerberus.core.crud.entity.UserSystem;
import org.cerberus.core.crud.factory.IFactoryUser;
import org.cerberus.core.crud.factory.IFactoryUserRole;
import org.cerberus.core.crud.factory.IFactoryUserSystem;
import org.cerberus.core.crud.service.IAppServiceHeaderService;
import org.cerberus.core.crud.service.IAppServiceService;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.ICampaignLabelService;
import org.cerberus.core.crud.service.ICampaignParameterService;
import org.cerberus.core.crud.service.ICampaignService;
import org.cerberus.core.crud.service.ICountryEnvParamService;
import org.cerberus.core.crud.service.ICountryEnvironmentParametersService;
import org.cerberus.core.crud.service.IEventHookService;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.ILabelService;
import org.cerberus.core.crud.service.ITestCaseLabelService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.crud.service.ITestDataLibService;
import org.cerberus.core.crud.service.ITestService;
import org.cerberus.core.crud.service.IUserRoleService;
import org.cerberus.core.crud.service.IUserService;
import org.cerberus.core.crud.service.IUserSystemService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.springframework.stereotype.Service;

/**
 * Creates, or updates, everything needed to run the smoke campaign on a freshly
 * created instance: system, application, environment, test folder, testcase, login
 * data, campaign and its end of campaign email. Every call is idempotent, so the
 * same endpoint is used to create the content and to update it with the variables
 * of the instance (recipient, URL, credentials, robot).
 */
@AllArgsConstructor
@Service
public class SmokeBootstrapService {

    private static final Logger LOG = LogManager.getLogger(SmokeBootstrapService.class);

    public static final String APPLICATION_GUI = "Cerberus";
    public static final String APPLICATION_API = "CerberusAPI";
    /** Name of the campaign and label of the former single smoke, deleted as well. */
    private static final String LEGACY_NAME = "CERBERUS_SMOKE_TESTS";
    public static final String FOLDER_WEB = SmokeGuiTestcaseFactory.TEST_FOLDER;
    public static final String FOLDER_API = "Cerberus Smoke Tests API";
    public static final String CAMPAIGN_API = "CERBERUS_SMOKE_TESTS_API";
    public static final String CAMPAIGN_WEB = "CERBERUS_SMOKE_TESTS_WEB";
    public static final String LABEL_API = "CERBERUS_SMOKE_TESTS_API";
    public static final String LABEL_WEB = "CERBERUS_SMOKE_TESTS_WEB";
    public static final String DATALIB = "CERBERUS_SMOKE_USER";
    /** Roles of the smoke user : it opens every page and calls the API, the same way an administrator does. */
    private static final List<String> SMOKE_USER_ROLES = List.of("Administrator", "Test", "TestRO", "TestAdmin",
            "TestStepLibrary", "TestDataManager", "RunTest", "Label", "AS", "Integrator", "IntegratorRO",
            "IntegratorNewChain", "IntegratorDeploy");

    private final IInvariantService invariantService;
    private final IApplicationService applicationService;
    private final ICountryEnvParamService countryEnvParamService;
    private final ICountryEnvironmentParametersService countryEnvironmentParametersService;
    private final ITestService testService;
    private final ITestCaseService testCaseService;
    private final ITestDataLibService testDataLibService;
    private final ICampaignService campaignService;
    private final ICampaignParameterService campaignParameterService;
    private final ILabelService labelService;
    private final ITestCaseLabelService testCaseLabelService;
    private final ICampaignLabelService campaignLabelService;
    private final IEventHookService eventHookService;
    private final QueuedExecutionService queuedExecutionService;
    private final IAppServiceService appServiceService;
    private final IAppServiceHeaderService appServiceHeaderService;
    private final IUserService userService;
    private final IUserRoleService userRoleService;
    private final IUserSystemService userSystemService;
    private final IFactoryUser factoryUser;
    private final IFactoryUserRole factoryUserRole;
    private final IFactoryUserSystem factoryUserSystem;
    private final SmokeGuiTestcaseFactory testcaseFactory;
    private final SmokeApiTestcaseFactory apiTestcaseFactory;

    /**
     * Sets up the shared content (system, applications, environment, user, login data), then the folder, testcases,
     * label and campaign of one kind of smoke. The web and the API smokes are set up by two separate calls.
     */
    public Map<String, Object> setup(SmokeBootstrapRequestDTO request, String login, boolean web) {
        validate(request);
        request.setCountry(request.getCountry().trim().toUpperCase(Locale.ROOT));
        List<String> actions = new ArrayList<>();

        ensureInvariant("SYSTEM", request.getSystem(), login, actions);
        ensureApplication(APPLICATION_GUI, Application.TYPE_GUI, "Cerberus instance under smoke test", 10, request, login, actions);
        ensureApplication(APPLICATION_API, Application.TYPE_SRV, "Public API of the Cerberus instance under smoke test", 20, request, login, actions);
        ensureEnvironment(request, login, actions);
        String folder = web ? FOLDER_WEB : FOLDER_API;
        String campaign = web ? CAMPAIGN_WEB : CAMPAIGN_API;
        String label = web ? LABEL_WEB : LABEL_API;
        resetTestFolder(folder, login, actions);
        String apiKey = ensureSmokeUser(request, login, actions);
        ensureDataLib(request, apiKey, login, actions);
        List<String> testcaseIds = web ? ensureWebTestCases(request, login, actions) : ensureApiTestCases(request, login, actions);
        Integer labelId = ensureLabel(request, label, (web ? "Smoke web" : "Smoke API") + " tests created by the instance bootstrap",
                web ? "#2c7be5" : "#00a389", folder, testcaseIds, login, actions);
        ensureCampaign(campaign, (web ? "Smoke web" : "Smoke API") + " campaign created by the instance bootstrap", request, labelId, login, actions);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("campaign", campaign);
        result.put("actions", actions);
        if (request.isLaunch()) {
            QueuedExecution queuedExecution = QueuedExecution.builder().testcases(new ArrayList<>()).build();
            Principal principal = () -> login;
            QueuedExecutionResult queued = queuedExecutionService.addCampaignToExecutionQueue(campaign, queuedExecution, principal);
            result.put("tag", queued.getTag());
            result.put("nbExecutions", queued.getNbExecutions());
            result.put("messages", queued.getMessages());
        }
        return result;
    }

    /**
     * Deletes the smoke content : campaigns (web, API and the former single one), their end of campaign hooks and
     * labels, test folders with their testcases, and the API services. The shared content (system, applications,
     * environment, smoke user, login data) is kept, it may be used by something else.
     */
    public Map<String, Object> teardown(String system) {
        List<String> actions = new ArrayList<>();
        for (String campaign : List.of(CAMPAIGN_WEB, CAMPAIGN_API, LEGACY_NAME)) {
            if (campaignService.readByKey(campaign).getItem() != null) {
                check(eventHookService.deleteBycampaign(campaign), "hooks of campaign " + campaign);
                Campaign existing = campaignService.readByKey(campaign).getItem();
                check(campaignService.delete(existing), "campaign " + campaign);
                actions.add("campaign " + campaign + " deleted");
            }
        }
        for (String label : List.of(LABEL_WEB, LABEL_API, LEGACY_NAME)) {
            Integer labelId = findLabelId(system, label);
            if (labelId != null) {
                Label existing = new Label();
                existing.setId(labelId);
                existing.setSystem(system);
                existing.setLabel(label);
                check(labelService.delete(existing), "label " + label);
                actions.add("label " + label + " deleted");
            }
        }
        for (String folder : List.of(FOLDER_WEB, FOLDER_API, "Cerberus Smoke Tests")) {
            if (testService.exist(folder)) {
                Test existing = new Test();
                existing.setTest(folder);
                check(testService.delete(existing), "test folder " + folder);
                actions.add("test folder " + folder + " deleted");
            }
        }
        int services = 0;
        for (AppService existing : appServiceService.readByLikeName(SmokeApiTestcaseFactory.SERVICE_PREFIX, 10000).getDataList()) {
            if (existing.getService().startsWith(SmokeApiTestcaseFactory.SERVICE_PREFIX)) {
                check(appServiceService.delete(existing), "deletion of service " + existing.getService());
                services++;
            }
        }
        actions.add(services + " API services deleted");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("actions", actions);
        return result;
    }

    private void validate(SmokeBootstrapRequestDTO request) {
        for (Map.Entry<String, String> mandatory : Map.of(
                "baseUrl", String.valueOf(request.getBaseUrl()),
                "recipientEmail", String.valueOf(request.getRecipientEmail()),
                "testUser", String.valueOf(request.getTestUser()),
                "testPassword", String.valueOf(request.getTestPassword()),
                "country", String.valueOf(request.getCountry()),
                "robot", String.valueOf(request.getRobot())).entrySet()) {
            if (StringUtil.isEmptyOrNull(mandatory.getValue()) || "null".equals(mandatory.getValue())) {
                throw new InvalidRequestException("Field '" + mandatory.getKey() + "' is mandatory");
            }
        }
        try {
            URI uri = URI.create(request.getBaseUrl().trim());
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new InvalidRequestException("Field 'baseUrl' must be an absolute URL, ex: https://acme.cerberus-testing.com/");
            }
        } catch (IllegalArgumentException ex) {
            throw new InvalidRequestException("Field 'baseUrl' is not a valid URL");
        }
    }

    private void ensureInvariant(String idName, String value, String login, List<String> actions) {
        if (invariantService.isInvariantExist(idName, value)) {
            return;
        }
        Invariant invariant = new Invariant();
        invariant.setIdName(idName);
        invariant.setValue(value);
        invariant.setSort(100);
        invariant.setDescription(value);
        invariant.setVeryShortDesc(value);
        check(invariantService.create(invariant), "invariant " + idName + "=" + value);
        actions.add("invariant " + idName + "=" + value + " created");
    }

    private void ensureApplication(String name, String type, String description, int sort,
            SmokeBootstrapRequestDTO request, String login, List<String> actions) {
        if (applicationService.exist(name)) {
            // An existing application is left as it is.
            return;
        }
        Application application = Application.builder()
                .application(name).system(request.getSystem()).type(type)
                .description(description).sort(sort).subsystem("").repoUrl("")
                .bugTrackerConnector("NONE").bugTrackerUrl("").bugTrackerNewUrl("").poolSize(0)
                .deploytype(null).mavengroupid("").usrCreated(login).usrModif(login).build();
        check(applicationService.create(application), "application " + name);
        actions.add("application " + name + " created");
    }

    private void ensureEnvironment(SmokeBootstrapRequestDTO request, String login, List<String> actions) {
        String system = request.getSystem();
        ensureInvariant("COUNTRY", request.getCountry(), login, actions);
        ensureInvariant("ENVIRONMENT", request.getEnvironment(), login, actions);

        boolean exist = countryEnvParamService.exist(system, request.getCountry(), request.getEnvironment());
        if (!exist) {
            CountryEnvParam param = new CountryEnvParam();
            param.setSystem(system);
            param.setCountry(request.getCountry());
            param.setEnvironment(request.getEnvironment());
            param.setDescription("Smoke environment");
            param.setBuild("");
            param.setRevision("");
            param.setChain("");
            param.setDistribList("");
            param.seteMailBodyRevision("");
            param.seteMailBodyChain("");
            param.seteMailBodyDisableEnvironment("");
            param.setType("STD");
            param.setActive(true);
            param.setMaintenanceAct(false);
            param.setMaintenanceStr("00:00:00");
            param.setMaintenanceEnd("00:00:00");
            check(countryEnvParamService.create(param), "environment " + request.getEnvironment());
            actions.add("environment " + request.getCountry() + "/" + request.getEnvironment() + " created");
        }

        // The ip holds scheme and host, the url holds the context path.
        URI uri = URI.create(request.getBaseUrl().trim());
        String ip = uri.getScheme() + "://" + uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : "");
        String contextPath = StringUtil.isEmptyOrNull(uri.getPath()) || "/".equals(uri.getPath()) ? "" : uri.getPath();
        // The list replaces the parameters of the whole environment : both applications go in one call.
        List<CountryEnvironmentParameters> parameters = List.of(
                environmentParameters(request, APPLICATION_GUI, ip, contextPath, login),
                environmentParameters(request, APPLICATION_API, ip, contextPath + "/api", login));
        check(countryEnvironmentParametersService.compareListAndUpdateInsertDeleteElements(
                system, request.getCountry(), request.getEnvironment(), parameters),
                "environment parameters");
        actions.add("environment url set to " + ip + contextPath);
    }

    private CountryEnvironmentParameters environmentParameters(SmokeBootstrapRequestDTO request, String application,
            String ip, String url, String login) {
        return CountryEnvironmentParameters.builder()
                .system(request.getSystem()).country(request.getCountry()).environment(request.getEnvironment())
                .application(application).isActive(true).ip(ip).domain("").url(url).urlLogin("")
                .var1("").var2("").var3("").var4("").secret1("").secret2("")
                .mobileActivity("").mobilePackage("").poolSize(0).usrCreated(login).usrModif(login).build();
    }

    /**
     * The test folder is dedicated to one kind of smoke tests : it is deleted, with all its testcases, and created again.
     * The folder is deleted directly : the testcases go with it, without an event (mail, hook) for each of them.
     */
    private void resetTestFolder(String folder, String login, List<String> actions) {
        boolean replaced = testService.exist(folder);
        if (replaced) {
            Test existing = new Test();
            existing.setTest(folder);
            check(testService.delete(existing), "deletion of test folder " + folder);
        }
        Test test = new Test();
        test.setTest(folder);
        test.setDescription("Smoke tests created by the instance bootstrap");
        test.setActive(true);
        test.setParentTest("");
        test.setUsrCreated(login);
        check(testService.create(test), "test folder " + folder);
        actions.add("test folder " + folder + (replaced ? " replaced" : " created"));
    }

    /**
     * Makes sure the smoke user exists in the database, with an API key used by the API tests.
     * An existing user is kept as it is, only a missing API key is generated.
     *
     * @return the API key of the smoke user
     */
    private String ensureSmokeUser(SmokeBootstrapRequestDTO request, String login, List<String> actions) {
        String smokeLogin = request.getTestUser();
        User existing = userService.readByKey(smokeLogin).getItem();
        if (existing != null && !StringUtil.isEmptyOrNull(existing.getLogin())) {
            if (!StringUtil.isEmptyOrNull(existing.getApiKey())) {
                return existing.getApiKey();
            }
            String generated = storeApiKey(smokeLogin, UUID.randomUUID().toString());
            actions.add("user " + smokeLogin + " already exists, API key generated");
            return generated;
        }

        String apiKey = UUID.randomUUID().toString();
        // The password is the one of the web test : it is only used when the instance authenticates against its own database.
        User user = factoryUser.create(0, smokeLogin, request.getTestPassword(), "", "N", smokeLogin, "", "en", "", "", "", "",
                "", "", "", request.getSystem(), "", "", "", "", "", "", "", "Smoke test user", apiKey, login, null, login, null);
        check(userService.create(user), "user " + smokeLogin);
        // The insert of a user does not save the API key : it is set afterwards, on the user read back.
        storeApiKey(smokeLogin, apiKey);

        List<UserRole> roles = new ArrayList<>();
        SMOKE_USER_ROLES.forEach(role -> roles.add(factoryUserRole.create(smokeLogin, role)));
        check(userRoleService.updateRolesByUser(user, roles), "roles of user " + smokeLogin);
        List<UserSystem> systems = List.of(factoryUserSystem.create(smokeLogin, request.getSystem()));
        check(userSystemService.updateSystemsByUser(user, systems), "systems of user " + smokeLogin);

        actions.add("user " + smokeLogin + " created with an API key");
        return apiKey;
    }

    /**
     * Saves the API key on the user and checks it is the one stored, as the tests would be refused (401) otherwise.
     *
     * @return the API key stored
     */
    private String storeApiKey(String smokeLogin, String apiKey) {
        User user = userService.readByKey(smokeLogin).getItem();
        if (user == null || StringUtil.isEmptyOrNull(user.getLogin())) {
            throw new IllegalStateException("User " + smokeLogin + " cannot be read to set its API key");
        }
        user.setApiKey(apiKey);
        check(userService.update(user), "API key of user " + smokeLogin);
        User stored = userService.readByKey(smokeLogin).getItem();
        if (stored == null || !apiKey.equals(stored.getApiKey())) {
            throw new IllegalStateException("The API key of user " + smokeLogin + " was not saved");
        }
        return apiKey;
    }

    private void ensureDataLib(SmokeBootstrapRequestDTO request, String apiKey, String login, List<String> actions) {
        List<TestDataLibData> subData = new ArrayList<>();
        // The key entry (empty sub-data name) is the value returned by %property.USER%.
        subData.add(subData("", request.getTestUser()));
        subData.add(subData("Password", request.getTestPassword()));
        subData.add(subData("ApiKey", apiKey));

        TestDataLib existing = null;
        for (TestDataLib candidate : testDataLibService.readByVariousAPI(DATALIB, request.getSystem(), "", "", null)) {
            if (DATALIB.equalsIgnoreCase(candidate.getName())
                    && request.getSystem().equalsIgnoreCase(StringUtil.isEmptyOrNull(candidate.getSystem()) ? "" : candidate.getSystem())) {
                existing = candidate;
            }
        }
        TestDataLib lib = new TestDataLib();
        lib.setName(DATALIB);
        lib.setSystem(request.getSystem());
        lib.setEnvironment("");
        lib.setCountry("");
        lib.setType(TestDataLib.TYPE_INTERNAL);
        lib.setPrivateData("Y");
        lib.setDescription("Login data of the smoke web test");
        lib.setSubDataLib(subData);
        if (existing == null) {
            testDataLibService.createAPI(lib, login);
            actions.add("data library " + DATALIB + " created");
        } else {
            testDataLibService.updateAPI(existing.getTestDataLibID(), lib, false, login, true);
            actions.add("data library " + DATALIB + " updated");
        }
    }

    private TestDataLibData subData(String name, String value) {
        TestDataLibData data = new TestDataLibData();
        data.setSubData(name);
        data.setValue(value);
        data.setEncrypt("N");
        data.setColumn("");
        data.setParsingAnswer("");
        data.setColumnPosition("");
        data.setDescription("");
        return data;
    }

    /**
     * @return the ids of the web testcases, one per menu section
     */
    private List<String> ensureWebTestCases(SmokeBootstrapRequestDTO request, String login, List<String> actions) {
        // The test folder has just been emptied : the web testcases are 0001A, 0002A...
        List<String> testcaseIds = new ArrayList<>();
        for (TestCase testCase : testcaseFactory.build(request.getSystem(), APPLICATION_GUI, request.getCountry(), DATALIB, login)) {
            createTestCase(testCase);
            testcaseIds.add(testCase.getTestcase());
        }
        actions.add(testcaseIds.size() + " web testcases created in " + FOLDER_WEB + " (one per menu section)");
        return testcaseIds;
    }

    /**
     * @return the ids of the API testcases, created now or already there
     */
    private List<String> ensureApiTestCases(SmokeBootstrapRequestDTO request, String login, List<String> actions) {
        // The path variables only need to exist : the smoke data created above are used.
        Map<String, String> samples = Map.ofEntries(
                Map.entry("application", APPLICATION_GUI),
                Map.entry("system", request.getSystem()),
                Map.entry("country", request.getCountry()),
                Map.entry("environment", request.getEnvironment()),
                Map.entry("robot", request.getRobot()),
                Map.entry("testFolderId", FOLDER_API),
                Map.entry("testcaseId", "0001A"),
                Map.entry("idName", "COUNTRY"),
                Map.entry("value", request.getCountry()),
                Map.entry("stepId", "10"),
                Map.entry("actionId", "10"),
                Map.entry("controlId", "10"));
        List<SmokeApiTestcaseFactory.Endpoint> endpoints = apiTestcaseFactory.endpoints(samples);

        // The services of the smoke are deleted and created again (the testcases that used them are already gone).
        int deletedServices = 0;
        for (AppService existing : appServiceService.readByLikeName(SmokeApiTestcaseFactory.SERVICE_PREFIX, 10000).getDataList()) {
            if (existing.getService().startsWith(SmokeApiTestcaseFactory.SERVICE_PREFIX)) {
                check(appServiceService.delete(existing), "deletion of service " + existing.getService());
                deletedServices++;
            }
        }
        for (AppService service : apiTestcaseFactory.services(endpoints, APPLICATION_API, login)) {
            check(appServiceService.create(service), "service " + service.getService());
            check(appServiceHeaderService.createList(service.getHeaderList()), "headers of " + service.getService());
        }

        // The test folder has just been emptied : the API testcases are 0001A, 0002A...
        List<String> testcaseIds = new ArrayList<>();
        int lastNumber = 0;
        for (SmokeApiTestcaseFactory.Endpoint endpoint : endpoints) {
            String testcaseId = String.format("%04dA", ++lastNumber);
            testcaseIds.add(testcaseId);
            createTestCase(apiTestcaseFactory.testCase(endpoint, testcaseId, FOLDER_API,
                    request.getSystem(), APPLICATION_API, request.getCountry(), DATALIB, login));
        }
        actions.add(deletedServices + " API services replaced by " + endpoints.size() + " services and " + endpoints.size() + " testcases (one per endpoint)");
        return testcaseIds;
    }

    private void createTestCase(TestCase testCase) {
        try {
            testCaseService.createTestcaseWithDependencies(testCase);
        } catch (CerberusException | RuntimeException ex) {
            // The creation is not transactional : never leave a partial testcase, it would be skipped next time.
            testCaseService.delete(testCase);
            throw new IllegalStateException("Unable to create the testcase " + testCase.getTestcase() + ": " + ex.getMessage(), ex);
        }
    }

    /**
     * Label put on the smoke testcases of one kind (web or API), and on the campaign that runs them.
     *
     * @return the id of the label
     */
    private Integer ensureLabel(SmokeBootstrapRequestDTO request, String name, String description, String color,
            String folder, List<String> testcaseIds, String login, List<String> actions) {
        Integer labelId = findLabelId(request.getSystem(), name);
        if (labelId == null) {
            Label label = new Label();
            label.setSystem(request.getSystem());
            label.setLabel(name);
            label.setType(Label.TYPE_BATTERY);
            label.setColor(color);
            label.setFontColor("#ffffff");
            label.setParentLabelID(0);
            label.setRequirementType("");
            label.setRequirementStatus("");
            label.setRequirementCriticity("");
            label.setDescription(description);
            label.setLongDescription("");
            label.setUsrCreated(login);
            label.setUsrModif(login);
            // The label DAO inserts the dates of the entity, the columns are not nullable.
            Timestamp now = new Timestamp(System.currentTimeMillis());
            label.setDateCreated(now);
            label.setDateModif(now);
            check(labelService.create(label), "label " + name);
            labelId = findLabelId(request.getSystem(), name);
            if (labelId == null) {
                throw new IllegalStateException("Label " + name + " was created but cannot be found");
            }
            actions.add("label " + name + " created");
        }
        for (String testcase : testcaseIds) {
            if (testCaseLabelService.readByKey(folder, testcase, labelId).getItem() == null) {
                TestCaseLabel testCaseLabel = new TestCaseLabel();
                testCaseLabel.setTest(folder);
                testCaseLabel.setTestcase(testcase);
                testCaseLabel.setLabelId(labelId);
                testCaseLabel.setUsrCreated(login);
                check(testCaseLabelService.create(testCaseLabel), "label of testcase " + testcase);
            }
        }
        return labelId;
    }

    private Integer findLabelId(String system, String name) {
        for (Label label : labelService.readByVariousAPI(List.of(system), List.of(Label.TYPE_BATTERY))) {
            if (name.equalsIgnoreCase(label.getLabel()) && system.equalsIgnoreCase(label.getSystem())) {
                return label.getId();
            }
        }
        return null;
    }

    private void ensureCampaign(String name, String description, SmokeBootstrapRequestDTO request, Integer labelId, String login, List<String> actions) {
        Campaign existing = campaignService.readByKey(name).getItem();
        boolean exist = existing != null;
        Campaign campaign = new Campaign();
        campaign.setCampaign(name);
        if (exist) {
            campaign.setCampaignID(existing.getCampaignID());
        }
        campaign.setDescription(description);
        campaign.setLongDescription("");
        campaign.setTag(name + ".%TIMESTAMP%");
        campaign.setManualExecution("N");
        campaign.setUsrCreated(login);
        campaign.setUsrModif(login);
        campaign.setScreenshot("1");
        campaign.setVideo("0");
        campaign.setVerbose("1");
        campaign.setPageSource("0");
        campaign.setRobotLog("0");
        campaign.setConsoleLog("0");
        campaign.setTimeout("");
        campaign.setRetries("0");
        campaign.setPriority("");
        campaign.setCIScoreThreshold("");
        campaign.setGroup1("");
        campaign.setGroup2("");
        campaign.setGroup3("");
        if (exist) {
            check(campaignService.update(name, campaign), "campaign " + name);
        } else {
            check(campaignService.create(campaign), "campaign " + name);
        }

        List<CampaignParameter> parameters = new ArrayList<>();
        parameters.add(parameter(name, CampaignParameter.COUNTRY_PARAMETER, request.getCountry()));
        parameters.add(parameter(name, CampaignParameter.ENVIRONMENT_PARAMETER, request.getEnvironment()));
        parameters.add(parameter(name, CampaignParameter.ROBOT_PARAMETER, request.getRobot()));
        check(campaignParameterService.compareListAndUpdateInsertDeleteElements(name, parameters), "campaign parameters");

        if (campaignLabelService.readByKey(name, labelId).getItem() == null) {
            CampaignLabel campaignLabel = new CampaignLabel();
            campaignLabel.setCampaign(name);
            campaignLabel.setLabelId(labelId);
            campaignLabel.setUsrCreated(login);
            check(campaignLabelService.create(campaignLabel), "label of campaign " + name);
        }

        EventHook hook = new EventHook();
        hook.setEventReference(EventHook.EVENTREFERENCE_CAMPAIGN_END);
        hook.setObjectKey1(name);
        hook.setObjectKey2("");
        hook.setActive(true);
        hook.setHookConnector(EventHook.HOOKCONNECTOR_EMAIL);
        hook.setHookRecipient(request.getRecipientEmail());
        hook.setHookChannel("");
        hook.setDescription("Smoke campaign result");
        hook.setUsrCreated(login);
        check(eventHookService.compareListAndUpdateInsertDeleteElements(name, List.of(hook)), "campaign end email");

        actions.add("campaign " + name + (exist ? " updated" : " created") + ", result mailed to " + request.getRecipientEmail());
    }

    private CampaignParameter parameter(String campaign, String name, String value) {
        CampaignParameter parameter = new CampaignParameter();
        parameter.setCampaign(campaign);
        parameter.setParameter(name);
        parameter.setValue(value);
        return parameter;
    }

    private void check(Answer answer, String what) {
        if (!answer.isCodeStringEquals("OK")) {
            LOG.warn("Smoke bootstrap failed on {} : {}", what, answer.getMessageDescription());
            throw new IllegalStateException("Unable to set up " + what + ": " + answer.getMessageDescription());
        }
    }
}
