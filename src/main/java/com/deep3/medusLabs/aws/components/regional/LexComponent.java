package com.deep3.medusLabs.aws.components.regional;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lexmodelbuilding.AmazonLexModelBuilding;
import com.amazonaws.services.lexmodelbuilding.AmazonLexModelBuildingClientBuilder;
import com.amazonaws.services.lexmodelbuilding.model.*;
import com.deep3.medusLabs.aws.exceptions.InvalidAWSCredentials;
import com.deep3.medusLabs.model.enums.LogLevel;
import com.deep3.medusLabs.aws.components.AWSRegionalServiceComponentInterface;
import com.deep3.medusLabs.service.DeployedLabLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class LexComponent implements AWSRegionalServiceComponentInterface {

    private static final Logger LOG = LoggerFactory.getLogger(LexComponent.class);

    private final AmazonLexModelBuilding amazonLexModelBuilding;

    @Autowired
    DeployedLabLogService deployedLabLogService;

    public LexComponent(STSAssumeRoleSessionCredentialsProvider credentials, Regions region) throws InvalidAWSCredentials {
        if (credentials == null) {
            throw new InvalidAWSCredentials();
        }
        this.amazonLexModelBuilding = AmazonLexModelBuildingClientBuilder.standard().withCredentials(credentials).withRegion(region).build();
    }

    @Override
    public void deleteAll(long deployedLabId) {
        // Need to work out how to use lex before verifying if this works properly...
        GetBotsResult bots = amazonLexModelBuilding.getBots(new GetBotsRequest());
        List<BotMetadata> botMetadata = new ArrayList<>(bots.getBots());

        while (bots.getNextToken() != null) {
            bots = amazonLexModelBuilding.getBots(new GetBotsRequest()
                    .withNextToken(bots.getNextToken()));
            botMetadata.addAll(bots.getBots());
        }

        for (BotMetadata bot : botMetadata) {
            try {
                amazonLexModelBuilding.deleteBot(new DeleteBotRequest()
                        .withName(bot.getName()));
            } catch (AmazonLexModelBuildingException e) {
                deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                        "Failed to delete bot [%s]: %s", bot.getName(), e.getMessage());
                LOG.error("Failed to delete bot [{}]: {}", bot.getName(), e.getMessage());
            }
        }
    }
}
