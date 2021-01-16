package org.jorion.trainingtool.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.stereotype.Service;

import org.jorion.trainingtool.common.Constants;
import org.jorion.trainingtool.config.ldap.IdentityAttributeMapper;
import org.jorion.trainingtool.dto.GroupinfraIdentityDTO;
import org.jorion.trainingtool.entities.User;

/**
 * LDAP Service
 */
@Service
public class LdapService
{
    // --- Constants ---
    private static final Logger LOG = LoggerFactory.getLogger(LdapService.class);

    // --- Variables ---
    @Value("${app.users.ldap:true}")
    private boolean ldap;

    @Value("${ldap.basedn}")
    private String ldapBaseDn;

    @Autowired
    private LdapTemplate ldapTemplate;

    @Autowired
    private IdentityAttributeMapper identityAttributeMapper;

    // --- Methods ---
    /**
     * Search the LDAP to return the user whose CN (common name) equals the given username.
     * 
     * @param username the username used for the search
     * @return a LDAP user with the given username, or null if not found
     */
    public User searchByName(String username)
    {
        if (!ldap || StringUtils.isBlank(username)) {
            return null;
        }

        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter(Constants.COMMON_NAME, username));

        List<GroupinfraIdentityDTO> dtos = search(filter);
        if (dtos.isEmpty()) {
            LOG.warn("No user found in LDAP for [{}]", username);
            return null;
        }
        if (dtos.size() > 1) {
            LOG.warn("LDAP list size greater than 1. Size: [{}]", dtos.size());
        }

        GroupinfraIdentityDTO dto = dtos.get(0);
        User user = new User();
        user.setSubContractor(dto.isSubcontractor());
        user.setPnr(dto.getPnr());
        user.setUsername(dto.getAccount());
        user.setRoles(dto.getRoles());
        user.setLastname(dto.getLastname());
        user.setFirstname(dto.getFirstname());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getMobileOrPhone());
        user.setFunction(dto.getFunction());
        user.setManagername(dto.getManagername());

        return user;
    }

    /**
     * @return A list of LDAP users corresponding to the given source user
     */
    public List<User> searchByExample(User srcUser)
    {
        if (!ldap) {
            return new ArrayList<>();
        }

        AndFilter filter = new AndFilter();
        if (StringUtils.isNotBlank(srcUser.getLastname())) {
            filter.and(new LikeFilter(Constants.SURNAME, srcUser.getLastname().trim()));
        }
        if (StringUtils.isNotBlank(srcUser.getFirstname())) {
            filter.and(new LikeFilter(Constants.GIVEN_NAME, srcUser.getFirstname().trim()));
        }

        // LOG.debug("LDAP filter: {}", filter.encode());
        List<GroupinfraIdentityDTO> dtos = search(filter);
        LOG.debug("By Example: LDAP size [{}]", dtos.size());

        List<User> users = new ArrayList<>();
        for (GroupinfraIdentityDTO dto : dtos) {
            User user = new User();
            user.setUsername(dto.getAccount());
            user.setFirstname(dto.getFirstname());
            user.setLastname(dto.getLastname());
            user.setFunction(dto.getFunction());
            users.add(user);
        }
        return users;
    }

    /**
     * Retrieve a list of users for the given manager. Sub-contractor are excluded from this list.
     * 
     * @return A list of LDAP users.
     */
    public List<User> searchByManager(String manager)
    {
        if (StringUtils.isBlank(manager)) {
            return new ArrayList<>();
        }

        AndFilter filter = new AndFilter();
        filter.and(new LikeFilter(Constants.MANAGER, Constants.CN_EQUAL + manager + "," + ldapBaseDn));
        // "E" means non-subco
        filter.and(new LikeFilter(Constants.SUBCO, "E"));

        // LOG.debug("LDAP filter: {}", filter.encode());
        List<GroupinfraIdentityDTO> dtos = search(filter);
        LOG.debug("By Manager: LDAP size [{}]", dtos.size());

        List<User> users = new ArrayList<>();
        for (GroupinfraIdentityDTO dto : dtos) {
            User user = new User();
            user.setUsername(dto.getAccount());
            user.setSubContractor(dto.isSubcontractor());
            user.setFirstname(dto.getFirstname());
            user.setLastname(dto.getLastname());
            user.setFunction(dto.getFunction());
            users.add(user);
        }
        return users;
    }

    /**
     * Search the LDAP directory using the given filter.
     * 
     * @return a list of {@link GroupinfraIdentityDTO}
     */
    private List<GroupinfraIdentityDTO> search(Filter filter)
    {
        List<GroupinfraIdentityDTO> dtos = new ArrayList<>();
        try {
            dtos = ldapTemplate.search("", filter.encode(), identityAttributeMapper);
        }
        catch (Exception e) {
            LOG.error(e.toString());
        }
        return dtos;
    }
}
