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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.controllers.wrappers.ResponseWrapper;
import org.cerberus.core.api.dto.application.CountryEnvironmentParametersDTOV001;
import org.cerberus.core.api.dto.robot.RobotCapabilityDTOV001;
import org.cerberus.core.api.dto.robot.RobotCapabilityMapperV001;
import org.cerberus.core.api.dto.robot.RobotDTOV001;
import org.cerberus.core.api.dto.robot.RobotExecutorDTOV001;
import org.cerberus.core.api.dto.robot.RobotExecutorMapperV001;
import org.cerberus.core.api.dto.robot.RobotMapperV001;
import org.cerberus.core.api.dto.views.View;
import org.cerberus.core.api.exceptions.EntityNotFoundException;
import org.cerberus.core.api.exceptions.FailedInsertOperationException;
import org.cerberus.core.api.exceptions.InvalidRequestException;
import org.cerberus.core.api.services.PublicApiAuthenticationService;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.entity.Robot;
import org.cerberus.core.crud.entity.RobotCapability;
import org.cerberus.core.crud.entity.RobotExecutor;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.impl.RobotCapabilityService;
import org.cerberus.core.crud.service.impl.RobotExecutorService;
import org.cerberus.core.crud.service.impl.RobotService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author bcivel
 */
@AllArgsConstructor
@Tag(name = "Robot", description = "Endpoints related to Robots")
@RestController
@RequestMapping(path = "/public/robots")
public class RobotController {

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "X-API-KEY";

    private final RobotService robotService;
    private final RobotExecutorService robotExecutorService;
    private final RobotCapabilityService robotCapabilityService;
    private final RobotMapperV001 robotMapper;
    private final RobotExecutorMapperV001 robotExecutorMapper;
    private final RobotCapabilityMapperV001 robotCapabilityMapper;
    private final ILogEventService logEventService;
    private final PublicApiAuthenticationService apiAuthenticationService;

    private static final Logger LOG = LogManager.getLogger(RobotController.class);

