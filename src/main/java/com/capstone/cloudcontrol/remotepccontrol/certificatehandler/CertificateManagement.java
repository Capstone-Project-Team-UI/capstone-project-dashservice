package com.capstone.cloudcontrol.remotepccontrol.certificatehandler;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.*;
import com.capstone.cloudcontrol.remotepccontrol.usermanagement.UserManagement;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonObjectSerializer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.capstone.cloudcontrol.remotepccontrol.usermanagement.user;
import com.capstone.cloudcontrol.remotepccontrol.usermanagement.UserManagement;

@Service
public class CertificateManagement {
    private static final Logger logger = LoggerFactory.getLogger(CertificateManagement.class);
    private final AWSSecretsManager secretsManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final JdbcTemplate jdbcTemplate;
    private final JdbcClient jdbcClient;

    @Autowired
    public CertificateManagement(JdbcClient jdbcClient, JdbcTemplate jdbcTemplate) {
        Dotenv dotenv = Dotenv.load();
        String accessKeyId = dotenv.get("AWS_ACCESS_KEY_ID");
        String secretAccessKey = dotenv.get("AWS_SECRET_ACCESS_KEY");
        String region = dotenv.get("AWS_REGION");

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKeyId, secretAccessKey);
        this.secretsManager = AWSSecretsManagerClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(region)
                .build();
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcClient = jdbcClient;
    }
    
    public void downloadCertificateFiles(Map<String, String> certificateData, String targetDirectory) {
            try {
                // Ensure the target directory exists
                File directory = new File(targetDirectory);
                if (!directory.exists()) {
                    if (!directory.mkdirs()) {
                        logger.error("Failed to create the directory: " + targetDirectory);
                        return;
                    }
                }
    
                // Iterate over the key-value pairs and create a file for each
                for (Map.Entry<String, String> entry : certificateData.entrySet()) {
                    String fileName = entry.getKey(); // Use the key as the file name
                    String fileContent = entry.getValue(); // The value is the content of the file
                    
                    // Create the file in the target directory
                    File file = new File(directory, fileName);
                    
                    // Write the content to the file
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(fileContent.getBytes());
                        logger.info("File created: " + file.getAbsolutePath());
                    } catch (IOException e) {
                        logger.error("Error writing to file: " + file.getAbsolutePath(), e);
                    }
                }
            } catch (Exception e) {
                logger.error("Error in downloading certificate files: " + e.getMessage(), e);
            }
        }
    public void pushCertificate(Certificate certificate) {
        try {
            user user = new user(
                certificate.userID(), 
                certificate.organization(), 
                certificate.serialNumber(), 
                certificate.uniqueID(), 
                certificate.emailAddress()
            );
            
            
            if (!UserManagement.userExists(jdbcClient, user)) {
                logger.warn("User does not exist. Cannot push certificate for: " + certificate.uniqueID());
                throw new IllegalArgumentException("User does not exist in the database.");
            }
            

            String secretName = getSecretName(certificate);
            String secretValue = objectMapper.writeValueAsString(certificate.certificates());

            // Check if the secret exists
            GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);
            secretsManager.getSecretValue(getSecretValueRequest);

            // If secret exists, update it
            logger.info("Secret exists. Updating: " + secretName);
            UpdateSecretRequest updateSecretRequest = new UpdateSecretRequest()
                    .withSecretId(secretName)
                    .withSecretString(secretValue)
                    .withDescription("Updated certificates for organization: " + certificate.organization());
            secretsManager.updateSecret(updateSecretRequest);
        } catch (ResourceNotFoundException e) {
            // Secret does not exist, create a new one
            logger.info("Secret does not exist. Creating new secret for: " + certificate.uniqueID());
            createSecret(certificate);
        } catch (Exception e) {
            logger.error("Error pushing certificate: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> getCertificate(String organization, String serialNumber, String uniqueID) {
        try {
            String secretName = String.format("%s/%s/%s", organization, serialNumber, uniqueID);
            GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);
            GetSecretValueResult secretValueResult = secretsManager.getSecretValue(getSecretValueRequest);
            return objectMapper.readValue(secretValueResult.getSecretString(), HashMap.class);
        } catch (ResourceNotFoundException e) {
            logger.warn("Certificate not found for organization: " + organization);
            return null;
        } catch (Exception e) {
            logger.error("Error retrieving certificate: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void createSecret(Certificate certificate) {
        try {
            String secretName = getSecretName(certificate);
            String secretValue = objectMapper.writeValueAsString(certificate.certificates());
            CreateSecretRequest createSecretRequest = new CreateSecretRequest()
                    .withName(secretName)
                    .withSecretString(secretValue)
                    .withDescription("Certificates for organization: " + certificate.organization());
            secretsManager.createSecret(createSecretRequest);
            logger.info("Secret created for: " + certificate.uniqueID());
        } catch (Exception e) {
            logger.error("Error creating secret: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String getSecretName(Certificate certificate) {
        return String.format("%s/%s/%s", 
                             certificate.organization().replace(" ", "_"),
                             certificate.serialNumber().replace(" ", "_"),
                             certificate.uniqueID().replace(" ", "_"));
    }
    
}
