package com.deep3.medusLabs.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.List;

public class LabModelTest {

    @Test
    public void testConstructor() {

        Lab testLab = new Lab("test-name", "test-template-url");

        Assert.assertNotNull(testLab);
        Assert.assertEquals(testLab.getName(), "test-name");
        Assert.assertEquals(testLab.getTemplateUrl(), "test-template-url");
    }

    @Test
    public void testSetGetId(){

        Lab testLab = new Lab();

        Long id = 1001l;
        testLab.setId(id);
        Long response = testLab.getId();

        Assert.assertEquals(response, id);
    }

    @Test
    public void testSetGetName() {

        Lab testLab = new Lab();

        testLab.setName("test-name");

        Assert.assertEquals(testLab.getName(), "test-name");
    }

    @Test
    public void testSetGetStatus() {

        Lab testLab = new Lab();

        testLab.setStatus("test-status");

        Assert.assertEquals(testLab.getStatus(), "test-status");
    }

    @Test
    public void testSetGetDateCreated() {

        Date testDate = new Date();
        Lab testLab = new Lab();

        testLab.setCreated(testDate);

        Assert.assertEquals(testLab.getCreated(), testDate);
    }

    @Test
    public void testSetGetLastDeployed() {

        Date testDate = new Date();
        Lab testLab = new Lab();

        testLab.setLastDeployed(testDate);

        Assert.assertEquals(testLab.getLastDeployed(), testDate);

    }

    @Test
    public void testSetGetUser() {

        User testUser = new User("test-name", "test-password");
        Lab testLab = new Lab();

        testLab.setUser(testUser);

        Assert.assertEquals(testLab.getUser(), testUser);

    }

    @Test
    public void testSetGetDescription() {
        Lab testLab = new Lab();

        testLab.setDescription("test-description");

        Assert.assertEquals(testLab.getDescription(), "test-description");
    }

    @Test
    public void testAddParameter(){
        Lab testLab = new Lab();
        PrivateParameter testParam = new PrivateParameter("param-name", "param-value", "param-type", "param-desc");
        testLab.addPrivateParameter(testParam);
        List<PrivateParameter> params = testLab.getPrivateParameters();

        Assert.assertTrue(params.contains(testParam));
    }
}
