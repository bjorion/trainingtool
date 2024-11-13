package org.jorion.trainingtool.registration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegistrationMapperTest {

    private static final RegistrationMapper MAPPER = RegistrationMapper.INSTANCE;

    @Test
    void testToRegistrationDTO() {

        Registration r = new Registration();
        r.setDescription("some description");
        RegistrationDTO dto = MAPPER.toRegistrationDTO(r);
        assertEquals(r.getDescription(), dto.getDescription());
    }
}
