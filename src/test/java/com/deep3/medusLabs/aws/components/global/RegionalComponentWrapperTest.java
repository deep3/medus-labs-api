package com.deep3.medusLabs.aws.components.global;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.deep3.medusLabs.aws.components.AWSRegionalServiceComponentInterface;
import com.deep3.medusLabs.aws.components.EnabledRegions;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class RegionalComponentWrapperTest {

    @Mock
    private STSAssumeRoleSessionCredentialsProvider credentials;

    public class MockBadClass {}

    public class MockBadClassConstructor implements AWSRegionalServiceComponentInterface{
        @Override
        public void deleteAll(long deployedLabId) {}
    }

    @Test
    public void testConstructor() throws InvalidClassException {

        RegionalComponentWrapper test = new RegionalComponentWrapper(TestMockGoodClass.class, credentials);
        Assert.assertNotNull("Could not construct RegionalComponentWrapper class", test);
    }

    @Test(expected = InvalidClassException.class)
    public void testInvalidComponentClass() throws InvalidClassException {
        new RegionalComponentWrapper(MockBadClass.class, credentials);
    }

    @Test(expected = InvalidClassException.class)
    public void testInvalidComponentConstructor() throws InvalidClassException {
        new RegionalComponentWrapper(MockBadClassConstructor.class, credentials);
    }

    @Test
    public void testGetRegionalService() throws InvalidClassException {
        RegionalComponentWrapper test = new RegionalComponentWrapper(TestMockGoodClass.class, credentials);
        AWSRegionalServiceComponentInterface result = test.getRegionalService(EnabledRegions.getRegions(TestMockGoodClass.class).get(0));
        Assert.assertTrue(result.getClass().equals(TestMockGoodClass.class));

    }


    @Test
    public void testDeleteAll() throws Exception {

        RegionalComponentWrapper test = new RegionalComponentWrapper(TestMockGoodClass.class, credentials);


        TestMockGoodClass mockedGoodClass = PowerMockito.mock(TestMockGoodClass.class);
        ArrayList<Regions> regions = EnabledRegions.getRegions(TestMockGoodClass.class);
        HashMap<Regions,AWSRegionalServiceComponentInterface> regionalServices = new HashMap<>();

        for(Regions region : regions) {
            regionalServices.put(region, mockedGoodClass);
        }

        FieldUtils.writeField(test,"regionalServices", regionalServices,true);
        test.deleteAll(any(Long.class));
        verify(mockedGoodClass,times(regions.size())).deleteAll(any(Long.class));


    }
}
