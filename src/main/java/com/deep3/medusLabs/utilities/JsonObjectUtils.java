package com.deep3.medusLabs.utilities;

import com.amazonaws.services.cloudformation.model.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonObjectUtils {

    private static final Logger LOG = LoggerFactory.getLogger(JsonObjectUtils.class);

    private JsonObjectUtils() { throw new IllegalStateException("Static Utility class"); }

    /**
     * Parse a JSON object containing parameters and return a List<Parameter>()
     * @param jsonMap - A Hash-map of json object parameters
     * @return - A List of Parameters.
     */
    public static List<Parameter> getParametersFromJson(Map jsonMap) {
        List<Parameter> results = new ArrayList<>();
        Iterator mapIt = jsonMap.entrySet().iterator();
        try {
            while (mapIt.hasNext()) {
                Map.Entry parameterPair = (Map.Entry)mapIt.next();
                results.add(new com.amazonaws.services.cloudformation.model.Parameter()
                        .withParameterKey(String.valueOf(parameterPair.getKey()))
                        .withParameterValue(String.valueOf(parameterPair.getValue())));
                mapIt.remove();
            }
        }
        catch(Exception e) {
            LOG.error("Failed to load Parameters from supplied JSON [" + e.getMessage() + "]");
        }

        return results;
    }
}
