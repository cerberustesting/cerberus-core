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
package org.cerberus.controller.publicv1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.dto.publicv1.InvariantDTOV1;
import org.cerberus.exception.CerberusException;
import org.cerberus.mapper.InvariantMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author mlombard
 */
@RestController
@RequestMapping(path = "/public/invariants")
public class InvariantController {

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "API_KEY";
    private final IInvariantService invariantService;

    @Autowired
    public InvariantController(IInvariantService invariantService) {
        this.invariantService = invariantService;
    }

    @ApiOperation("Get all invariants filtered by idName")
    @ApiResponse(code = 200, message = "operation successful", response = InvariantDTOV1.class, responseContainer = "List")
    @GetMapping(path = "/{idName}", headers = {API_VERSION_1, API_KEY}, produces = "application/json")
    public List<InvariantDTOV1> findInvariantByIdName(@PathVariable("idName") String idName) throws CerberusException {
        return this.invariantService.readByIdName(idName)
                .stream()
                .map(InvariantMapper::convertToDto)
                .collect(Collectors.toList());
    }

    @ApiOperation("Get all invariants filtered by idName and value")
    @ApiResponse(code = 200, message = "ok", response = InvariantDTOV1.class)
    @GetMapping(path = "/{idName}/{value}", headers = {API_VERSION_1, API_KEY}, produces = "application/json")
    public InvariantDTOV1 findInvariantByIdNameAndValue(@PathVariable("idName") String idName, @PathVariable("value") String value) {
        return InvariantMapper.convertToDto(
                this.invariantService.readByKey(idName, value)
                        .getItem()
        );
    }
}
