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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iab.gpp.encoder.GppModel;
import com.iab.gpp.encoder.section.TcfEuV2;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.controllers.wrappers.ResponseWrapper;
import org.cerberus.core.api.dto.views.View;
import org.cerberus.core.api.services.PublicApiAuthenticationService;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.util.IabGppDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.*;

@AllArgsConstructor
@Tag(name = "Utils", description = "Operations related to Utils")
@RestController
@RequestMapping(path = "/public/utils")
public class UtilsController {

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "X-API-KEY";
    private final PublicApiAuthenticationService apiAuthenticationService;
    private static final Logger LOG = LogManager.getLogger(UtilsController.class);
    private final ILogEventService logEventService;
    private final IabGppDecoder iapGppDecoder;

    @GetMapping(path = "/decode-tcfeuv2", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Decode the TCFEuV2 section",
            description = "Decode only the TCFEuV2 (section 2) from an IAB GPP string and return JSON"
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<Map<String, Object>> decodeTCFEuV2(
            @Parameter(description = "IAB GPP String")
            @RequestParam("iabgpp") String iabgpp,
            @RequestHeader(name = API_KEY, required = false) String apiKey,
            HttpServletRequest request,
            Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls(
                "/public/utils/decode-tcfeuv2",
                "CALL-GET",
                LogEvent.STATUS_INFO,
                String.format("API /decode-tcfeuv2 called with URL: %s", request.getRequestURL()),
                request,
                login);

        return ResponseWrapper.wrap(iapGppDecoder.decodeTcfEuV2(iabgpp));
    }

}

