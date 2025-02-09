package com.capstone.cloudcontrol.remotepccontrol.usermanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserManagement userManagementService;

    @Autowired
    public UserController(UserManagement userManagementService) {
        this.userManagementService = userManagementService;
    }

    // GET endpoint: Return all users from the database
    @GetMapping
    public ResponseEntity<List<user>> getAllUsers() {
        logger.info("Received request to fetch all users from the database");

        List<user> users = userManagementService.getAllUsers();
        
        if (users.isEmpty()) {
            logger.info("No users found in the database");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); 
        } else {
            logger.info("Fetched {} users from the database", users.size());
            return ResponseEntity.status(HttpStatus.OK).body(users);
        }
    }

    // POST endpoint: Add a new user after verifying serial number and organization exist
    @PostMapping
    public ResponseEntity<String> addUser(@Valid @RequestBody user newUser, BindingResult bindingResult) throws JsonProcessingException {
        if (bindingResult.hasErrors()) {
            logger.info("Problem with validation");
            Map<String, String> errors = new HashMap<>();
            
            // Loop through each error and add it to the errors map
            bindingResult.getAllErrors().forEach(error -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);  // Add the field and its corresponding error message
            });
    
            // Convert the errors map to a JSON string and return it
            String errorJson = new ObjectMapper().writeValueAsString(errors);
    
            // Return the JSON string with the BAD_REQUEST status
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorJson);
        }

        logger.info("Received request to add a new user with ID: {}", newUser.userID());

        if (!userManagementService.isSerialNumberAndOrganizationValid(newUser.serialNumber(), newUser.organization())) {
            logger.error("Error: Serial Number and Organization must exist in the VerifiedUsers table for user with ID: {}", newUser.userID());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\": \"Serial Number and Organization must exist in the VerifiedUsers table\"}");
        }

        try {
            if (userManagementService.addUser(newUser)) {
                logger.info("User with ID: {} added successfully.", newUser.userID());
                return ResponseEntity.ok("{\"message\": \"User added successfully!\"}");
            } else {
                logger.error("Error: Failed to insert user with ID: {}", newUser.userID());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("{\"error\": \"Failed to add user\"}");
            }
        } catch (DataIntegrityViolationException ex) {
            logger.error("Unique constraint violation for userID: {}", newUser.userID());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"User ID '" + newUser.userID() + "' already exists.\"}");
        }
    }
}
