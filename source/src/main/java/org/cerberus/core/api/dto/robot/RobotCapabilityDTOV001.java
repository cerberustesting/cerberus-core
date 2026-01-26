package org.cerberus.core.api.dto.robot;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.views.View;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
        "id",
        "robot",
        "capability",
        "value"
})
@Schema(name = "RobotCapability")
public class RobotCapabilityDTOV001 {

    @JsonView(View.Public.GET.class)
    @Schema(description = "Technical identifier", example = "42")
    private Integer id;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Robot name", example = "CHROME_LINUX")
    private String robot;

    @NotBlank
    @JsonView({View.Public.GET.class, View.Public.POST.class})
    @Schema(description = "Capability key", example = "browserName", required = true)
    private String capability;

    @NotBlank
    @JsonView({View.Public.GET.class, View.Public.POST.class})
    @Schema(description = "Capability value", example = "chrome", required = true)
    private String value;
}
