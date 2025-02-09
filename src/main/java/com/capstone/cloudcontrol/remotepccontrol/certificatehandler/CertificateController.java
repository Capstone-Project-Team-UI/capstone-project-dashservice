package com.capstone.cloudcontrol.remotepccontrol.certificatehandler;

import com.capstone.cloudcontrol.remotepccontrol.certificatehandler.Certificate;
import com.capstone.cloudcontrol.remotepccontrol.certificatehandler.CertificateManagement;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;

import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/certificates")
public class CertificateController {

    private final CertificateManagement certificateManagement;

    // Constructor injection to initialize CertificateManagement instance
    public CertificateController(CertificateManagement certificateManagement) {
        this.certificateManagement = certificateManagement;
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> addCertificate(@Valid @RequestBody Certificate certificate, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            
            // Loop through each error and add it to the errors map
            bindingResult.getAllErrors().forEach(error -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);  // Add the field and its corresponding error message
            });
    
            // Return the errors map with the BAD_REQUEST status
            return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body(errors); // return as Map
        }
        certificateManagement.pushCertificate(certificate);

        // Create a response map for the message
        Map<String, String> responseMessage = Map.of("message", "Certificate added successfully");

        // Return the response with status 201 (Created)
        return ResponseEntity.status(HttpStatus.SC_CREATED).body(responseMessage);
    }

    @GetMapping("/get/{organization}/{serialNumber}/{uniqueID}")
    public ResponseEntity<Map<String, String>> getCertificate(
            @PathVariable String organization,
            @PathVariable String serialNumber,
            @PathVariable String uniqueID) {
        try {
            Map<String, String> certificate = certificateManagement.getCertificate(organization, serialNumber, uniqueID);
            if (certificate != null) {
                String targetDirectory = "C:\\Program Files\\DASH CLI 7.0\\certs";
                // deepcode ignore PT: <please specify a reason of ignoring this>
                certificateManagement.downloadCertificateFiles(certificate, targetDirectory);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}
