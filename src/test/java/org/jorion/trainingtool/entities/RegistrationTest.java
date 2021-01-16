package org.jorion.trainingtool.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.Test;

import org.jorion.trainingtool.types.Provider;

/**
 * Unit test for {@link Registration}.
 */
public class RegistrationTest
{
	// --- Constants ---
	private static final String USERNAME = "username";
	
	// --- Methods ---
	@Test
	public void testRegistration()
	{
	    Registration reg = new Registration();
	    reg.setId(1L);
	    assertEquals((Long)1L, reg.getId());
	    assertNotNull(reg.toString());
	}
	
	@Test
	public void testBelongsTo()
	{
		Registration reg = new Registration();
		User user = new User(USERNAME);
		reg.setMember(user);
		
		assertFalse(reg.belongsTo(null));
		assertTrue(reg.belongsTo(USERNAME));
		assertFalse(reg.belongsTo("dummy"));
	}

	@Test
	public void testConvertFrom()
	{
		Registration reg1 = new Registration();
		reg1.setMotivation("motivation1");
		reg1.setProvider(Provider.CEVORA);
		reg1.setSsin("00000000000");
		reg1.setTitle("TITLE_1");
		reg1.setStartDate(LocalDate.now());
		
		Registration reg2 = new Registration();
		reg2.setMotivation("motivation2");
		
		// check conversion did happen
		reg2.convertFrom(reg1);
		assertEquals("motivation1", reg2.getMotivation());
		assertEquals("00000000000", reg2.getSsin());
		assertEquals("TITLE_1", reg2.getTitle());
		assertEquals(LocalDate.now(), reg2.getStartDate());
		
		// check conversion did not happen
		reg2.setMotivation("motivation2");
		Registration reg3 = new Registration();
		reg3.setMotivation("");
		reg3.setSsin("");
		reg2.convertFrom(reg3);
		assertEquals("motivation2", reg2.getMotivation());
		assertEquals("00000000000", reg2.getSsin());
	}
	
	@Test
	public void testSssinFormatted()
	{
	    Registration r = new Registration();
	    assertEquals("", r.getSsinFormatted());
	    r.setSsin("00000000000");
	    assertEquals("000000-000.00", r.getSsinFormatted());
	}
}
