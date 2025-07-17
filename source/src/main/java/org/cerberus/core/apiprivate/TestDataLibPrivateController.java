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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import javax.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.Operation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.exceptions.EntityNotFoundException;
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.crud.entity.TestDataLib;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestDataLibService;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.servlet.ServletUtil;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author bcivel
 */
@RestController
@RequestMapping("/testdatalibs")
public class TestDataLibPrivateController {

    private static final Logger LOG = LogManager.getLogger(TestDataLibPrivateController.class);
    private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

    @Autowired
    ITestDataLibService testDataLibService;
    @Autowired
    ILogEventService logEventService;
    @Autowired
    IParameterService parameterService;

    /**
     * Read By Key
     *
     * @param testdatalibid
     * @param request
     * @return
     */
    @Operation(hidden=true)
    @GetMapping(path = "{testdatalibid}/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Resource> downloadCsv(
            @PathVariable("testdatalibid") int testdatalibid,
            HttpServletRequest request) {
        Resource resource;
        String filename = "";
        try {

            ServletUtil.servletStart(request);

            AnswerItem<TestDataLib> answerTest = testDataLibService.readByKey(testdatalibid);
            TestDataLib res = answerTest.getItem();
            if ((res == null) || !(TestDataLib.TYPE_FILE.equals(res.getType())) || (StringUtil.isEmptyOrNull(res.getCsvUrl()))) {
                throw new EntityNotFoundException(TestDataLib.class, "Data Library", testdatalibid);
            }

            filename = res.getName();
            String servicePathCsv = res.getCsvUrl();
            if (!StringUtil.isURL(servicePathCsv)) {
                // Url is still not valid. We try to add the path from csv parameter.
                String csv_path = parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_testdatalibfile_path, "", "");
                csv_path = StringUtil.addSuffixIfNotAlready(csv_path, File.separator);
                servicePathCsv = csv_path + servicePathCsv;
                resource = new InputStreamResource(new FileInputStream(servicePathCsv));

            } else {

                resource = new UrlResource(servicePathCsv);

            }

        } catch (MalformedURLException e) {
            LOG.error(e, e);
            return null;
        } catch (FileNotFoundException ex) {
            LOG.error(ex, ex);
            return null;
        }

        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);

    }

}
