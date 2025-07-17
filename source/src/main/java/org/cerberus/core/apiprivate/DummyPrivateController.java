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

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.Operation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.service.IAppServiceService;
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
@RequestMapping("/dummy")
public class DummyPrivateController {

    private static final Logger LOG = LogManager.getLogger(DummyPrivateController.class);
    private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

    @Autowired
    IAppServiceService appserviceService;

    @Operation(hidden=true)
    @GetMapping("/testcsv")
    public String getnbByCriteria(
            HttpServletRequest request) {

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        JSONObject jsonResponse = new JSONObject();

        try {
            return "id,model,code,openingDate,name\n"
                    + "123,referential.site,10,04/30/1981 10:00:00,LILLE V2";
        } catch (Exception ex) {
            LOG.warn(ex, ex);
            return "error " + ex.getMessage();
        }
    }

    @Operation(hidden=true)
    @GetMapping("/empty")
    public String getEmpty(
            HttpServletRequest request) {

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        try {
            return null;
        } catch (Exception ex) {
            LOG.warn(ex, ex);
            return "error " + ex.getMessage();
        }
    }

}
