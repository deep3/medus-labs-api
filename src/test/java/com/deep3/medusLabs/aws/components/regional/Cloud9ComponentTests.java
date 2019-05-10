package com.deep3.medusLabs.aws.components.regional;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloud9.AWSCloud9;
import com.amazonaws.services.cloud9.AWSCloud9ClientBuilder;
import com.amazonaws.services.cloud9.model.AWSCloud9Exception;
import com.amazonaws.services.cloud9.model.DeleteEnvironmentRequest;
import com.amazonaws.services.cloud9.model.ListEnvironmentsRequest;
import com.amazonaws.services.cloud9.model.ListEnvironmentsResult;
import com.deep3.medusLabs.aws.exceptions.InvalidAWSCredentials;
import com.deep3.medusLabs.model.LogEntry;
import com.deep3.medusLabs.service.DeployedLabLogService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Cloud9Component.class, AWSCloud9ClientBuilder.class})
public class Cloud9ComponentTests {

    @Mock
    private AWSCloud9 client;

    @Mock
    private DeployedLabLogService deployedLabLogService;

    @Mock
    private STSAssumeRoleSessionCredentialsProvider credentials;

    AWSCloud9ClientBuilder builder;

    @Before
    public void initialize() throws Exception {
        MockitoAnnotations.initMocks(this);
        builder = PowerMockito.mock(AWSCloud9ClientBuilder.class);
        PowerMockito.mockStatic(AWSCloud9ClientBuilder.class);
        PowerMockito.when(AWSCloud9ClientBuilder.standard()).thenReturn(builder);
        PowerMockito.when(builder.withCredentials(any())).thenReturn(builder);
        PowerMockito.when(builder.withRegion(any(Regions.class))).thenReturn(builder);
        PowerMockito.when(builder.build()).thenReturn(client);
    }

    @Test
    public void testConstructor() throws InvalidAWSCredentials {
        Cloud9Component cloud9Component = new Cloud9Component(credentials, Regions.EU_WEST_2);
        Assert.assertNotNull("Error creating instance of Cloud9Component", cloud9Component);
    }

    @Test(expected = InvalidAWSCredentials.class)
    public void testNullCredentials() throws InvalidAWSCredentials {
        Cloud9Component cloud9Component = new Cloud9Component(null, Regions.EU_WEST_2);
    }

    @Test
    public void testDeleteAll() throws Exception {
        ListEnvironmentsResult environmentsResult = PowerMockito.mock(ListEnvironmentsResult.class);
        PowerMockito.when(client.listEnvironments(any(ListEnvironmentsRequest.class))).thenReturn(environmentsResult);

        // Returning a token triggers a loop to indicate there is more data in the AWS API, the second time we return null to indicate we have all the data.
        PowerMockito.when(environmentsResult.getNextToken()).thenReturn("something!").thenReturn(null);
        //Return two lists as we are exercising the loop functionality of the method to simulate multiple queries to AWS.
        PowerMockito.when(environmentsResult.getEnvironmentIds()).thenReturn(Arrays.asList("A","B","C")).thenReturn(Arrays.asList("E","F","G"));

        DeleteEnvironmentRequest deleteMock = PowerMockito.mock(DeleteEnvironmentRequest.class);
        PowerMockito.whenNew(DeleteEnvironmentRequest.class).withAnyArguments().thenReturn(deleteMock);

        Cloud9Component cloud9Component = new Cloud9Component(credentials, Regions.EU_WEST_2);

        cloud9Component.deleteAll(any(Long.class));
        List<String> expectedDeletions = Arrays.asList("A", "B", "C", "E", "F", "G");
        // verify 6 delete calls are made.
        verify(client, times(6)).deleteEnvironment(any());
        // And that the IDs match the expected list above.
        verify(deleteMock, times(6)).withEnvironmentId(argThat(deleting -> expectedDeletions.contains(deleting) ));

    }

    @Test
    public void testDeleteAllExceptionHandling() throws Exception {
        ListEnvironmentsResult environmentsResult = PowerMockito.mock(ListEnvironmentsResult.class);
        PowerMockito.when(environmentsResult.getEnvironmentIds()).thenReturn(Arrays.asList("A","B","C"));
        PowerMockito.when(client.listEnvironments(any(ListEnvironmentsRequest.class))).thenReturn(environmentsResult);
        PowerMockito.when(environmentsResult.getNextToken()).thenReturn(null);
        DeleteEnvironmentRequest deleteMock = PowerMockito.mock(DeleteEnvironmentRequest.class);
        PowerMockito.whenNew(DeleteEnvironmentRequest.class).withAnyArguments().thenReturn(deleteMock);
        PowerMockito.when(client.deleteEnvironment(any())).thenThrow(AWSCloud9Exception.class);
        Cloud9Component cloud9Component = new Cloud9Component(credentials, Regions.EU_WEST_2);
        cloud9Component.deployedLabLogService = deployedLabLogService;
        PowerMockito.doReturn(null).when(deployedLabLogService).save(any(LogEntry.class));

        cloud9Component.deleteAll(1L);
        // verify delete continues 3 times.
        verify(client, times(3)).deleteEnvironment(any());

    }
}
