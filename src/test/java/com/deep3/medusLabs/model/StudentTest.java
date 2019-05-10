package com.deep3.medusLabs.model;

import org.junit.Assert;
import org.junit.Test;

public class StudentTest {

    @Test
    public void testConstructor()
    {
        Student testStudent = new Student("test-student", "test-password", "123456789");

        Assert.assertEquals(testStudent.getUsername(), "test-student");
        Assert.assertEquals(testStudent.getPassword(), "test-password");
        Assert.assertEquals(testStudent.getLoginUrl().contains("123456789"), true);
    }

    @Test
    public void testSetGetUsername()
    {
        Student testStudent = new Student("test-student", "test-password", "123456789");
        testStudent.setUsername("test-student-1");

        Assert.assertEquals(testStudent.getUsername(), "test-student-1");
    }

    @Test
    public void testSetGetPassword()
    {
        Student testStudent = new Student("test-student", "test-password", "123456789");
        testStudent.setPassword("test-password-1");

        Assert.assertEquals(testStudent.getPassword(), "test-password-1");
    }

    @Test
    public void testSetGetLoginUrl()
    {
        Student testStudent = new Student("test-student", "test-password", "123456789");
        testStudent.setLoginUrl("987654321");

        Assert.assertEquals(testStudent.getLoginUrl().contains("987654321"),true);
    }
}
