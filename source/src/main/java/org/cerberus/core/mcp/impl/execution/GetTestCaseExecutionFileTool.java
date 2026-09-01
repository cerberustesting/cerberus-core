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
package org.cerberus.core.mcp.impl.execution;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.crud.entity.TestCaseExecutionFile;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestCaseExecutionFileService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that returns the textual content of one artefact recorded during an execution, under
 * the tool name {@code cerberus_testcase_execution_file_get}.
 *
 * <p>This is the deepest level of the execution debug chain. {@code cerberus_testcase_execution_get}
 * at {@code full} detail reports <em>which</em> artefacts a run produced; this tool returns what is
 * actually inside one of them — the page source captured when a control failed, the HTTP response
 * a service call received, the browser console log. Without it an agent can see that a control did
 * not find an element, but not what the page looked like at that moment.</p>
 *
 * <p>Only text artefacts are served. Screenshots and videos are returned as a reference rather than
 * as content: an MCP text response cannot carry them usefully, and base64-encoding a screenshot
 * would cost more context than the whole rest of the execution.</p>
 *
 * <p>Delegation: {@link ITestCaseExecutionFileService#readByKey(long)} resolves the recorded
 * reference, and the bytes are read from the media directory configured by the
 * {@code cerberus_exeautomedia_path} parameter.</p>
 */
@Component
public class GetTestCaseExecutionFileTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_execution_file_get";

    /**
     * Artefact types whose content is text and can therefore be returned inline. Anything else
     * (JPG, PNG, GIF, PDF, BIN) is reported as a reference only.
     */
    private static final List<String> TEXT_FILE_TYPES = List.of(
            TestCaseExecutionFile.FILETYPE_HTML,
            TestCaseExecutionFile.FILETYPE_JSON,
            TestCaseExecutionFile.FILETYPE_XML,
            TestCaseExecutionFile.FILETYPE_TXT
    );

    /**
     * Default and maximum number of characters returned. A captured page source routinely exceeds
     * a megabyte, which is more than an agent's whole context: the caller asks for the slice it
     * needs rather than receiving the whole document by accident.
     */
    private static final int DEFAULT_MAX_CHARS = 20_000;
    private static final int HARD_MAX_CHARS = 200_000;

    /**
     * Artefact types returned as a viewable image, mapped to the MIME type the MCP client needs
     * to render them. A screenshot is very often the only thing that explains why a control failed
     * to find an element, so refusing to serve it leaves an agent guessing at exactly the moment
     * it has the most to gain from looking.
     */
    private static final Map<String, String> IMAGE_MIME_TYPES = Map.of(
            "PNG", "image/png",
            "JPG", "image/jpeg",
            "JPEG", "image/jpeg",
            "GIF", "image/gif");

    /**
     * Largest image returned inline. Base64 inflates the payload by about a third, and a
     * full-page screenshot can already run to several megabytes; past this point the cost to the
     * caller's context outweighs what one more image reveals.
     */
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;

    /** Serialises the metadata block that accompanies an image. */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ITestCaseExecutionFileService testCaseExecutionFileService;
    private final IParameterService parameterService;
    private final MCPLogUtils mcpLogUtils;

    public GetTestCaseExecutionFileTool(ITestCaseExecutionFileService testCaseExecutionFileService,
                                        IParameterService parameterService,
                                        MCPLogUtils mcpLogUtils) {
        this.testCaseExecutionFileService = testCaseExecutionFileService;
        this.parameterService = parameterService;
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
     * Builds the MCP tool descriptor.
     *
     * <p>The artefact is addressed by the {@code fileId} reported by
     * {@code cerberus_testcase_execution_get}, never by a caller-supplied path: the filesystem
     * location is derived server-side from a database row, so the agent has no way to name an
     * arbitrary file.</p>
     *
     * @return the fully-described {@link McpSchema.Tool} for registration with the MCP server.
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("fileId", Map.of(
                "type", "integer",
                "description", "Identifier of the recorded artefact, from the \"files\" list returned by "
                        + "cerberus_testcase_execution_get with detail=full."
        ));
        properties.put("offset", Map.of(
                "type", "integer",
                "description", "Character offset to start reading from. Defaults to 0. Use it to page through a "
                        + "large document after a first truncated read."
        ));
        properties.put("maxChars", Map.of(
                "type", "integer",
                "description", "Maximum number of characters to return. Defaults to " + DEFAULT_MAX_CHARS
                        + ", capped at " + HARD_MAX_CHARS + "."
        ));
        properties.put("search", Map.of(
                "type", "string",
                "description", "Optional text to locate inside the artefact. When provided, the response is "
                        + "centred on the first match instead of starting at offset, which is the cheapest way "
                        + "to inspect a specific element in a large page source."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Returns an artefact recorded during an execution, so you can see what actually happened rather
                than infer it: the captured page source, a service response body, a console or robot log, or a
                screenshot.

                Call this when an execution failed and the step, action and control messages are not enough to
                explain why — to check whether an element was really in the page, what a REST call returned, or
                what the screen looked like at the moment a control failed.

                Text artefacts (HTML, JSON, XML, TXT) come back as text. Screenshots (PNG, JPG, GIF) come back as
                a viewable image, so look at the screenshot before concluding anything about a UI failure. Videos
                and other binaries return a reference only.

                Get the fileId from cerberus_testcase_execution_get with detail=full, where every step, action and
                control lists the artefacts it produced. Pick the one attached to the step that failed rather than
                an execution-level file.

                For text, prefer search over reading from offset 0: page sources are large, and a targeted look-up
                costs a fraction of the whole document.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("fileId"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("Get execution artefact", false),
                null
        );
    }

    /**
     * Validates the arguments, resolves the artefact and returns the requested slice of its content.
     *
     * @param args tool arguments extracted from the MCP request (may be empty but never null).
     * @return a {@link McpSchema.CallToolResult} carrying the content, or an error description.
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        long fileId = MCPToolUtils.getLong(args, "fileId", 0L);
        int offset = Math.max(MCPToolUtils.getInteger(args, "offset", 0), 0);
        int maxChars = Math.min(Math.max(MCPToolUtils.getInteger(args, "maxChars", DEFAULT_MAX_CHARS), 1), HARD_MAX_CHARS);
        String search = MCPToolUtils.getString(args, "search", "");

        mcpLogUtils.call(TOOL_NAME, "testcase_execution_file_get",
                String.format("MCP tool %s called with fileId=%s", TOOL_NAME, fileId));

        if (fileId <= 0) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: fileId");
        }

        AnswerItem<TestCaseExecutionFile> answer = testCaseExecutionFileService.readByKey(fileId);
        if (!answer.isCodeStringEquals("OK") || answer.getItem() == null) {
            return MCPToolUtils.errorText("Execution artefact does not exist: " + fileId);
        }

        TestCaseExecutionFile file = answer.getItem();
        String fileType = MCPToolUtils.nullSafe(file.getFileType()).toUpperCase();

        boolean isImage = IMAGE_MIME_TYPES.containsKey(fileType);

        if (!TEXT_FILE_TYPES.contains(fileType) && !isImage) {
            // Not an error : the caller asked a reasonable question about a real artefact, and the
            // useful answer is "this one cannot be shown, here is what it is".
            Map<String, Object> response = describe(file);
            response.put("content", null);
            response.put("message", "Artefact of type " + fileType + " can be neither read as text nor "
                    + "displayed as an image. Readable text types: " + TEXT_FILE_TYPES
                    + "; viewable image types: " + IMAGE_MIME_TYPES.keySet() + ".");
            return MCPToolUtils.successJson(response);
        }

        Path resolved;
        try {
            resolved = resolveWithinMediaRoot(file.getFileName());
        } catch (IllegalArgumentException e) {
            return MCPToolUtils.errorText("Unable to read artefact " + fileId + ": " + e.getMessage());
        }

        if (isImage) {
            return readImage(file, resolved, fileType);
        }

        if (!Files.isRegularFile(resolved)) {
            return MCPToolUtils.errorText("Artefact " + fileId + " is recorded in the database but its file is "
                    + "missing from the media directory. It may have been purged by the retention job.");
        }

        String content;
        try {
            content = Files.readString(resolved, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return MCPToolUtils.errorText("Unable to read artefact " + fileId + ": " + e.getMessage());
        }

        Map<String, Object> response = describe(file);
        response.put("totalChars", content.length());

        int start = offset;
        if (!search.isBlank()) {
            int matchIndex = content.toLowerCase().indexOf(search.toLowerCase());
            if (matchIndex < 0) {
                response.put("searchFound", false);
                response.put("content", null);
                response.put("message", "'" + search + "' was not found in this artefact.");
                return MCPToolUtils.successJson(response);
            }
            response.put("searchFound", true);
            response.put("searchIndex", matchIndex);
            // Centre the window on the match so the agent sees the surrounding markup, not just
            // the match itself — context is what makes a selector diagnosable.
            start = Math.max(matchIndex - maxChars / 2, 0);
        }

        if (start >= content.length()) {
            response.put("content", "");
            response.put("offset", start);
            response.put("truncated", false);
            response.put("message", "Offset " + start + " is past the end of the artefact (" + content.length() + " characters).");
            return MCPToolUtils.successJson(response);
        }

        int end = Math.min(start + maxChars, content.length());
        response.put("offset", start);
        response.put("content", content.substring(start, end));
        response.put("truncated", end < content.length());
        if (end < content.length()) {
            response.put("nextOffset", end);
        }

        return MCPToolUtils.successJson(response);
    }

    /**
     * Returns a recorded image as a viewable MCP image, alongside a text block describing it.
     *
     * <p>Two content blocks are returned rather than one: the JSON block keeps the artefact's
     * identity and level machine-readable, while the image block is what the client actually
     * renders. An agent diagnosing a failed control therefore sees both which moment of the run
     * the picture belongs to, and the picture.</p>
     *
     * @param file     the artefact record.
     * @param resolved the verified path inside the media directory.
     * @param fileType the artefact type, already known to be an image type.
     * @return the image result, or an error when the file is missing or too large to serve.
     */
    private McpSchema.CallToolResult readImage(TestCaseExecutionFile file, Path resolved, String fileType) {
        if (!Files.isRegularFile(resolved)) {
            return MCPToolUtils.errorText("Artefact " + file.getId() + " is recorded in the database but its file is "
                    + "missing from the media directory. It may have been purged by the retention job.");
        }

        long size;
        byte[] bytes;
        try {
            size = Files.size(resolved);
            if (size > MAX_IMAGE_BYTES) {
                Map<String, Object> tooLarge = describe(file);
                tooLarge.put("sizeBytes", size);
                tooLarge.put("content", null);
                tooLarge.put("message", "This image is " + size + " bytes, above the " + MAX_IMAGE_BYTES
                        + " byte limit for inline display. Open it in the Cerberus execution report instead.");
                return MCPToolUtils.successJson(tooLarge);
            }
            bytes = Files.readAllBytes(resolved);
        } catch (IOException e) {
            return MCPToolUtils.errorText("Unable to read artefact " + file.getId() + ": " + e.getMessage());
        }

        Map<String, Object> described = describe(file);
        described.put("sizeBytes", size);
        described.put("rendered", "image");

        String metadata;
        try {
            metadata = OBJECT_MAPPER.writeValueAsString(described);
        } catch (JsonProcessingException e) {
            return MCPToolUtils.errorText("Unable to describe artefact " + file.getId() + ": " + e.getMessage());
        }

        return new McpSchema.CallToolResult(
                List.of(
                        new McpSchema.TextContent(null, metadata, null),
                        new McpSchema.ImageContent(null,
                                Base64.getEncoder().encodeToString(bytes),
                                IMAGE_MIME_TYPES.get(fileType))),
                false,
                null,
                null);
    }

    /**
     * Builds the descriptive envelope returned with (or instead of) the artefact content.
     */
    private Map<String, Object> describe(TestCaseExecutionFile file) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("fileId", file.getId());
        map.put("executionId", file.getExeId());
        map.put("level", MCPToolUtils.nullSafe(file.getLevel()));
        map.put("fileDesc", MCPToolUtils.nullSafe(file.getFileDesc()));
        map.put("fileType", MCPToolUtils.nullSafe(file.getFileType()));
        map.put("fileName", MCPToolUtils.nullSafe(file.getFileName()));
        return map;
    }

    /**
     * Resolves a recorded relative file name against the configured media directory, and refuses
     * anything that escapes it.
     *
     * <p>The stored name comes from the database rather than from the caller, so this is defence
     * in depth rather than input validation — but a recorded name is written by the execution
     * engine from testcase-controlled values, and this tool is the first thing that turns one into
     * a filesystem read. Normalising and re-checking the parent makes that read safe regardless of
     * what ends up in the column.</p>
     *
     * @param fileName the recorded relative file name.
     * @return the absolute, normalised path, guaranteed to sit under the media root.
     * @throws IllegalArgumentException when the media root is not configured, or the name escapes it.
     */
    private Path resolveWithinMediaRoot(String fileName) {
        String mediaPath = parameterService.getParameterStringByKey(
                Parameter.VALUE_cerberus_exeautomedia_path, "", "");

        if (mediaPath == null || mediaPath.isBlank()) {
            throw new IllegalArgumentException(
                    "the cerberus_exeautomedia_path parameter is not configured on this Cerberus instance.");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("the artefact has no recorded file name.");
        }

        Path root = Paths.get(mediaPath).toAbsolutePath().normalize();
        Path resolved = root.resolve(fileName).normalize();

        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("the recorded file name resolves outside the media directory.");
        }

        return resolved;
    }

}
