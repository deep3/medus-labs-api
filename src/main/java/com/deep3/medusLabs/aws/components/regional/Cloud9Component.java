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
import com.deep3.medusLabs.model.enums.LogLevel;
import com.deep3.medusLabs.aws.components.AWSRegionalServiceComponentInterface;
import com.deep3.medusLabs.service.DeployedLabLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class Cloud9Component implements AWSRegionalServiceComponentInterface {

    private static final Logger LOG = LoggerFactory.getLogger(Cloud9Component.class);

    private final AWSCloud9 awsCloud9;

    @Autowired
    DeployedLabLogService deployedLabLogService;

    public Cloud9Component(STSAssumeRoleSessionCredentialsProvider credentials, Regions region) throws InvalidAWSCredentials {
        if (credentials == null) {
            throw new InvalidAWSCredentials();
        }
        this.awsCloud9 = AWSCloud9ClientBuilder.standard().withCredentials(credentials).withRegion(region).build();
    }

    @Override
    public void deleteAll(long deployedLabId) {
        ListEnvironmentsResult environments = awsCloud9.listEnvironments(new ListEnvironmentsRequest());
        List<String> environmentIds = new ArrayList<>(environments.getEnvironmentIds());

        while (environments.getNextToken() != null) {
            environments = awsCloud9.listEnvironments(new ListEnvironmentsRequest()
                    .withNextToken(environments.getNextToken()));
            environmentIds.addAll(environments.getEnvironmentIds());
        }

        for (String environment : environmentIds) {
            try {
                awsCloud9.deleteEnvironment(new DeleteEnvironmentRequest()
                        .withEnvironmentId(environment));
            } catch (AWSCloud9Exception e) {
                deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                        "Failed to delete environment [%s]: %s", environment, e.getMessage());
                LOG.error("Failed to delete environment [{}]: {}", environment, e.getMessage());
            }

        }
    }
}
