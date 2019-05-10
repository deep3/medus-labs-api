package com.deep3.medusLabs.model;

import com.amazonaws.services.cloudformation.model.Parameter;
import org.junit.Assert;
import org.junit.Test;

public class PrivateParameterTests {

    @Test
    public void testConstructor(){

        PrivateParameter test1 = new PrivateParameter();
        PrivateParameter test2 = new PrivateParameter("param-name", "param-value", "param-type", "param-desc");
        Assert.assertEquals("param-name",test2.getParamName());
        Assert.assertEquals("param-type",test2.getParamType());
        Assert.assertEquals("param-desc",test2.getParamDescription());
        Assert.assertEquals("param-value",test2.getParamValue());
    }

    @Test
    public void testGetSetId(){
        PrivateParameter test = new PrivateParameter();

        test.setId(1235123L);
        Assert.assertEquals((Long) 1235123L,test.getId());

    }

    @Test
    public void testGetSetParamName(){
        PrivateParameter test = new PrivateParameter();

        test.setParamName("test-name");
        Assert.assertEquals("test-name",test.getParamName());

    }

    @Test
    public void testGetSetParamValue(){
        PrivateParameter test = new PrivateParameter();

        test.setParamValue("test-value");
        Assert.assertEquals("test-value",test.getParamValue());

    }

    @Test
    public void testGetSetParamType(){
        PrivateParameter test = new PrivateParameter();

        test.setParamType("test-type");
        Assert.assertEquals("test-type",test.getParamType());

    }

    @Test
    public void testGetSetParamDescription(){
        PrivateParameter test = new PrivateParameter();

        test.setParamDescription("test-description");
        Assert.assertEquals("test-description",test.getParamDescription());

    }

    @Test
    public void testGetAsParameter(){
        PrivateParameter test = new PrivateParameter("param-name", "param-value", "param-type", "param-desc");
        Parameter result = test.getAsParameter();

        Assert.assertEquals(result.getParameterKey(),"param-name");
        Assert.assertEquals(result.getParameterValue(),"param-value");
    }
}
