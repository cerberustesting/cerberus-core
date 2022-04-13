/*
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.api.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.api.dto.v001.InvariantDTOV001;
import org.cerberus.api.mappers.v001.InvariantMapperV001;
import org.cerberus.api.services.InvariantApiService;
import org.cerberus.api.services.PublicApiAuthenticationService;
import org.cerberus.exception.CerberusException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author mlombard
 */
@AllArgsConstructor
@Api(tags = "Invariant")
@RestController
@RequestMapping(path = "/public/invariants")
public class InvariantController {

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "X-API-KEY";
    private final InvariantApiService invariantApiService;
    private final InvariantMapperV001 invariantMapper;
    private final PublicApiAuthenticationService apiAuthenticationService;
    private static final Logger LOG = LogManager.getLogger(InvariantController.class);

    @ApiOperation("Get all invariants filtered by idName")
    @ApiResponse(code = 200, message = "operation successful", response = InvariantDTOV001.class, responseContainer = "List")
    @GetMapping(path = "/{idName}", headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<InvariantDTOV001> findInvariantByIdName(
            @PathVariable("idName") String idName,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            Principal principal) throws CerberusException {
        this.apiAuthenticationService.authenticate(principal, apiKey);
        return this.invariantApiService.readyByIdName(idName)
                .stream()
                .map(this.invariantMapper::toDTO)
                .collect(Collectors.toList());
    }

    @ApiOperation("Get all invariants filtered by idName and value")
    @ApiResponse(code = 200, message = "operation successful", response = InvariantDTOV001.class)
    @GetMapping(path = "/{idName}/{value}", headers = API_VERSION_1, produces = MediaType.APPLICATION_JSON_VALUE)
    public InvariantDTOV001 findInvariantByIdNameAndValue(
            @PathVariable("idName") String idName,
            @PathVariable("value") String value,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            Principal principal) throws CerberusException {
        this.apiAuthenticationService.authenticate(principal, apiKey);
        return this.invariantMapper.toDTO(this.invariantApiService.readByKey(idName, value));
    }

}
