package com.deep3.medusLabs.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DeployedLabModelTest {

    @Test
    public void testConstructorSimple() {

       DeployedLab testLab = new DeployedLab();
       Assert.assertNotNull(testLab.getDeployed());
    }

    @Test
    public void testConstructor() {

        List<String> accounts = new ArrayList<>();
        accounts.add("123456789");
        accounts.add("987654321");

        Lab testLab = new Lab("test-name", "test-template-url");

        DeployedLab lab = new DeployedLab(testLab, accounts );

        Assert.assertNotNull(lab);
        Assert.assertNotNull(lab.getDeployed());
        Assert.assertEquals(lab.getLab().getName(), "test-name");
        Assert.assertEquals(lab.getLab().getTemplateUrl(), "test-template-url");
        Assert.assertNotNull(lab.getLabAccounts());
        Assert.assertEquals(2, lab.getLabAccounts().size());
    }

    @Test
    public void testSetGetId(){

        DeployedLab testLab = new DeployedLab();
        Long id = 1001l;
        testLab.setId(id);
        Long response = testLab.getId();
        Assert.assertEquals(response, id);
    }

    @Test
    public void testSetGetLab() {

        DeployedLab testLab = new DeployedLab();
        Lab lab = new Lab("test-name", "test-template-url");

        testLab.setLab(lab);

        Lab response = testLab.getLab();

        Assert.assertEquals(response, lab);
    }

    @Test
    public void testSetGetUndeployed() {

        DeployedLab testLab = new DeployedLab();
        Date thisDate = new Date();
        testLab.setUndeployed(thisDate);

        Date response = testLab.getUndeployed();
        Assert.assertEquals(response, thisDate);
    }

    @Test
    public void testSetGetDeployed() {

        DeployedLab testLab = new DeployedLab();
        Date thisDate = new Date();
        testLab.setDeployed(thisDate);

        Date response = testLab.getDeployed();
        Assert.assertEquals(response, thisDate);
    }

    @Test
    public void testSetGetLabAccounts() {

        DeployedLab testLab = new DeployedLab();

        List<String> accounts = new ArrayList<>();
        accounts.add("123456789");
        accounts.add("987654321");

        testLab.setLabAccounts(accounts);
        List<String> response = testLab.getLabAccounts();

        Assert.assertNotNull(testLab.getLabAccounts());
        Assert.assertEquals(testLab.getLabAccounts().size(), 2);
        Assert.assertEquals(testLab.getLabAccounts().get(0), "123456789");
        Assert.assertEquals(testLab.getLabAccounts().get(1), "987654321");
    }
}

