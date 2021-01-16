package org.jorion.trainingtool.config.ldap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.stereotype.Component;

import org.jorion.trainingtool.types.Role;

/**
 * Implementation of {@link LdapAuthoritiesPopulator} to customize the way to set the roles.
 */
@Component
public class CustomLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator
{
    // --- Constants ---
    private static final Logger LOG = LoggerFactory.getLogger(CustomLdapAuthoritiesPopulator.class);

    // --- Variables ---
    @Autowired
    private IdentityAttributeMapper identityAttributeMapper;

    // --- Methods ---
    /**
     * Associate for the given username a role. This is delegated to
     * {@link IdentityAttributeMapper#mapRoles(String, javax.naming.directory.Attributes)}.
     * 
     * @param userData the context object which was returned by the LDAP authenticator.
     * @param username the user who is logging in
     * @return the granted authorities for the given user (a collection with only one element)
     */
    @Override
    public Collection<? extends GrantedAuthority> getGrantedAuthorities(DirContextOperations userData, String username)
    {
        Set<Role> roles = identityAttributeMapper.mapRoles(username, userData.getAttributes());
        if (LOG.isDebugEnabled()) {
            String rolesAsStr = roles.stream().map(Role::getName).collect(Collectors.joining(":"));
            LOG.debug("Setting role for [{}]: [{}]", username, rolesAsStr);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.getRoleName()));
        }
        return authorities;
    }

}
