package org.jorion.trainingtool.ldap;

import lombok.extern.slf4j.Slf4j;
import org.jorion.trainingtool.type.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.stereotype.Component;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Map the LDAP attributes to the class GroupInfraIdentityDTO.
 */
@Slf4j
@Component
public class IdentityAttributeMapper implements AttributesMapper<GroupInfraIdentityDTO> {

    protected static final String EMPLOYEE_ID = "employeeId";
    @SuppressWarnings("unused")
    private static final String DEPARTMENT = "department";
    private static final String DISPLAY_NAME = "displayName";
    @SuppressWarnings("unused")
    private static final String DIVISION = "division";
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

    @Value("${app.role.manager:}")
    private String[] roleManager;

    @Value("${app.role.hr}")
    private String[] roleHr;

    @Value("${app.role.training}")
    private String[] roleTraining;

    @Value("${app.role.admin:}")
    private String[] roleAdmin;

    /**
     * A manager by definition is someone with persons reporting to him (DIRECT_REPORTS), and the reporting person value
     * must contain the string {@code OU=USERS}. If the person left, the value will be
     * {@code OU=Deactivated User Accounts}.
     *
     * @param attributes the LDAP attributes
     * @return true if the current person has at least one active person reporting to him.
     */
    private static boolean isManager(Attributes attributes) {

        boolean result = false;
        Attribute attrib = attributes.get(LdapConstants.DIRECT_REPORTS);
        if (attrib != null && attrib.size() > 0) {
            try {
                NamingEnumeration<?> values = attrib.getAll();
                while (values.hasMore()) {
                    Object obj = values.next();
                    if (obj != null) {
                        if (obj.toString().contains(LdapConstants.OU_USERS)) {
                            result = true;
                            break;
                        }
                    }
                }
            } catch (NamingException e) {
                log.error("Could not read enumeration values for [{}]: {}", LdapConstants.DIRECT_REPORTS, e.getMessage());
            }
        }
        return result;
    }

    /**
     * @param attributes the LDAP attributes
     * @param name       the attribute name you want to read
     * @return the attribute corresponding to the given name, or an empty string if not found
     */
    protected static String readAttribute(Attributes attributes, String name) {

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
    protected static String removeParenthesis(String value) {

        String newValue = value;
        int pos = value.indexOf(LEFT_PARENTHESIS);
        if (pos >= 0) {
            newValue = value.substring(0, pos).trim();
        }
        return newValue;
    }

    @Override
    public GroupInfraIdentityDTO mapFromAttributes(Attributes attribs) {

        GroupInfraIdentityDTO identity = new GroupInfraIdentityDTO();
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
        } else {
            log.info("LDAP Account does not exist");
            return null;
        }

        String subco = readAttribute(attribs, LdapConstants.SUBCO);
        identity.setSubcontractor("N".equals(subco));
        identity.setAccount(readAttribute(attribs, LdapConstants.COMMON_NAME));
        identity.setFirstname(removeParenthesis(readAttribute(attribs, LdapConstants.GIVEN_NAME)));
        identity.setLastname(readAttribute(attribs, LdapConstants.SURNAME));
        identity.setEmail(readAttribute(attribs, MAIL));
        identity.setPhone(readAttribute(attribs, TELEPHONE_NUMBER).replaceAll(" ", ""));
        identity.setMobile(readAttribute(attribs, MOBILE).replaceAll(" ", ""));
        identity.setFunction(readAttribute(attribs, TITLE));

        // filter manager account name
        // 'CN=seldonh,OU=Users,...' is converted to 'seldonh'
        String manager = readAttribute(attribs, LdapConstants.MANAGER);
        if (!manager.isEmpty()) {
            int posCn = LdapConstants.CN_EQUAL.length();
            int posComma = manager.indexOf(COMMA);
            String managerName = manager.substring(posCn, posComma);
            identity.setManagerName(managerName);
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
     * This method associates to each user a collection of roles:
     * <ul>
     * <li>The role MEMBER is always included (if you were able to log in, you're a member)
     * <li>The role MANAGER is included if the user has people reporting to him
     * <li>The role HR is included if the username belongs to the property {@code app.role.hr}
     * <li>The role TRAINING is included if the username belongs to the property {@code app.role.training}
     * <li>The role ADMIN is included if the username belongs to the property {@code app.role.admin}
     * </ul>
     *
     * @param username the current user's username
     * @param attribs  the LDAP attributes
     * @return a collection of roles corresponding to the given user
     */
    protected Set<Role> mapRoles(String username, Attributes attribs) {

        Set<Role> roles = new HashSet<>();

        // member
        roles.add(Role.MEMBER);

        // manager
        if (Arrays.asList(roleManager).contains(username) || isManager(attribs)) {
            roles.add(Role.MANAGER);
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

}
