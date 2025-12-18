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

import java.time.LocalDate;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.Operation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.dto.application.ApplicationMonthlyStatsDTOV001;
import org.cerberus.core.api.dto.application.ApplicationStatsDTOV001;
import org.cerberus.core.crud.entity.stats.ApplicationStats;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author bcivel
 */
@RestController
@RequestMapping("/applications")
public class ApplicationPrivateController {

    private static final Logger LOG = LogManager.getLogger(ApplicationPrivateController.class);
    private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

    @Autowired
    IApplicationService applicationService;

    @Operation(hidden=true)
    @GetMapping("/count")
    public String getnbByCriteria(
            @RequestParam(name = "system", value = "system", required = false) List<String> systems,
            HttpServletRequest request) {

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        JSONObject jsonResponse = new JSONObject();

        try {
            LOG.debug(systems);

            return jsonResponse.put("iTotalRecords", applicationService.getNbApplications(systems)).toString();
        } catch (Exception ex) {
            LOG.warn(ex, ex);
            return "error " + ex.getMessage();
        }
    }


    @Operation(hidden = true)
    @GetMapping("/monthlyStats")
    public ApplicationMonthlyStatsDTOV001 getMonthlyStats(
            @RequestParam(name = "system", required = false) List<String> systems) {

        LocalDate today = LocalDate.now();

        // Périodes
        LocalDate thisStartDate = today.minusDays(30);
        LocalDate thisEndDate   = today;

        // --- Get Global Stats : All dates, All systems --- and build DTO
        ApplicationStats statsGlobal = applicationService
                .readApplicationStats(null, null, null)
                .getItem();
        ApplicationStatsDTOV001 statsGlobalDto = ApplicationStatsDTOV001.builder()
                .totalApplications(statsGlobal.getTotalApplications())
                .totalApplicationsByType(statsGlobal.getTotalApplicationsByType())
                .fromDate(statsGlobal.getFromDate())
                .toDate(statsGlobal.getToDate())
                .build();

        // --- Get last month Stats : Last 30 days, All systems --- and build DTO
        ApplicationStats statsGlobalPreviousMonth = applicationService
                .readApplicationStats(thisStartDate.toString(), thisEndDate.toString(), null)
                .getItem();
        ApplicationStatsDTOV001 statsGlobalPreviousMonthDto = ApplicationStatsDTOV001.builder()
                .totalApplications(statsGlobalPreviousMonth.getTotalApplications())
                .totalApplicationsByType(statsGlobalPreviousMonth.getTotalApplicationsByType())
                .fromDate(statsGlobalPreviousMonth.getFromDate())
                .toDate(statsGlobalPreviousMonth.getToDate())
                .build();

        // --- Get Selected System Stats : All dates, selected systems --- and build DTO
        ApplicationStats statsSystems = applicationService
                .readApplicationStats(null, null, systems)
                .getItem();
        ApplicationStatsDTOV001 statsSystemsDto = ApplicationStatsDTOV001.builder()
                .totalApplications(statsSystems.getTotalApplications())
                .totalApplicationsByType(statsSystems.getTotalApplicationsByType())
                .fromDate(statsSystems.getFromDate())
                .toDate(statsSystems.getToDate())
                .build();

        // --- Get Selected System Stats : Last 30 days, selected systems --- and build DTO
        ApplicationStats statsSystemsPreviousMonth = applicationService
                .readApplicationStats(thisStartDate.toString(), thisEndDate.toString(), systems)
                .getItem();
        ApplicationStatsDTOV001 statsSystemsPreviousMonthDto = ApplicationStatsDTOV001.builder()
                .totalApplications(statsSystemsPreviousMonth.getTotalApplications())
                .totalApplicationsByType(statsSystemsPreviousMonth.getTotalApplicationsByType())
                .fromDate(statsSystemsPreviousMonth.getFromDate())
                .toDate(statsSystemsPreviousMonth.getToDate())
                .build();

       // --- Build DTO final ---
        return ApplicationMonthlyStatsDTOV001.builder()
                .global(statsGlobalDto)
                .globalPreviousMonth(statsGlobalPreviousMonthDto)
                .system(statsSystemsDto)
                .systemPreviousMonth(statsSystemsPreviousMonthDto)
                .build();
    }

}
