package org.jorion.trainingtool.registration;

import org.jorion.trainingtool.type.Provider;
import org.jorion.trainingtool.user.RandomUser;
import org.jorion.trainingtool.user.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationTest {

    private static final String USERNAME = "username";

    @Test
    void testRegistration() {

        Registration reg = new Registration();
        reg.setId(1L);
        assertEquals((Long) 1L, reg.getId());
        assertNotNull(reg.toString());
    }

    @Test
    void testBelongsTo() {

        Registration reg = new Registration();
        User user = RandomUser.createUser(USERNAME);
        reg.setMember(user);

        assertFalse(reg.belongsTo(null));
        assertTrue(reg.belongsTo(USERNAME));
        assertFalse(reg.belongsTo("dummy"));
    }

    @Test
    void testConvertFrom() {

        Registration reg1 = new Registration();
        reg1.setJustification("justification1");
        reg1.setProvider(Provider.CEVORA);
        reg1.setSsin("00000000000");
        reg1.setTitle("TITLE_1");
        reg1.setStartDate(LocalDate.now());

        Registration reg2 = new Registration();
        reg2.setJustification("justification2");

        // check conversion did happen
        reg2.convertFrom(reg1);
        assertEquals("justification1", reg2.getJustification());
        assertEquals("00000000000", reg2.getSsin());
        assertEquals("TITLE_1", reg2.getTitle());
        assertEquals(LocalDate.now(), reg2.getStartDate());

        // check conversion did not happen
        reg2.setJustification("justification2");
        Registration reg3 = new Registration();
        reg3.setJustification("");
        reg3.setSsin("");
        reg2.convertFrom(reg3);
        assertEquals("justification2", reg2.getJustification());
        assertEquals("00000000000", reg2.getSsin());
    }

    @Test
    void testSsinFormatted() {

        Registration r = new Registration();
        assertEquals("", r.getSsinFormatted());
        r.setSsin("00000000000");
        assertEquals("000000-000.00", r.getSsinFormatted());
    }
}
