package com.deep3.medusLabs.aws.components.global;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.deep3.medusLabs.aws.exceptions.InvalidAWSCredentials;
import com.deep3.medusLabs.model.enums.LogLevel;
import com.deep3.medusLabs.aws.components.AWSGlobalServiceComponentInterface;
import com.deep3.medusLabs.service.DeployedLabLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

/**
 * Manages interaction with S3 service.
 */
public class S3Component implements AWSGlobalServiceComponentInterface {

    private static final Logger LOG = LoggerFactory.getLogger(S3Component.class);

    private AmazonS3 s3Client;

    @Autowired
    DeployedLabLogService deployedLabLogService;

    public S3Component(STSAssumeRoleSessionCredentialsProvider credentials) throws InvalidAWSCredentials {
        if(credentials == null){
            throw new InvalidAWSCredentials();
        }
        this.s3Client = AmazonS3ClientBuilder.standard().withCredentials(credentials).withForceGlobalBucketAccessEnabled(true).build();

    }

    /**
     * Create an S3 Bucket through the s3 client
     * @param bucketName - the name of the new bucket
     */
    public void createBucket(String bucketName){
        if (!s3Client.doesBucketExistV2(bucketName.toLowerCase())) {
            s3Client.createBucket(new CreateBucketRequest(bucketName.toLowerCase()));
        }
    }

    /**
     * Convert a given string to a byte array and then place that byte array in an s3 bucket using java's
     * streams API
     * @param fileBody - The body of the file in string format
     * @param fileName - name of the file
     * @param bucketName - name of the bucket in which to place it
     */
    public void putStringIntoS3File(String fileBody, String fileName, String bucketName){
        byte[] bytes = Charset.forName("UTF-8").encode(fileBody).array();
        InputStream is = new ByteArrayInputStream(bytes);
        ObjectMetadata metaData = new ObjectMetadata();
        metaData.setContentLength(bytes.length);
        PutObjectRequest putObject = new PutObjectRequest(bucketName.toLowerCase(),fileName, is, metaData);
        s3Client.putObject(putObject);
    }

    /**
     * Delete all s3 buckets for a given lab
     * @param deployedLabId - ID of the lab associated with the bucket
     */
    @Override
    public void deleteAll(long deployedLabId) {
        List<Bucket> buckets = s3Client.listBuckets();
        buckets.stream().forEach(bucket -> deleteBucket(bucket.getName(), deployedLabId));

    }

    /**
     * Delete a single s3 bucket
     * @param bucketName name of the bucket
     * @param deployedLabId ID of the associted lab
     */
    private void deleteBucket(String bucketName, Long deployedLabId){
        // Delete all objects from the bucket. This is sufficient
        // for unversioned buckets. For versioned buckets, when you attempt to delete objects, Amazon S3 inserts
        // delete markers for all objects, but doesn't delete the object versions.
        // To delete objects from versioned buckets, delete all of the object versions before deleting
        // the bucket (see below for an example).
        if (s3Client.doesBucketExistV2(bucketName)) {
            ObjectListing objectListing = s3Client.listObjects(bucketName);

            while (true) {
                Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();
                while (objIter.hasNext()) {
                    String objectKey = objIter.next().getKey();
                    try {
                        s3Client.deleteObject(bucketName, objectKey);
                    } catch (SdkClientException e) {
                        deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                                "Failed to delete object [%s] from bucket [%s]: %s", objectKey, bucketName, e.getMessage());
                        LOG.error("Failed to delete object [{}] from bucket [{}]: {}", objectKey, bucketName, e.getMessage());
                    }
                }

                // If the bucket contains many objects, the listObjects() call
                // might not return all of the objects in the first listing. Check to
                // see whether the listing was truncated. If so, retrieve the next page of objects
                // and delete them.
                if (objectListing.isTruncated()) {
                    objectListing = s3Client.listNextBatchOfObjects(objectListing);
                } else {
                    break;
                }
            }

            // Delete all object versions (required for versioned buckets).
            VersionListing versionList = s3Client.listVersions(new ListVersionsRequest().withBucketName(bucketName));
            while (true) {
                Iterator<S3VersionSummary> versionIter = versionList.getVersionSummaries().iterator();
                while (versionIter.hasNext()) {
                    S3VersionSummary vs = versionIter.next();

                    try {
                        s3Client.deleteVersion(bucketName, vs.getKey(), vs.getVersionId());
                    } catch (SdkClientException e) {
                        deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                                "Failed to delete version object [%s] with version ID [%s] from bucket [%s]: %s", vs.getKey(), vs.getVersionId(), bucketName, e.getMessage());
                        LOG.error("Failed to delete version object [{}] with version ID [{}] from bucket [{}]: {}", vs.getKey(), vs.getVersionId(), bucketName, e.getMessage());
                    }
                }

                if (versionList.isTruncated()) {
                    versionList = s3Client.listNextBatchOfVersions(versionList);
                } else {
                    break;
                }
            }

            // After all objects and object versions are deleted, delete the bucket.

            try {
                s3Client.deleteBucket(bucketName);
            } catch (SdkClientException e) {
                deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                        "Failed to delete bucket [%s]: %s", bucketName, e.getMessage());
                LOG.error("Failed to delete bucket [{}]: {}", bucketName, e.getMessage());
            }
        } else {
            deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                    "Bucket {} did not exist", bucketName);
            LOG.error("Bucket {} did not exist", bucketName);
        }
    }
}
