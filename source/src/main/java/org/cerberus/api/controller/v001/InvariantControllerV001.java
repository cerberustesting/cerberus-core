/**
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
package org.cerberus.api.controller.v001;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.cerberus.api.dto.v001.InvariantDTOV001;
import org.cerberus.exception.CerberusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.cerberus.api.mapper.v001.InvariantMapperV001;
import org.cerberus.api.service.InvariantApiService;
import org.springframework.http.MediaType;

/**
 *
 * @author mlombard
 */
@AllArgsConstructor
@Api(tags = "Invariant endpoint")
@RestController
@RequestMapping(path = "/public/invariants")
public class InvariantControllerV001 {

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "apikey";
    private final InvariantApiService invariantApiService;
    private final InvariantMapperV001 invariantMapper;
    
    @ApiOperation("Get all invariants filtered by idName")
    @ApiResponse(code = 200, message = "operation successful", response = InvariantDTOV001.class, responseContainer = "List")
    @GetMapping(path = "/{idName}", headers = {API_VERSION_1, API_KEY}, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<InvariantDTOV001> findInvariantByIdName(@PathVariable("idName") String idName) throws CerberusException {
        return this.invariantApiService.readyByIdName(idName)
                .stream()
                .map(this.invariantMapper::toDTO)
                .collect(Collectors.toList());
    }

    @ApiOperation("Get all invariants filtered by idName and value")
    @ApiResponse(code = 200, message = "operation successful", response = InvariantDTOV001.class)
    @GetMapping(path = "/{idName}/{value}", headers = {API_VERSION_1, API_KEY}, produces = MediaType.APPLICATION_JSON_VALUE)
    public InvariantDTOV001 findInvariantByIdNameAndValue(@PathVariable("idName") String idName, @PathVariable("value") String value) throws CerberusException {
        return this.invariantMapper.toDTO(this.invariantApiService.readByKey(idName, value));
    }
}