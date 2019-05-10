package com.deep3.medusLabs.aws.components;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloud9.AWSCloud9;
import com.amazonaws.services.lexmodelbuilding.AmazonLexModelBuilding;
import com.deep3.medusLabs.aws.components.regional.Cloud9Component;
import com.deep3.medusLabs.aws.components.regional.LexComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Util class that has a list of regions we want to create services in.
 *
 * Keep this list minimal as it will reduce the number of API calls required
 * 
 */
public class EnabledRegions {

    private static ArrayList<Regions> enabledRegions =
            new ArrayList<>( Arrays.asList(
                    Regions.EU_CENTRAL_1,
                    Regions.EU_WEST_2,
                    Regions.US_WEST_2
            ));


    private EnabledRegions(){

    }

    /**
     * Get All enabled regions as string objects
     * @return List of enabled regions
     */
    public static List<String> getEnabledRegionsAsString() {
        List<String> regions = new ArrayList<>();
        for (Regions region : enabledRegions) {
            regions.add(region.getName());
        }
        return regions;
    }

    /**
     * Get an array list of region objects
     * @param clazz - A given class that we can check if is compatible with either AWS Lex or Cloud 9 services - if so
     *  we return a list of regions in which these services are available.
     * @return ArrayList of enabled regions
     */
    public static ArrayList<Regions> getRegions(Class clazz) {
        if (LexComponent.class.isAssignableFrom(clazz)) {
            return getServiceRegions(RegionUtils.getRegionsForService(AmazonLexModelBuilding.ENDPOINT_PREFIX));
        } else if (Cloud9Component.class.isAssignableFrom(clazz)) {
            return getServiceRegions(RegionUtils.getRegionsForService(AWSCloud9.ENDPOINT_PREFIX));
        } else {
            return enabledRegions;
        }
    }

    /**
     * Get a list of regions that are compatible for a given service.
     * @param serviceRegions
     * @return
     */
    private static ArrayList<Regions> getServiceRegions(List<Region> serviceRegions) {
        ArrayList<Regions> regions = new ArrayList<>();
        for (Regions r : enabledRegions) {
            if (serviceRegions.contains(Region.getRegion(r))) {
                regions.add(r);
            }
        }
        return regions;
    }
}
