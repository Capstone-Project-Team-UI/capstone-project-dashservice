package com.capstone.cloudcontrol.remotepccontrol.usermanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserManagement {

    private final JdbcTemplate jdbcTemplate;
    private final JdbcClient jdbcClient;

    @Autowired
    public UserManagement(JdbcTemplate jdbcTemplate, JdbcClient jdbcClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcClient = jdbcClient;
    }

    // Method to fetch all users from the database
    public List<user> getAllUsers() {
        return jdbcTemplate.query(
            "SELECT * FROM Users",
            (rs, rowNum) -> new user(
                rs.getString("userID"),
                rs.getString("organization"),
                rs.getString("serialNumber"),
                rs.getString("uniqueID"),
                rs.getString("emailAddress")
            )
        );
    }

    // Method to check if the serial number and organization exist in the VerifiedUsers table
    public boolean isSerialNumberAndOrganizationValid(String serialNumber, String organization) {
        String query = "SELECT COUNT(*) FROM VerifiedUsers WHERE registeredDevice = :serialNumber AND organization = :organization";
        List<Map<String, Object>> result = jdbcClient.sql(query)
                                                    .param("serialNumber", serialNumber)
                                                    .param("organization", organization)
                                                    .query()
                                                    .listOfRows();
        return !result.isEmpty() && ((Long) result.get(0).get("COUNT(*)")) > 0;
    }

    // Method to add a new user
    public boolean addUser(user newUser) throws DataIntegrityViolationException {
        String insertQuery = "INSERT INTO Users (userID, organization, serialNumber, uniqueID, emailAddress) VALUES (?, ?, ?, ?, ?)";
        int updated = jdbcClient.sql(insertQuery)
                                .params(List.of(newUser.userID(), newUser.organization(), newUser.serialNumber(), newUser.uniqueID(), newUser.emailAddress()))
                                .update();
        return updated == 1;
    }

    // Method to check if a user exists in the Users table based on multiple fields
    public static boolean userExists(JdbcClient jdbcClient, user existingUser) {
        String query = "SELECT COUNT(*) FROM Users WHERE userID = :userID AND organization = :organization AND serialNumber = :serialNumber AND uniqueID = :uniqueID AND emailAddress = :emailAddress";
        
        // Execute the query with named parameters using the passed jdbcClient
        List<Map<String, Object>> result = jdbcClient.sql(query)
                                                    .param("userID", existingUser.userID())
                                                    .param("organization", existingUser.organization())
                                                    .param("serialNumber", existingUser.serialNumber())
                                                    .param("uniqueID", existingUser.uniqueID())
                                                    .param("emailAddress", existingUser.emailAddress())
                                                    .query()
                                                    .listOfRows();
        
        // Check if the result is not empty and the count is greater than 0
        return !result.isEmpty() && ((Long) result.get(0).get("COUNT(*)")) > 0;
    }
    
    
    

}
