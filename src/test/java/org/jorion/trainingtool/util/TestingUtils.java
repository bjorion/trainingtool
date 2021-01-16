package org.jorion.trainingtool.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for the unit test.
 */
public class TestingUtils
{
    // --- Constants ---
    public static final String PASSWORD = "password";

    // --- Methods ---
    /**
     * Create a simple authentication in the SecurityContext.
     *
     * @param username the User's username
     */
    public static void setAuthentication(String username)
    {
        List<GrantedAuthority> authorities = new ArrayList<>();
        org.springframework.security.core.userdetails.User user = new org.springframework.security.core.userdetails.User(username, PASSWORD, authorities);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * Remove the authentication from the Security Context.
     */
    public static void cleanAuthentication()
    {
        SecurityContextHolder.getContext().setAuthentication(null);
    }
}
