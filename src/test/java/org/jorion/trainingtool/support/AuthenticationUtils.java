package org.jorion.trainingtool.support;

import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;

public class AuthenticationUtils {

    public static final String PASSWORD = "password";

    /**
     * Create a simple authentication in the SecurityContext.
     */
    public static void setAuthentication(String username) {

        var authorities = new ArrayList<GrantedAuthority>();
        var user = new org.springframework.security.core.userdetails.User(username, PASSWORD, authorities);
        var authenticationToken = new TestingAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    /**
     * Remove the authentication from the Security Context.
     */
    public static void cleanAuthentication() {

        SecurityContextHolder.getContext().setAuthentication(null);
    }
}
