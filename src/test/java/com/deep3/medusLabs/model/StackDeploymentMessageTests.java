package com.deep3.medusLabs.model;

import com.deep3.medusLabs.model.socketmessages.StackDeploymentMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class StackDeploymentMessageTests {

    @Test
    public void testConstructor() throws IOException {
        StackDeploymentMessage jsonResponse = new StackDeploymentMessage(5L, "test-StackName", "test-AccountName", "test-AccountId", "test-Message");

        ObjectMapper objectMapper = new ObjectMapper();
        String val = objectMapper.writeValueAsString(jsonResponse);

        JsonNode node = objectMapper.readTree(val);
        Assert.assertEquals(5L, node.get("deployedLabId").asLong());
        Assert.assertEquals("test-StackName", node.get("stackName").asText());
        Assert.assertEquals("test-AccountName", node.get("accountName").asText());
        Assert.assertEquals("test-AccountId", node.get("accountId").asText());
        Assert.assertEquals("test-Message", node.get("message").asText());

    }



}
