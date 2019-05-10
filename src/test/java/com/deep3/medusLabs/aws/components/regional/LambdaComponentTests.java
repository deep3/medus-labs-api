package com.deep3.medusLabs.aws.components.regional;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.AWSLambdaException;
import com.amazonaws.services.lambda.model.DeleteFunctionRequest;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import com.deep3.medusLabs.aws.exceptions.InvalidAWSCredentials;
import com.deep3.medusLabs.model.LogEntry;
import com.deep3.medusLabs.service.DeployedLabLogService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
@PrepareForTest({LambdaComponent.class, AWSLambdaClientBuilder.class})
public class LambdaComponentTests {

    @Mock
    private AWSLambda client;

    @Mock
    private STSAssumeRoleSessionCredentialsProvider credentials;

    @Mock
    private DeployedLabLogService deployedLabLogService;

    AWSLambdaClientBuilder builder;

    @Test
    public void testConstructor() throws InvalidAWSCredentials {
        LambdaComponent lambdaComponent = new LambdaComponent(credentials, Regions.EU_WEST_2);
        Assert.assertNotNull("Error creating instance of LambdaComponent", lambdaComponent);
    }

    @Test(expected = InvalidAWSCredentials.class)
    public void testNullCredentials() throws InvalidAWSCredentials {
        LambdaComponent lambdaComponent = new LambdaComponent(null, Regions.EU_WEST_2);
    }

    @Before
    public void initialize() throws Exception {
        MockitoAnnotations.initMocks(this);
        builder = PowerMockito.mock(AWSLambdaClientBuilder.class);
        PowerMockito.mockStatic(AWSLambdaClientBuilder.class);
        PowerMockito.when(AWSLambdaClientBuilder.standard()).thenReturn(builder);
        PowerMockito.when(builder.withCredentials(any())).thenReturn(builder);
        PowerMockito.when(builder.withRegion(any(Regions.class))).thenReturn(builder);
        PowerMockito.when(builder.build()).thenReturn(client);
    }

    @Test
    public void testDeleteAll() throws Exception {
        ListFunctionsResult listFunctionsResult = PowerMockito.mock(ListFunctionsResult.class);
        PowerMockito.when(client.listFunctions(any())).thenReturn(listFunctionsResult);

        // Returning a token triggers a loop to indicate there is more data in the AWS API, the second time we return null to indicate we have all the data.
        PowerMockito.when(listFunctionsResult.getNextMarker()).thenReturn("something!").thenReturn(null);
        //Return two lists as we are exercising the loop functionality of the method to simulate multiple queries to AWS.
        PowerMockito.when(listFunctionsResult.getFunctions()).thenReturn(Arrays.asList(new FunctionConfiguration().withFunctionName("A"),new FunctionConfiguration().withFunctionName("B"),new FunctionConfiguration().withFunctionName("C")))
                .thenReturn(Arrays.asList(new FunctionConfiguration().withFunctionName("D"),new FunctionConfiguration().withFunctionName("E"), new FunctionConfiguration().withFunctionName("F")));

        DeleteFunctionRequest deleteMock = PowerMockito.mock(DeleteFunctionRequest.class);
        PowerMockito.whenNew(DeleteFunctionRequest.class).withAnyArguments().thenReturn(deleteMock);

        LambdaComponent lambdaComponent = new LambdaComponent(credentials, Regions.EU_WEST_2);

        lambdaComponent.deleteAll(any(Long.class));
        List<String> expectedDeletions = Arrays.asList("A", "B", "C", "D", "E", "F");
        // verify 6 delete calls are made.
        verify(client, times(6)).deleteFunction(any());
        // And that the IDs match the expected list above.
        verify(deleteMock, times(6)).withFunctionName(argThat(deleting -> expectedDeletions.contains(deleting) ));

    }

    @Test
    public void testDeleteAllExceptionHandling() throws Exception {
        ListFunctionsResult listFunctionsResult = PowerMockito.mock(ListFunctionsResult.class);
        PowerMockito.when(client.listFunctions(any())).thenReturn(listFunctionsResult);
        PowerMockito.when(listFunctionsResult.getNextMarker()).thenReturn(null);
        PowerMockito.when(listFunctionsResult.getFunctions()).thenReturn(Arrays.asList(new FunctionConfiguration().withFunctionName("A"),new FunctionConfiguration().withFunctionName("B"),new FunctionConfiguration().withFunctionName("C")));

        DeleteFunctionRequest deleteMock = PowerMockito.mock(DeleteFunctionRequest.class);
        PowerMockito.whenNew(DeleteFunctionRequest.class).withAnyArguments().thenReturn(deleteMock);
        PowerMockito.when(client.deleteFunction(any())).thenThrow(AWSLambdaException.class);

        LambdaComponent lambdaComponent = new LambdaComponent(credentials, Regions.EU_WEST_2);
        lambdaComponent.deployedLabLogService = deployedLabLogService;
        PowerMockito.doReturn(null).when(deployedLabLogService).save(any(LogEntry.class));
        lambdaComponent.deleteAll(1L);

        // verify 3 delete calls are made.
        verify(client, times(3)).deleteFunction(any());

    }

}
