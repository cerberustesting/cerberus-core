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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.controllers.wrappers.ResponseWrapper;
import org.cerberus.core.api.dto.application.CountryEnvironmentParametersDTOV001;
import org.cerberus.core.api.dto.robot.RobotDTOV001;
import org.cerberus.core.api.dto.robot.RobotExecutorDTOV001;
import org.cerberus.core.api.dto.robot.RobotExecutorMapperV001;
import org.cerberus.core.api.dto.robot.RobotMapperV001;
import org.cerberus.core.api.dto.views.View;
import org.cerberus.core.api.exceptions.EntityNotFoundException;
import org.cerberus.core.api.services.PublicApiAuthenticationService;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.entity.RobotExecutor;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.impl.RobotExecutorService;
import org.cerberus.core.crud.service.impl.RobotService;
import org.cerberus.core.exception.CerberusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;

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
    private final RobotMapperV001 robotMapper;
    private final RobotExecutorMapperV001 robotExecutorMapper;
    private final ILogEventService logEventService;
    private final PublicApiAuthenticationService apiAuthenticationService;

    private static final Logger LOG = LogManager.getLogger(RobotController.class);

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

}
