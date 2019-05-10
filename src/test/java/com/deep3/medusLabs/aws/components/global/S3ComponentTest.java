package com.deep3.medusLabs.aws.components.global;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.deep3.medusLabs.aws.exceptions.InvalidAWSCredentials;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest({S3Component.class, AmazonS3ClientBuilder.class})
public class S3ComponentTest {

    @Mock
    private STSAssumeRoleSessionCredentialsProvider credentials;

    @Mock
    AmazonS3 client;

    AmazonS3ClientBuilder builder;

    @Before
    public void initialize() throws Exception {
        MockitoAnnotations.initMocks(this);
        builder = PowerMockito.mock(AmazonS3ClientBuilder.class);
        PowerMockito.mockStatic(AmazonS3ClientBuilder.class);
        PowerMockito.when(AmazonS3ClientBuilder.standard()).thenReturn(builder);
        PowerMockito.when(builder.withCredentials(any())).thenReturn(builder);
        PowerMockito.when(builder.withForceGlobalBucketAccessEnabled(true)).thenReturn(builder);
        PowerMockito.when(builder.build()).thenReturn(client);
    }

    @Test
    public void testConstructor() throws InvalidAWSCredentials {
        S3Component test = new S3Component(credentials);
        Assert.assertNotNull("Error creating instance of s3 component",test);
    }

    @Test(expected = InvalidAWSCredentials.class)
    public void testNullCredentials() throws InvalidAWSCredentials {
        new S3Component(null);
    }

    @Test
    public void testCreateBucket() throws InvalidAWSCredentials{
        S3Component test = new S3Component(credentials);
        Mockito.when(client.doesBucketExistV2(any())).thenReturn(false);
        test.createBucket("test-bucket");
        verify(client).createBucket(any(CreateBucketRequest.class));
    }

    @Test
    public void testCreateFileInBucket() throws InvalidAWSCredentials {
        S3Component test = new S3Component(credentials);
        test.putStringIntoS3File("myTestString!","text.txt","test-bucket");
        verify(client).putObject(any(PutObjectRequest.class));

    }
    @Test
    public void testDeleteAll() throws InvalidAWSCredentials {
        S3Component test = new S3Component(credentials);
        test.deleteAll(1L);
    }
}
