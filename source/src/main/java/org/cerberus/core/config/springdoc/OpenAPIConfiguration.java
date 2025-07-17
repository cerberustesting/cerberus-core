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

package org.cerberus.core.config.springdoc;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springdoc.core.*;
import org.springdoc.webmvc.api.OpenApiWebMvcResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springdoc.webmvc.core.SpringDocWebMvcConfiguration;
import org.springframework.web.filter.ForwardedHeaderFilter;

import javax.annotation.PostConstruct;
import java.util.Arrays;

@Configuration
@OpenAPIDefinition(
        servers = {
                @io.swagger.v3.oas.annotations.servers.Server(url = "/api")
        }
)
@Import({
        org.springdoc.core.SpringDocConfiguration.class,
        org.springdoc.webmvc.core.SpringDocWebMvcConfiguration.class
})
public class OpenAPIConfiguration {

    private static final Logger LOG = LogManager.getLogger(OpenAPIConfiguration.class);
    private static final String PUBLIC_API_VERSION_1 = "1";
    private static final String LICENSE_URL = "https://www.gnu.org/licenses/gpl-3.0.en.html";
    private static final String GITHUB_REPOSITORY = "https://github.com/cerberustesting/cerberus-source";

    @PostConstruct
    public void logSwaggerStartup() {
        LOG.debug("Swagger configuration loaded !");
    }

    @Autowired
    ApplicationContext context;

    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

    @PostConstruct
    public void logBeans() {
        Arrays.stream(context.getBeanDefinitionNames())
                .filter(name -> name.contains("GroupedOpenApi") || name.contains("springdoc"))
                .forEach(name -> LOG.debug("Found bean: {}", name));
    }

    @Bean
    public SpringDocConfigProperties springDocConfigProperties() {
        SpringDocConfigProperties props = new SpringDocConfigProperties();
        SpringDocConfigProperties.ApiDocs ad = new SpringDocConfigProperties.ApiDocs();
        ad.setPath("/api/v3/api-docs");
        props.setApiDocs(ad);
        return props;
    }

    @Bean
    public GroupedOpenApi publicApi() {
        LOG.info("GroupedOpenApi bean for 'public' loaded");
        String paths[] = {"/public/**", "/api/public/**"};
        String packagesToScan[] = {"org.cerberus.core.api"};
        return GroupedOpenApi.builder()
                .group("public")
                .displayName("Cerberus Public API")
                .pathsToMatch(paths)
                .packagesToScan(packagesToScan)
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cerberus public API")
                        .version(PUBLIC_API_VERSION_1)
                        .description("Documentation for Cerberus testing public API")
                        .termsOfService(GITHUB_REPOSITORY)
                        .license(new License().name("GNU General Public License v3.0").url(LICENSE_URL))
                );
    }

}
