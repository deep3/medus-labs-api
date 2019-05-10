package com.deep3.medusLabs.aws.components.regional;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.*;
import com.deep3.medusLabs.aws.exceptions.InvalidAWSCredentials;
import com.deep3.medusLabs.model.enums.LogLevel;
import com.deep3.medusLabs.aws.components.AWSRegionalServiceComponentInterface;
import com.deep3.medusLabs.service.DeployedLabLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class LambdaComponent implements AWSRegionalServiceComponentInterface {

    private static final Logger LOG = LoggerFactory.getLogger(LambdaComponent.class);

    private final AWSLambda client;

    DeployedLabLogService deployedLabLogService;

    public LambdaComponent(STSAssumeRoleSessionCredentialsProvider credentials, Regions region) throws InvalidAWSCredentials {
        if (credentials == null) {
            throw new InvalidAWSCredentials();
        }
        this.client = AWSLambdaClientBuilder.standard().withCredentials(credentials).withRegion(region).build();
    }

    @Override
    public void deleteAll(long deployedLabId) {
        ListFunctionsResult functions = client.listFunctions(new ListFunctionsRequest());
        List<FunctionConfiguration> functionConfigurations = new ArrayList<>(functions.getFunctions());

        while (functions.getNextMarker() != null) {
            functions = client.listFunctions(new ListFunctionsRequest()
                    .withMarker(functions.getNextMarker()));
            functionConfigurations.addAll(functions.getFunctions());
        }

        for (FunctionConfiguration function : functionConfigurations) {
            try {
                client.deleteFunction(new DeleteFunctionRequest()
                        .withFunctionName(function.getFunctionName()));
            } catch (AWSLambdaException e) {
                deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                        "Failed to delete function [%s]: %s", function.getFunctionName(), e.getMessage());
                LOG.error("Failed to delete function [{}]: {}", function.getFunctionName(), e.getMessage());
            }
        }
    }
}
