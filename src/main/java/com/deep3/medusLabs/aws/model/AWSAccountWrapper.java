package com.deep3.medusLabs.aws.model;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.organizations.model.Account;
import com.deep3.medusLabs.aws.components.AWSGlobalServiceComponentInterface;
import com.deep3.medusLabs.aws.components.AWSRegionalServiceComponentInterface;
import com.deep3.medusLabs.aws.components.global.IAMComponent;
import com.deep3.medusLabs.aws.components.global.RegionalComponentWrapper;
import com.deep3.medusLabs.aws.components.global.S3Component;
import com.deep3.medusLabs.aws.components.regional.*;
import com.deep3.medusLabs.aws.exceptions.InvalidAWSCredentials;
import com.deep3.medusLabs.aws.exceptions.NoAvailableServiceComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InvalidClassException;
import java.util.ArrayList;

/**
 * A wrapper for a member account.
 *
 * Contains the account and the relevant credentials to access the account.
 *
 */
public class AWSAccountWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(AWSAccountWrapper.class);
    private Account account;
    private STSAssumeRoleSessionCredentialsProvider credentials;
    private ArrayList<AWSGlobalServiceComponentInterface> globalServices = new ArrayList<>();


    public AWSAccountWrapper(Account account, STSAssumeRoleSessionCredentialsProvider credentials) {
        this.account = account;
        this.credentials = credentials;
        connectGlobalServices();
        connectRegionalServices();
    }

    /**
     * Searches through the global services list to find a global service of type clazz. Casts the matching service to
     * type clazz and returns it. Throws an exception if no service can be found.
     * @param clazz class of service to search for and return
     * @return global service of type clazz
     * @throws NoAvailableServiceComponent if no matching service component can be found
     */
    public <T extends AWSGlobalServiceComponentInterface> T getGlobalService(Class<T> clazz) throws NoAvailableServiceComponent {
        for (AWSGlobalServiceComponentInterface globalService : globalServices) {

            if (globalService.getClass().equals(clazz)) {
                return clazz.cast(globalService);
            }
        }
        throw new NoAvailableServiceComponent();
    }

    /**
     * Searches through the global services list to find a regional service of type clazz for the provided region. Casts
     * the matching service to type clazz and returns it. Throws an exception if no service can be found.
     * @param clazz class of service to search for and return
     * @param region region of regional service to return
     * @return regional service of type clazz in the provided region
     * @throws NoAvailableServiceComponent if no matching service component can be found
     */
    public <T extends AWSRegionalServiceComponentInterface> T getRegionalService(Class<T> clazz, Regions region) throws NoAvailableServiceComponent {
        for (AWSGlobalServiceComponentInterface globalService : globalServices) {
            if (globalService.getClass().equals(RegionalComponentWrapper.class)) {
                AWSRegionalServiceComponentInterface regionalComponent = ((RegionalComponentWrapper) globalService).getRegionalService(region);
                if (regionalComponent != null) {
                    if (regionalComponent.getClass().equals(clazz)) {
                        return clazz.cast(regionalComponent);
                    }
                }
            }
        }
        throw new NoAvailableServiceComponent();
    }

    /**
     * Wipes all services from an Account, related to the DeployedLabID parameter supplied
     * @param deployedLabId - The DeployedLab that has been removed/wiped
     */
    public void wipeAccount(long deployedLabId) {
        LOG.info("Deleting content of account: {}", account.getId());
        for (AWSGlobalServiceComponentInterface service : globalServices) {
            service.deleteAll(deployedLabId);
        }
    }

    /**
     * Convert regional services into a global service using the regional wrapper.
     * This will create clients for each region.
     */
    private void connectRegionalServices() {
        try {
            globalServices.add(new RegionalComponentWrapper(EC2Component.class, credentials));
            globalServices.add(new RegionalComponentWrapper(Cloud9Component.class, credentials));
            globalServices.add(new RegionalComponentWrapper(LambdaComponent.class, credentials));
            globalServices.add(new RegionalComponentWrapper(LexComponent.class, credentials));
            globalServices.add(new RegionalComponentWrapper(CloudFormationComponent.class,credentials));
        } catch (InvalidClassException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add in global services that are regionless. We have one of these per member account
     */
    private void connectGlobalServices() {

        try {
            globalServices.add(new IAMComponent(credentials));
            globalServices.add(new S3Component(credentials));
        } catch (InvalidAWSCredentials invalidAWSCredentials) {
            invalidAWSCredentials.printStackTrace();
        }
    }

    /**
     * Get this AWS Account
     * @return the account object
     */
    public Account getAccount() {
        return account;
    }

    /**
     * Reassign the account object
     * @param account the new account
     */
    public void setAccount(Account account) {
        this.account = account;
    }
}
