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
package org.cerberus.core.config.openapi;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/v3")
public class OpenApiController {

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    @GetMapping(value = "/api-docs", produces = MediaType.APPLICATION_JSON_VALUE)
    public String apiDocs() {
        OpenAPI openAPI = new OpenAPI()
                .addServersItem(new Server().url("/api"))
                .info(new Info()
                        .title("Cerberus public API")
                        .version("1")
                        .description("Documentation for Cerberus testing public API")
                        .license(new License()
                                .name("GNU General Public License v3.0")
                                .url("https://www.gnu.org/licenses/gpl-3.0.en.html"))
                );
        Paths paths = new Paths();
        Components components = new Components();
        Map<String, Schema> schemas = new LinkedHashMap<>();

        handlerMapping.getHandlerMethods().forEach((info, handlerMethod) -> {

            Operation swaggerOp = handlerMethod.getMethodAnnotation(Operation.class);
            if (swaggerOp != null && swaggerOp.hidden()) {
                return; // skip private controller
            }

            io.swagger.v3.oas.models.Operation operation = new io.swagger.v3.oas.models.Operation();
            operation.addTagsItem(handlerMethod.getBeanType().getSimpleName());

            if (swaggerOp != null) {
                operation.setSummary(swaggerOp.summary());
                operation.setDescription(swaggerOp.description());
                operation.setOperationId(swaggerOp.operationId());
            } else {
                operation.setSummary(handlerMethod.getMethod().getName());
            }

            // ── Paramètres
            List<Parameter> parameters = new ArrayList<>();
            MethodParameter[] methodParams = handlerMethod.getMethodParameters();

            for (MethodParameter mp : methodParams) {
                // @PathVariable
                PathVariable pv = mp.getParameterAnnotation(PathVariable.class);
                if (pv != null) {
                    String name = pv.value().isEmpty() ? mp.getParameterName() : pv.value();
                    parameters.add(new PathParameter()
                            .name(name)
                            .required(true)
                            .schema(schemaForType(mp.getParameterType())));
                }

                // @RequestParam
                RequestParam rp = mp.getParameterAnnotation(RequestParam.class);
                if (rp != null) {
                    String name = rp.value().isEmpty() ? mp.getParameterName() : rp.value();
                    parameters.add(new QueryParameter()
                            .name(name)
                            .required(rp.required())
                            .schema(schemaForType(mp.getParameterType())));
                }

                // @RequestBody
                RequestBody rb = mp.getParameterAnnotation(RequestBody.class);
                if (rb != null) {
                    String schemaName = mp.getParameterType().getSimpleName();
                    Schema<?> schema = schemaForType(mp.getParameterType());
                    schemas.put(schemaName, schema);

                    io.swagger.v3.oas.models.parameters.RequestBody requestBody =
                            new io.swagger.v3.oas.models.parameters.RequestBody()
                                    .required(rb.required())
                                    .content(new Content().addMediaType(
                                            "application/json",
                                            new io.swagger.v3.oas.models.media.MediaType().schema(
                                                    new Schema<>().$ref("#/components/schemas/" + schemaName)
                                            )
                                    ));
                    operation.setRequestBody(requestBody);
                }
            }

            if (!parameters.isEmpty()) {
                operation.setParameters(parameters);
            }

            // ── Responses
            ApiResponses apiResponses = new ApiResponses();
            if (swaggerOp != null && swaggerOp.responses().length > 0) {
                for (io.swagger.v3.oas.annotations.responses.ApiResponse r : swaggerOp.responses()) {
                    apiResponses.addApiResponse(r.responseCode(),
                            new io.swagger.v3.oas.models.responses.ApiResponse()
                                    .description(r.description()));
                }
            } else {
                apiResponses.addApiResponse("200",
                        new io.swagger.v3.oas.models.responses.ApiResponse()
                                .description("OK"));
            }
            operation.setResponses(apiResponses);

            // ── PathItem
            info.getPatternValues().forEach(pattern -> {
                PathItem pathItem = paths.getOrDefault(pattern, new PathItem());
                info.getMethodsCondition().getMethods().forEach(httpMethod -> {
                    switch (httpMethod.name()) {
                        case "GET"    -> pathItem.setGet(operation);
                        case "POST"   -> pathItem.setPost(operation);
                        case "PUT"    -> pathItem.setPut(operation);
                        case "DELETE" -> pathItem.setDelete(operation);
                        case "PATCH"  -> pathItem.setPatch(operation);
                    }
                });
                paths.addPathItem(pattern, pathItem);
            });
        });

        if (!schemas.isEmpty()) {
            components.setSchemas(schemas);
            openAPI.setComponents(components);
        }

        openAPI.setPaths(paths);
        return Json.pretty(openAPI);
    }

    // ── Helper : type Java → Schema OpenAPI
    private Schema<?> schemaForType(Class<?> type) {
        if (type == String.class)                          return new StringSchema();
        if (type == Integer.class || type == int.class)    return new IntegerSchema();
        if (type == Long.class    || type == long.class)   return new IntegerSchema().format("int64");
        if (type == Boolean.class || type == boolean.class) return new BooleanSchema();
        if (type == Double.class  || type == double.class) return new NumberSchema().format("double");
        // Objet complexe → schema par référence
        return new ObjectSchema().name(type.getSimpleName());
    }
}
