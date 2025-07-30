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
package org.cerberus.core.api.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.controllers.wrappers.ResponseWrapper;
import org.cerberus.core.api.dto.testcase.TestcaseDTOV001;
import org.cerberus.core.api.dto.testcase.TestcaseMapperV001;
import org.cerberus.core.api.dto.testcase.TestcaseSimplifiedCreationDTOV001;
import org.cerberus.core.api.dto.views.View;
import org.cerberus.core.api.services.PublicApiAuthenticationService;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.CountryEnvironmentParameters;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseCountry;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.entity.TestCaseStepActionControl;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.ICountryEnvironmentParametersService;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestCaseCountryService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.crud.service.ITestCaseStepActionControlService;
import org.cerberus.core.crud.service.ITestCaseStepActionService;
import org.cerberus.core.crud.service.ITestCaseStepService;
import static org.cerberus.core.engine.execution.enums.ConditionOperatorEnum.CONDITIONOPERATOR_ALWAYS;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author MorganLmd
 */
@AllArgsConstructor
@Tag(name = "Testcase", description = "Endpoints related to Testcase")
@Validated
@RestController
@RequestMapping(path = "/public/testcases")
public class TestcaseController {

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "X-API-KEY";
    private final IInvariantService invariantService;
    private final ITestCaseService testCaseService;
    private final ITestCaseCountryService testCaseCountryService;
    private final ITestCaseStepService testCaseStepService;
    private final ITestCaseStepActionService testCaseStepActionService;
    private final ITestCaseStepActionControlService testCaseStepActionControlService;
    private final IApplicationService applicationService;
    private final ICountryEnvironmentParametersService countryEnvironmentParametersService;
    private final TestcaseMapperV001 testcaseMapper;
    private final IParameterService parameterService;
    private final PublicApiAuthenticationService apiAuthenticationService;
    private final ILogEventService logEventService;
    private static final Logger LOG = LogManager.getLogger(TestcaseController.class);

    @GetMapping(path = "/{testFolderId}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get all testcases by test folder",
            description = "Get all testcases by test folder",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Found the testcases", content = { @Content(mediaType = "application/json",array = @ArraySchema(schema = @Schema(implementation = TestcaseDTOV001.class)))})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<List<TestcaseDTOV001>> findTestcasesByTest(
            @PathVariable("testFolderId") String testFolderId,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            HttpServletRequest request,
            Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/testcases", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /testcases called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.testCaseService.findTestCaseByTest(testFolderId)
                        .stream()
                        .map(this.testcaseMapper::toDTO)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping(path = "/{testFolderId}/{testcaseId}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get a testcase filtered by its FolderId and testCaseId",
            description = "Get a testcase filtered by its FolderId and testCaseId",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Found the testcase", content = { @Content(mediaType = "application/json",schema = @Schema(implementation = TestcaseDTOV001.class))})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<TestcaseDTOV001> findTestcaseByTestAndTestcase(
            @PathVariable("testFolderId") String testFolderId,
            @PathVariable("testcaseId") String testcaseId,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            HttpServletRequest request,
            Principal principal) throws CerberusException {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/testcases", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /testcases called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.testcaseMapper
                        .toDTO(
                                this.testCaseService.findTestCaseByKeyWithDependencies(testFolderId, testcaseId, true).getItem()
                        )
        );
    }

