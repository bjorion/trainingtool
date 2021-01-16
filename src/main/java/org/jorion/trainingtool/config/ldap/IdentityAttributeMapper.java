package org.jorion.trainingtool.config.ldap;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.stereotype.Component;

import org.jorion.trainingtool.common.Constants;
import org.jorion.trainingtool.dto.GroupinfraIdentityDTO;
import org.jorion.trainingtool.types.Role;

/**
 * Map the LDAP attributes to the class GroupinfraIdentityDTO.
 */
@Component
public class IdentityAttributeMapper implements AttributesMapper<GroupinfraIdentityDTO>
{
    // --- Constants ---
    private static final Logger LOG = LoggerFactory.getLogger(IdentityAttributeMapper.class);

    // LDAP Attributes
    @SuppressWarnings("unused")
    private static final String DEPARTMENT = "department";

    private static final String DISPLAY_NAME = "displayName";

    @SuppressWarnings("unused")
    private static final String DIVISION = "division";

    private static final String EMPLOYEE_ID = "employeeId";

    private static final String EMPLOYEE_ID_FY2019 = "extensionAttribute2";

    private static final String EMPLOYEE_TYPE = "employeeType";

    private static final String MAIL = "mail";

    private static final String MOBILE = "mobile";

    private static final String TELEPHONE_NUMBER = "telephoneNumber";

    private static final String TITLE = "title";

    // Various characters
    private static final String COMMA = ",";

    private static final String LEFT_PARENTHESIS = "(";

    @SuppressWarnings("unused")
    private static final String RIGHT_PARENTHESIS = ")";

    // --- Variables ---
    @Value("${app.role.manager:}")
    private String[] roleManager;

    @Value("${app.role.hr}")
    private String[] roleHr;

    @Value("${app.role.training}")
    private String[] roleTraining;

    @Value("${app.role.admin:}")
    private String[] roleAdmin;

    // --- Methods ---
    public IdentityAttributeMapper()
    {}

    /**
     * {@inheritDoc}
     */
    @Override
    public GroupinfraIdentityDTO mapFromAttributes(Attributes attribs)
    {
        GroupinfraIdentityDTO identity = new GroupinfraIdentityDTO();
        String employeeId = readAttribute(attribs, EMPLOYEE_ID);
        String employeeId2019 = readAttribute(attribs, EMPLOYEE_ID_FY2019);

        // Old accounts (is it still relevant?)
        if (!employeeId.isEmpty() && !"0".equals(employeeId)) {
            identity.setPnr(employeeId);
            identity.setEmployeeType(readAttribute(attribs, EMPLOYEE_TYPE));
            // identity.setDivision(readAttribute(attribs, DIVISION));
        }
        // Accounts created starting FY2019
        // If found, it means the account is active
        else if (!employeeId2019.isEmpty() && !"0".equals(employeeId)) {
            identity.setEmployeeType("active"); // use enum
            identity.setPnr(readAttribute(attribs, EMPLOYEE_ID_FY2019));
            // identity.setDivision(readAttribute(attribs, DEPARTMENT));
        }
        else {
            LOG.info("LDAP Account does not exist");
            return null;
        }

        String subco = readAttribute(attribs, Constants.SUBCO);
        identity.setSubcontractor("N".equals(subco));
        identity.setAccount(readAttribute(attribs, Constants.COMMON_NAME));
        identity.setFirstname(removeParenthesis(readAttribute(attribs, Constants.GIVEN_NAME)));
        identity.setLastname(readAttribute(attribs, Constants.SURNAME));
        identity.setEmail(readAttribute(attribs, MAIL));
        identity.setPhone(readAttribute(attribs, TELEPHONE_NUMBER).replaceAll(" ", ""));
        identity.setMobile(readAttribute(attribs, MOBILE).replaceAll(" ", ""));
        identity.setFunction(readAttribute(attribs, TITLE));

        // filter manager account name
        // 'CN=seldonh,OU=Users,...' is converted to 'seldonh'
        String manager = readAttribute(attribs, Constants.MANAGER);
        if (!manager.isEmpty()) {
            int posCn = Constants.CN_EQUAL.length();
            int posComma = manager.indexOf(COMMA);
            String managerName = manager.substring(posCn, posComma);
            identity.setManagername(managerName);
        }

        // override firstname/lastname
        String displayName = removeParenthesis(readAttribute(attribs, DISPLAY_NAME));
        int posComma = displayName.indexOf(", ");
        String fullname = identity.getLastname() + ", " + identity.getFirstname();
        if (posComma >= 0 && !displayName.equals(fullname)) {
            identity.setLastname(displayName.substring(0, posComma));
            identity.setFirstname(displayName.substring(posComma + 2));
        }

        // roles
        identity.addRoles(mapRoles(identity.getAccount(), attribs));

        return identity;
    }

    /**
     * This method associate to each user a collection of roles:
     * <ul>
     * <li>The role MEMBER is always included (if you were able to log in, you're a member)
     * <li>The role MANAGER is included if the user has people reporting to him
     * <li>The role HR is included if the username belongs to the property {@code app.role.hr}
     * <li>The role TRAINING is included if the username belongs to the property {@code app.role.training}
     * <li>The role ADMIN is included if the username belongs to the property {@code app.role.admin}
     * </ul>
     * 
     * @param username the current user's username
     * @param attribs the LDAP attributes
     * @return a collection of roles corresponding to the given user
     */
    protected Set<Role> mapRoles(String username, Attributes attribs)
    {
        Set<Role> roles = new HashSet<>();

        // member
        roles.add(Role.MEMBER);

        // manager
        if (Arrays.asList(roleManager).contains(username)) {
            roles.add(Role.MANAGER);
        }
        else {
            Attribute attrib = attribs.get(Constants.DIRECT_REPORTS);
            if (attrib != null && attrib.size() > 0) {
                roles.add(Role.MANAGER);
            }
        }
        // HR
        if (Arrays.asList(roleHr).contains(username)) {
            roles.add(Role.HR);
        }
        // Training
        if (Arrays.asList(roleTraining).contains(username)) {
            roles.add(Role.TRAINING);
        }
        // Admin
        if (Arrays.asList(roleAdmin).contains(username)) {
            roles.add(Role.ADMIN);
        }
        return roles;
    }

    /**
     * @param attributes a list of attributes
     * @param name the attribute name you want to read
     * @return the attribute corresponding to the given name, or an empty string if not found
     */
    protected static String readAttribute(Attributes attributes, String name)
    {
        if (attributes.get(name) == null) {
            return "";
        }

        String attr = attributes.get(name).toString();
        attr = attr.substring(name.length() + 1).trim();
        return attr;
    }

    /**
     * Remove training text between parenthesis.
     */
    protected static String removeParenthesis(String value)
    {
        String newValue = value;
        int pos = value.indexOf(LEFT_PARENTHESIS);
        if (pos >= 0) {
            newValue = value.substring(0, pos).trim();
        }
        return newValue;
    }

}
