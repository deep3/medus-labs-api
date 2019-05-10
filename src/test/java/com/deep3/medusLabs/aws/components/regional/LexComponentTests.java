package com.deep3.medusLabs.aws.components.regional;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lexmodelbuilding.AmazonLexModelBuilding;
import com.amazonaws.services.lexmodelbuilding.AmazonLexModelBuildingClientBuilder;
import com.amazonaws.services.lexmodelbuilding.model.AmazonLexModelBuildingException;
import com.amazonaws.services.lexmodelbuilding.model.BotMetadata;
import com.amazonaws.services.lexmodelbuilding.model.DeleteBotRequest;
import com.amazonaws.services.lexmodelbuilding.model.GetBotsResult;
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
@PrepareForTest({LexComponent.class, AmazonLexModelBuildingClientBuilder.class})
public class LexComponentTests {

    @Mock
    private AmazonLexModelBuilding client;

    @Mock
    private STSAssumeRoleSessionCredentialsProvider credentials;

    @Mock
    private DeployedLabLogService deployedLabLogService;

    private AmazonLexModelBuildingClientBuilder builder;


    @Before
    public void initialize() throws Exception {
        MockitoAnnotations.initMocks(this);
        builder = PowerMockito.mock(AmazonLexModelBuildingClientBuilder.class);
        PowerMockito.mockStatic(AmazonLexModelBuildingClientBuilder.class);
        PowerMockito.when(AmazonLexModelBuildingClientBuilder.standard()).thenReturn(builder);
        PowerMockito.when(builder.withCredentials(any())).thenReturn(builder);
        PowerMockito.when(builder.withRegion(any(Regions.class))).thenReturn(builder);
        PowerMockito.when(builder.build()).thenReturn(client);
    }

    @Test
    public void testConstructor() throws InvalidAWSCredentials {
        LexComponent lexComponent = new LexComponent(credentials, Regions.EU_WEST_2);
        Assert.assertNotNull("Error creating instance of LexComponent", lexComponent);
    }

    @Test(expected = InvalidAWSCredentials.class)
    public void testNullCredentials() throws InvalidAWSCredentials {
        LexComponent lexComponent = new LexComponent(null, Regions.EU_WEST_2);
    }

    @Test
    public void testDeleteAll() throws Exception {

        GetBotsResult getBotsResult = PowerMockito.mock(GetBotsResult.class);
        PowerMockito.when(client.getBots(any())).thenReturn(getBotsResult);
        List<BotMetadata> firstResult = Arrays.asList(new BotMetadata().withName("A"), new BotMetadata().withName("B"),new BotMetadata().withName("C"));
        List<BotMetadata> secondResult = Arrays.asList(new BotMetadata().withName("E"), new BotMetadata().withName("F"), new BotMetadata().withName("G"));
        PowerMockito.when(getBotsResult.getBots()).thenReturn(firstResult).thenReturn(secondResult);
        PowerMockito.when(getBotsResult.getNextToken()).thenReturn("something!").thenReturn(null);

        DeleteBotRequest deleteMock = PowerMockito.mock(DeleteBotRequest.class);
        PowerMockito.whenNew(DeleteBotRequest.class).withAnyArguments().thenReturn(deleteMock);

        LexComponent lexComponent = new LexComponent(credentials, Regions.EU_WEST_2);
        List<String> expectedDeletions = Arrays.asList("A", "B", "C", "E", "F", "G");

        lexComponent.deleteAll(any(Long.class));
        verify(client, times(6)).deleteBot(any());
        verify(deleteMock, times(6)).withName(argThat(deleting -> expectedDeletions.contains(deleting) ));

    }

    @Test
    public void testDeleteAllExceptionHandling() throws Exception {
        GetBotsResult getBotsResult = PowerMockito.mock(GetBotsResult.class);
        PowerMockito.when(client.getBots(any())).thenReturn(getBotsResult);
        List<BotMetadata> firstResult = Arrays.asList(new BotMetadata().withName("A"), new BotMetadata().withName("B"),new BotMetadata().withName("C"));
        PowerMockito.when(getBotsResult.getBots()).thenReturn(firstResult);
        PowerMockito.when(getBotsResult.getNextToken()).thenReturn(null);

        DeleteBotRequest deleteMock = PowerMockito.mock(DeleteBotRequest.class);
        PowerMockito.whenNew(DeleteBotRequest.class).withAnyArguments().thenReturn(deleteMock);
        PowerMockito.when(client.deleteBot(any())).thenThrow(AmazonLexModelBuildingException.class);
        LexComponent lexComponent = new LexComponent(credentials, Regions.EU_WEST_2);
        lexComponent.deployedLabLogService = deployedLabLogService;
        PowerMockito.doReturn(null).when(deployedLabLogService).save(any(LogEntry.class));

        lexComponent.deleteAll(1L);
        // verify delete continues 3 times.
        verify(client, times(3)).deleteBot(any());
    }
}
