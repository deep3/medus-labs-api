package com.deep3.medusLabs.utilities;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class StackTrackingUtilsTests {

    @Test(expected = IllegalStateException.class)
    public void testPrivateConstructor() throws Throwable {
        Constructor[] constructors = StackTrackingUtils.class.getDeclaredConstructors();
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
    public void testSetGetNumberOfUsers() {

        StackTrackingUtils.setNumOfUsers(10);

        Assert.assertEquals(StackTrackingUtils.getNumOfUsers(), 10);
        Assert.assertEquals(StackTrackingUtils.getNumOfStacksFinished(), 0);

        StackTrackingUtils.reset();
    }

    @Test
    public void testGetNumberOfFailedStacks() {

        StackTrackingUtils.setNumOfUsers(10);
        for (int i = 0; i < 10; i++) {
            StackTrackingUtils.increaseNumOfStacksFailed();
        }

        Assert.assertEquals(StackTrackingUtils.getNumOfStacksFinished(), 10);
        Assert.assertEquals(StackTrackingUtils.getNumOfFailedStacks(), 10);
        StackTrackingUtils.reset();
    }
    @Test
    public void testSetGetNumberOfStacksCompleted() {

        StackTrackingUtils.setNumOfUsers(10);
        for(int i = 0; i < 10; i++){
            StackTrackingUtils.increaseNumOfStacksCompleted();
        }

        Assert.assertEquals(StackTrackingUtils.getNumOfStacksFinished(), 10);
        Assert.assertEquals(StackTrackingUtils.getNumOfFailedStacks(), 0);
        StackTrackingUtils.reset();

    }
}
