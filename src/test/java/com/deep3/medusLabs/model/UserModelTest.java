package com.deep3.medusLabs.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class UserModelTest {

    @Test
    public void testConstructorWithId() {

        Long id = 1l;
        User testUser = new User(id, "test-user", "test-password");

        Long response = testUser.getId();

        Assert.assertEquals(response, id);

        Assert.assertEquals(testUser.getUsername(), "test-user");
        Assert.assertEquals(testUser.getPassword(), "test-password");

    }

    @Test
    public void testConstructorWithoutId() {

        User testUser = new User("test-user", "test-password");

        Assert.assertEquals(testUser.getUsername(), "test-user");
        Assert.assertEquals(testUser.getPassword(), "test-password");

    }

    @Test
    public void testSetGetId(){

        User testUser = new User("test-user", "test-password");

        Long id = 1001l;
        testUser.setId(id);
        Long response = testUser.getId();

        Assert.assertEquals(response, id);
    }

    @Test
    public void testSetGetUsername() {

        User testUser = new User("test-user", "test-password");

        testUser.setUsername("Username");

        Assert.assertEquals(testUser.getUsername(), "Username");
    }

    @Test
    public void testSetGetPassword() {

        User testUser = new User("test-user", "test-password");

        testUser.setPassword("Password");

        Assert.assertEquals(testUser.getPassword(), "Password");
    }

    @Test
    public void testSetGetLastLoggedIn() {

        User testUser = new User("test-user", "test-password");

        Date now = new Date();

        testUser.setLastLoggedIn(now);

        Assert.assertEquals(testUser.getLastLoggedIn(), now);
    }

    @Test
    public void testSetAlreadyHashedPassword() {

        User testUser = new User("test-user", "test-password");

        testUser.setAlreadyHashedPassword("PasswordHashed");

        Assert.assertEquals(testUser.getPassword(), "PasswordHashed");
    }
}
