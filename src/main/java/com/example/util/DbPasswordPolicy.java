package com.example.util;

import java.util.regex.Pattern;

/**
 * Database passwords are embedded by the agent into SQL run on customer databases,
 * so only characters that cannot break out of a quoted SQL literal are allowed.
 */
public final class DbPasswordPolicy {
    public static final String ALLOWED = "6-64 characters: letters, digits and ! @ # % ^ & * ( ) _ + = . , : ? ~ -";
    private static final Pattern PATTERN = Pattern.compile("[A-Za-z0-9!@#%^&*()_+=.,:?~-]{6,64}");

    private DbPasswordPolicy() {
    }

    public static boolean isSafe(String password) {
        return password != null && PATTERN.matcher(password).matches();
    }
}
