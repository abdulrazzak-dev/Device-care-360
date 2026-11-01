package com.devicecare360.technician.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityUpdateRequest {

    @NotNull(message = "isAvailable must be provided")
    @JsonProperty("isAvailable")
    @JsonAlias({"available", "isAvailable"})
    private Boolean isAvailable;
}
