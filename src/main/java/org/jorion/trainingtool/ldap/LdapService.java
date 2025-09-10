package org.jorion.trainingtool.ldap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jorion.trainingtool.user.User;
import org.jorion.trainingtool.user.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LdapService {

    private final LdapTemplate ldapTemplate;
    private final IdentityAttributeMapper identityAttributeMapper;

    @Value("${app.users.ldap:true}")
    private boolean ldap;

    @Value("${ldap.basedn}")
    private String ldapBaseDn;

    /**
     * Search the LDAP to return the user whose CN (common name) equals the given username.
     *
     * @param username the username used for the search
     * @return an LDAP user with the given username, or null if not found
     */
    public User searchByName(String username) {

        if (!ldap || StringUtils.isBlank(username)) {
            return null;
        }

        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter(LdapConstants.COMMON_NAME, username));

        List<GroupInfraIdentityDTO> dtos = search(filter);
        if (dtos.isEmpty()) {
            log.warn("No user found in LDAP for [{}]", username);
            return null;
        }
        if (dtos.size() > 1) {
            log.warn("LDAP list size greater than 1. Size: [{}]", dtos.size());
        }

        GroupInfraIdentityDTO dto = dtos.getFirst();
        return UserMapper.INSTANCE.toUser(dto);
    }

    /**
     * @return A list of LDAP users corresponding to the given source user
     */
    public List<User> searchByExample(User srcUser) {

        if (!ldap) {
            return new ArrayList<>();
        }

        AndFilter filter = new AndFilter();
        if (StringUtils.isNotBlank(srcUser.getLastName())) {
            filter.and(new LikeFilter(LdapConstants.SURNAME, srcUser.getLastName().trim()));
        }
        if (StringUtils.isNotBlank(srcUser.getFirstName())) {
            filter.and(new LikeFilter(LdapConstants.GIVEN_NAME, srcUser.getFirstName().trim()));
        }

        // log.debug("LDAP filter: {}", filter.encode());
        List<GroupInfraIdentityDTO> dtos = search(filter);
        log.debug("By Example: LDAP size [{}]", dtos.size());

        List<User> users = new ArrayList<>();
        for (GroupInfraIdentityDTO dto : dtos) {
            User user = new User();
            user.setUserName(dto.getAccount());
            user.setFirstName(dto.getFirstname());
            user.setLastName(dto.getLastname());
            user.setFunction(dto.getFunction());
            users.add(user);
        }
        return users;
    }

    /**
     * Retrieve a list of users for the given manager. Subcontractor are excluded from this list.
     *
     * @return A list of LDAP users.
     */
    public List<User> searchByManager(String manager) {

        if (StringUtils.isBlank(manager)) {
            return new ArrayList<>();
        }

        AndFilter filter = new AndFilter();
        filter.and(new LikeFilter(LdapConstants.MANAGER, LdapConstants.CN_EQUAL + manager + "," + ldapBaseDn));
        // "E" means non-subco
        filter.and(new LikeFilter(LdapConstants.SUBCO, "E"));

        // log.debug("LDAP filter: {}", filter.encode());
        List<GroupInfraIdentityDTO> dtos = search(filter);
        log.debug("By Manager: LDAP size [{}]", dtos.size());

        List<User> users = new ArrayList<>();
        for (GroupInfraIdentityDTO dto : dtos) {
            User user = new User();
            user.setUserName(dto.getAccount());
            user.setSubContractor(dto.isSubcontractor());
            user.setFirstName(dto.getFirstname());
            user.setLastName(dto.getLastname());
            user.setFunction(dto.getFunction());
            users.add(user);
        }

        return users;
    }

    /**
     * Search the LDAP directory using the given filter.
     *
     * @return a list of {@link GroupInfraIdentityDTO}
     */
    private List<GroupInfraIdentityDTO> search(Filter filter) {

        List<GroupInfraIdentityDTO> dtos = new ArrayList<>();
        try {
            dtos = ldapTemplate.search("", filter.encode(), identityAttributeMapper);
        } catch (Exception e) {
            log.error(e.toString());
        }
        return dtos;
    }
}
