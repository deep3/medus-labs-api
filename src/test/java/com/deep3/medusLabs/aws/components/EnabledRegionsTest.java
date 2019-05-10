package com.deep3.medusLabs.aws.components;

import com.amazonaws.regions.Regions;
import com.deep3.medusLabs.aws.components.regional.Cloud9Component;
import com.deep3.medusLabs.aws.components.regional.EC2Component;
import com.deep3.medusLabs.aws.components.regional.LexComponent;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class EnabledRegionsTest {

    @Test
    public void testConstructor(){

    }

    @Test
    public void testGetRegions(){
        ArrayList<Regions> regions = EnabledRegions.getRegions(EC2Component.class);
        Assert.assertNotNull("Enabled region list returned null, expected ArrayList",regions);
        Assert.assertNotSame("Enabled region list is empty",0,regions.size());

        ArrayList<Regions> regions2 = EnabledRegions.getRegions(LexComponent.class);
        Assert.assertNotNull("Enabled region list returned null, expected ArrayList",regions2);
        Assert.assertNotSame("Enabled region list is empty",0,regions2.size());

        ArrayList<Regions> regions3 = EnabledRegions.getRegions(Cloud9Component.class);
        Assert.assertNotNull("Enabled region list returned null, expected ArrayList",regions3);
        Assert.assertNotSame("Enabled region list is empty",0,regions3.size());
    }

}