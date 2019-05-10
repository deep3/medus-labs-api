package com.deep3.medusLabs.aws.components.global;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.deep3.medusLabs.aws.components.AWSGlobalServiceComponentInterface;
import com.deep3.medusLabs.aws.components.AWSRegionalServiceComponentInterface;
import com.deep3.medusLabs.aws.components.EnabledRegions;

import java.io.InvalidClassException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Takes a regional component and makes it global!
 */
public class RegionalComponentWrapper implements AWSGlobalServiceComponentInterface {

    private HashMap<Regions,AWSRegionalServiceComponentInterface> regionalServices = new HashMap<>();

    /**
     * Constructor for Regional Component Wrapper
     * @param clazz - generic class object
     * @param credentials - STS credentials
     * @throws InvalidClassException
     */
    public RegionalComponentWrapper(Class clazz, STSAssumeRoleSessionCredentialsProvider credentials) throws InvalidClassException {

        if(!AWSRegionalServiceComponentInterface.class.isAssignableFrom(clazz)) {
            throw new InvalidClassException("Cannot wrap class that does not implement AWSRegionalServiceComponentInterface");
        }

        ArrayList<Regions> regions = EnabledRegions.getRegions(clazz);

        try {
            Constructor c = clazz.getConstructor(STSAssumeRoleSessionCredentialsProvider.class, Regions.class);
            for(Regions region : regions) {
                regionalServices.put(region, (AWSRegionalServiceComponentInterface) c.newInstance(credentials, region));
            }
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new InvalidClassException("Problems Instantiating expected constructor, did not expect " + clazz.getName() + "(STSAssumeRoleSessionCredentailsProvider, Regions)");
        }
    }

    /**
     * Get the service for a given region
     * @param region - the given region against which we check service availability
     * @return Regional Service Component Interface object
     */
    public AWSRegionalServiceComponentInterface getRegionalService(Regions region) {
        return regionalServices.get(region);
    }

    /**
     * Delete all regional services in a given lab
     * @param deployedLabId
     */
    @Override
    public void deleteAll(long deployedLabId) {
        regionalServices.values().stream().forEach(service -> service.deleteAll(deployedLabId));
    }
}
