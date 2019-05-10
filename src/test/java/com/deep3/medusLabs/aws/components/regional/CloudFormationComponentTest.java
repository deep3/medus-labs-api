package com.deep3.medusLabs.aws.components.regional;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.deep3.medusLabs.aws.exceptions.InvalidAWSCredentials;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;

@RunWith(MockitoJUnitRunner.class)
public class CloudFormationComponentTest {

    @Mock
    private AmazonCloudFormation amazonCloudFormation;

    @Mock
    private STSAssumeRoleSessionCredentialsProvider credentials;

    @Test
    public void testConstructor() throws InvalidAWSCredentials {
        CloudFormationComponent cloudFormationComponent = new CloudFormationComponent(credentials, Regions.EU_WEST_2);
        Assert.assertNotNull("Error creating instance of CloudFormationComponent", cloudFormationComponent);
    }

    @Test(expected = InvalidAWSCredentials.class)
    public void testNullCredentials() throws InvalidAWSCredentials {
        CloudFormationComponent cloudFormationComponent = new CloudFormationComponent(null, Regions.EU_WEST_2);
    }

    public void testDeleteAll() throws InvalidAWSCredentials, IllegalAccessException {
        CloudFormationComponent cloudFormationComponent = new CloudFormationComponent(credentials, Regions.EU_WEST_2);
        FieldUtils.writeField(cloudFormationComponent, "cloudFormationClient", amazonCloudFormation, true);

        cloudFormationComponent.deleteAll(any(Long.class));
    }

}
