package com.capstone.cloudcontrol.remotepccontrol.certificatehandler;


import java.util.Map;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public record Certificate(
    @NotBlank(message = "UserID Can't be blank")
    String userID,

    @NotBlank(message= "Organization Can't be blank")
    String organization,

    @NotBlank(message = "Serial Number can't be blank")
    String serialNumber,

    @NotBlank(message = "Unique ID is required")
    String uniqueID,

    @Email(message = "Email should be Valid")
    @NotBlank(message = "Email can't be Blank")
    String emailAddress,
    
    Map<String, String> certificates

) {

    @AssertTrue(message = "Invalid unique ID format. Must be a valid hash.")
    private boolean isValidHash() {
            return uniqueID.matches("^[a-fA-F0-9]{64}$");
    }

    @AssertTrue(message = "Certificates must have non-blank keys and values")
    private boolean isCertificatesValid() {
        if (certificates == null || certificates.isEmpty()) {
            return false;
        }
        return certificates.entrySet().stream()
            .allMatch(entry -> entry.getKey() != null && !entry.getKey().isBlank() &&
                                entry.getValue() != null && !entry.getValue().isBlank());
    }
}
