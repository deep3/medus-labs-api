package com.deep3.medusLabs.utilities;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class PasswordUtilsTests {

    @Test(expected = IllegalStateException.class)
    public void testPrivateConstructor() throws Throwable {

        Constructor[] constructors = PasswordUtils.class.getDeclaredConstructors();
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
    public void testGeneratePasswordNoParam() {

        String result = PasswordUtils.generatePassword();
        assertEquals(result.length(), 10);

    }

    @Test
    public void testGeneratePasswordWithParam() {

        String result = PasswordUtils.generatePassword(5);
        assertEquals(result.length(), 5);
    }

    @Test
    public void testGeneratePasswordAllDistinct() {

        List<String> test = new ArrayList();

        for (int i = 0; i < 100; i++){
            test.add(PasswordUtils.generatePassword());
        }

        Assert.assertEquals(test.stream().distinct().collect(Collectors.toList()).size(), test.size());
    }
}
