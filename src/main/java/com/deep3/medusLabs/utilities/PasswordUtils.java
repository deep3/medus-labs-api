package com.deep3.medusLabs.utilities;

import org.apache.commons.lang3.RandomStringUtils;

public class PasswordUtils {

    private PasswordUtils() {
        throw new IllegalStateException("Static Utility class");
    }

    /***
     * Generate a random password
     * @return A randomly generated password with length of 10(Default).
     */
    public static String generatePassword()
    {
        return generatePassword(10);
    }

    /***
     * Generate a random password
     * @param length - The length of the password to be generated
     * @return - A randomly generated password of the given length
     */
    public static String generatePassword(int length) {
        return RandomStringUtils.random( length, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#%&=+?" );
    }
}
