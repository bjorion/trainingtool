package org.jorion.trainingtool.services;

import static org.jorion.trainingtool.types.RegistrationEvent.SUBMIT;
import static org.jorion.trainingtool.types.RegistrationStatus.APPROVED_BY_PROVIDER;
import static org.jorion.trainingtool.types.RegistrationStatus.DRAFT;
import static org.jorion.trainingtool.types.RegistrationStatus.REFUSED_BY_MANAGER;
import static org.jorion.trainingtool.types.RegistrationStatus.SUBMITTED_TO_HR;
import static org.jorion.trainingtool.types.RegistrationStatus.SUBMITTED_TO_MANAGER;
import static org.jorion.trainingtool.types.RegistrationStatus.SUBMITTED_TO_PROVIDER;
import static org.jorion.trainingtool.types.RegistrationStatus.SUBMITTED_TO_TRAINING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import org.jorion.trainingtool.dto.UpdateEventDTO;
import org.jorion.trainingtool.dto.UpdateEventDTO.Builder;
import org.jorion.trainingtool.entities.User;
import org.jorion.trainingtool.types.RegistrationEvent;
import org.jorion.trainingtool.types.RegistrationStatus;

/**
 * Unit test for {@link EmailService}.
 */
public class EmailServiceTest
{
    // --- Variables ---
    @Mock
    private LdapService mockLdapService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    // --- Methods ---
    /**
     * Test the method {@code sendEmail(UpdateEventDTO dto)}.
     */
    @Test
    public void testSendEmailUpdateEventDTO()
            throws MessagingException
    {
        StringBuilder sbFilename = new StringBuilder();
        MutableObject<Map<String, Object>> ctnModel = new MutableObject<>();
        MutableBoolean ctnBool = new MutableBoolean();

        /**
         * Custom version of EmailService that will override some methods.
         */
        EmailService service = new EmailService() {

            @Override
            protected String buildMail(String filename, Map<String, Object> model)
            {
                sbFilename.append(filename);
                ctnModel.setValue(model);
                return "";
            }

            @Override
            public void sendEmail(String from, String to, String subject, String htmlTemplate)
                    throws MessagingException
            {
                ctnBool.setTrue();
            }
        };

        service.setSendMail(true);
        service.setFromAddress("donotreply@jorion.org");
        service.setServerDomain("http://www.jorion.org");
        service.setServerContext("/trainingtool");

        // execute the test
        service.sendEmail(buildUpdateEventDTO(DRAFT, SUBMIT).build());

        // check the results
        Map<String, Object> model = ctnModel.getValue();
        assertTrue(ctnBool.isTrue());
        assertEquals("mails/req-submitted.html", sbFilename.toString());
        assertEquals("http://www.jorion.org/trainingtool/registration?id=1", model.get("reqLink"));
        assertEquals("John", model.get("recipient"));
        assertNotNull(model.get("dto"));
    }

    /**
     * Test the insertion of the DTO to the mail.
     */
    @Test
    public void testBuildMail()
    {
        final String link = "http://www.jorion.org/trainingtool/registration?id=1";

        EmailService service = new EmailService();
        SpringTemplateEngine templateEngine = buildTemplateEngine();
        ReflectionTestUtils.setField(service, "templateEngine", templateEngine);

        // test "req-assigned"
        UpdateEventDTO dto = buildUpdateEventDTO(DRAFT, SUBMIT).withActorName("Manager").withRegMotivation("Requested by Company").build();
        Map<String, Object> model = new HashMap<>();
        model.put("recipient", "John");
        model.put("reqLink", link);
        model.put("dto", dto);

        String mail = service.buildMail("mails/req-assigned.html", model);
        // System.out.println(mail);
        assertTrue(mail.contains(link));
        assertTrue(mail.contains("Motivation"));

        // test "req-submitted"
        dto = buildUpdateEventDTO(DRAFT, SUBMIT).withActorName("Manager").build();
        model = new HashMap<>();
        model.put("recipient", "John");
        model.put("reqLink", link);
        model.put("dto", dto);

        mail = service.buildMail("mails/req-submitted.html", model);
        // System.out.println(mail);
        assertFalse(mail.contains("Motivation"));
    }

    @Test
    public void testGetServerContextPath()
    {
        assertEquals("http://www.jorion.org/trainingtool/", EmailService.getServerContextPath("http://www.jorion.org", "/trainingtool"));
        assertEquals("http://www.jorion.org/trainingtool/", EmailService.getServerContextPath("http://www.jorion.org/", "/trainingtool"));
        assertEquals("http://www.jorion.org/trainingtool/", EmailService.getServerContextPath("http://www.jorion.org", "/trainingtool/"));
        assertEquals("http://www.jorion.org/trainingtool/", EmailService.getServerContextPath("http://www.jorion.org/", "/trainingtool/"));
    }

