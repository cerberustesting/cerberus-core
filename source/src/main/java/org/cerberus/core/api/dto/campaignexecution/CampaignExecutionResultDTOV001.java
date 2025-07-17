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

package org.cerberus.core.api.dto.campaignexecution;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.views.View;

/**
 * @author lucashimpens
 */
@ToString
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(name = "CampaignExecutionResult")
@JsonPropertyOrder({"ok", "ko", "fa", "na", "ne", "we", "pe", "qu", "qe", "ca", "totalWithRetries", "total"})
public class CampaignExecutionResultDTOV001 {

    @JsonView(View.Public.GET.class)
    @Schema(description = "Number of OK executions", example = "25")
    private int ok;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Number of KO executions", example = "3")
    private int ko;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Number of FA (Failure) executions", example = "2")
    private int fa;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Number of NA (Not Applicable) executions", example = "1")
    private int na;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Number of NE (Not Executed) executions", example = "0")
    private int ne;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Number of WE (Warning) executions", example = "0")
    private int we;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Number of PE (Pending) executions", example = "0")
    private int pe;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Number of QU (Queued) executions", example = "0")
    private int qu;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Number of QE (Queue Error) executions", example = "0")
    private int qe;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Number of CA (Cancelled) executions", example = "0")
    private int ca;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Total executions including retries", example = "32")
    private int totalWithRetries;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Total executions", example = "30")
    private int total;
}