    //LIST ROBOTS
    @GetMapping(headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "List Robots",
        description = "Get the list of all robots",
        responses = {
            @ApiResponse(responseCode = "200", description = "Found the robots", content = { @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RobotDTOV001.class)))}),
        }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<List<RobotDTOV001>> findAllRobots(
        @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
        @Parameter(hidden = true) HttpServletRequest request,
        @Parameter(hidden = true) Principal principal) throws CerberusException {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/robots", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /robots called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.robotService.convert(this.robotService.readAll())
                        .stream()
                        .map(this.robotMapper::toDTO)
                        .collect(Collectors.toList())
        );
    }

    //FIND ROBOT BY NAME
    @GetMapping(path = "/{robot}", headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get Robot",
        description = "Get a robot by its name",
        responses = {
            @ApiResponse(responseCode = "200", description = "Found the robot", content = { @Content(mediaType = "application/json",schema = @Schema(implementation = RobotDTOV001.class))}),
        }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<RobotDTOV001> findRobotByIdName(
        @Parameter(description = "Robot name") @PathVariable("robot") String robot,
        @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
        @Parameter(hidden = true) HttpServletRequest request,
        @Parameter(hidden = true) Principal principal) throws CerberusException {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/robots", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /applications called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.robotMapper.toDTO(
                        this.robotService.readByKey(robot)
                )
        );
    }


    //CREATE ROBOT
    @PostMapping(headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Create Robot",
        description = "Create a new robot",
        responses = {
            @ApiResponse(responseCode = "201", description = "Created the robot", content = { @Content(mediaType = "application/json",schema = @Schema(implementation = RobotDTOV001.class))}),
        }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseWrapper<RobotDTOV001> createRobot(
        @Valid @JsonView(View.Public.POST.class) @RequestBody RobotDTOV001 robotToCreate,
        @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
        @Parameter(hidden = true) HttpServletRequest request,
        @Parameter(hidden = true) Principal principal) throws CerberusException {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/robots", "CALL-POST", LogEvent.STATUS_INFO, String.format("API /robots called with URL: %s", request.getRequestURL()), request, login);

        if (this.robotService.readByKey(robotToCreate.getRobot()) != null) {
            throw new InvalidRequestException("Robot already exists: " + robotToCreate.getRobot());
        }

        Robot robot = this.robotMapper.toEntity(robotToCreate);
        robot.setActive(robotToCreate.getIsActive() == null || robotToCreate.getIsActive());
        robot.setAcceptInsecureCerts(Boolean.TRUE.equals(robotToCreate.getIsAcceptInsecureCerts()));
        robot.setAcceptNotifications(robotToCreate.getAcceptNotifications() == null ? 0 : robotToCreate.getAcceptNotifications());
        robot.setUsrCreated(login);

        List<RobotCapability> capabilities = new ArrayList<>();
        if (robotToCreate.getCapabilities() != null) {
            for (RobotCapabilityDTOV001 capabilityToCreate : robotToCreate.getCapabilities()) {
                capabilities.add(this.toRobotCapabilityEntity(robot.getRobot(), capabilityToCreate));
            }
        }
        robot.setCapabilities(capabilities);

        List<RobotExecutor> executors = new ArrayList<>();
        if (robotToCreate.getExecutors() != null) {
            for (RobotExecutorDTOV001 executorToCreate : robotToCreate.getExecutors()) {
                executors.add(this.toRobotExecutorEntity(robot.getRobot(), executorToCreate, login));
            }
        }
        robot.setExecutors(executors);

        Answer answer = this.robotService.create(robot);
        if (!answer.isCodeStringEquals("OK")) {
            throw new FailedInsertOperationException("Unable to create robot: " + answer.getMessageDescription());
        }

        return ResponseWrapper.wrap(
                this.robotMapper.toDTO(
                        this.robotService.readByKey(robot.getRobot())
                )
        );
    }

    //DELETE ROBOT
    @DeleteMapping(path = "/{robot}", headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Delete Robot",
        description = "Delete a robot by its name",
        responses = {
            @ApiResponse(responseCode = "200", description = "Deleted the robot", content = { @Content(mediaType = "application/json",schema = @Schema(implementation = RobotDTOV001.class))}),
        }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<RobotDTOV001> deleteRobot(
        @Parameter(description = "Robot name") @PathVariable("robot") String robot,
        @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
        @Parameter(hidden = true) HttpServletRequest request,
        @Parameter(hidden = true) Principal principal) throws CerberusException {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/robots", "CALL-DELETE", LogEvent.STATUS_INFO, String.format("API /robots called with URL: %s", request.getRequestURL()), request, login);

        Robot robotToDelete = this.robotService.readByKey(robot);
        if (robotToDelete == null) {
            throw new EntityNotFoundException(Robot.class, "robot", robot);
        }

        RobotDTOV001 dto = this.robotMapper.toDTO(robotToDelete);

        Answer answer = this.robotService.delete(robotToDelete);
        if (!answer.isCodeStringEquals("OK")) {
            throw new FailedInsertOperationException("Unable to delete robot: " + answer.getMessageDescription());
        }

        return ResponseWrapper.wrap(dto);
    }

    //CREATE ROBOT CAPABILITY
    @PostMapping(path = "/{robot}/capabilities", headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Create Robot Capability",
        description = "Create a new capability (WebDriver key/value pair) for a robot",
        responses = {
            @ApiResponse(responseCode = "201", description = "Created the robot capability", content = { @Content(mediaType = "application/json",schema = @Schema(implementation = RobotCapabilityDTOV001.class))}),
        }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseWrapper<RobotCapabilityDTOV001> createRobotCapability(
        @Parameter(description = "Robot name") @PathVariable("robot") String robot,
        @Valid @JsonView(View.Public.POST.class) @RequestBody RobotCapabilityDTOV001 capabilityToCreate,
        @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
        @Parameter(hidden = true) HttpServletRequest request,
        @Parameter(hidden = true) Principal principal) throws CerberusException {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/robots", "CALL-POST", LogEvent.STATUS_INFO, String.format("API /robots called with URL: %s", request.getRequestURL()), request, login);

        if (this.robotService.readByKey(robot) == null) {
            throw new EntityNotFoundException(Robot.class, "robot", robot);
        }

        boolean alreadyExists = this.robotCapabilityService.readByRobot(robot).getDataList().stream()
                .anyMatch(c -> capabilityToCreate.getCapability().equals(c.getCapability()));
        if (alreadyExists) {
            throw new InvalidRequestException("Robot capability already exists: " + robot + "|" + capabilityToCreate.getCapability());
        }

        RobotCapability capability = this.toRobotCapabilityEntity(robot, capabilityToCreate);

        Answer answer = this.robotCapabilityService.create(capability);
        if (!answer.isCodeStringEquals("OK")) {
            throw new FailedInsertOperationException("Unable to create robot capability: " + answer.getMessageDescription());
        }

        return ResponseWrapper.wrap(this.robotCapabilityMapper.toDTO(capability));
    }

    //CREATE ROBOT EXECUTOR
    @PostMapping(path = "/{robot}/executors", headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Create Robot Executor",
        description = "Create a new executor (host) for a robot",
        responses = {
            @ApiResponse(responseCode = "201", description = "Created the robot executor", content = { @Content(mediaType = "application/json",schema = @Schema(implementation = RobotExecutorDTOV001.class))}),
        }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseWrapper<RobotExecutorDTOV001> createRobotExecutor(
        @Parameter(description = "Robot name") @PathVariable("robot") String robot,
        @Valid @JsonView(View.Public.POST.class) @RequestBody RobotExecutorDTOV001 executorToCreate,
        @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
        @Parameter(hidden = true) HttpServletRequest request,
        @Parameter(hidden = true) Principal principal) throws CerberusException {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/robots", "CALL-POST", LogEvent.STATUS_INFO, String.format("API /robots called with URL: %s", request.getRequestURL()), request, login);

        if (this.robotService.readByKey(robot) == null) {
            throw new EntityNotFoundException(Robot.class, "robot", robot);
        }

        if (this.robotExecutorService.exist(robot, executorToCreate.getExecutor())) {
            throw new InvalidRequestException("Robot executor already exists: " + robot + "|" + executorToCreate.getExecutor());
        }

        RobotExecutor executor = this.toRobotExecutorEntity(robot, executorToCreate, login);

        Answer answer = this.robotExecutorService.create(executor);
        if (!answer.isCodeStringEquals("OK")) {
            throw new FailedInsertOperationException("Unable to create robot executor: " + answer.getMessageDescription());
        }

        RobotExecutorDTOV001 dto = this.robotExecutorMapper.toDTO(executor);
        dto.setHostPassword(null);

        return ResponseWrapper.wrap(dto);
    }

    //PATCH ROBOT EXECUTOR
    @PatchMapping(path = "/{robot}/{executor}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Patch Robot Executor",
            description = "Patch an robot executor",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Patch the robot executor", content = { @Content(mediaType = "application/json",schema = @Schema(implementation = ResponseWrapper.class))}),
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<RobotExecutorDTOV001> patchExecutor(
            @Parameter(description = "Robot name") @PathVariable("robot") String robot,
            @Parameter(description = "Executor name") @PathVariable("executor") String executor,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Valid @JsonView(View.Public.PATCH.class) @RequestBody RobotExecutorDTOV001 robotExecutorToUpdate,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) throws CerberusException {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/robots/", "CALL-PATCH", LogEvent.STATUS_INFO, String.format("API /robots called with URL: %s", request.getRequestURL()), request, login);

        // We first get the application in order to retreive the system.
        RobotExecutor robotExecutor = this.robotExecutorService.readByKey(robot, executor).getItem();
        if (robotExecutor == null) {
            throw new EntityNotFoundException(RobotExecutor.class, "robotExecutor", robot + "|" + executor);
        }

        LOG.warn(robotExecutorToUpdate.toString());

        return ResponseWrapper.wrap(
                this.robotExecutorMapper.toDTO(
                        this.robotExecutorService.updateRobotExecutorPATCH(
                                robot,
                                executor,
                                robotExecutorToUpdate,
                                principal,
                                login
                        ))
        );
    }

    private RobotCapability toRobotCapabilityEntity(String robot, RobotCapabilityDTOV001 capabilityDTO) {
        RobotCapability capability = this.robotCapabilityMapper.toEntity(capabilityDTO);
        capability.setRobot(robot);
        return capability;
    }

    private RobotExecutor toRobotExecutorEntity(String robot, RobotExecutorDTOV001 executorDTO, String login) {
        RobotExecutor executor = this.robotExecutorMapper.toEntity(executorDTO);
        executor.setRobot(robot);
        executor.setIsActive(executorDTO.getIsActive() == null || executorDTO.getIsActive());
        executor.setRank(executorDTO.getRank() == null ? 1 : executorDTO.getRank());
        executor.setIsDeviceLockUnlock(Boolean.TRUE.equals(executorDTO.getIsDeviceLockUnlock()));
        if (executor.getExecutorProxyType() == null) {
            executor.setExecutorProxyType(RobotExecutor.PROXY_TYPE_NONE);
        }
        // deviceUdid, deviceName and description are NOT NULL columns with no DB default.
        if (executor.getDeviceUdid() == null) {
            executor.setDeviceUdid("");
        }
        if (executor.getDeviceName() == null) {
            executor.setDeviceName("");
        }
        if (executor.getDescription() == null) {
            executor.setDescription("");
        }
        // ExecutorExtensionPort is bound with setInt() (no null-check) in RobotExecutorDAO.create().
        if (executor.getExecutorExtensionPort() == null) {
            executor.setExecutorExtensionPort(0);
        }
        executor.setUsrCreated(login);
        return executor;
    }

}
