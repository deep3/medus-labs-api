package com.deep3.medusLabs.utilities;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PasswordComplexityValidator implements ConstraintValidator<PasswordComplexityConstraint, String> {

    private static int MIN_LENGTH = 8;

    @Override
    public void initialize(PasswordComplexityConstraint password)
    {}

    /**
     * Check if the supplied password meets the defined complexity
     * @param password - The password supplied
     * @param context - The current ConstraintValidatorContext
     * @return True if valid, false if invalid
     */
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {

        // Patterns that password must comply with
        List<Pattern> patterns = new ArrayList<>();
        patterns.add(Pattern.compile("[^A-Za-z0-9]")); // >= 1 Symbol (Not 'a-z' or 'A-Z' or '0-9')
        patterns.add(Pattern.compile(".*[A-Z].*")); // >= 1 Upper case

        for (Pattern pattern : patterns) {
            if (pattern.matcher(password).find()) {
                continue;
            }
            else {
                return false;
            }
        }

        // Check Minimum length
        return password.length() >= MIN_LENGTH;
    }
}
