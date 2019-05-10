package com.deep3.medusLabs.configuration;

import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Principal;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.actions.S3Actions;
import com.amazonaws.auth.policy.resources.S3ObjectResource;
import com.amazonaws.services.organizations.model.Account;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.deep3.medusLabs.aws.service.OrganisationsService;
import com.deep3.medusLabs.aws.service.S3Service;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CloudFormationScriptManager {

    private static final Logger LOG = LoggerFactory.getLogger(CloudFormationScriptManager.class);

    private final String CLOUDFORMATION_RESOURCE_DIRECTORY = "src/main/resources/cloudFormationscripts";
    private final String BUCKET_ROOT = "";

    private S3Service s3Service;
    private OrganisationsService organisationsService;
    private Map<String, String> templateUrls;
    private String bucketName;


    @Autowired
    public CloudFormationScriptManager(S3Service s3Service, OrganisationsService organisationsService) {
        this.organisationsService = organisationsService;
        this.bucketName = "aws-api-cf-scripts-" + organisationsService.getAccountId() + "-" + s3Service.getRegion().toString();
        this.s3Service = s3Service;
        createS3resources(bucketName);

    }

    /**
     * Gets the https URL for the specified CloudFormation template stored in S3.
     * @param templateName template name, excluding extension
     * @return S3 https URL for specified file
     */
    public String getTemplateUrl(String templateName) {
        return this.templateUrls.get(templateName);
    }

    /**
     * Gets the bucket name containing the CloudFormation scripts for the Labs
     * @return name of CloudFormation scripts bucket
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * Creates a bucket and uploads cloudFormation scripts for use by application. If the bucket already exists assumes
     * the scripts are already present.
     * @param scriptsBucket Name of bucket to hold cloudFormation scripts
     */
    private void createS3resources(String scriptsBucket) {
        // upload scripts to s3
        Bucket bucket = s3Service.getBucket(scriptsBucket);
        if (bucket != null) {
            updateBucketPolicy();
            // bucket already exists - assume all correct files are present
        } else {
            // Bucket doesn't exist, create bucket and upload scripts
            s3Service.createInstance(scriptsBucket);
            updateBucketPolicy();
            uploadCloudFormationScripts(CLOUDFORMATION_RESOURCE_DIRECTORY, scriptsBucket, BUCKET_ROOT);
        }
        populateTemplateUrls(bucketName);
    }

    /**
     * Populates the templates URL map with cloudFormation https URLs. Where the key is the name of the script (without
     * the extension) and the value is it's https accessible URL.
     * @param bucketName bucket containing scripts
     */
    private void populateTemplateUrls(String bucketName) {
        this.templateUrls = new HashMap<>();

        List<String> objectKeys = s3Service.listAllObjects(bucketName);
        for (String objectKey : objectKeys) {
            templateUrls.put(FilenameUtils.getBaseName(objectKey), s3Service.getObjectResourceUrl(bucketName, objectKey));
        }
    }

    /**
     * Adds a bucket policy to the specified bucket allowing access from all valid organisation accounts.
     */
    public void updateBucketPolicy() {
        List<Account> accounts = organisationsService.getValidAccounts();
        List<Principal> principals = new ArrayList<>();
        for (Account account : accounts) {
            principals.add(new Principal(account.getId()));
        }

        Statement allowAccessFromMemberAccounts = new Statement(Statement.Effect.Allow)
                .withPrincipals(principals.toArray(new Principal[0]))
                .withActions(S3Actions.GetObject)
                .withResources(new S3ObjectResource(bucketName, "*"));
        Policy policy = new Policy()
                .withStatements(allowAccessFromMemberAccounts);

        s3Service.setBucketPolicy(bucketName, policy.toJson());
    }

    /**
     * Iterates through the cloudFormation resources folder uploading the contents to S3
     * @param directory file path containing files
     * @param bucket S3 bucket to upload to
     */
    private void uploadCloudFormationScripts(String directory, String bucket, String keyValue) {
        File dir = new File(directory);
        File[] dirContents = dir.listFiles();

        if (dirContents != null) {
            for (File file : dirContents) {
                if (file.isDirectory()) {
                    uploadCloudFormationScripts(file.getAbsolutePath(), bucket, keyValue + file.getName() + "/");
                } else {
                    s3Service.uploadObject(new PutObjectRequest(bucket, keyValue + file.getName(), new File(file.getAbsolutePath())));
                }
            }
        }
    }
}
