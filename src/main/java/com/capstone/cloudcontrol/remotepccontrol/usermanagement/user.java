package com.capstone.cloudcontrol.remotepccontrol.usermanagement;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import com.capstone.cloudcontrol.remotepccontrol.usermanagement.user;


import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record user(
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
    String emailAddress

) {

    @AssertTrue(message = "Invalid unique ID format. Must be a valid hash.")
    private boolean isValidHash() {
            return uniqueID.matches("^[a-fA-F0-9]{64}$");
    }
    
    
}
