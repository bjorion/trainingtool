package org.jorion.trainingtool.config.ldap;

import lombok.extern.slf4j.Slf4j;
import org.jorion.trainingtool.type.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link LdapAuthoritiesPopulator} to customize the way to set the roles.
 */
@Slf4j
@Component
public class CustomLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator {

    @Autowired
    private IdentityAttributeMapper identityAttributeMapper;

    /**
     * Associate for the given username a role. This is delegated to
     * {@link IdentityAttributeMapper#mapRoles(String, javax.naming.directory.Attributes)}.
     *
     * @param userData the context object which was returned by the LDAP authenticator.
     * @param username the user who is logging in
     * @return the granted authorities for the given user (a collection with only one element)
     */
    @Override
    public Collection<? extends GrantedAuthority> getGrantedAuthorities(DirContextOperations userData, String username) {

        Set<Role> roles = identityAttributeMapper.mapRoles(username, userData.getAttributes());
        if (log.isDebugEnabled()) {
            String rolesAsStr = roles.stream().map(Role::getName).collect(Collectors.joining(":"));
            log.debug("Setting role for [{}]: [{}]", username, rolesAsStr);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.getRoleName()));
        }
        return authorities;
    }

}
