package com.deep3.medusLabs.utilities;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;

public class EmailUtils {
    private EmailUtils() { throw new IllegalStateException("Static Utility class"); }

    /**
     * Generate a number of email Aliases for the provided address
     * @param qty - the number of aliases required
     * @param email - The email address to create an alias for
     * @return - A List of String objects representing the created aliases
     */
    public static List<String> generateEmailAliases(int qty, String email){
        ArrayList<String> list = new ArrayList<>();

        int at = email.indexOf('@');
        for(int i=0; i < qty; i++){
            list.add(email.substring(0,at) + "+" + RandomStringUtils.randomAlphabetic(5).toUpperCase() + email.substring(at));
        }
        return list;
    }
}
