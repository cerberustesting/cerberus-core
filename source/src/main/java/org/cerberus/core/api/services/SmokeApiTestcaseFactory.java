/*
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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
package org.cerberus.core.api.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cerberus.core.api.controllers.BootstrapController;
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.entity.AppServiceHeader;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseCountry;
import org.cerberus.core.crud.entity.TestCaseCountryProperties;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.entity.TestCaseStepActionControl;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Builds the API smoke content from the endpoints really deployed: one REST service
 * and one testcase per public GET endpoint that can be called with the smoke data.
 * The list follows the deployed version, like the OpenAPI documentation does.
 */
@Component
public class SmokeApiTestcaseFactory {

    public static final String CREDENTIAL_PROPERTY = "CRED";
    public static final String SERVICE_PREFIX = "CERBERUS_SMOKE_API_GET";
    public static final String SERVICE_COLLECTION = "CERBERUS_SMOKE";
    
    private static final String API_PACKAGE = "org.cerberus.core.api.controllers";
    private static final Pattern PATH_VARIABLE = Pattern.compile("\\{([^}/]+)}");
    private static final Pattern VERSION_HEADER = Pattern.compile("X-API-VERSION=(\\S+)");

    private final RequestMappingHandlerMapping handlerMapping;

    public SmokeApiTestcaseFactory(@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    /**
     * A public GET endpoint callable with the smoke data.
     *
     * @param serviceName name of the service
     * @param path path to call, with the special characters encoded
     * @param displayPath path as a human reads it
     * @param apiVersion value of the X-API-VERSION header, null if the endpoint has none
     */
    public record Endpoint(String serviceName, String path, String displayPath, String apiVersion) {
    }

    /**
     * @param samples value to use for each path variable. An endpoint with another variable is left out.
     */
    public List<Endpoint> endpoints(Map<String, String> samples) {
        Map<String, Endpoint> result = new LinkedHashMap<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMapping.getHandlerMethods().entrySet()) {
            RequestMappingInfo info = entry.getKey();
            HandlerMethod handler = entry.getValue();
            if (!handler.getBeanType().getName().startsWith(API_PACKAGE)
                    || BootstrapController.class.equals(handler.getBeanType())
                    || !info.getMethodsCondition().getMethods().contains(RequestMethod.GET)
                    || hasRequiredQueryParameter(handler)
                    || !producesJsonOrNothing(info)
                    || info.getPathPatternsCondition() == null) {
                continue;
            }
            String version = apiVersion(info);
            for (String pattern : info.getPathPatternsCondition().getPatternValues()) {
                String displayPath = substitute(pattern, samples);
                if (displayPath != null && displayPath.startsWith("/public/")) {
                    result.putIfAbsent(displayPath, new Endpoint(
                            SERVICE_PREFIX + sanitize(displayPath),
                            displayPath.replace(" ", "%20"),
                            displayPath,
                            version));
                }
            }
        }
        // A fixed order : the same endpoints always get the same numbers on a first run.
        List<Endpoint> endpoints = new ArrayList<>(result.values());
        endpoints.sort(java.util.Comparator.comparing(Endpoint::displayPath));
        return endpoints;
    }

    /** Description of the testcase of an endpoint, also used to find it again. */
    public String description(Endpoint endpoint) {
        return "Smoke - GET " + endpoint.displayPath() + " returns HTTP 200";
    }

    public List<AppService> services(List<Endpoint> endpoints, String application, String login) {
        List<AppService> services = new ArrayList<>();
        for (Endpoint endpoint : endpoints) {
            List<AppServiceHeader> headers = new ArrayList<>();
            headers.add(header(endpoint, "X-API-KEY", "%" + CREDENTIAL_PROPERTY + "(ApiKey)%", 10, login));
            headers.add(header(endpoint, "Accept", MediaType.APPLICATION_JSON_VALUE, 20, login));
            if (endpoint.apiVersion() != null) {
                headers.add(header(endpoint, "X-API-VERSION", endpoint.apiVersion(), 30, login));
            }
            services.add(AppService.builder()
                    .service(endpoint.serviceName()).application(application).type(AppService.TYPE_REST)
                    .method("GET").servicePath(endpoint.path()).bodyType("none").serviceRequest("")
                    .serviceRequestExtra1("").operation("").attachementURL("").fileName("")
                    .schemaRegistryURL("").avroSchemaKey("").avroSchemaValue("")
                    .kafkaTopic("").kafkaKey("").kafkaFilterPath("").kafkaFilterValue("")
                    .kafkaFilterHeaderPath("").kafkaFilterHeaderValue("")
                    .description("Smoke call of GET " + endpoint.displayPath())
                    .collection(SERVICE_COLLECTION).usrCreated(login).headerList(headers).build());
        }
        return services;
    }

