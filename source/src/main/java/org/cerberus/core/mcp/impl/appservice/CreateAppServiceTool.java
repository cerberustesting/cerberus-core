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
package org.cerberus.core.mcp.impl.appservice;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.CurlCommandParser;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.api.dto.appservice.AppServiceMapperV001;
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.entity.AppServiceHeader;
import org.cerberus.core.crud.service.IAppServiceHeaderService;
import org.cerberus.core.crud.service.IAppServiceService;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that creates a new {@link AppService} entity in Cerberus.
 *
 * <p>Exposed MCP tool name: {@code cerberus_appservice_create}</p>
 *
 * <p>Performs a duplicate check via {@link IAppServiceService#readByKey(String)} before
 * attempting to persist, returning a clear error when the service name is already taken.</p>
 *
 * <p>Delegates to {@link IAppServiceService} for persistence.</p>
 */
@Component
public class CreateAppServiceTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_appservice_create";

    /** Authorization value prefixes Cerberus can store as first-class authentication. */
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String BASIC_PREFIX = "Basic ";

    private final IAppServiceService appServiceService;
    private final IAppServiceHeaderService appServiceHeaderService;
    private final AppServiceMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public CreateAppServiceTool(IAppServiceService appServiceService,
                                IAppServiceHeaderService appServiceHeaderService,
                                AppServiceMapperV001 mapper,
                                MCPLogUtils mcpLogUtils) {
        this.appServiceService = appServiceService;
        this.appServiceHeaderService = appServiceHeaderService;
        this.mapper = mapper;
        this.mcpLogUtils = mcpLogUtils;
    }

    @Override
    public McpServerFeatures.SyncToolSpecification toToolSpecification() {
        return new McpServerFeatures.SyncToolSpecification(
                createTool(),
                (exchange, request) -> {
                    Map<String, Object> args = MCPToolUtils.argumentsOrEmpty(request.arguments());
                    return execute(args);
                }
        );
    }

    /**
     * Builds the MCP tool schema, declaring the tool name, description, and JSON input schema.
     *
     * @return the tool specification describing accepted parameters and their constraints
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("service", Map.of(
                "type", "string",
                "description", "Unique name / identifier of the new app service."
        ));
        properties.put("type", Map.of(
                "type", "string",
                "description", "Service type. Allowed values: "
                        + AppService.TYPE_REST + ", "
                        + AppService.TYPE_SOAP + ", "
                        + AppService.TYPE_FTP + ", "
                        + AppService.TYPE_KAFKA + ", "
                        + AppService.TYPE_MONGODB + ".",
                "enum", List.of(
                        AppService.TYPE_REST,
                        AppService.TYPE_SOAP,
                        AppService.TYPE_FTP,
                        AppService.TYPE_KAFKA,
                        AppService.TYPE_MONGODB
                )
        ));
        properties.put("method", Map.of(
                "type", "string",
                "description", "HTTP / protocol method. Allowed values: "
                        + AppService.METHOD_HTTPGET + ", "
                        + AppService.METHOD_HTTPPOST + ", "
                        + AppService.METHOD_HTTPPUT + ", "
                        + AppService.METHOD_HTTPPATCH + ", "
                        + AppService.METHOD_HTTPDELETE + ", "
                        + AppService.METHOD_KAFKAPRODUCE + ", "
                        + AppService.METHOD_KAFKASEARCH + ", "
                        + AppService.METHOD_MONGODBFIND + ", "
                        + AppService.METHOD_MONGODBUPDATEONE + ", "
                        + AppService.METHOD_MONGODBINSERTONE + ", "
                        + AppService.METHOD_MONGODBREPLACEONE + ".",
                "enum", List.of(
                        AppService.METHOD_HTTPGET,
                        AppService.METHOD_HTTPPOST,
                        AppService.METHOD_HTTPPUT,
                        AppService.METHOD_HTTPPATCH,
                        AppService.METHOD_HTTPDELETE,
                        AppService.METHOD_KAFKAPRODUCE,
                        AppService.METHOD_KAFKASEARCH,
                        AppService.METHOD_MONGODBFIND,
                        AppService.METHOD_MONGODBUPDATEONE,
                        AppService.METHOD_MONGODBINSERTONE,
                        AppService.METHOD_MONGODBREPLACEONE
                )
        ));
        properties.put("application", Map.of(
                "type", "string",
                "description", "Optional application this service belongs to."
        ));
        properties.put("servicePath", Map.of(
                "type", "string",
                "description", "Optional URL / path of the service endpoint."
        ));
        properties.put("serviceRequest", Map.of(
                "type", "string",
                "description", "Optional default request body / payload."
        ));
        properties.put("bodyType", Map.of(
                "type", "string",
                "description", "Optional body type. Allowed values: "
                        + AppService.SRVBODYTYPE_NONE + ", "
                        + AppService.SRVBODYTYPE_RAW + ", "
                        + AppService.SRVBODYTYPE_FORMDATA + ", "
                        + AppService.SRVBODYTYPE_FORMURLENCODED + ".",
                "enum", List.of(
                        AppService.SRVBODYTYPE_NONE,
                        AppService.SRVBODYTYPE_RAW,
                        AppService.SRVBODYTYPE_FORMDATA,
                        AppService.SRVBODYTYPE_FORMURLENCODED
                )
        ));
        properties.put("description", Map.of(
                "type", "string",
                "description", "Optional human-readable description of the service."
        ));
        properties.put("authType", Map.of(
                "type", "string",
                "description", "Authentication scheme used to call the service. Defaults to '"
                        + AppService.AUTHTYPE_NONE + "'. Use authUser and authPassword to supply the credentials.",
                "enum", List.of(
                        AppService.AUTHTYPE_NONE,
                        AppService.AUTHTYPE_APIKEY,
                        AppService.AUTHTYPE_BEARERTOKEN,
                        AppService.AUTHTYPE_BASICAUTH
                )
        ));
        properties.put("authUser", Map.of(
                "type", "string",
                "description", "User name for Basic Auth, or the key name for API Key. Ignored when authType is '"
                        + AppService.AUTHTYPE_NONE + "'."
        ));
        properties.put("authPassword", Map.of(
                "type", "string",
                "description", "Password, API key value or bearer token. Ignored when authType is '"
                        + AppService.AUTHTYPE_NONE + "'. Never invent a value: ask the user for the real "
                        + "credential, or leave the service unauthenticated."
        ));
        properties.put("authAddTo", Map.of(
                "type", "string",
                "description", "Where to place the credential, only used when authType is '"
                        + AppService.AUTHTYPE_APIKEY + "'. Defaults to '" + AppService.AUTHADDTO_HEADERS + "'.",
                "enum", List.of(
                        AppService.AUTHADDTO_HEADERS,
                        AppService.AUTHADDTO_QUERYSTRING
                )
        ));

        properties.put("curl", Map.of(
                "type", "string",
                "description", """
                        A complete curl command describing the call to register. When provided, the service type,
                        method, URL, body and headers are all derived from it, and type and method become optional.

                        Pass the user's command through verbatim — including quotes, backslash line continuations
                        and the body — rather than decomposing it yourself. The command is parsed server-side by
                        the same rules as the cURL import button on the App Service screen, so the resulting
                        service is identical to what that button produces.

                        Any explicit parameter you also pass wins over the value parsed from the command, which
                        lets you override just the piece the user wants changed.
                        """
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Creates a new app service in Cerberus.

                Call this tool whenever the user asks to create, add, or register a new service / API endpoint.
                Always requires a unique service name.

                When the user gives you a curl command — pasted from a terminal, from API documentation, or
                copied out of a browser's network tab — pass it whole in the curl parameter instead of splitting
                it into fields. One call then creates the service and all of its headers together, the method and
                body are inferred exactly as curl itself would, and credentials land in the dedicated
                authentication fields where Cerberus masks them, rather than in a plain header row.

                Without curl, describe the call field by field: type and method are then both required, and each
                header needs a separate cerberus_appservice_header_create call afterwards.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("service"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Create app service", false),
                null
        );
    }

    /**
     * Validates input, checks for duplicates, creates the {@link AppService} entity, and returns the result.
     *
     * <p>The duplicate check with {@link IAppServiceService#readByKey(String)} prevents a redundant
     * DAO call and surfaces a clearer error message than letting the database throw a unique-key
     * violation.</p>
     *
     * @param args the MCP call arguments extracted from the request
     * @return a success result containing the created service DTO, or an error result on failure
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String service = MCPToolUtils.getString(args, "service", "");
        String type = MCPToolUtils.getString(args, "type", "");
        String method = MCPToolUtils.getString(args, "method", "");
        String application = MCPToolUtils.getString(args, "application", "");
        String servicePath = MCPToolUtils.getString(args, "servicePath", "");
        String serviceRequest = MCPToolUtils.getString(args, "serviceRequest", "");
        String bodyType = MCPToolUtils.getString(args, "bodyType", "");
        String description = MCPToolUtils.getString(args, "description", "");
        String authType = MCPToolUtils.getString(args, "authType", "");
        String authUser = MCPToolUtils.getString(args, "authUser", "");
        String authPassword = MCPToolUtils.getString(args, "authPassword", "");
        String authAddTo = MCPToolUtils.getString(args, "authAddTo", AppService.AUTHADDTO_HEADERS);
        String curl = MCPToolUtils.getString(args, "curl", "");

        // The curl command is never written to the audit log, and neither are the header values
        // derived from it : a pasted command routinely carries a bearer token, an API key or a
        // password, and the log event table is readable from the GUI.
        mcpLogUtils.call(TOOL_NAME, "appservice_create",
                String.format("MCP tool %s called with service=%s, type=%s, method=%s, fromCurl=%s",
                        TOOL_NAME, service, type, method, !curl.isBlank()));

        if (service.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: service");
        }

        List<CurlCommandParser.CurlHeader> parsedHeaders = List.of();
        List<String> derived = new ArrayList<>();

        if (!curl.isBlank()) {
            CurlCommandParser.ParsedCurl parsed = CurlCommandParser.parse(curl);

            if (parsed == null) {
                return MCPToolUtils.errorText(
                        "Could not parse this curl command. It must start with \"curl\" and carry at least a URL, "
                        + "a header or a body. Pass the command exactly as the user wrote it, or describe the "
                        + "call with the type / method / servicePath parameters instead.");
            }

            // A curl command always describes an HTTP call, so the service type follows from it.
            if (type.isBlank()) {
                type = AppService.TYPE_REST;
                derived.add("type");
            }
            if (method.isBlank()) {
                method = parsed.method();
                derived.add("method");
            }
            if (servicePath.isBlank() && !parsed.url().isEmpty()) {
                servicePath = parsed.url();
                derived.add("servicePath");
            }
            if (serviceRequest.isBlank() && !parsed.body().isEmpty()) {
                serviceRequest = parsed.body();
                derived.add("serviceRequest");
            }
            if (bodyType.isBlank() && !parsed.body().isEmpty()) {
                bodyType = AppService.SRVBODYTYPE_RAW;
                derived.add("bodyType");
            }

            CurlAuthentication curlAuthentication = extractAuthentication(parsed);
            parsedHeaders = curlAuthentication.remainingHeaders();

            // Credentials go to the dedicated auth fields rather than staying plain header rows :
            // the engine rebuilds the exact same Authorization header at call time, and storing
            // them here is what lets Cerberus mask the value instead of displaying it.
            if (authType.isBlank() && !curlAuthentication.authType().isEmpty()) {
                authType = curlAuthentication.authType();
                authUser = authUser.isBlank() ? curlAuthentication.authUser() : authUser;
                authPassword = authPassword.isBlank() ? curlAuthentication.authPassword() : authPassword;
                derived.add("authentication");
            }
        }

        if (type.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: type (or pass a curl command to derive it)");
        }
        if (method.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: method (or pass a curl command to derive it)");
        }

        AnswerItem<AppService> existingAnswer = appServiceService.readByKey(service);
        if (existingAnswer.isCodeStringEquals("OK") && existingAnswer.getItem() != null) {
            return MCPToolUtils.errorText("Service already exists: " + service);
        }

        AppService appService = new AppService();
        appService.setService(service);
        appService.setType(type);
        appService.setMethod(method);
        appService.setApplication(application);
        appService.setServicePath(servicePath);
        appService.setServiceRequest(serviceRequest);
        appService.setBodyType(bodyType);
        appService.setDescription(description);
        // AuthType, AuthUser, AuthPassword and AuthAddTo are NOT NULL in the appservice table and
        // the INSERT names every column, so a schema default never applies: leaving these unset
        // makes the insert fail outright. They are populated here rather than relying on the DAO
        // safety net so the entity handed to the service layer is valid on its own.
        appService.setAuthType(authType.isBlank() ? AppService.AUTHTYPE_NONE : authType);
        appService.setAuthUser(authUser);
        appService.setAuthPassword(authPassword);
        // Only read for API Key auth, but NOT NULL for every service, so it always carries a value.
        appService.setAuthAddTo(authAddTo.isBlank() ? AppService.AUTHADDTO_HEADERS : authAddTo);
        appService.setUsrCreated("MCP");

        Answer answer = appServiceService.create(appService);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to create service " + service + ": " + answer.getMessageDescription());
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "created");
        response.put("service", mapper.toDTO(appService));

        if (!curl.isBlank()) {
            response.put("derivedFromCurl", derived);
            response.put("headers", createHeaders(service, parsedHeaders, response));
        }

        return MCPToolUtils.successJson(response);
    }

    /**
     * Persists the headers parsed from a curl command, in the order they appeared.
     *
     * <p>The service row already exists by the time this runs, and Cerberus exposes no transaction
     * spanning both. A header that fails is therefore reported by name instead of being swallowed
     * or rolled back : leaving the caller with a service it believes has headers it does not have
     * would surface much later as a confusing execution failure.</p>
     *
     * @param service  the service the headers belong to.
     * @param headers  the parsed headers.
     * @param response the response being built, to which a warning is added when a header fails.
     * @return the names of the headers that were created, in order.
     */
    private List<String> createHeaders(String service,
                                       List<CurlCommandParser.CurlHeader> headers,
                                       Map<String, Object> response) {
        List<String> created = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        int sort = 10;

        for (CurlCommandParser.CurlHeader header : headers) {
            AppServiceHeader entity = new AppServiceHeader();
            entity.setService(service);
            entity.setKey(header.key());
            entity.setValue(header.value());
            entity.setSort(sort);
            // The setter generated for the isActive field is setActive(boolean).
            entity.setActive(true);
            entity.setDescription("");
            entity.setUsrCreated("MCP");

            Answer headerAnswer = appServiceHeaderService.create(entity);
            if (headerAnswer.isCodeStringEquals("OK")) {
                created.add(header.key());
                sort += 10;
            } else {
                failed.add(header.key() + " (" + headerAnswer.getMessageDescription() + ")");
            }
        }

        if (!failed.isEmpty()) {
            response.put("headersFailed", failed);
            response.put("message", "The service was created but " + failed.size()
                    + " header(s) could not be added. Add them with cerberus_appservice_header_create.");
        }

        return created;
    }

    /**
     * Splits the credentials out of a parsed curl command.
     *
     * <p>{@code -u user:password} and an {@code Authorization} header both describe the same thing
     * to Cerberus, and the execution engine regenerates that header from the auth fields at call
     * time (see {@code RestService}), so recognising them here produces an identical request while
     * keeping the secret in a field the GUI masks.</p>
     *
     * <p>Only the two unambiguous schemes are recognised. Anything else — a bespoke
     * {@code Authorization} value, or an API key under a vendor-specific header name — is left as
     * an ordinary header, because guessing which part of it is the key would risk building a
     * request that no longer matches the command the user pasted.</p>
     *
     * @param parsed the parsed command.
     * @return the extracted credentials and the headers that remain.
     */
    private CurlAuthentication extractAuthentication(CurlCommandParser.ParsedCurl parsed) {
        List<CurlCommandParser.CurlHeader> remaining = new ArrayList<>();
        String authType = "";
        String authUser = "";
        String authPassword = "";

        for (CurlCommandParser.CurlHeader header : parsed.headers()) {
            if (!"authorization".equalsIgnoreCase(header.key()) || !authType.isEmpty()) {
                remaining.add(header);
                continue;
            }

            String value = header.value();
            if (value.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
                authType = AppService.AUTHTYPE_BEARERTOKEN;
                authPassword = value.substring(BEARER_PREFIX.length()).trim();
            } else if (value.regionMatches(true, 0, BASIC_PREFIX, 0, BASIC_PREFIX.length())) {
                String[] credentials = decodeBasic(value.substring(BASIC_PREFIX.length()).trim());
                if (credentials == null) {
                    // Not decodable : keep the header verbatim rather than dropping a credential.
                    remaining.add(header);
                    continue;
                }
                authType = AppService.AUTHTYPE_BASICAUTH;
                authUser = credentials[0];
                authPassword = credentials[1];
            } else {
                remaining.add(header);
            }
        }

        // An explicit -u wins over an Authorization header, matching curl's own precedence.
        if (!parsed.user().isEmpty()) {
            int separator = parsed.user().indexOf(':');
            authType = AppService.AUTHTYPE_BASICAUTH;
            authUser = separator >= 0 ? parsed.user().substring(0, separator) : parsed.user();
            authPassword = separator >= 0 ? parsed.user().substring(separator + 1) : "";
        }

        return new CurlAuthentication(authType, authUser, authPassword, remaining);
    }

    /**
     * Decodes the base64 payload of a {@code Basic} Authorization header.
     *
     * @return the {user, password} pair, or {@code null} when the value is not valid base64 or
     *         does not contain the expected colon.
     */
    private String[] decodeBasic(String encoded) {
        try {
            String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
            int separator = decoded.indexOf(':');
            if (separator < 0) {
                return null;
            }
            return new String[]{decoded.substring(0, separator), decoded.substring(separator + 1)};
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Credentials lifted out of a curl command, together with the headers that were not consumed.
     */
    private record CurlAuthentication(String authType,
                                      String authUser,
                                      String authPassword,
                                      List<CurlCommandParser.CurlHeader> remainingHeaders) {
    }
}