    @Test
    public void testGetTemplate()
    {
        assertEquals("mails/req-assigned.html", EmailService.getTemplate(RegistrationEvent.ASSIGN));
        assertEquals("mails/req-submitted.html", EmailService.getTemplate(RegistrationEvent.SUBMIT));
        assertEquals("mails/req-refused.html", EmailService.getTemplate(RegistrationEvent.REFUSE));
        assertEquals("mails/req-sent-back.html", EmailService.getTemplate(RegistrationEvent.SEND_BACK));
        assertNull(EmailService.getTemplate(RegistrationEvent.SAVE));
    }

    @Test
    public void testGetMailInfos()
    {
        String[] results;
        EmailService service = new EmailService();
        ReflectionTestUtils.setField(service, "ldapService", mockLdapService);
        ReflectionTestUtils.setField(service, "mgtMailAddress", "mgt@jorion.org");
        ReflectionTestUtils.setField(service, "trainingMailAddress", "training@jorion.org");

        User manager = new User("manager");
        manager.setEmail("hari.seldon@jorion.org");
        manager.setFirstname("hari");
        manager.setLastname("seldon");
        when(mockLdapService.searchByName("manager")).thenReturn(manager);

        UpdateEventDTO dto = buildUpdateEventDTO(DRAFT, SUBMIT).build();
        results = service.getMailInfo(dto);
        assertEquals("John", results[0]);
        assertEquals("john.doe@jorion.org", results[1]);

        dto = buildUpdateEventDTO(SUBMITTED_TO_MANAGER, SUBMIT).withManager("manager").build();
        results = service.getMailInfo(dto);
        assertEquals("hari SELDON", results[0]);
        assertEquals("hari.seldon@jorion.org", results[1]);

        dto = buildUpdateEventDTO(SUBMITTED_TO_MANAGER, SUBMIT).withManager(null).build();
        results = service.getMailInfo(dto);
        assertNull(results[0]);
        assertNull(results[1]);

        dto = buildUpdateEventDTO(SUBMITTED_TO_HR, SUBMIT).build();
        results = service.getMailInfo(dto);
        assertEquals("Management", results[0]);
        assertEquals("mgt@jorion.org", results[1]);

        dto = buildUpdateEventDTO(SUBMITTED_TO_TRAINING, SUBMIT).build();
        results = service.getMailInfo(dto);
        assertEquals("Training Team", results[0]);
        assertEquals("training@jorion.org", results[1]);

        dto = buildUpdateEventDTO(SUBMITTED_TO_PROVIDER, SUBMIT).build();
        results = service.getMailInfo(dto);
        assertEquals("Provider", results[0]);
        assertNull(results[1]);

        dto = buildUpdateEventDTO(APPROVED_BY_PROVIDER, SUBMIT).build();
        results = service.getMailInfo(dto);
        assertEquals("John", results[0]);
        assertEquals("john.doe@jorion.org", results[1]);

        dto = buildUpdateEventDTO(REFUSED_BY_MANAGER, SUBMIT).build();
        results = service.getMailInfo(dto);
        assertEquals("John", results[0]);
        assertEquals("john.doe@jorion.org", results[1]);
    }

    // utility methods
    /**
     * Utility method that pref-fills the {@link UpdateEventDTO.Builder} object.
     *
     * @param status the registration status
     * @param event the registration event
     * @return an instance of Builder
     */
    private static Builder buildUpdateEventDTO(RegistrationStatus status, RegistrationEvent event)
    {
        Builder builder = new Builder();
        builder.withRegId(1L).withRegTitle("title").withRegStatus(status).withRegEvent(event);
        builder.withMemberEmail("john.doe@jorion.org").withMemberFirstname("John").withMemberLastname("Doe");
        return builder;
    }

    /**
     * @return an instance of SpringTemplateEngine
     */
    public static SpringTemplateEngine buildTemplateEngine()
    {
        FileTemplateResolver templateResolver = new FileTemplateResolver();
        templateResolver.setPrefix("src/main/resources/templates/");
        // templateResolver.setCharacterEncoding(config.getTemplateEncoding());
        templateResolver.setTemplateMode(TemplateMode.HTML);

        SpringTemplateEngine te = new SpringTemplateEngine();
        te.setTemplateResolver(templateResolver);
        te.clearTemplateCache();
        return te;
    }

}
