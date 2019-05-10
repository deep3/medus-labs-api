package com.deep3.medusLabs.aws.monitoring;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackEventsResult;
import com.amazonaws.services.cloudformation.model.StackEvent;
import com.deep3.medusLabs.aws.components.regional.CloudFormationComponent;
import com.deep3.medusLabs.model.DeployedLab;
import com.deep3.medusLabs.service.DeployedLabLogService;
import com.deep3.medusLabs.service.DeployedLabService;
import com.deep3.medusLabs.service.SocketService;
import com.deep3.medusLabs.utilities.StackTrackingUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(PowerMockRunner.class)
@PrepareForTest({StackTrackingUtils.class, DescribeStackEventsResult.class})
public class StackStatusHandlerTests {

    @Mock
    private SocketService socketService;

    @Mock
    private CloudFormationComponent cloudFormationComponent;

    @Mock
    private AmazonWebServiceRequest request;

    @Mock
    private Exception exception;

    @Mock
    private DescribeStackEventsResult describeStackEventsResult;

    @Mock
    DeployedLabService deployedLabService;

    @Mock
    DeployedLab deployedLab;

    @Mock
    DeployedLabLogService deployedLabLogService;

    @Test
    public void testConstructorPrivateFields() throws IllegalAccessException, JsonProcessingException {
        StackStatusHandler stackStatusHandler = new StackStatusHandler("test-stackName", "test-accountId", socketService, deployedLabService, deployedLab, deployedLabLogService);

        Field[] allFields = StackStatusHandler.class.getDeclaredFields();

        for(Field field: allFields){
            field.setAccessible(true);
            if (field.getType() == String.class){
                Assert.assertTrue(field.get(stackStatusHandler).toString().contains("test-stackName")||
                field.get(stackStatusHandler).toString().contains("test-accountId"));
            }
        }

        stackStatusHandler.onWaitSuccess(request);
        Mockito.verify(socketService, times(1)).sendMessage(any());

    }

    @Test
    public void testOnWaitSuccess() throws JsonProcessingException {
        StackStatusHandler stackStatusHandler = new StackStatusHandler("test-stackName", "test-accountId", socketService, deployedLabService, deployedLab, deployedLabLogService);
        stackStatusHandler.onWaitSuccess(request);
        Mockito.verify(socketService, times(1)).sendMessage(any());
    }

    @Test
    public void testOnWaitFailure() throws Exception {

        StackStatusHandler stackStatusHandler = new StackStatusHandler("test-stackName", "test-accountId", socketService, deployedLabService, deployedLab, deployedLabLogService);

        List<StackEvent> stackEvents = new ArrayList<>();

        StackEvent stackEvent = new StackEvent();
        stackEvent.setResourceStatus("NULL");
        stackEvent.setStackId("test-stackId");
        stackEvent.setEventId("test-eventId");

        stackEvents.add(stackEvent);

        PowerMockito.whenNew(DescribeStackEventsResult.class).withNoArguments().thenReturn(describeStackEventsResult);
        Mockito.when(cloudFormationComponent.returnStackEvent(any())).thenReturn(describeStackEventsResult);
        Mockito.when(describeStackEventsResult.getStackEvents()).thenReturn(stackEvents);

        stackStatusHandler.onWaitFailure(exception);

        Mockito.verify(socketService, times(1)).sendMessage(any());
    }

    @Test
    public void testEndMessage() throws JsonProcessingException {

        deployedLab = new DeployedLab();

        StackStatusHandler stackStatusHandler = new StackStatusHandler("test-stackName", "test-accountId", socketService, deployedLabService, deployedLab, deployedLabLogService);


        PowerMockito.mockStatic(StackTrackingUtils.class);
        PowerMockito.when(StackTrackingUtils.getNumOfUsers()).thenReturn(10);
        PowerMockito.when(StackTrackingUtils.getNumOfStacksFinished()).thenReturn(10);

        List<StackEvent> stackEvents = new ArrayList<>();

        StackEvent stackEvent = new StackEvent();
        stackEvent.setResourceStatus("NULL");
        stackEvent.setStackId("test-stackId");
        stackEvent.setEventId("test-eventId");

        stackEvents.add(stackEvent);
        PowerMockito.when(cloudFormationComponent.returnStackEvent(matches("test-stackName"))).thenReturn(describeStackEventsResult);
        PowerMockito.when(describeStackEventsResult.getStackEvents()).thenReturn(stackEvents);

        stackStatusHandler.onWaitSuccess(request);


        Assert.assertEquals(DeployedLab.DeployedLabStatus.ACTIVE, deployedLab.getDeployedLabStatus());
        Mockito.verify(deployedLabService).saveDeployedLab(deployedLab);
        Mockito.verify(socketService, times(2)).sendMessage(any());

    }


    @Test
    public void testEndWithFailuresMessage() throws JsonProcessingException {

        deployedLab = new DeployedLab();
        StackStatusHandler stackStatusHandler = new StackStatusHandler("test-stackName", "test-accountId", socketService, deployedLabService, deployedLab, deployedLabLogService);


        PowerMockito.mockStatic(StackTrackingUtils.class);
        PowerMockito.when(StackTrackingUtils.getNumOfUsers()).thenReturn(10);
        PowerMockito.when(StackTrackingUtils.getNumOfStacksFinished()).thenReturn(10);
        PowerMockito.when(StackTrackingUtils.getNumOfFailedStacks()).thenReturn(1);
        Exception e = PowerMockito.mock(Exception.class);

        List<StackEvent> stackEvents = new ArrayList<>();

        StackEvent stackEvent = new StackEvent();
        stackEvent.setResourceStatus("NULL");
        stackEvent.setStackId("test-stackId");
        stackEvent.setEventId("test-eventId");

        stackEvents.add(stackEvent);
        PowerMockito.when(cloudFormationComponent.returnStackEvent(matches("test-stackName"))).thenReturn(describeStackEventsResult);
        PowerMockito.when(describeStackEventsResult.getStackEvents()).thenReturn(stackEvents);

        stackStatusHandler.onWaitFailure(e);

        Assert.assertEquals(DeployedLab.DeployedLabStatus.FAILED, deployedLab.getDeployedLabStatus());
        Mockito.verify(deployedLabService).saveDeployedLab(deployedLab);
        Mockito.verify(socketService, times(2)).sendMessage(any());

    }


}
