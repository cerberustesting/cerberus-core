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
package org.cerberus.core.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.services.PublicApiAuthenticationService;
import org.cerberus.core.crud.entity.EventHook;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.User;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.crud.service.ITestCaseExecutionService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.crud.service.IUserService;
import org.cerberus.core.service.notifications.email.IEmailGenerationService;
import org.cerberus.core.service.notifications.email.IEmailService;
import org.cerberus.core.service.notifications.email.entity.Email;
import org.cerberus.core.util.StringUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sends a notification email built from real data, to check its rendering.
 * The mail is ALWAYS sent to the email address of the authenticated user, whatever
 * the recipients configured on the parameters or the environments.
 */
@AllArgsConstructor
@Tag(name = "Notification", description = "Endpoints to test the notification emails")
@RestController
@RequestMapping(path = "/public/notifications/")
public class NotificationTestController {

    private static final Logger LOG = LogManager.getLogger(NotificationTestController.class);
    private static final String API_KEY = "X-API-KEY";

    private static final String TYPE_CAMPAIGN_START = EventHook.EVENTREFERENCE_CAMPAIGN_START;
    private static final String TYPE_CAMPAIGN_END = EventHook.EVENTREFERENCE_CAMPAIGN_END;
    private static final String TYPE_EXECUTION_START = EventHook.EVENTREFERENCE_EXECUTION_START;
    private static final String TYPE_EXECUTION_END = EventHook.EVENTREFERENCE_EXECUTION_END;
    private static final String TYPE_TESTCASE_CREATE = EventHook.EVENTREFERENCE_TESTCASE_CREATE;
    private static final String TYPE_TESTCASE_UPDATE = EventHook.EVENTREFERENCE_TESTCASE_UPDATE;
    private static final String TYPE_TESTCASE_DELETE = EventHook.EVENTREFERENCE_TESTCASE_DELETE;
    private static final String TYPE_REVISION_CHANGE = EventHook.EVENTREFERENCE_ENVIRONMENT_REVISIONCHANGE;
    private static final String TYPE_ENVIRONMENT_DISABLE = EventHook.EVENTREFERENCE_ENVIRONMENT_DISABLE;
    private static final String TYPE_NEW_CHAIN = "ENVIRONMENT_NEWCHAIN";
    private static final String TYPE_ACCOUNT_CREATION = "ACCOUNT_CREATION";
    private static final String TYPE_FORGOT_PASSWORD = "FORGOT_PASSWORD";

    private final PublicApiAuthenticationService apiAuthenticationService;
    private final ILogEventService logEventService;
    private final IUserService userService;
    private final ITagService tagService;
    private final ITestCaseExecutionService testCaseExecutionService;
    private final ITestCaseService testCaseService;
    private final IEmailGenerationService emailGenerationService;
    private final IEmailService emailService;

