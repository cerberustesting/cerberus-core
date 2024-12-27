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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.controllers.wrappers.ResponseWrapper;
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
@Api(tags = "User")
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

    @ApiOperation("Get a user by its login name")
    @ApiResponse(code = 200, message = "ok", response = AppServiceDTOV001.class)
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{user}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<UserDTOV001> findByKey(
            @PathVariable("user") String user,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            HttpServletRequest request,
            Principal principal) {
        
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

    @ApiOperation("Get all users")
    @ApiResponse(code = 200, message = "ok", response = AppServiceDTOV001.class)
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<List<UserDTOV001>> findAll(
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            HttpServletRequest request,
            Principal principal) throws CerberusException {
        
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
