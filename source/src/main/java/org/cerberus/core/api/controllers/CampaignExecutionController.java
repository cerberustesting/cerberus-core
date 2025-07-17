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
package org.cerberus.core.api.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;
import org.cerberus.core.api.controllers.wrappers.ResponseWrapper;
import org.cerberus.core.api.dto.appservice.AppServiceDTOV001;
import org.cerberus.core.api.dto.campaignexecution.CICampaignResultDTOV001;
import org.cerberus.core.api.dto.campaignexecution.CICampaignResultMapperV001;
import org.cerberus.core.api.dto.campaignexecution.CampaignExecutionDTOV001;
import org.cerberus.core.api.dto.campaignexecution.CampaignExecutionMapperV001;
import org.cerberus.core.api.dto.views.View;
import org.cerberus.core.api.entity.CICampaignResult;
import org.cerberus.core.api.exceptions.EntityNotFoundException;
import org.cerberus.core.api.exceptions.FailedReadOperationException;
import org.cerberus.core.api.services.CampaignExecutionService;
import org.cerberus.core.api.services.PublicApiAuthenticationService;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.service.ciresult.ICIService;
import org.cerberus.core.util.DateUtil;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.cerberus.core.service.pdf.IPDFCampaignReportService;

/**
 * @author lucashimpens
 */
@AllArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Campaign Execution", description = "Endpoints related to application Campaign Execution")
@Validated
@RestController
@RequestMapping(path = "/public/campaignexecutions/")
public class CampaignExecutionController {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(CampaignExecutionController.class);

    private static final String API_VERSION_1 = "X-API-VERSION=1";
    private static final String API_KEY = "X-API-KEY";

    private final CampaignExecutionMapperV001 campaignExecutionMapper;
    private final CICampaignResultMapperV001 ciCampaignResultMapper;
    private final ILogEventService logEventService;
    private final CampaignExecutionService campaignExecutionService;
    private final PublicApiAuthenticationService apiAuthenticationService;
    private final ICIService ciService;
    private final IPDFCampaignReportService pdfService;

    private static final String EXECUTIONS_CAMPAIGN_CI_PATH = "/campaignexecutions/ci";
    private static final String EXECUTIONS_CAMPAIGN_CI_SVG_PATH = "/campaignexecutions/ci/svg";
    private static final String EXECUTIONS_CAMPAIGN_PDF_PATH = "/campaignexecutions/pdf";

