package org.jorion.trainingtool.services;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import org.jorion.trainingtool.dto.UpdateEventDTO;
import org.jorion.trainingtool.events.UpdatedStatusEvent;
import org.jorion.trainingtool.types.RegistrationEvent;
import org.jorion.trainingtool.types.RegistrationStatus;

/**
 * Unit test for {@link EventListenerService}.
 * <p>
 * See https://www.vogella.com/tutorials/Mockito/article.html for info on Mockito
 */
public class EventListenerServiceTest
{
    // --- Variables ---
    @Mock
    EmailService mockEmailService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    // --- Methods ---
    @Test
    public void testOnUpdateStatusEvent_SendMail()
            throws Exception
    {
        UpdateEventDTO.Builder builder = new UpdateEventDTO.Builder();
        UpdateEventDTO dto = builder.withRegId(1L).withRegTitle("TITLE").withRegStatus(RegistrationStatus.DRAFT).withRegEvent(RegistrationEvent.SUBMIT).build();
        UpdatedStatusEvent event = new UpdatedStatusEvent(new Object(), dto);

        // Event = Submit: mail sent
        EventListenerService service = new EventListenerService();
        ReflectionTestUtils.setField(service, "emailService", mockEmailService);
        service.onUpdateStatusEvent(event);
        verify(mockEmailService, times(1)).sendEmail(dto);
    }

    @Test
    public void testOnUpdateStatusEvent_NotSendMail()
            throws Exception
    {
        UpdateEventDTO.Builder builder = new UpdateEventDTO.Builder();
        UpdateEventDTO dto = builder.withRegId(1L).withRegTitle("TITLE").withRegStatus(RegistrationStatus.DRAFT).withRegEvent(RegistrationEvent.SAVE).build();
        UpdatedStatusEvent event = new UpdatedStatusEvent(new Object(), dto);

        // Event = Save: mail not sent
        EventListenerService service = new EventListenerService();
        ReflectionTestUtils.setField(service, "emailService", mockEmailService);
        service.onUpdateStatusEvent(event);
        verify(mockEmailService, never()).sendEmail(dto);
    }
    
    @Test
    public void testOnAuthenticationEvent()
    {
        // if successful, no exception
        EventListenerService service = new EventListenerService();

        Authentication auth = new TestingAuthenticationToken(null, null);
        AuthenticationSuccessEvent successEvent = new AuthenticationSuccessEvent(auth);
        service.onAuthenticationEvent(successEvent);
        
        LogoutSuccessEvent logoutEvent = new LogoutSuccessEvent(auth);
        service.onAuthenticationEvent(logoutEvent);
    }
}
