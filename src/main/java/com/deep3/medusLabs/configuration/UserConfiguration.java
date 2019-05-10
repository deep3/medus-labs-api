package com.deep3.medusLabs.configuration;

import com.amazonaws.services.s3.model.PutObjectRequest;
import com.deep3.medusLabs.aws.exceptions.InvalidPasswordComplexity;
import com.deep3.medusLabs.aws.service.S3Service;
import com.deep3.medusLabs.model.User;
import com.deep3.medusLabs.service.UserService;
import com.deep3.medusLabs.utilities.PasswordUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class UserConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(UserConfiguration.class);

    private CloudFormationScriptManager cloudFormationScriptManager;
    private UserService userService;
    private S3Service s3Service;


    @Autowired
    public UserConfiguration(CloudFormationScriptManager cloudFormationScriptManager,
                             UserService userService,
                             S3Service s3Service
                             ) {
        this.cloudFormationScriptManager = cloudFormationScriptManager;
        this.userService = userService;
        this.s3Service = s3Service;
        populateDefaultUser();
    }

    /**
     * Create a default user for the application
     */
    private void populateDefaultUser() {
        String password = "";

        while(userService.findAll().size() < 1){

            try {
                password = PasswordUtils.generatePassword(12);
                User user = new User();
                user.setUsername("admin");
                user.setPassword(password);
                userService.save(user);
                createCredentialsInBucket(password);
            }
            catch (InvalidPasswordComplexity e){
                LOG.error("Could not store user: " + e.getMessage());
            }
        }
    }

    /**
     * Store the supplied password in a new s3 bucket
     * @param password - The password used
     */
    private void createCredentialsInBucket(String password) {
        String bucketName = "aws-lab-deployment-tool-user-" + UUID.randomUUID().toString().substring(0,10);
        String credentialsFileName = "default-credentials.txt";

        try {
            s3Service.createInstance(bucketName);
            Files.write(Paths.get(credentialsFileName), password.getBytes());
            s3Service.uploadObject(new PutObjectRequest(bucketName, credentialsFileName, new File(credentialsFileName)));
            Files.delete(Paths.get(credentialsFileName));
        } catch (Exception e ){
            LOG.error("AWS event error : " + e.getMessage());
        }
    }
}