    /** One testcase per endpoint : call the service, check the HTTP code. */
    public TestCase testCase(Endpoint endpoint, String testcaseId, String testFolder, String system, String application, String country,
            String dataLibName, String login) {
        TestCaseStepActionControl control = TestCaseStepActionControl.builder()
                .stepId(10).actionId(10).controlId(10).sort(10).control("verifyStringEqual")
                .value1("%system.LASTSERVICE_HTTPCODE%").value2("200").value3("")
                .description("HTTP status is 200")
                .conditionOperator("always").conditionValue1("").conditionValue2("").conditionValue3("")
                .conditionOptions(new JSONArray()).options(new JSONArray())
                .isFatal(true).usrCreated(login).build();
        TestCaseStepAction action = TestCaseStepAction.builder()
                .stepId(10).actionId(10).sort(10).action("callService")
                .value1(endpoint.serviceName()).value2("").value3("")
                .description("GET " + endpoint.displayPath())
                .conditionOperator("always").conditionValue1("").conditionValue2("").conditionValue3("")
                .conditionOptions(new JSONArray()).options(new JSONArray())
                .isFatal(true).usrCreated(login).build();
        action.setControls(new ArrayList<>(List.of(control)));
        TestCaseStep step = TestCaseStep.builder()
                .stepId(10).sort(10).description("Call GET " + endpoint.displayPath()).loop("onceIfConditionTrue")
                .conditionOperator("always").conditionValue1("").conditionValue2("").conditionValue3("")
                .conditionOptions(new JSONArray())
                .libraryStepTest("").libraryStepTestcase("").libraryStepStepId(0)
                .usrCreated(login).actions(new ArrayList<>(List.of(action))).build();

        TestCaseCountryProperties credentials = TestCaseCountryProperties.builder()
                .country(country).property(CREDENTIAL_PROPERTY).type("getFromDataLib").value1(dataLibName)
                .value2("").value3("").nature("STATIC").length("0").rowLimit(1).description("Smoke API credentials")
                .database("").usrCreated(login).build();
        TestCaseCountry testCaseCountry = TestCaseCountry.builder()
                .country(country).testCaseCountryProperty(List.of(credentials)).usrCreated(login).build();

        return TestCase.builder()
                .test(testFolder).testcase(testcaseId).application(application).system(system)
                .description(description(endpoint))
                .detailedDescription("Created by the instance bootstrap from the endpoints deployed on the instance.")
                .type("AUTOMATED").status("WORKING").priority(1)
                .isActive(true).isActiveQA(true).isActiveUAT(true).isActivePROD(true)
                .conditionOperator("always").conditionValue1("").conditionValue2("").conditionValue3("")
                .conditionOptions(new JSONArray()).bugs(new JSONArray())
                .origine("").refOrigine("").comment("").implementer("").executor("").userAgent("").screenSize("")
                .fromMajor("").fromMinor("").toMajor("").toMinor("").targetMajor("").targetMinor("")
                .usrCreated(login)
                .steps(new ArrayList<>(List.of(step)))
                .testCaseCountries(List.of(testCaseCountry))
                .dependencies(new ArrayList<>()).testCaseLabels(new ArrayList<>())
                .build();
    }

    private AppServiceHeader header(Endpoint endpoint, String key, String value, int sort, String login) {
        AppServiceHeader header = new AppServiceHeader();
        header.setService(endpoint.serviceName());
        header.setKey(key);
        header.setValue(value);
        header.setSort(sort);
        header.setActive(true);
        header.setDescription("");
        header.setUsrCreated(login);
        return header;
    }

    private String substitute(String pattern, Map<String, String> samples) {
        Matcher matcher = PATH_VARIABLE.matcher(pattern);
        StringBuilder path = new StringBuilder();
        while (matcher.find()) {
            // "{version}" can come with a regex ("{id:[0-9]+}"), only the name matters.
            String value = samples.get(matcher.group(1).split(":")[0]);
            if (value == null) {
                return null;
            }
            matcher.appendReplacement(path, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(path);
        return path.toString();
    }

    private boolean hasRequiredQueryParameter(HandlerMethod handler) {
        for (var parameter : handler.getMethodParameters()) {
            RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
            if (requestParam != null && requestParam.required() && ValueConstants.DEFAULT_NONE.equals(requestParam.defaultValue())) {
                return true;
            }
        }
        return false;
    }

    private boolean producesJsonOrNothing(RequestMappingInfo info) {
        var produces = info.getProducesCondition().getProducibleMediaTypes();
        return produces.isEmpty() || produces.contains(MediaType.APPLICATION_JSON);
    }

    private String apiVersion(RequestMappingInfo info) {
        for (var expression : info.getHeadersCondition().getExpressions()) {
            Matcher matcher = VERSION_HEADER.matcher(expression.toString());
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    private String sanitize(String text) {
        return text.replaceAll("[^A-Za-z0-9]", "_");
    }
}
