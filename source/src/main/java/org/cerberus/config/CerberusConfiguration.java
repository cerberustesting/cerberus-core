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
package org.cerberus.config;

import java.security.Principal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
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
 *
 * @author bcivel
 */
@Configuration
@EnableSwagger2
@EnableAsync
@EnableScheduling
@ComponentScan("org.cerberus")
public class CerberusConfiguration {

    @Bean
    public PropertiesResolver properties() {
        PropertiesResolver pr = new PropertiesResolver();
        pr.setOrder(1);
        pr.setIgnoreUnresolvablePlaceholders(false);
        return pr;
    }

    @Bean
    public JndiObjectFactoryBean dataSource() {
        JndiObjectFactoryBean jpfb = new JndiObjectFactoryBean();
        jpfb.setJndiName("jdbc/cerberus" + System.getProperty(Property.ENVIRONMENT));
        jpfb.setResourceRef(true);
        return jpfb;
    }

    @Bean
    public Docket swaggerDocV1() {
        return configureVersion("1");
    }

    private Docket configureVersion(String version) {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("public API version " + version)
                .ignoredParameterTypes(Principal.class)
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.cerberus.api.controller"))
                .apis(p -> {
                    if (p.headers() != null) {
                        for (NameValueExpression nve : p.headers()) {
                            if ((nve.getName().equals("X-API-VERSION")) && (nve.getValue().equals(version))) {
                                return true;
                            }
                        }
                    }
                    return false;
                })
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo(version))
                .tags(new Tag("Invariant", "Description of Invariant endpoint"))
                .tags(new Tag("Testcase", "Description of Testcase endpoint"))
                .tags(new Tag("Testcase Action", "Description of Testcase Control endpoint"))
                .tags(new Tag("Testcase Step", "Description of Testcase Step endpoint"))
                .useDefaultResponseMessages(false);
    }

    private ApiInfo apiInfo(String version) {
        return new ApiInfoBuilder()
                .title("Cerberus public API")
                .description("Documentation for Cerberus testing public API")
                .version(version)
                .license("GNU General Public License v3.0")
                .licenseUrl("https://www.gnu.org/licenses/gpl-3.0.en.html")
                .termsOfServiceUrl("https://github.com/cerberustesting/cerberus-source")
                .build();
    }
}
