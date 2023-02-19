package org.jorion.trainingtool.common;

import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

public class AuthenticationUtils {

    public static final String PASSWORD = "password";

    /**
     * Create a simple authentication in the SecurityContext.
     */
    public static void setAuthentication(String username) {

        List<GrantedAuthority> authorities = new ArrayList<>();
        org.springframework.security.core.userdetails.User user = new org.springframework.security.core.userdetails.User(username, PASSWORD, authorities);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * Remove the authentication from the Security Context.
     */
    public static void cleanAuthentication() {

        SecurityContextHolder.getContext().setAuthentication(null);
    }
}
