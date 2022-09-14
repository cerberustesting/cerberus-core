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

package org.cerberus.core.api.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.cerberus.core.api.controllers.wrappers.ResponseWrapper;
import org.cerberus.core.api.dto.v001.CampaignExecutionDTOV001;
import org.cerberus.core.api.dto.views.View;
import org.cerberus.core.api.mappers.v001.CampaignExecutionMapperV001;
import org.cerberus.core.api.services.CampaignExecutionService;
import org.cerberus.core.api.services.PublicApiAuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * @author lucashimpens
 */
@AllArgsConstructor
@Api(tags = "Campaign Execution")
@Validated
@RestController
@RequestMapping(path = "/public/campaignexecutions")
public class CampaignExecutionController {

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "X-API-KEY";

    private final CampaignExecutionMapperV001 campaignExecutionMapper;

    private final CampaignExecutionService campaignExecutionService;
    private final PublicApiAuthenticationService apiAuthenticationService;

    @ApiOperation(value = "Get a campaign execution by id", response = CampaignExecutionDTOV001.class, notes = "Campaign execution must exists")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Campaign execution successfully returned.", response = CampaignExecutionDTOV001.class),
            @ApiResponse(code = 404, message = "Campaign execution was not found."),
            @ApiResponse(code = 500, message = "An error occurred when retrieving the campaign execution.")
    })
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{campaignExecutionId}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<CampaignExecutionDTOV001> findCampaignExecutionById(
            @PathVariable("campaignExecutionId") String campaignExecutionId,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            Principal principal) {
        this.apiAuthenticationService.authenticate(principal, apiKey);
        return ResponseWrapper.wrap(
                this.campaignExecutionMapper
                        .toDto(
                                this.campaignExecutionService.findByExecutionIdWithExecutions(campaignExecutionId)
                        )
        );
    }
}
