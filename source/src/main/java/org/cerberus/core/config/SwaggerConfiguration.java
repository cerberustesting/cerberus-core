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

package org.cerberus.core.config;

import java.security.Principal;
import java.util.Objects;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.condition.NameValueExpression;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author MorganLmd
 */
@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    private static final String PUBLIC_API_VERSION_1 = "1";

    private static final Tag INVARIANT_TAG = new Tag("Invariant", "Invariant endpoint");
    private static final Tag TESTCASE_TAG = new Tag("Testcase", "Testcase endpoint");
    private static final Tag TESTCASEACTION_TAG = new Tag("Testcase Action", "Testcase Action endpoint");
    private static final Tag TESTCASECONTROL_TAG = new Tag("Testcase Control", "Testcase Control endpoint");
    private static final Tag TESTCASESTEP_TAG = new Tag("Testcase Step", "Testcase Step endpoint");
    private static final Tag SERVICE_TAG = new Tag("Service", "Service endpoint");
    private static final Tag CAMPAIGNEXECUTION_TAG = new Tag("Campaign Execution", "Campaign Execution endpoint");
    private static final Tag QUEUEDEXECUTION_TAG = new Tag("Queued Execution", "Queued Execution endpoint");
    private static final Tag USER_TAG = new Tag("User", "User endpoint");
    private static final Tag APPLICATION_TAG = new Tag("Application", "Application endpoint");
    private static final Tag MANAGE_TAG = new Tag("Manage", "Cerberus Management endpoint");

    private static final String LICENSE_URL = "https://www.gnu.org/licenses/gpl-3.0.en.html";
    private static final String GITHUB_REPOSITORY = "https://github.com/cerberustesting/cerberus-source";

    @Bean
    public Docket swaggerDocV1() {
        return configureVersion(PUBLIC_API_VERSION_1);
    }

    private Docket configureVersion(String version) {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("public API version " + version)
                .ignoredParameterTypes(Principal.class)
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.cerberus.core.api.controllers"))
                .apis(p -> {
                    //Bypass API version for SVG because these routes are used only directly with URL without HTTP headers
                    if (p.getName().contains("Svg")) {
                        return true;
                    }
                    if (p.headers() != null || p.getName().equals("findCiSvgCampaignExecutionByCampaignId")) {
                        for (NameValueExpression<String> nve : p.headers()) {
                            if ((nve.getName().equals("X-API-VERSION")) && (Objects.equals(nve.getValue(), version))) {
                                return true;
                            }
                        }
                    }
                    return false;
                })
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo(version))
                .tags(INVARIANT_TAG)
                .tags(TESTCASE_TAG)
                .tags(TESTCASESTEP_TAG)
                .tags(TESTCASEACTION_TAG)
                .tags(TESTCASECONTROL_TAG)
                .tags(SERVICE_TAG)
                .tags(CAMPAIGNEXECUTION_TAG)
                .tags(QUEUEDEXECUTION_TAG)
                .tags(USER_TAG)
                .tags(APPLICATION_TAG)
                .tags(MANAGE_TAG)
                .useDefaultResponseMessages(false);
    }

    private ApiInfo apiInfo(String version) {
        return new ApiInfoBuilder()
                .title("Cerberus public API")
                .description("Documentation for Cerberus testing public API")
                .version(version)
                .license("GNU General Public License v3.0")
                .licenseUrl(LICENSE_URL)
                .termsOfServiceUrl(GITHUB_REPOSITORY)
                .build();
    }
}