    @PostMapping(headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Create a new Testcase",
            description = "Create a new Testcase",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Created the testcase", content = { @Content(mediaType = "application/json",schema = @Schema(implementation = TestcaseDTOV001.class))})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseWrapper<TestcaseDTOV001> createTestcase(
            @Valid @JsonView(View.Public.POST.class) @RequestBody TestcaseDTOV001 newTestcase,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            HttpServletRequest request,
            Principal principal) throws CerberusException {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/testcases", "CALL-POST", LogEvent.STATUS_INFO, String.format("API /testcases called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.testcaseMapper.toDTO(
                        this.testCaseService.createTestcaseWithDependenciesAPI(
                                this.testcaseMapper.toEntity(newTestcase)
                        )
                )
        );
    }

    @PostMapping(path = "/create", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Create a new Testcase with only few information",
            description = "Create a new Testcase with only few information",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Created the testcase", content = { @Content(mediaType = "application/json")})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.CREATED)
    public String createSimplifiedTestcase(
            @Valid @JsonView(View.Public.POST.class) @RequestBody TestcaseSimplifiedCreationDTOV001 newTestcase,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            HttpServletRequest request,
            Principal principal) throws CerberusException {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/testcases", "CALL-POST", LogEvent.STATUS_INFO, String.format("API /testcases/create called with URL: %s", request.getRequestURL()), request, login);

        JSONObject jsonResponse = new JSONObject();

        if (!this.applicationService.exist(newTestcase.getApplication())) {

            this.applicationService.create(
                    Application.builder()
                            .application(newTestcase.getApplication())
                            .description("")
                            .sort(10)
                            .type(newTestcase.getType())
                            .system(newTestcase.getSystem())
                            .subsystem("")
                            .repoUrl("")
                            .bugTrackerNewUrl("")
                            .bugTrackerNewUrl("")
                            .usrCreated(login)
                            .build());

            this.countryEnvironmentParametersService.create(
                    CountryEnvironmentParameters.builder()
                            .system(newTestcase.getSystem())
                            .country(newTestcase.getCountry())
                            .environment(newTestcase.getEnvironment())
                            .application(newTestcase.getApplication())
                            .ip(newTestcase.getUrl())
                            .mobileActivity("")
                            .mobilePackage("")
                            .domain("")
                            .url("")
                            .urlLogin("")
                            .var1("")
                            .var2("")
                            .var3("")
                            .var4("")
                            .usrCreated(login)
                            .build());
        }

        Answer createTestCaseAnswer = this.testCaseService.create(
                TestCase.builder()
                        .test(newTestcase.getTestFolderId())
                        .testcase(newTestcase.getTestcaseId())
                        .application(newTestcase.getApplication())
                        .description(newTestcase.getDescription())
                        .priority(1)
                        .status(invariantService.convert(invariantService.readFirstByIdName(Invariant.IDNAME_TCSTATUS)).getValue())
                        .conditionOperator(CONDITIONOPERATOR_ALWAYS.getCondition())
                        .conditionValue1("")
                        .conditionValue2("")
                        .conditionValue3("")
                        .type(TestCase.TESTCASE_TYPE_AUTOMATED)
                        .isActive(true)
                        .isActivePROD(true)
                        .isActiveQA(true)
                        .isActiveUAT(true)
                        .usrCreated(login)
                        .build());

        if (createTestCaseAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {

            List<Invariant> countryInvariantList = this.invariantService.readByIdName("COUNTRY");
            for (Invariant countryInvariant : countryInvariantList) {

                this.testCaseCountryService.create(
                        TestCaseCountry.builder()
                                .test(newTestcase.getTestFolderId())
                                .testcase(newTestcase.getTestcaseId())
                                .country(countryInvariant.getValue())
                                .usrCreated(login)
                                .build());
            }

            if (parameterService.getParameterBooleanByKey(Parameter.VALUE_cerberus_testcaseautofeed_enable, newTestcase.getSystem(), true)) {

                this.testCaseStepService.create(
                        TestCaseStep.builder()
                                .test(newTestcase.getTestFolderId())
                                .testcase(newTestcase.getTestcaseId())
                                .stepId(0)
                                .sort(1)
                                .isUsingLibraryStep(false)
                                .libraryStepStepId(0)
                                .loop(TestCaseStep.LOOP_ONCEIFCONDITIONTRUE)
                                .conditionOperator(CONDITIONOPERATOR_ALWAYS.getCondition())
                                .description("Go to the homepage and take a screenshot")
                                .usrCreated(login)
                                .build());

                this.testCaseStepActionService.create(
                        TestCaseStepAction.builder()
                                .test(newTestcase.getTestFolderId())
                                .testcase(newTestcase.getTestcaseId())
                                .stepId(0)
                                .actionId(0)
                                .sort(1)
                                .conditionOperator(CONDITIONOPERATOR_ALWAYS.getCondition())
                                .conditionValue1("")
                                .conditionValue2("")
                                .conditionValue3("")
                                .action(TestCaseStepAction.ACTION_OPENURLWITHBASE)
                                .value1("/")
                                .value2("")
                                .value3("")
                                .description("Open the homepage")
                                .conditionOperator(CONDITIONOPERATOR_ALWAYS.getCondition())
                                .usrCreated(login)
                                .build());

                this.testCaseStepActionControlService.create(
                        TestCaseStepActionControl.builder()
                                .test(newTestcase.getTestFolderId())
                                .testcase(newTestcase.getTestcaseId())
                                .stepId(0)
                                .actionId(0)
                                .controlId(0)
                                .sort(1)
                                .conditionOperator(CONDITIONOPERATOR_ALWAYS.getCondition())
                                .conditionValue1("")
                                .conditionValue2("")
                                .conditionValue3("")
                                .control(TestCaseStepActionControl.CONTROL_TAKESCREENSHOT)
                                .value1("")
                                .value2("")
                                .value3("")
                                .description("Take a screenshot")
                                .usrCreated(login)
                                .build());

            }
        }

        try {
            if (createTestCaseAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                jsonResponse.put("test", newTestcase.getTestFolderId());
                jsonResponse.put("testcase", newTestcase.getTestcaseId());
                jsonResponse.put("messageType", "OK");
                jsonResponse.put("message", "success");
            } else {
                jsonResponse.put("messageType", "KO");
                jsonResponse.put("message", createTestCaseAnswer.getMessageDescription());
            }
        } catch (JSONException e) {
            LOG.error(e, e);
        }

        return jsonResponse.toString();
    }

    @PutMapping(path = "/{testFolderId}/{testcaseId}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Update a Testcase",
            description = "Update a Testcase",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Updated the testcase", content = { @Content(mediaType = "application/json",schema = @Schema(implementation = TestcaseDTOV001.class))})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<TestcaseDTOV001> update(
            @PathVariable("testcaseId") String testcaseId,
            @PathVariable("testFolderId") String testFolderId,
            @Valid @JsonView(View.Public.PUT.class) @RequestBody TestcaseDTOV001 testcaseToUpdate,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            HttpServletRequest request,
            Principal principal) throws CerberusException {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/testcases", "CALL-PUT", LogEvent.STATUS_INFO, String.format("API /testcases called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.testcaseMapper.toDTO(
                        this.testCaseService.updateTestcaseAPI(
                                testFolderId,
                                testcaseId,
                                this.testcaseMapper.toEntity(testcaseToUpdate)
                        ))
        );
    }
}
