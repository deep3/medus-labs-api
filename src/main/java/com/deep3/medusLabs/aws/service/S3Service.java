package com.deep3.medusLabs.aws.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;


@Service
public class S3Service
{
    private AmazonS3 amazons3;

    @Autowired
    public S3Service()
    {
        this.amazons3 = AmazonS3ClientBuilder.standard().build();
    }

    /**
     * Gets the current region of this S3 Service
     * @return the current regions
     */
    public Region getRegion() {
        return amazons3.getRegion();
    }

    /**
     * Create a new S3 Bucket Instance
     * @param bucketName - the name of the S3 Bucket to be created
     * @return - A S3 Bucket
     */
    public Bucket createInstance(String bucketName)
    {
        return amazons3.createBucket(bucketName);
    }

    /**
     * Get all S3 Bucket instances for this AWS Organisation
     * @return - A List of S3 Bucket instances
     */
    public List<Bucket> getBucketInstances()
    {
        return amazons3.listBuckets();
    }

    /**
     * Get a specific S3 Bucket instance
     * @param bucketName - the name of the S3 Bucket instance to search for / return
     * @return - A S3 Bucket
     */
    public Bucket getBucket(String bucketName)
    {
        Optional<Bucket> bucket = getBucketInstances().stream().filter(b -> bucketName.equalsIgnoreCase(b.getName())).findFirst();
        return bucket.orElse(null);
    }

    public void uploadObject(PutObjectRequest object) {
        amazons3.putObject(object);
    }

    /**
     * Gets a https addressable URL for the specified object within the specified bucket
     * @param bucket the bucket containing the object
     * @param key the oject to get the URL from
     * @return https address for the object
     */
    public String getObjectResourceUrl(String bucket, String key) {
        return amazons3.getUrl(bucket, key).toString();
    }

    /**
     * Sets a buckets access policy
     * @param bucketName bucket to set policy on
     * @param policy policy to set
     */
    public void setBucketPolicy(String bucketName, String policy) {
        amazons3.setBucketPolicy(new SetBucketPolicyRequest(bucketName, policy));
    }

    /**
     * Returns a list of all object keys contained within the specified bucket.
     * @param bucketName bucket to search
     * @return list of object keys
     */
    public List<String> listAllObjects(String bucketName) {
        List<String> s3Objects = new ArrayList<>();
        ObjectListing objectListing = amazons3.listObjects(bucketName);
        return collectTruncatedObjects(s3Objects, objectListing);
    }

    /**
     * Gets an object with the specified key from the specified bucket
     * @param bucketName the bucket to retrieve the object from
     * @param objectKey the objects key
     * @return an S3 object
     */
    public S3Object getObject(String bucketName, String objectKey) {
        return amazons3.getObject(bucketName, objectKey);
    }


    /**
     * Return a list of objects in a given bucket
     * @param bucket - The bucket to retrieve the list of objects from
     * @param prefix - an optional prefix - say if you needed only to retrieve objects whose names began with '2019_obj'
     * @return - A list of objects
     */
    public List<String> listObjects(String bucket, String prefix) {
        List<String> s3Objects = new ArrayList<>();
        ObjectListing objectListing = amazons3.listObjects(bucket, prefix);
        return collectTruncatedObjects(s3Objects, objectListing);
    }

    /**
     * Collect truncated objects from a given bucket
     * @param s3Objects - the objects from the s3 bucket
     * @param objectListing - AWS ObjectListing object containing contents of s3 bucket
     * @return list of s3 objects
     */
    private List<String> collectTruncatedObjects(List<String> s3Objects, ObjectListing objectListing) {
        while (true) {
            Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();
            while (objIter.hasNext()) {
                s3Objects.add(objIter.next().getKey());
            }

            if (objectListing.isTruncated()) {
                objectListing = amazons3.listNextBatchOfObjects(objectListing);
            } else {
                break;
            }
        }
        return s3Objects;
    }
}
