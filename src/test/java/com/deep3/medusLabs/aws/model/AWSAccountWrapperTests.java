package com.deep3.medusLabs.aws.model;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.organizations.model.Account;
import com.deep3.medusLabs.aws.components.AWSGlobalServiceComponentInterface;
import com.deep3.medusLabs.aws.components.global.IAMComponent;
import com.deep3.medusLabs.aws.components.global.RegionalComponentWrapper;
import com.deep3.medusLabs.aws.components.global.S3Component;
import com.deep3.medusLabs.aws.components.regional.CloudFormationComponent;
import com.deep3.medusLabs.aws.exceptions.InvalidAWSCredentials;
import com.deep3.medusLabs.aws.exceptions.NoAvailableServiceComponent;
import com.deep3.medusLabs.service.DeployedLabLogService;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.MockGateway;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.InvalidClassException;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AWSAccountWrapper.class,RegionalComponentWrapper.class,IAMComponent.class})
public class AWSAccountWrapperTests {

    @Mock
    private Account account;

    @Mock
    private STSAssumeRoleSessionCredentialsProvider credentials;

    @Mock
    private IAMComponent iamComponent;

    @Mock
    private S3Component s3Component;

    @Mock
    private DeployedLabLogService deployedLabLogService;

    @Mock
    private CloudFormationComponent cloudFormationComponent;

    private RegionalComponentWrapper regionalComponentWrapper;


    @Before
    public void before() throws Exception {
        MockGateway.MOCK_GET_CLASS_METHOD = true;
        regionalComponentWrapper = PowerMockito.mock(RegionalComponentWrapper.class);
        doReturn(RegionalComponentWrapper.class).when(regionalComponentWrapper).getClass();

        MockitoAnnotations.initMocks(this);
        PowerMockito.whenNew(CloudFormationComponent.class).withAnyArguments().thenReturn(cloudFormationComponent);

        PowerMockito.whenNew(RegionalComponentWrapper.class).withAnyArguments().thenReturn(regionalComponentWrapper);
        PowerMockito.whenNew(IAMComponent.class).withAnyArguments().thenReturn(iamComponent);
        PowerMockito.whenNew(S3Component.class).withAnyArguments().thenReturn(s3Component);


    }

    @Test
    public void testGetGlobalService() throws NoAvailableServiceComponent {
        AWSAccountWrapper accountWrapper = new AWSAccountWrapper(account, credentials);
        S3Component globalService = accountWrapper.getGlobalService(s3Component.getClass());
        Assert.assertNotNull(globalService);
    }

    @Test(expected = NoAvailableServiceComponent.class)
    public void testGetUnavailableGlobalService() throws NoAvailableServiceComponent {
        AWSAccountWrapper accountWrapper = new AWSAccountWrapper(account, credentials);
        accountWrapper.getGlobalService(null);
    }

    @Test
    public void testGetRegionalService() throws NoAvailableServiceComponent, InvalidClassException, IllegalAccessException {

        Mockito.when(regionalComponentWrapper.getRegionalService(any())).thenReturn(cloudFormationComponent);
        AWSAccountWrapper accountWrapper = new AWSAccountWrapper(account, credentials);

        CloudFormationComponent regionalService = accountWrapper.getRegionalService(cloudFormationComponent.getClass(), Regions.EU_WEST_2);
        Assert.assertNotNull(regionalService);
    }

    @Test(expected = NoAvailableServiceComponent.class)
    public void testGetUnavailableRegionalService() throws NoAvailableServiceComponent {
        AWSAccountWrapper accountWrapper = new AWSAccountWrapper(account, credentials);
        CloudFormationComponent regionalService = accountWrapper.getRegionalService(CloudFormationComponent.class, Regions.AP_SOUTHEAST_2);

        Assert.assertEquals("Regional components did not match", cloudFormationComponent,regionalService);
    }

    @Test
    public void testAccountWipe() throws Exception {

        AWSAccountWrapper accountWrapper = new AWSAccountWrapper(account, credentials);
        accountWrapper.wipeAccount(1L);

        verify(iamComponent).deleteAll(any(Long.class));
        verify(s3Component).deleteAll(any(Long.class));
        verify(regionalComponentWrapper,times(5)).deleteAll(any(Long.class));

        ArrayList<AWSGlobalServiceComponentInterface> services = (ArrayList<AWSGlobalServiceComponentInterface>) FieldUtils.readField(accountWrapper, "globalServices", true);
        Assert.assertEquals("This test accounts for 7 AWS services in a delete all test, however there no longer seems to 7 services",7,services.size());
    }

    @Test
    public void testGlobalServiceException() throws Exception {
        PowerMockito.whenNew(IAMComponent.class).withAnyArguments().thenThrow(InvalidAWSCredentials.class);
        AWSAccountWrapper accountWrapper = new AWSAccountWrapper(account, credentials);
    }

    @Test
    public void testRegionalServiceException() throws Exception {
        PowerMockito.whenNew(RegionalComponentWrapper.class).withAnyArguments().thenThrow(InvalidClassException.class);
        AWSAccountWrapper accountWrapper = new AWSAccountWrapper(account, credentials);
    }


    @Test
    public void testAccountGetterSetter(){
        AWSAccountWrapper accountWrapper = new AWSAccountWrapper(account, credentials);
        Account testAccount = accountWrapper.getAccount();

        Assert.assertEquals(account,testAccount);

        accountWrapper.setAccount(null);

        Assert.assertNull(accountWrapper.getAccount());

        accountWrapper.setAccount(testAccount);

        Assert.assertEquals(account, accountWrapper.getAccount());
    }
}
