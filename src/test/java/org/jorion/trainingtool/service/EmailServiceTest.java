package org.jorion.trainingtool.service;

import jakarta.mail.MessagingException;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jorion.trainingtool.common.EntityUtils;
import org.jorion.trainingtool.dto.UpdateEventDTO;
import org.jorion.trainingtool.dto.UpdateEventDTO.UpdateEventDTOBuilder;
import org.jorion.trainingtool.entity.User;
import org.jorion.trainingtool.type.RegistrationEvent;
import org.jorion.trainingtool.type.RegistrationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.util.HashMap;
import java.util.Map;

import static org.jorion.trainingtool.type.RegistrationEvent.SUBMIT;
import static org.jorion.trainingtool.type.RegistrationStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private LdapService mockLdapService;

    /**
     * Utility method that pre-fills the {@link UpdateEventDTOBuilder} object.
     */
    private static UpdateEventDTOBuilder buildUpdateEventDTO(RegistrationStatus status, RegistrationEvent event) {

        UpdateEventDTOBuilder builder = UpdateEventDTO.builder()
                .regId(1L)
                .regTitle("title")
                .regStatus(status)
                .regEvent(event)
                .memberEmail("john.doe@example.org")
                .memberFirstname("John")
                .memberLastname("Doe");

        return builder;
    }

    public static SpringTemplateEngine buildTemplateEngine() {
        
        FileTemplateResolver templateResolver = new FileTemplateResolver();
        templateResolver.setPrefix("src/main/resources/templates/");
        // templateResolver.setCharacterEncoding(config.getTemplateEncoding());
        templateResolver.setTemplateMode(TemplateMode.HTML);

        SpringTemplateEngine te = new SpringTemplateEngine();
        te.setTemplateResolver(templateResolver);
        te.clearTemplateCache();
        return te;
    }

    /**
     * Test the method {@code sendEmail(UpdateEventDTO dto)}.
     */
    @Test
    void testSendEmailUpdateEventDTO()
            throws MessagingException {
        
        StringBuilder sbFilename = new StringBuilder();
        MutableObject<Map<String, Object>> ctnModel = new MutableObject<>();
        MutableBoolean ctnBool = new MutableBoolean();

        /**
         * Custom version of EmailService that will override some methods.
         */
        EmailService service = new EmailService() {

            @Override
            protected String buildMail(String filename, Map<String, Object> model) {
                sbFilename.append(filename);
                ctnModel.setValue(model);
                return "";
            }

            @Override
            public void doSendEmail(String from, String to, String subject, String htmlTemplate)
                    throws MessagingException {
                ctnBool.setTrue();
            }
        };

        service.setSendMail(true);
        service.setFromAddress("donotreply@example.org");
        service.setServerDomain("http://www.example.org");
        service.setServerContext("/trainingtool");

        // execute the test
        service.sendEmail(buildUpdateEventDTO(DRAFT, SUBMIT).build());

        // check the results
        Map<String, Object> model = ctnModel.getValue();
        assertTrue(ctnBool.isTrue());
        assertEquals("mails/req-submitted.html", sbFilename.toString());
        assertEquals("http://www.example.org/trainingtool/registration?id=1", model.get("reqLink"));
        assertEquals("John", model.get("recipient"));
        assertNotNull(model.get("dto"));
    }

    /**
     * Test the insertion of the DTO to the mail.
     */
    @Test
    void testBuildMail() {
        
        final String link = "http://www.example.org/trainingtool/registration?id=1";

        EmailService service = new EmailService();
        SpringTemplateEngine templateEngine = buildTemplateEngine();
        ReflectionTestUtils.setField(service, "templateEngine", templateEngine);

        // test "req-assigned"
        UpdateEventDTO dto = buildUpdateEventDTO(DRAFT, SUBMIT).actorName("Manager").regJustification("Requested").build();
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("recipient", "John");
        model.put("reqLink", link);
        model.put("dto", dto);

        String mail = service.buildMail("mails/req-assigned.html", model);
        // System.out.println(mail);
        assertTrue(mail.contains(link));
        assertTrue(mail.contains("Justification"));

        // test "req-submitted"
        dto = buildUpdateEventDTO(DRAFT, SUBMIT).actorName("Manager").build();
        model = new HashMap<String, Object>();
        model.put("recipient", "John");
        model.put("reqLink", link);
        model.put("dto", dto);

        mail = service.buildMail("mails/req-submitted.html", model);
        // System.out.println(mail);
        assertFalse(mail.contains("Justification"));
    }

    @Test
    void testGetServerContextPath() {
        
        assertEquals("http://www.example.org/trainingtool/", EmailService.getServerContextPath("http://www.example.org", "/trainingtool"));
        assertEquals("http://www.example.org/trainingtool/", EmailService.getServerContextPath("http://www.example.org/", "/trainingtool"));
        assertEquals("http://www.example.org/trainingtool/", EmailService.getServerContextPath("http://www.example.org", "/trainingtool/"));
        assertEquals("http://www.example.org/trainingtool/", EmailService.getServerContextPath("http://www.example.org/", "/trainingtool/"));
    }

    // utility methods

    @Test
    void testGetTemplate() {
        
        assertEquals("mails/req-assigned.html", EmailService.getTemplate(RegistrationEvent.ASSIGN, null));
        assertEquals("mails/req-submitted.html", EmailService.getTemplate(RegistrationEvent.SUBMIT, null));
        assertEquals("mails/req-approved-by-provider.html", EmailService.getTemplate(RegistrationEvent.SUBMIT, RegistrationStatus.APPROVED_BY_PROVIDER));
        assertEquals("mails/req-refused.html", EmailService.getTemplate(RegistrationEvent.REFUSE, null));
        assertEquals("mails/req-sent-back.html", EmailService.getTemplate(RegistrationEvent.SEND_BACK, null));
        assertNull(EmailService.getTemplate(RegistrationEvent.SAVE, null));
    }

    @Test
    void testGetMailInfos() {
        
        String[] results;
        EmailService service = new EmailService();
        ReflectionTestUtils.setField(service, "ldapService", mockLdapService);
        ReflectionTestUtils.setField(service, "mgtMailAddress", "mgt@example.org");
        ReflectionTestUtils.setField(service, "trainingMailAddress", "training@example.org");

        User.UserBuilder builder = EntityUtils.createUserBuilder("manager");
        builder.email("my.manager@example.org");
        builder.firstName("my");
        builder.lastName("manager");
        when(mockLdapService.searchByName("manager")).thenReturn(builder.build());

        UpdateEventDTO dto = buildUpdateEventDTO(DRAFT, SUBMIT).build();
        results = service.getMailInfo(dto);
        assertEquals("John", results[0]);
        assertEquals("john.doe@example.org", results[1]);

        dto = buildUpdateEventDTO(SUBMITTED_TO_MANAGER, SUBMIT).manager("manager").build();
        results = service.getMailInfo(dto);
        assertEquals("my MANAGER", results[0]);
        assertEquals("my.manager@example.org", results[1]);

        dto = buildUpdateEventDTO(SUBMITTED_TO_MANAGER, SUBMIT).manager(null).build();
        results = service.getMailInfo(dto);
        assertNull(results[0]);
        assertNull(results[1]);

        dto = buildUpdateEventDTO(SUBMITTED_TO_HR, SUBMIT).build();
        results = service.getMailInfo(dto);
        assertEquals("Management", results[0]);
        assertEquals("mgt@example.org", results[1]);

        dto = buildUpdateEventDTO(SUBMITTED_TO_TRAINING, SUBMIT).build();
        results = service.getMailInfo(dto);
        assertEquals("BeTraining Team", results[0]);
        assertEquals("training@example.org", results[1]);

        dto = buildUpdateEventDTO(SUBMITTED_TO_PROVIDER, SUBMIT).build();
        results = service.getMailInfo(dto);
        assertEquals("Provider", results[0]);
        assertNull(results[1]);

        dto = buildUpdateEventDTO(APPROVED_BY_PROVIDER, SUBMIT).build();
        results = service.getMailInfo(dto);
        assertEquals("John", results[0]);
        assertEquals("john.doe@example.org", results[1]);

        dto = buildUpdateEventDTO(REFUSED_BY_MANAGER, SUBMIT).build();
        results = service.getMailInfo(dto);
        assertEquals("John", results[0]);
        assertEquals("john.doe@example.org", results[1]);
    }

}
