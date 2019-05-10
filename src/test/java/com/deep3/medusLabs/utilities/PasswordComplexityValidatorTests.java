package com.deep3.medusLabs.utilities;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class PasswordComplexityValidatorTests {

    @Mock
    PasswordComplexityConstraint passwordComplexityConstraint;
    @Test
    public void passwordComplexityTest(){
        PasswordComplexityValidator testedValidator = new PasswordComplexityValidator();

        Assert.assertFalse(testedValidator.isValid("NoValid",null));
        Assert.assertFalse(testedValidator.isValid("A!",null));
        Assert.assertTrue(testedValidator.isValid("A!TestReallyGoodPassw0rd",null));
    }

    @Test
    public void passwordComplexityInit(){
        PasswordComplexityValidator testedValidator = new PasswordComplexityValidator();

        testedValidator.initialize(passwordComplexityConstraint);
    }
}
