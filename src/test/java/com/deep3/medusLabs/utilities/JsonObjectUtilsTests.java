package com.deep3.medusLabs.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonObjectUtilsTests {

    @Test(expected = IllegalStateException.class)
    public void testPrivateConstructor() throws Throwable {
        Constructor[] constructors = JsonObjectUtils.class.getDeclaredConstructors();
        Assert.assertEquals(1, constructors.length);
        Constructor constructor = constructors[0];
        // use the Reflection API to check that it is Private
        Assert.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
        } catch (Exception e) {
            // Get the error thrown by the constructor
            throw e.getCause();
        }
    }

    @Test
    public void testGetParametersFromJson() {

        Map testMap = new HashMap<>();
        testMap.put("testKey", "testValue");

        List testReturnedList = JsonObjectUtils.getParametersFromJson(testMap);

        Assert.assertTrue(testReturnedList.get(0).toString().contains("testKey"));
        Assert.assertTrue(testReturnedList.get(0).toString().contains("testValue"));
    }

    @Test(expected = Exception.class)
    public void testThrowException() throws JSONException, IOException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("test-Key", 1);

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(jsonObject);

        HashMap<String,Object> result = new ObjectMapper().readValue(jsonArray.toString(), HashMap.class);

        JsonObjectUtils.getParametersFromJson(result);

    }
}
