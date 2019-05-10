package com.deep3.medusLabs.aws.exceptions;

import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Thrown when an entity can not be found with the supplied parameters
 */
public class ObjectNotFoundException extends Exception {

    public ObjectNotFoundException(Class c, String... searchParamsMap) {
        super(ObjectNotFoundException.composeMessage(c.getSimpleName(), toParamMap(String.class, String.class, (Object[])searchParamsMap)));
    }

    private static String composeMessage(String entityName, Map<String, String> searchParams) {
        return StringUtils.capitalize(entityName) +
                " could not be found with supplied parameters " +
                searchParams; // This is a map of Key/Value pairs
    }

    /**
     * Function to create a Map from the supplied parameters
     * @param keyType - The TYPE of the Key (usually String)
     * @param valueType  - The TYPE of the Value
     * @param entries - The entries that need to be used to create this Map
     * @return - A Map made up of 'Key/Pair' objects from the parameters supplied
     */
    private static <K, V> Map<K, V> toParamMap(
            Class<K> keyType, Class<V> valueType, Object... entries)
    {
        if (entries.length % 2 == 1)
        {
            throw new IllegalArgumentException("Invalid number of entries - must be multiple of 2");
        }

        return IntStream
                .range(0, entries.length / 2)
                .map(i -> i * 2)
                .collect(HashMap::new,
                        (map, i) -> map.put(keyType.cast(entries[i]), valueType.cast(entries[i + 1])),
                        Map::putAll);
    }
}