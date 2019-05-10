package com.deep3.medusLabs.utilities;

import org.apache.commons.validator.routines.EmailValidator;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;

public class EmailUtilsTests {

    @Test(expected = IllegalStateException.class)
    public void testPrivateConstructor() throws Throwable {

        Constructor[] constructors = EmailUtils.class.getDeclaredConstructors();
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
    public void generateEmailAliasesTest(){
        List<String> list = EmailUtils.generateEmailAliases(3, "bobsmith@gmail.com");
        Assert.assertNotNull("Email alias list should not be null", list);
        Assert.assertEquals("Expecting a list of 3 email addresses",3,list.size());
        Assert.assertTrue("First email in the list is not valid!", EmailValidator.getInstance(false).isValid(list.get(0)));
        Assert.assertTrue("Second email in the list is not valid!", EmailValidator.getInstance(false).isValid(list.get(1)));
        Assert.assertTrue("Third email in the list is not valid!", EmailValidator.getInstance(false).isValid(list.get(2)));
    }
}