    @PostMapping(path = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Send a notification email built from real data to the authenticated user",
            description = "type is one of CAMPAIGN_START, CAMPAIGN_END (tag), EXECUTION_START, EXECUTION_END (executionId), "
            + "TESTCASE_CREATE, TESTCASE_UPDATE, TESTCASE_DELETE (test, testcase), "
            + "ENVIRONMENT_REVISIONCHANGE (system, country, env, build, revision), ENVIRONMENT_DISABLE (system, country, env), "
            + "ENVIRONMENT_NEWCHAIN (system, country, env, chain), ACCOUNT_CREATION, FORGOT_PASSWORD. "
            + "The mail is always sent to the email address of the authenticated user."
    )
    public ResponseEntity<Map<String, String>> sendTestNotification(
            @Parameter(description = "Notification type") @RequestParam("type") String type,
            @RequestParam(name = "tag", required = false) String tag,
            @RequestParam(name = "executionId", required = false) Long executionId,
            @RequestParam(name = "test", required = false) String test,
            @RequestParam(name = "testcase", required = false) String testcase,
            @RequestParam(name = "system", required = false) String system,
            @RequestParam(name = "country", required = false) String country,
            @RequestParam(name = "env", required = false) String env,
            @RequestParam(name = "build", required = false) String build,
            @RequestParam(name = "revision", required = false) String revision,
            @RequestParam(name = "chain", required = false) String chain,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/notifications", "CALL-POST", LogEvent.STATUS_INFO, String.format("API /notifications/test called with URL: %s", request.getRequestURL()), request, login);

        try {
            User user = userService.findUserByKey(login);
            if (user == null || StringUtil.isEmptyOrNull(user.getEmail())) {
                return error(HttpStatus.BAD_REQUEST, "The user '" + login + "' has no email address : define one on the user profile.");
            }
            String recipient = user.getEmail();

            List<String> missing = new ArrayList<>();
            Email email;
            switch (type) {
                case TYPE_CAMPAIGN_START:
                case TYPE_CAMPAIGN_END:
                    require(missing, "tag", tag);
                    if (!missing.isEmpty()) {
                        return missingParameters(missing);
                    }
                    org.cerberus.core.crud.entity.Tag myTag = tagService.convert(tagService.readByKey(tag));
                    email = TYPE_CAMPAIGN_START.equals(type)
                            ? emailGenerationService.generateNotifyStartTagExecution(myTag, recipient)
                            : emailGenerationService.generateNotifyEndTagExecution(myTag, recipient);
                    break;

                case TYPE_EXECUTION_START:
                case TYPE_EXECUTION_END:
                    if (executionId == null) {
                        return missingParameters(List.of("executionId"));
                    }
                    TestCaseExecution execution = testCaseExecutionService.convert(testCaseExecutionService.readByKey(executionId));
                    email = TYPE_EXECUTION_START.equals(type)
                            ? emailGenerationService.generateNotifyStartExecution(execution, recipient)
                            : emailGenerationService.generateNotifyEndExecution(execution, recipient);
                    break;

                case TYPE_TESTCASE_CREATE:
                case TYPE_TESTCASE_UPDATE:
                case TYPE_TESTCASE_DELETE:
                    require(missing, "test", test);
                    require(missing, "testcase", testcase);
                    if (!missing.isEmpty()) {
                        return missingParameters(missing);
                    }
                    TestCase testCase = testCaseService.convert(testCaseService.readByKey(test, testcase));
                    email = emailGenerationService.generateNotifyTestCaseChange(testCase, recipient, type);
                    break;

                case TYPE_REVISION_CHANGE:
                    require(missing, "system", system);
                    require(missing, "country", country);
                    require(missing, "env", env);
                    require(missing, "build", build);
                    require(missing, "revision", revision);
                    if (!missing.isEmpty()) {
                        return missingParameters(missing);
                    }
                    email = emailGenerationService.generateRevisionChangeEmail(system, country, env, build, revision);
                    break;

                case TYPE_ENVIRONMENT_DISABLE:
                    require(missing, "system", system);
                    require(missing, "country", country);
                    require(missing, "env", env);
                    if (!missing.isEmpty()) {
                        return missingParameters(missing);
                    }
                    email = emailGenerationService.generateDisableEnvEmail(system, country, env);
                    break;

                case TYPE_NEW_CHAIN:
                    require(missing, "system", system);
                    require(missing, "country", country);
                    require(missing, "env", env);
                    require(missing, "chain", chain);
                    if (!missing.isEmpty()) {
                        return missingParameters(missing);
                    }
                    email = emailGenerationService.generateNewChainEmail(system, country, env, chain);
                    break;

                case TYPE_ACCOUNT_CREATION:
                    email = emailGenerationService.generateAccountCreationEmail(user);
                    break;

                case TYPE_FORGOT_PASSWORD:
                    // Fake token, never saved : the link in the mail will not work.
                    user.setResetPasswordToken("TEST-TOKEN-NOT-VALID");
                    email = emailGenerationService.generateForgotPasswordEmail(user);
                    break;

                default:
                    return error(HttpStatus.BAD_REQUEST, "Unknown type '" + type + "'.");
            }

            // Whatever the recipients defined on parameters or environments, only the caller receives the test.
            email.setTo(recipient);
            email.setCc(null);
            emailService.sendHtmlMail(email);
            return ResponseEntity.ok(Map.of("message", "'" + type + "' email sent to " + recipient + "."));

        } catch (Exception ex) {
            LOG.error("Could not send the test notification '" + type + "'", ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Could not send the email: " + ex.getMessage());
        }
    }

    private static void require(List<String> missing, String name, String value) {
        if (StringUtil.isEmptyOrNull(value)) {
            missing.add(name);
        }
    }

    private static ResponseEntity<Map<String, String>> missingParameters(List<String> missing) {
        return error(HttpStatus.BAD_REQUEST, "Missing parameter(s) : " + String.join(", ", missing));
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}
