package com.deep3.medusLabs.aws.service;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.organizations.model.Account;
import com.deep3.medusLabs.aws.components.global.S3Component;
import com.deep3.medusLabs.aws.components.regional.CloudFormationComponent;
import com.deep3.medusLabs.aws.components.regional.EC2Component;
import com.deep3.medusLabs.aws.exceptions.NoAvailableServiceComponent;
import com.deep3.medusLabs.aws.model.AWSAccountWrapper;
import com.deep3.medusLabs.model.DeployedLab;
import com.deep3.medusLabs.model.Lab;
import com.deep3.medusLabs.service.DeployedLabLogService;
import com.deep3.medusLabs.service.DeployedLabService;
import com.deep3.medusLabs.service.SocketService;
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

import java.util.ArrayList;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(AccountWranglerService.class)
public class AccountWranglerServiceTests {

    @Mock
    private OrganisationsService organisationsService;

    @Mock
    private SocketService socketService;

    @Mock
    private Account account;

    @Mock
    private STSAssumeRoleSessionCredentialsProvider stsAssumeRoleSessionCredentialsProvider;

    @Mock
    private AWSAccountWrapper awsAccountWrapper;

    @Mock
    private S3Component s3Component;

    @Mock
    private EC2Component ec2Component;

    @Mock
    private CloudFormationComponent cloudFormationComponent;

    @Mock
    private DeployedLabService deployedLabService;

    @Mock
    private DeployedLab deployedLab;

    @Mock
    private DeployedLabLogService deployedLabLogService;

    @Mock
    Lab lab;

    @Before
    public void initialize() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when(organisationsService.getValidAccounts()).thenReturn(Stream.of(account).collect(toList()));
        Mockito.when(organisationsService.getCredentialsForAccount(account)).thenReturn(stsAssumeRoleSessionCredentialsProvider);
        PowerMockito.whenNew(AWSAccountWrapper.class).withAnyArguments().thenReturn(awsAccountWrapper);
    }

    @Test
    public void testCreateService()  {

        AccountWranglerService testedAccountWrangler = new AccountWranglerService(organisationsService, socketService, deployedLabService, deployedLabLogService);

        Assert.assertEquals(1, testedAccountWrangler.getAccounts().size());
        Assert.assertEquals(awsAccountWrapper, testedAccountWrangler.getAccounts().get(0));

    }

    @Test
    public void testWipeAccounts() throws Exception {

        AccountWranglerService testedAccountWrangler = new AccountWranglerService(organisationsService, socketService, deployedLabService, deployedLabLogService);

        testedAccountWrangler.wipeAllAccounts(any(Long.class));
        verify(awsAccountWrapper).wipeAccount(any(Long.class));
    }



    @Test
    public void testDeployCloudformation() throws Exception {

        doReturn(s3Component).when(awsAccountWrapper).getGlobalService(S3Component.class);
        doReturn(ec2Component).when(awsAccountWrapper).getRegionalService(EC2Component.class,Regions.EU_WEST_1);
        Mockito.when(awsAccountWrapper.getAccount()).thenReturn(account);

        doReturn(cloudFormationComponent).when(awsAccountWrapper).getRegionalService(CloudFormationComponent.class,Regions.EU_WEST_1);
        Mockito.when(deployedLab.getLab()).thenReturn(lab);
        AccountWranglerService testedAccountWrangler = new AccountWranglerService(organisationsService, socketService, deployedLabService, deployedLabLogService);
        testedAccountWrangler.deployCloudFormationStack(Regions.EU_WEST_1, "testStack", awsAccountWrapper, deployedLab, new ArrayList<>());

        verify(ec2Component).createKeyPair(any());
        verify(s3Component).createBucket(any());
        verify(s3Component).putStringIntoS3File(any(),any(),any());

        verify(cloudFormationComponent).deploy(any());
        verify(cloudFormationComponent).monitorStackCreateStatus(any(),any());

    }

    @Test
    public void testDeleteCloudformationStacks() throws NoAvailableServiceComponent {
        AccountWranglerService testedAccountWrangler = new AccountWranglerService(organisationsService, socketService, deployedLabService, deployedLabLogService);
        doReturn(cloudFormationComponent).when(awsAccountWrapper).getRegionalService(CloudFormationComponent.class,Regions.EU_WEST_1);

        testedAccountWrangler.deleteCloudFormationStacks(Regions.EU_WEST_1, 1L);
        verify(cloudFormationComponent).deleteAll(any(Long.class));

    }
}
