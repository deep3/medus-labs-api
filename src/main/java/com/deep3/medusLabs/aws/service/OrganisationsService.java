package com.deep3.medusLabs.aws.service;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.services.organizations.AWSOrganizations;
import com.amazonaws.services.organizations.AWSOrganizationsClientBuilder;
import com.amazonaws.services.organizations.model.*;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityResult;
import com.deep3.medusLabs.configuration.CloudFormationScriptManager;
import com.deep3.medusLabs.utilities.EmailUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class OrganisationsService {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisationsService.class);

    private static final int PAGESIZE = 10;
    private static final String ROLE_NAME = "OrganizationAccountAccessRole";
    private static final String ACCOUNT_NAME_PREFIX = "AWSLabsAccount";
    private final AWSOrganizations client;
    private final String accountId;
    private final String accountArn;
    private final String currentUserId;
    private CloudFormationScriptManager cloudFormationScriptManager;

    public OrganisationsService() {
        client = AWSOrganizationsClientBuilder.defaultClient();
        AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.defaultClient();
        GetCallerIdentityResult identity = stsClient.getCallerIdentity(new GetCallerIdentityRequest());
        this.accountId =  identity.getAccount();
        this.accountArn =  identity.getArn();
        this.currentUserId = identity.getUserId();
    }

    @Autowired
    public void setCloudFormationScriptManager(CloudFormationScriptManager cloudFormationScriptManager) {
        this.cloudFormationScriptManager = cloudFormationScriptManager;
    }

    /**
     * Get the current account ID
     * @return accountID
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * Get the current account amazon resource number
     * @return the ARN
     */
    public String getAccountArn() {
        return accountArn;
    }

    /**
     * Get the current user ID
     * @return user ID
     */
    public String getCurrentUserId() {
        return currentUserId;
    }

    /**
     * Get a list of all valid accounts
     * @return list of valid accounts
     */
    public List<Account> getValidAccounts(){
        return getAllAccounts()
                .stream()
                .filter(account ->
                        account.getStatus().equals("ACTIVE")
                                && account.getName().startsWith(ACCOUNT_NAME_PREFIX)
                )
                .collect(Collectors.toList());
    }

    /**
     * Return a list of all invalid / unsuccessfully created accounts
     * @return the list of invalid accounts
     */
    public List<Account> getInvalidAccounts(){
        return getAllAccounts()
                .stream()
                .filter(account ->
                        !account.getStatus().equals("ACTIVE")
                                || !account.getName().startsWith(ACCOUNT_NAME_PREFIX)
                )
                .collect(Collectors.toList());
    }

    /**
     *  Gets accounts by building a ListAccountsRequest object, and passing to the AWS SDK
     * @return A list of Accounts
     */
    public List<Account> getAllAccounts(){
        ArrayList<Account> result = new ArrayList<>();
        ListAccountsRequest req = new ListAccountsRequest();
        req.setMaxResults(PAGESIZE);
        return getAccountRequest(result, req);
    }

    private List<Account> getAccountRequest(List<Account> result, ListAccountsRequest request){
        ListAccountsResult listOfAccountsResult = client.listAccounts(request);

        result.addAll(listOfAccountsResult.getAccounts()
                        .stream()
                        .filter(account  -> !account.getId().equals(this.accountId))
                        .collect(Collectors.toList()));


        if(listOfAccountsResult.getNextToken() != null) {
            ListAccountsRequest newRequest = new ListAccountsRequest();
            newRequest.setNextToken(listOfAccountsResult.getNextToken());
            newRequest.setMaxResults(PAGESIZE);
            getAccountRequest(result, newRequest);
        }
        return result;
    }

    /**
     * Retrieve simple token service credentials for a given account
     * @param awsAccount
     * @return - the set of credentials
     */
    public STSAssumeRoleSessionCredentialsProvider getCredentialsForAccount(Account awsAccount){

        return new STSAssumeRoleSessionCredentialsProvider.
                Builder("arn:aws:iam::" + awsAccount.getId() + ":role/" + ROLE_NAME,UUID.randomUUID().toString())
                .build();
    }


    /**
     *  Creates a collection of AWS accounts from a single email address - Each AWS Account requires a unique email
     *  address, this can be circumvented using Gmails alias functionality.
     * @param numberOfAccounts - number of accounts to be created
     * @param email - the base email address upon which aliases are created
     * @return - A list of account names
     */
    public ArrayList<String> createAccounts(int numberOfAccounts, String email) {
        List<String> accountEmails = EmailUtils.generateEmailAliases(numberOfAccounts, email);
        int existingAccounts = this.getValidAccounts().size();
        ArrayList<String> result = new ArrayList<>();
        for(int i = 0; i < accountEmails.size(); i++){
            // We offset the account number based of the existing accounts.
            // This is to ensure if the method is run twice we don't get Duplicate AWSLab1 etc.
            try {
                CreateAccountStatus createAccountStatus = createNewAccount(accountEmails.get(i), (i + 1 + existingAccounts));
                // Not the account ID - that isn't populated when the account is IN_PROGRESS
                result.add(createAccountStatus.getAccountName());
            } catch (ConstraintViolationException e) {
                LOG.error("Maximum AWS accounts reached {}", e.getMessage());
                updateS3BucketAccess(existingAccounts + i);
                return result;
            }
        }
        updateS3BucketAccess(existingAccounts + accountEmails.size());
        return result;
    }

    /**
     * Updates the S3 Bucket policy if all the accounts in question have been successfully created
     * @param expectedAccounts - the number of accounts to update the bucket policy with
     */
    private void updateS3BucketAccess(int expectedAccounts) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            int retries = 10;
            int i;
            for (i = 0; i < retries; i++) {
                if (expectedAccounts == this.getValidAccounts().size()) {
                    cloudFormationScriptManager.updateBucketPolicy();
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LOG.error("Interrupted waiting for new accounts to create", e);
                }
            }
            if (i >= retries) {
                LOG.warn("Waiting for new account creation has timed out - You may experience issues accessing S3 resources");
                cloudFormationScriptManager.updateBucketPolicy();
            }
        });
        executor.shutdown();
    }

    /**
     * Creates a new AWS Account using the AWS SDK.
     * @param accountEmail The account email address
     * @param accountNumber The number to append to ensure unique
     * @return - The status of the attempt to create an AWS account
     */
    private CreateAccountStatus createNewAccount(String accountEmail, int accountNumber) {
        return client.createAccount(new CreateAccountRequest().withEmail(accountEmail).withRoleName(ROLE_NAME).withAccountName(ACCOUNT_NAME_PREFIX + " " + accountNumber)).getCreateAccountStatus();
    }

    public String getAccountName() {

        return StringUtils.substringAfterLast(accountArn, "/");
    }

}