    @GetMapping(path = "/{campaignExecutionId}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get a campaign execution by campaign execution id (tag)",
            description = "Get a campaign execution by campaign execution id (tag)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Campaign execution successfully returned.", content = { @Content(mediaType = "application/json",schema = @Schema(implementation = CampaignExecutionDTOV001.class))})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<CampaignExecutionDTOV001> findCampaignExecutionById(
            @Parameter(description = "Campaign Execution ID") @PathVariable("campaignExecutionId") String campaignExecutionId,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/campaignexecutions", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /campaignexecutions called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.campaignExecutionMapper
                        .toDto(
                                this.campaignExecutionService.findByExecutionIdWithExecutions(campaignExecutionId, "")
                        )
        );
    }

    @GetMapping(path = "/pdf/{campaignExecutionId}", produces = "application/zip")
    @Operation(
            summary = "Get Campaign execution reports in pdf format inside a zip file by campaign execution id (tag)",
            description = "Get Campaign execution reports in pdf format inside a zip file by campaign execution id (tag)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Campaign execution pdfs report successfully returned.", content = { @Content(mediaType = "application/json")})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<InputStreamResource> findCampaignExecutionPdfById(
            @Parameter(description = "Campaign Execution ID") @PathVariable("campaignExecutionId") String campaignExecutionId,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        LOG.debug("pdf Called.");
        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/campaignexecutions", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /campaignexecutions/pdf called with URL: %s", request.getRequestURL()), request, login);

        try {
            Tag campaignExeIdTag = this.campaignExecutionService.findByExecutionIdWithExecutions(campaignExecutionId, null);

            Date today = Calendar.getInstance().getTime();
            DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_REPORT_FILE);

            String filePostName = campaignExecutionId + "_" + String.valueOf(df.format(today));

            String rootPath = "";
            if (System.getProperty("java.io.tmpdir") != null) {
                rootPath = System.getProperty("java.io.tmpdir");
            } else {
                String sep = "" + File.separatorChar;
                if (sep.equalsIgnoreCase("/")) {
                    rootPath = "/tmp";
                } else {
                    rootPath = "C:";
                }
                LOG.warn("Java Property for temporary folder not defined. Default to :" + rootPath);
            }
            UUID fileUUID = UUID.randomUUID();
            String tmpFolderPath = rootPath + File.separatorChar + fileUUID.toString().substring(0, 17) + File.separatorChar;
            File folderPath = new File(tmpFolderPath);
            folderPath.mkdirs();

            // Summary PDF
            String pdfFilenameOri = this.pdfService.generatePdf(campaignExeIdTag, today, tmpFolderPath);
            String pdfFilename = this.pdfService.addHeaderAndFooter(pdfFilenameOri, tmpFolderPath + "Campaign Execution-" + filePostName + ".pdf", campaignExeIdTag, today, true);

            // Appendix PDFs
            List<String> pdfFilenameOriAppendix = this.pdfService.generatePdfAppendix(campaignExeIdTag, today, tmpFolderPath);
            int i = 0;
            List<String> pdfFilenameAppendixList = new ArrayList<>();
            for (String filenameAppendix : pdfFilenameOriAppendix) {
                i++;
                pdfFilenameAppendixList.add(this.pdfService.addHeaderAndFooter(filenameAppendix, tmpFolderPath + "Campaign Execution-" + filePostName + " - Appendix " + i + ".pdf", campaignExeIdTag, today, false));

            }

            // Creating a PdfWriter
            String zipPath = rootPath + File.separatorChar + "campaignExecutionReport-" + fileUUID.toString().substring(0, 17) + ".zip";
            Path zipFilePath = Paths.get(zipPath);

            List<String> filePaths = new ArrayList<>();
            filePaths.add(pdfFilename);
            for (String filenam : pdfFilenameAppendixList) {
                filePaths.add(filenam);
            }

            try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipPath))) {
                for (String filePath1 : filePaths) {
                    File fileToZip = new File(filePath1);
                    zipOut.putNextEntry(new ZipEntry(fileToZip.getName()));
                    Files.copy(fileToZip.toPath(), zipOut);
                }
            }

            logEventService.createForPublicCalls("/public/campaignexecutions", "CALLRESULT-GET", LogEvent.STATUS_INFO, String.format("PDFs calculated for campaign '%s'", campaignExecutionId), request, login);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentLength(zipFilePath.toFile().length())
                    .header("Content-Disposition", "attachment; filename=CampaignReport-" + filePostName + ".zip")
                    .body(new InputStreamResource(Files.newInputStream(zipFilePath)));
        } catch (EntityNotFoundException ex) {
            LOG.error(ex, ex);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(null);
        } catch (FailedReadOperationException ex) {
            LOG.error(ex, ex);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        } catch (FileNotFoundException ex) {
            LOG.error(ex, ex);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        } catch (IOException ex) {
            LOG.error(ex, ex);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        } catch (Exception ex) {
            LOG.error(ex, ex);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping(path = "/{campaignId}/last", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get the last execution of a campaign with its name",
            description = "Get the last execution of a campaign with its name",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Campaign execution successfully returned.", content = { @Content(mediaType = "application/json",schema = @Schema(implementation = CampaignExecutionDTOV001.class))})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<CampaignExecutionDTOV001> findLastCampaignExecutionByCampaignId(
            @Parameter(description = "Campaign Name") @PathVariable("campaignId") String campaignId,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/campaignexecutions", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /campaignexecutions called with URL: %s", request.getRequestURL()), request, login);

        return ResponseWrapper.wrap(
                this.campaignExecutionMapper
                        .toDto(
                                this.campaignExecutionService.findByExecutionIdWithExecutions("", campaignId)
                        )
        );
    }

    @GetMapping(path = "/ci/{campaignExecutionId}", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get a campaign execution (CI Results) by campaign execution id (tag)",
            description = "Get a campaign execution (CI Results) by campaign execution id (tag)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Campaign execution successfully returned.", content = { @Content(mediaType = "application/json",schema = @Schema(implementation = CICampaignResultDTOV001.class))})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<CICampaignResultDTOV001> findCiCampaignExecutionById(
            @Parameter(description = "Campaign Execution ID") @PathVariable("campaignExecutionId") String campaignExecutionId,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/campaignexecutions", LogEvent.STATUS_INFO, "CALL-GET", String.format("API /campaignexecutions/ci called with URL: %s", request.getRequestURL()), request, login);

        CICampaignResult ciCampaignResult = this.ciService.getCIResultApi(campaignExecutionId, "");
        logEventService.createForPublicCalls("/public/campaignexecutions", LogEvent.STATUS_INFO, "CALLRESULT-GET", String.format("CI Results calculated for tag '%s' result [%s]", ciCampaignResult.getCampaignExecutionId(), ciCampaignResult.getGlobalResult()), request, login);
        return ResponseWrapper.wrap(
                this.ciCampaignResultMapper.toDTO(
                        ciCampaignResult
                )
        );
    }

    @GetMapping(path = "/ci/{campaignId}/last", headers = {API_VERSION_1}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get the last execution (CI Results) of a campaign with its name",
            description = "Get the last execution (CI Results) of a campaign with its name",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Campaign execution successfully returned.", content = { @Content(mediaType = "application/json",schema = @Schema(implementation = CICampaignResultDTOV001.class))})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseWrapper<CICampaignResultDTOV001> findCiCampaignExecutionByCampaignId(
            @Parameter(description = "Campaign Name") @PathVariable("campaignId") String campaignId,
            @Parameter(description = "X-API-KEY for authentication") @RequestHeader(name = API_KEY, required = false) String apiKey,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) Principal principal) {

        String login = this.apiAuthenticationService.authenticateLogin(principal, apiKey);
        logEventService.createForPublicCalls("/public/campaignexecutions", LogEvent.STATUS_INFO, "CALL-GET", String.format("API /campaignexecutions/ci called with URL: %s", request.getRequestURL()), request, login);

        CICampaignResult ciCampaignResult = this.ciService.getCIResultApi("", campaignId);
        logEventService.createForPublicCalls("/public/campaignexecutions", LogEvent.STATUS_INFO, "CALLRESULT-GET", String.format("CI Results calculated for campaign '%s' result [%s]", campaignId, ciCampaignResult.getGlobalResult()), request, login);
        return ResponseWrapper.wrap(
                this.ciCampaignResultMapper.toDTO(
                        ciCampaignResult
                )
        );
    }

    @GetMapping(path = "/ci/svg/{campaignExecutionId}", produces = "image/svg+xml")
    @Operation(
            summary = "Get execution (CI Results) of a campaign in SVG output with the campaign execution id (tag)",
            description = "Get execution (CI Results) of a campaign in SVG output with the campaign execution id (tag)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Campaign execution successfully returned.", content = { @Content(mediaType = "application/json")})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> findCiSvgCampaignExecutionById(
            @Parameter(description = "Campaign Execution ID") @PathVariable("campaignExecutionId") String campaignExecutionId,
            @Parameter(hidden = true) HttpServletRequest request) {

        logEventService.createForPublicCalls("/public/campaignexecutions", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /campaignexecutions/ci/svg called with URL: %s", request.getRequestURL()), request);
        try {
            CICampaignResult ciCampaignResult = this.ciService.getCIResultApi(campaignExecutionId, "");
            logEventService.createForPublicCalls("/public/campaignexecutions", "CALLRESULT-GET", LogEvent.STATUS_INFO, String.format("CI Results calculated for campaign '%s' result [%s]", campaignExecutionId, ciCampaignResult.getGlobalResult()), request);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(this.ciService.generateSvg(ciCampaignResult.getCampaignExecutionId(), ciCampaignResult.getGlobalResult()));
        } catch (EntityNotFoundException exception) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(this.ciService.generateSvg("NOT FOUND", "ERR"));

        } catch (FailedReadOperationException exception) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(this.ciService.generateSvg("Error when retrieving the campaign execution", "ERR"));
        }
    }

    @GetMapping(path = "/ci/svg/{campaignId}/last", produces = "image/svg+xml")
    @Operation(
            summary = "Get the last execution (CI Results) of a campaign in SVG output with its name",
            description = "Get the last execution (CI Results) of a campaign in SVG output with its name",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Campaign execution successfully returned.", content = { @Content(mediaType = "application/json")})
            }
    )
    @JsonView(View.Public.GET.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> findLastCiSvgCampaignExecutionByCampaignId(
            @Parameter(description = "Campaign Name") @PathVariable("campaignId") String campaignId,
            @Parameter(hidden = true) HttpServletRequest request) {

        logEventService.createForPublicCalls("/public/campaignexecutions", "CALL-GET", LogEvent.STATUS_INFO, String.format("API /campaignexecutions/ci/svg called with URL: %s", request.getRequestURL()), request);
        try {
            CICampaignResult ciCampaignResult = this.ciService.getCIResultApi("", campaignId);
            logEventService.createForPublicCalls("/public/campaignexecutions", "CALLRESULT-GET", LogEvent.STATUS_INFO, String.format("CI Results calculated for campaign '%s' result [%s]", campaignId, ciCampaignResult.getGlobalResult()), request);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(this.ciService.generateSvg(ciCampaignResult.getCampaignExecutionId(), ciCampaignResult.getGlobalResult()));
        } catch (EntityNotFoundException exception) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(this.ciService.generateSvg("NOT FOUND", "ERR"));
        } catch (FailedReadOperationException exception) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(this.ciService.generateSvg("Error when retrieving the campaign execution", "ERR"));
        }
    }
}
