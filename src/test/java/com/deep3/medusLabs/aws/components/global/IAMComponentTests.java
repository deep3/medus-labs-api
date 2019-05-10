package com.deep3.medusLabs.aws.components.global;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.deep3.medusLabs.aws.exceptions.InvalidAWSCredentials;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.ArgumentMatchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest({IAMComponent.class, AmazonIdentityManagementClientBuilder.class})
public class IAMComponentTests {

    @Mock
    private STSAssumeRoleSessionCredentialsProvider credentials;

    @Mock
    AmazonIdentityManagementClient client;


    AmazonIdentityManagementClientBuilder builder;


    @Before
    public void initialize() throws Exception {
        MockitoAnnotations.initMocks(this);
        builder = PowerMockito.mock(AmazonIdentityManagementClientBuilder.class);
        PowerMockito.mockStatic(AmazonIdentityManagementClientBuilder.class);
        PowerMockito.when(AmazonIdentityManagementClientBuilder.standard()).thenReturn(builder);
        PowerMockito.when(builder.withCredentials(any())).thenReturn(builder);
        PowerMockito.when(builder.build()).thenReturn(client);
    }


    @Test
    public void testConstructor() throws InvalidAWSCredentials {
        IAMComponent iamComponent = new IAMComponent(credentials);
        Assert.assertNotNull("Error creating instance of IAMComponent", iamComponent);
    }

    @Test(expected = InvalidAWSCredentials.class)
    public void testNullCredentials() throws InvalidAWSCredentials {
        IAMComponent iamComponent = new IAMComponent(null);
    }


    //@Test
    public void deleteAll() throws InvalidAWSCredentials {
        //IAMComponent iamComponent = new IAMComponent(credentials);
       // iamComponent.deleteAll();
    }
}
