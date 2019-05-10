package com.deep3.medusLabs.aws.utilities;


import com.amazonaws.util.EC2MetadataUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

@RunWith(PowerMockRunner.class)
@PrepareForTest({EC2MetadataUtils.class})
public class AWSMetadataUtilsTests {

    @Mock
    EC2MetadataUtils.IAMInfo iamInfo;

    @Before
    public void initialize()
    {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(EC2MetadataUtils.class);
    }

    @Test(expected = IllegalStateException.class)
    public void testPrivateConstructor() throws Throwable {

        Constructor[] constructors = AWSMetadataUtils.class.getDeclaredConstructors();
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
    public void testRoleArn(){
        Mockito.when(EC2MetadataUtils.getIAMInstanceProfileInfo()).thenReturn(iamInfo);
        iamInfo.instanceProfileArn = "myTest";
        String result = AWSMetadataUtils.getRoleArn();
        Assert.assertEquals("Incorrect Role from AWSMetadataUtils",iamInfo.instanceProfileArn,result);

    }

    @Test
    public void testInstanceId(){
        String testInstanceId =  "testInstanceId";
        Mockito.when(EC2MetadataUtils.getInstanceId()).thenReturn(testInstanceId);
        String result = AWSMetadataUtils.getInstanceId();
        Assert.assertEquals("Incorrect InstanceId from AWSMetadataUtils",testInstanceId,result);

    }
}
