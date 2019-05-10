package com.deep3.medusLabs.aws.service;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.services.organizations.AWSOrganizationsClient;
import com.amazonaws.services.organizations.AWSOrganizationsClientBuilder;
import com.amazonaws.services.organizations.model.*;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityResult;
import com.deep3.medusLabs.configuration.CloudFormationScriptManager;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OrganisationsService.class, AWSOrganizationsClientBuilder.class, AWSSecurityTokenServiceClientBuilder.class, STSAssumeRoleSessionCredentialsProvider.class, STSAssumeRoleSessionCredentialsProvider.Builder.class})
public class OrganisationsServiceTests {

    @Mock
    AWSOrganizationsClient client;

    @Mock
    AWSSecurityTokenService stsClient;

    @Mock
    GetCallerIdentityResult resultClient;

    @Mock
    STSAssumeRoleSessionCredentialsProvider stsAssumeRoleSessionCredentialsProvider;

    @Before
    public void initialize() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(AWSOrganizationsClientBuilder.class);
        PowerMockito.mockStatic(AWSSecurityTokenServiceClientBuilder.class);
        Mockito.when(AWSOrganizationsClientBuilder.defaultClient()).thenReturn(client);
        Mockito.when(AWSSecurityTokenServiceClientBuilder.defaultClient()).thenReturn(stsClient);
        Mockito.when(stsClient.getCallerIdentity(Mockito.any())).thenReturn(resultClient);
        Mockito.when(resultClient.getAccount()).thenReturn("myAccountId");
        Mockito.when(resultClient.getUserId()).thenReturn("myId");
        Mockito.when(resultClient.getArn()).thenReturn("myArn");
    }

    @Test
    public void testCreateService() {


        OrganisationsService organisations = new OrganisationsService();

        Assert.assertEquals("Failed to return correct AWS account ID", organisations.getAccountId(), "myAccountId");
        Assert.assertEquals("Failed to return correct AWS user ID", organisations.getCurrentUserId(), "myId");
        Assert.assertEquals("Failed to return correct AWS arn", organisations.getAccountArn(), "myArn");

    }

    @Test
    public void testAccountFilters() {

        OrganisationsService organisations = new OrganisationsService();
        ListAccountsResult accountListResult = new ListAccountsResult();

        Account invalidAccount1 = new Account();
        invalidAccount1.setId("1111");
        invalidAccount1.setName("InvalidAccount");
        invalidAccount1.setStatus(AccountStatus.ACTIVE);

        Account invalidAccount2 = new Account();
        invalidAccount2.setId("2222");
        invalidAccount2.setName("AWSLabsAccount");
        invalidAccount2.setStatus(AccountStatus.SUSPENDED);

        Account account3 = new Account();
        account3.setId("3333");
        account3.setName("AWSLabsAccount");
        account3.setStatus(AccountStatus.ACTIVE);

        ArrayList<Account> accountList = new ArrayList<>(Arrays.asList(invalidAccount1, invalidAccount2, account3));

        accountListResult.setAccounts(accountList);

        Mockito.when(client.listAccounts(Mockito.any())).thenReturn(accountListResult);

        List<Account> invalidAccounts = organisations.getInvalidAccounts();
        List<Account> validAccounts = organisations.getValidAccounts();
        List<Account> allAccounts = organisations.getAllAccounts();

        Assert.assertEquals(2, invalidAccounts.size());
        Assert.assertEquals(1, validAccounts.size());
        Assert.assertEquals(3, allAccounts.size());

    }


    @Test
    public void testAccountTokenProcessing() {
        ListAccountsResult mockListAccountsResult = PowerMockito.mock(ListAccountsResult.class);
        PowerMockito.when(client.listAccounts(any())).thenReturn(mockListAccountsResult);
        Account mockedAccount = PowerMockito.mock(Account.class);
        PowerMockito.when(mockedAccount.getId()).thenReturn("myId!");
        PowerMockito.when(mockListAccountsResult.getAccounts()).thenReturn(Arrays.asList(mockedAccount, mockedAccount, mockedAccount)).thenReturn(Arrays.asList(mockedAccount, mockedAccount));
        PowerMockito.when(mockListAccountsResult.getNextToken()).thenReturn("Something!").thenReturn(null);
        OrganisationsService organisations = new OrganisationsService();
        List<Account> result = organisations.getAllAccounts();
        Assert.assertEquals("Unexpected number of accounts returned back", 5, result.size());
        Assert.assertEquals("Unexpected account ID", "myId!", result.get(0).getId());

    }

    @Test
    public void testCreateAccounts() throws Exception {

        OrganisationsService organisations = new OrganisationsService();
        ListAccountsResult accountListResult = new ListAccountsResult();

        Account account = new Account();
        account.setId("3333");
        account.setName("AWSLabsAccount");
        account.setStatus(AccountStatus.ACTIVE);

        ArrayList<Account> accountList = new ArrayList<>(Arrays.asList(account));
        accountListResult.setAccounts(accountList);
        Mockito.when(client.listAccounts(Mockito.any())).thenReturn(accountListResult);
        CreateAccountRequest createAccountRequest = PowerMockito.mock(CreateAccountRequest.class);
        PowerMockito.whenNew(CreateAccountRequest.class).withAnyArguments().thenReturn(createAccountRequest);
        PowerMockito.when(createAccountRequest.withEmail(any())).thenReturn(createAccountRequest);
        PowerMockito.when(createAccountRequest.withRoleName(any())).thenReturn(createAccountRequest);
        PowerMockito.when(createAccountRequest.withAccountName(any())).thenReturn(createAccountRequest);
        CreateAccountStatus createAccountStatus = PowerMockito.mock(CreateAccountStatus.class);
        CreateAccountResult createAccountResult = PowerMockito.mock(CreateAccountResult.class);
        PowerMockito.when(client.createAccount(any())).thenReturn(createAccountResult);
        PowerMockito.when(createAccountResult.getCreateAccountStatus()).thenReturn(createAccountStatus);
        PowerMockito.when(createAccountStatus.getAccountName()).thenReturn("12").thenReturn("34").thenReturn("56");

        ArrayList<String> result = organisations.createAccounts(3, "test@test.com");
        verify(client, times(3)).createAccount(any());
        Assert.assertEquals(3, result.size());
        Assert.assertTrue(result.contains("12"));
        Assert.assertTrue(result.contains("34"));
        Assert.assertTrue(result.contains("56"));

    }

    @Test
    public void testUpdateS3BucketAccess() throws Exception {

        OrganisationsService organisations = new OrganisationsService();
        ListAccountsResult accountListResult1 = new ListAccountsResult();
        ListAccountsResult accountListResult2 = new ListAccountsResult();

        Account account = new Account();
        account.setId("3333");
        account.setName("AWSLabsAccount");
        account.setStatus(AccountStatus.ACTIVE);

        ArrayList<Account> accountListComplete = new ArrayList<>(Arrays.asList(
                account,
                new Account().withId("4444").withName("AWSLabsAccount").withStatus(AccountStatus.ACTIVE),
                new Account().withId("5555").withName("AWSLabsAccount").withStatus(AccountStatus.ACTIVE),
                new Account().withId("6666").withName("AWSLabsAccount").withStatus(AccountStatus.ACTIVE)
        ));

        ArrayList<Account> accountList = new ArrayList<>(Arrays.asList(account));
        accountListResult1.setAccounts(accountList);
        accountListResult2.setAccounts(accountListComplete);
        Mockito.when(client.listAccounts(Mockito.any()))
                .thenReturn(accountListResult1)
                .thenReturn(accountListResult1)
                .thenReturn(accountListResult2);
        CreateAccountRequest createAccountRequest = PowerMockito.mock(CreateAccountRequest.class);
        PowerMockito.whenNew(CreateAccountRequest.class).withAnyArguments().thenReturn(createAccountRequest);
        PowerMockito.when(createAccountRequest.withEmail(any())).thenReturn(createAccountRequest);
        PowerMockito.when(createAccountRequest.withRoleName(any())).thenReturn(createAccountRequest);
        PowerMockito.when(createAccountRequest.withAccountName(any())).thenReturn(createAccountRequest);
        CreateAccountStatus createAccountStatus = PowerMockito.mock(CreateAccountStatus.class);
        CreateAccountResult createAccountResult = PowerMockito.mock(CreateAccountResult.class);
        PowerMockito.when(client.createAccount(any())).thenReturn(createAccountResult);
        PowerMockito.when(createAccountResult.getCreateAccountStatus()).thenReturn(createAccountStatus);

        // Update s3 bucket mocks
        CloudFormationScriptManager scriptManager = PowerMockito.mock(CloudFormationScriptManager.class);
        organisations.setCloudFormationScriptManager(scriptManager);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        PowerMockito.mockStatic(Executors.class);
        PowerMockito.when(Executors.newSingleThreadExecutor()).thenReturn(executor);

        ArrayList<String> result = organisations.createAccounts(3, "test@test.com");

        // Wait for the polling thread to finish
        executor.awaitTermination(1, TimeUnit.MINUTES);

        verify(client, times(3)).createAccount(any());
        verify(scriptManager, times(1)).updateBucketPolicy();
        Assert.assertEquals(3, result.size());
    }

    @Test
    public void testCreateAccountsLimitExceeded() throws Exception {

        OrganisationsService organisations = new OrganisationsService();
        ListAccountsResult accountListResult = new ListAccountsResult();

        Account account = new Account();
        account.setId("3333");
        account.setName("AWSLabsAccount");
        account.setStatus(AccountStatus.ACTIVE);

        ArrayList<Account> accountList = new ArrayList<>(Arrays.asList(account));
        accountListResult.setAccounts(accountList);
        Mockito.when(client.listAccounts(Mockito.any())).thenReturn(accountListResult);
        CreateAccountRequest createAccountRequest = PowerMockito.mock(CreateAccountRequest.class);
        PowerMockito.whenNew(CreateAccountRequest.class).withAnyArguments().thenReturn(createAccountRequest);
        PowerMockito.when(createAccountRequest.withEmail(any())).thenReturn(createAccountRequest);
        PowerMockito.when(createAccountRequest.withRoleName(any())).thenReturn(createAccountRequest);
        PowerMockito.when(createAccountRequest.withAccountName(any())).thenReturn(createAccountRequest);
        CreateAccountStatus createAccountStatus = PowerMockito.mock(CreateAccountStatus.class);
        CreateAccountResult createAccountResult = PowerMockito.mock(CreateAccountResult.class);
        PowerMockito.when(client.createAccount(any())).thenReturn(createAccountResult).thenReturn(createAccountResult).thenThrow(ConstraintViolationException.class);
        PowerMockito.when(createAccountResult.getCreateAccountStatus()).thenReturn(createAccountStatus);
        PowerMockito.when(createAccountStatus.getAccountName()).thenReturn("12").thenReturn("34");

        ArrayList<String> result = organisations.createAccounts(5, "test@test.com");
        verify(client, times(3)).createAccount(any());
        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.contains("12"));
        Assert.assertTrue(result.contains("34"));

    }


    @Test
    public void testGetCredentialsForAccount() throws Exception {
        OrganisationsService organisations = new OrganisationsService();
        STSAssumeRoleSessionCredentialsProvider.Builder mocked = PowerMockito.mock(STSAssumeRoleSessionCredentialsProvider.Builder.class);

        PowerMockito.whenNew(STSAssumeRoleSessionCredentialsProvider.Builder.class).withAnyArguments().thenReturn(mocked);
        PowerMockito.when(mocked.build()).thenReturn(stsAssumeRoleSessionCredentialsProvider);

        Account account = new Account();
        account.setId("1111");
        account.setName("Account");
        account.setStatus(AccountStatus.ACTIVE);

        STSAssumeRoleSessionCredentialsProvider result = organisations.getCredentialsForAccount(account);

        Assert.assertEquals("Invalid credentials returned", stsAssumeRoleSessionCredentialsProvider, result);

    }
}
