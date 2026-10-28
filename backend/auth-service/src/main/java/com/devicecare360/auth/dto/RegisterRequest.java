package com.devicecare360.auth.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    @NotBlank
    private String fullName;

    private String role; // USER, TECHNICIAN, ADMIN (defaults to USER)

    private String phone;

    // Optional technician-specific professional information
    @Min(value = 0, message = "Experience years must be at least 0")
    @Max(value = 50, message = "Experience years cannot exceed 50")
    @JsonAlias({"yearsOfExperience"})
    private Integer experienceYears;

    @JsonAlias({"categories"})
    private List<String> specializations;

    @PositiveOrZero(message = "Hourly rate must be non-negative")
    private Double hourlyRate;

    private String city;
    private String serviceArea;
    private String professionalBio;
    private String certificateUrl;
}
