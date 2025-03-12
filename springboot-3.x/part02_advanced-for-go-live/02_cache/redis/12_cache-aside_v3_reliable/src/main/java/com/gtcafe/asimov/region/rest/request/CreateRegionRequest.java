package com.gtcafe.asimov.region.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateRegionRequest {

	@NotBlank(message = "regionCode cannot empty")
    @Size(max = 50, message = "regionCode max lenght is 50.")
    private String regionCode;

    @NotBlank(message = "description cannot empty")
    @Size(max = 255, message = "description max lenght is 255.")
    private String description;

}
