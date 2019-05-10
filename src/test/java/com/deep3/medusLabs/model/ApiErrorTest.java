package com.deep3.medusLabs.model;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServletWebRequest.class, HttpServletRequest.class})
public class ApiErrorTest {

    @Test
    public void testConstructorTimeStamp() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Constructor<APIError> constructor = APIError.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        APIError apiError = constructor.newInstance();

        Assert.assertNotNull(apiError.getTimestamp());

        constructor.setAccessible(false);
    }

    @Test
    public void testConstructorSimple() {
        APIError apiError = new APIError(HttpStatus.ACCEPTED);

        Assert.assertEquals(apiError.getStatus(), HttpStatus.ACCEPTED);
    }

    @Test
    public void testConstructorStatusThrowable() {

        Throwable throwable = new Throwable("test-throwable");

        APIError apiError = new APIError(HttpStatus.ACCEPTED, throwable);

        Assert.assertEquals(apiError.getStatus(), HttpStatus.ACCEPTED);
        Assert.assertEquals(apiError.getMessage(), throwable.getMessage());
        Assert.assertEquals(apiError.getDebugMessage(), throwable.getLocalizedMessage());
    }

    @Test
    public void testConstructorStatusThrowableRequest() {
        Throwable throwable = new Throwable("test-throwable");

        ServletWebRequest request = PowerMockito.mock(ServletWebRequest.class);
        HttpServletRequest servletRequest = PowerMockito.mock(HttpServletRequest.class);
        PowerMockito.when(request.getRequest()).thenReturn(servletRequest);
        PowerMockito.when(servletRequest.getRequestURI()).thenReturn("/test/uri");

        APIError apiError = new APIError(HttpStatus.ACCEPTED, throwable, request);

        Assert.assertEquals(apiError.getStatus(), HttpStatus.ACCEPTED);
        Assert.assertEquals(apiError.getMessage(), throwable.getMessage());
        Assert.assertEquals(apiError.getDebugMessage(), throwable.getLocalizedMessage());
    }

    @Test
    public void testSetGetStatus() {
        APIError apiError = new APIError(HttpStatus.ACCEPTED);

        Assert.assertEquals(apiError.getStatus(), HttpStatus.ACCEPTED);
        Assert.assertEquals(202, apiError.getStatusCode());

        apiError.setStatus(HttpStatus.BAD_GATEWAY);

        Assert.assertEquals(apiError.getStatus(), HttpStatus.BAD_GATEWAY);
        Assert.assertEquals(502, apiError.getStatusCode());
    }

    @Test
    public void testSetGetStatusCode() {
        APIError apiError = new APIError(HttpStatus.ACCEPTED);

        Assert.assertEquals(202, apiError.getStatusCode());
        Assert.assertEquals(HttpStatus.ACCEPTED, apiError.getStatus());

        apiError.setStatusCode(502);

        Assert.assertEquals(502, apiError.getStatusCode());
        Assert.assertEquals(HttpStatus.BAD_GATEWAY, apiError.getStatus());
    }

    @Test
    public void testSetGetLocalDateTime() {
        LocalDateTime testDateTime = LocalDateTime.now();
        APIError apiError = new APIError(HttpStatus.ACCEPTED);

        LocalDateTime returnedDateTime = apiError.getTimestamp();

        Assert.assertTrue(apiError.getTimestamp() != testDateTime);

        apiError.setTimestamp(testDateTime);

        Assert.assertEquals(apiError.getTimestamp(), testDateTime);
    }

    @Test
    public void testSetGetMessage() {
        APIError apiError = new APIError(HttpStatus.ACCEPTED);

        Assert.assertNull(apiError.getMessage());

        apiError.setMessage("test-message");

        Assert.assertEquals(apiError.getMessage(), "test-message");
    }

    @Test
    public void testSetGetDebugMessage() {
        APIError apiError = new APIError(HttpStatus.ACCEPTED);

        Assert.assertNull(apiError.getDebugMessage());

        apiError.setDebugMessage("test-debug-message");

        Assert.assertEquals(apiError.getDebugMessage(), "test-debug-message");
    }

    @Test
    public void testSetGetUri() {
        APIError apiError = new APIError(HttpStatus.ACCEPTED);

        Assert.assertNull(apiError.getUri());

        apiError.setUri("/test/uri");

        Assert.assertEquals("/test/uri", apiError.getUri());
    }


}
