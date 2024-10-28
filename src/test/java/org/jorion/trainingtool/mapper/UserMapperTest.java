package org.jorion.trainingtool.mapper;

import org.jorion.trainingtool.dto.json.UserDTO;
import org.jorion.trainingtool.entity.User;
import org.jorion.trainingtool.type.Role;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class UserMapperTest {

    private static final String USERNAME = "Alice";

    @Test
    void testToUserDTO() {

        User user = User.builder().userName(USERNAME).build();
        UserDTO dto = UserMapper.INSTANCE.toUserDTO(user);
        assertEquals(user.getUserName(), dto.getUserName());
    }

    @Test
    void testToUser() {

        UserDTO dto = new UserDTO();
        dto.setUserName(USERNAME);
        User user = UserMapper.INSTANCE.toUser(dto);
        assertEquals(dto.getUserName(), user.getUserName());
    }

    @Test
    void testUpdateUserFromLdap() {

        User source = new User();
        source.setId(1L);
        source.setPnr("1-pnr");
        source.setUserName("1-username");
        source.setSector("1-sector");
        source.setRegistrations(null);
        source.setSubContractor(false);
        source.addRole(Role.MEMBER).addRole(Role.MANAGER);
        source.setLastName("1-lastname");
        source.setFirstName("1-firstname");
        source.setEmail("1-email");
        source.setPhoneNumber("1-phonenumber");
        source.setFunction("1-function");
        source.setManagerName("1-managername");

        User target = new User();
        target.setId(2L);
        target.setPnr("2-pnr");
        target.setUserName("2-username");
        target.setSector("2-sector");
        target.setRegistrations(new HashSet<>());
        target.setSubContractor(true);
        target.addRole(Role.MEMBER);
        target.setLastName("2-lastname");
        target.setFirstName("2-firstname");
        target.setEmail("2-email");
        target.setPhoneNumber("2-phonenumber");
        target.setFunction("2-function");
        target.setManagerName("2-managername");

        UserMapper.INSTANCE.updateUserFromLdap(source, target);

        // not modified because they identify the user
        assertEquals(2L, target.getId());
        assertEquals("2-pnr", target.getPnr());
        assertEquals("2-username", target.getUserName());

        // not modified because the information is not present in LDAP
        assertEquals("2-sector", target.getSector());
        assertNotNull(target.getRegistrations());

        // modified
        assertFalse(target.isSubContractor());
        assertEquals("1-lastname", target.getLastName());
        assertEquals("1-firstname", target.getFirstName());
        assertEquals("1-email", target.getEmail());
        assertEquals("1-phonenumber", target.getPhoneNumber());
        assertEquals("1-function", target.getFunction());
        assertEquals("1-managername", target.getManagerName());

        Role[] sourceRoles = source.getRoles().toArray(new Role[0]);
        Role[] targetRoles = target.getRoles().toArray(new Role[0]);
        assertArrayEquals(sourceRoles, targetRoles);
    }
}
