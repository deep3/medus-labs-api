package com.deep3.medusLabs.aws.service;


import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AmazonS3ClientBuilder.class})
public class S3ServiceTests {

    @Mock
    AmazonS3ClientBuilder builder;

    @Mock
    AmazonS3 client;

    @Before
    public void initialize() throws Exception {
        MockitoAnnotations.initMocks(this);
        builder = PowerMockito.mock(AmazonS3ClientBuilder.class);
        PowerMockito.mockStatic(AmazonS3ClientBuilder.class);
        PowerMockito.when(AmazonS3ClientBuilder.standard()).thenReturn(builder);
        PowerMockito.when(builder.withRegion(any(Regions.class))).thenReturn(builder);

        PowerMockito.when(builder.build()).thenReturn(client);

    }

    @Test
    public void testConstructor() {

        S3Service test = new S3Service();
        Assert.assertNotNull(test);
    }


    @Test
    public void testCreateInstance(){

        S3Service test = new S3Service();
        Bucket bucket = Mockito.mock(Bucket.class);
        Mockito.when(client.createBucket(matches("myBucket"))).thenReturn(bucket);
        Bucket result = test.createInstance("myBucket");

        Assert.assertEquals("Unexpected bucket returned" ,bucket,result);
    }

    @Test
    public void testGetBucketInstances(){
        S3Service test = new S3Service();
        Bucket bucket = Mockito.mock(Bucket.class);
        List<Bucket> list = Arrays.asList(bucket, bucket, bucket);
        Mockito.when(client.listBuckets()).thenReturn(list);

        List<Bucket> result = test.getBucketInstances();

        Assert.assertEquals("Unexpected bucket list returned", list,result);
    }


    @Test
    public void testGetBucketName(){
        S3Service test = new S3Service();
        Bucket mockBucket = Mockito.mock(Bucket.class);
        Bucket testBucket = new Bucket();
        testBucket.setName("findMe");
        List<Bucket> list = Arrays.asList(mockBucket, mockBucket, testBucket);
        Mockito.when(client.listBuckets()).thenReturn(list);

        Bucket result = test.getBucket("findme");

        Assert.assertEquals("Incorrect bucket found", testBucket,result);
    }


    @Test
    public void testUploadObject(){
        S3Service test = new S3Service();
        PutObjectRequest putObjectRequest = Mockito.mock(PutObjectRequest.class);
        test.uploadObject(putObjectRequest);

        verify(client).putObject(putObjectRequest);
    }

    @Test
    public void testGetObjectResourceUrl() throws MalformedURLException {
        S3Service test = new S3Service();
        Mockito.when(client.getUrl(matches("test-bucket"),matches("test-url"))).thenReturn(new URL("http://testing.com"));

        String result = test.getObjectResourceUrl("test-bucket", "test-url");
        Assert.assertEquals("Unexpected URL returned", "http://testing.com", result);

    }

    @Test
    public void testSetBucketPoliciy(){
        S3Service test = new S3Service();
        test.setBucketPolicy("test-name","test-policy");

        verify(client).setBucketPolicy(any(SetBucketPolicyRequest.class));
    }

    @Test
    public void testListAllObjects(){
        ObjectListing objectListing = Mockito.mock(ObjectListing.class);
        S3ObjectSummary dummySummary = new S3ObjectSummary();
        dummySummary.setBucketName("my-bucket");
        dummySummary.setKey("test");
        Mockito.when(client.listObjects(matches("my-bucket"))).thenReturn(objectListing);
        Mockito.when(objectListing.isTruncated()).thenReturn(true).thenReturn(false);
        Mockito.when(client.listNextBatchOfObjects(objectListing)).thenReturn(objectListing);
        List<S3ObjectSummary> objectSummaryList = Arrays.asList(dummySummary, dummySummary, dummySummary);
        Mockito.when(objectListing.getObjectSummaries()).thenReturn(objectSummaryList);
        S3Service test = new S3Service();

        List<String> result = test.listAllObjects("my-bucket");

        Assert.assertEquals("Object Array Size was incorrect", 6, result.size());
        Assert.assertEquals(" Bucket name was incorrect", "test", result.get(0));
        verify(client).listObjects(matches("my-bucket"));
        // Verify only triggered one call to get another batch
        verify(client).listNextBatchOfObjects(any(ObjectListing.class));
    }

    @Test
    public void testListObjects(){
        ObjectListing objectListing = Mockito.mock(ObjectListing.class);
        S3ObjectSummary dummySummary = new S3ObjectSummary();
        dummySummary.setBucketName("my-bucket");
        dummySummary.setKey("test");
        Mockito.when(client.listObjects(matches("my-bucket"), matches("my-prefix"))).thenReturn(objectListing);
        Mockito.when(objectListing.isTruncated()).thenReturn(true).thenReturn(false);
        Mockito.when(client.listNextBatchOfObjects(objectListing)).thenReturn(objectListing);
        List<S3ObjectSummary> objectSummaryList = Arrays.asList(dummySummary, dummySummary, dummySummary);
        Mockito.when(objectListing.getObjectSummaries()).thenReturn(objectSummaryList);
        S3Service test = new S3Service();

        List<String> result = test.listObjects("my-bucket","my-prefix");

        Assert.assertEquals("Object Array Size was incorrect", 6, result.size());
        Assert.assertEquals(" Bucket name was incorrect", "test", result.get(0));
        verify(client).listObjects(matches("my-bucket"),matches("my-prefix"));
        // Verify only triggered one call to get another batch
        verify(client).listNextBatchOfObjects(any(ObjectListing.class));
    }


    @Test
    public void testGetObject(){
        S3Service test = new S3Service();
        S3Object mockObject = Mockito.mock(S3Object.class);
        Mockito.when(client.getObject(matches("test-bucket"),matches("test-key"))).thenReturn(mockObject);
        S3Object result = test.getObject("test-bucket", "test-key");
        Assert.assertEquals("Unexpected Object returned", mockObject, result);
    }
}