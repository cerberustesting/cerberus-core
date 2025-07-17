/**
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
package org.cerberus.core.apiprivate;

import io.swagger.v3.oas.annotations.Operation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import lombok.AllArgsConstructor;
import org.cerberus.core.service.automatescore.IAutomateScoreService;
import org.json.JSONObject;

@AllArgsConstructor
@RestController
@RequestMapping("/automatescore/")
public class AutomateScorePrivateController {

    private static final Logger LOG = LogManager.getLogger(AutomateScorePrivateController.class);
    @Autowired
    private IAutomateScoreService automateScoreService;

    @Operation(hidden=true)
    @GetMapping("/statistics")
    public String getTagASStatistics(
            HttpServletRequest request,
            @RequestParam(name = "systems", value = "systems", required = false) List<String> systems,
            @RequestParam(name = "campaigns", value = "campaigns", required = false) List<String> campaigns,
            @RequestParam(name = "to", value = "to", required = false) String to,
            @RequestParam(name = "nbWeeks", value = "nbWeeks", required = false) int nbWeeks
    ) {

        JSONObject toto = new JSONObject();
        try {
            toto = automateScoreService.generateAutomateScore(request, systems, campaigns, to, nbWeeks);
        } catch (Exception e) {
            LOG.error(e, e);
        }
        
        return toto.toString();

    }

}
