package com.deep3.medusLabs.model;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiResponseTest {

    @Test
    public void testConstructorSimple() {
        APIResponse apiResponse = new APIResponse(HttpStatus.ACCEPTED);

        Assert.assertEquals(apiResponse.getHttpStatus(), HttpStatus.ACCEPTED);
        Assert.assertTrue(apiResponse.getContent().isEmpty());
    }

    @Test
    public void testConstructorIterable() {
        List<String> content = new ArrayList<>();
        content.add("test-content-1");
        content.add("test-content-2");

        Iterable<String> iterable = content;

        APIResponse apiResponse = new APIResponse(HttpStatus.ACCEPTED, iterable);

        Assert.assertEquals(apiResponse.getHttpStatus(), HttpStatus.ACCEPTED);
        Assert.assertEquals(apiResponse.getContent(), iterable);
    }

    @Test
    public void testConstructorList() {
        List<String> content = new ArrayList<>();
        content.add("test-content-1");
        content.add("test-content-2");

        APIResponse apiResponse = new APIResponse(HttpStatus.ACCEPTED, content);

        Assert.assertEquals(apiResponse.getHttpStatus(), HttpStatus.ACCEPTED);
        Assert.assertEquals(apiResponse.getContent(), content);
    }

    @Test
    public void testConstructorGeneric() {
        String testData = "generic";

        APIResponse apiResponse = new APIResponse(HttpStatus.ACCEPTED, testData );

        Assert.assertEquals(apiResponse.getHttpStatus(), HttpStatus.ACCEPTED);
        Assert.assertEquals(apiResponse.getContent(), new ArrayList<String>(Arrays.asList(testData)));
    }

    @Test
    public void testSetGetHttpStatus() {
        APIResponse apiResponse = new APIResponse(HttpStatus.ACCEPTED);

        Assert.assertEquals(apiResponse.getHttpStatus(), HttpStatus.ACCEPTED);

        apiResponse.setHttpStatus(HttpStatus.BAD_GATEWAY);

        Assert.assertEquals(apiResponse.getHttpStatus(), HttpStatus.BAD_GATEWAY);
    }

    @Test
    public void testSetGetContent() {
        APIResponse apiResponse = new APIResponse(HttpStatus.ACCEPTED);

        Assert.assertTrue(apiResponse.getContent().isEmpty());

        apiResponse.setContent(Arrays.asList("test-string"));

        Assert.assertEquals(apiResponse.getContent(), Arrays.asList("test-string") );
        Assert.assertEquals(apiResponse.getContent().size(), 1);
    }


}
