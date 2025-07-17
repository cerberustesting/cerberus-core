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
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

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
import org.cerberus.core.api.dto.application.ApplicationDTOV001;
import org.cerberus.core.api.dto.appservice.AppServiceDTOV001;
import org.cerberus.core.api.dto.user.UserDTOV001;
import org.cerberus.core.api.dto.user.UserMapperV001;
import org.cerberus.core.api.dto.views.View;
import org.cerberus.core.api.exceptions.EntityNotFoundException;
import org.cerberus.core.api.services.PublicApiAuthenticationService;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.entity.User;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IUserService;
import org.cerberus.core.exception.CerberusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@Tag(name = "User", description = "Operations related to User")
@RestController
@RequestMapping(path = "/public/users")
public class UserController {

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "X-API-KEY";
    private final PublicApiAuthenticationService apiAuthenticationService;
    private static final Logger LOG = LogManager.getLogger(UserController.class);
    private final UserMapperV001 userMapper;
    private final IUserService userService;
    private final ILogEventService logEventService;

    //FIND USER BY USER NAMES
    @GetMapping(path = "/{user}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get a user by its login name",
        description = "Get a user by its login name",
        responses = {
            @ApiResponse(responseCode = "200", description = "Found the user", content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,schema = @Schema(implementation = AppServiceDTOV001.class))}),
        }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<UserDTOV001> findByKey(
        @Parameter(description = "User name") @PathVariable("user") String user,
        @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
        @Parameter(hidden = true) HttpServletRequest request,
        @Parameter(hidden = true) Principal principal) {
        
        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/users", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /users called with URL: %s", request.getRequestURL()), request, login);
        
        Optional<User> userOptional = Optional.ofNullable(this.userService.readByKey(user).getItem());
        if (userOptional.isPresent()) {
            return ResponseWrapper.wrap(
                    this.userMapper.toDTO(
                            userOptional.get()
                    )
            );
        } else {
            throw new EntityNotFoundException(User.class, "user", user);
        }
    }

    //FIND ALL USER
    @GetMapping(path = "/", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get all users",
        description = "Get all users",
        responses = {
            @ApiResponse(responseCode = "200", description = "Found the users", content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,schema = @Schema(implementation = AppServiceDTOV001.class))}),
        }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<List<UserDTOV001>> findAll(
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) throws CerberusException {
        
        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/users", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /users called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.userService.findallUser()
                        .stream()
                        .map(this.userMapper::toDTO)
                        .collect(Collectors.toList())
        );

    }
}

