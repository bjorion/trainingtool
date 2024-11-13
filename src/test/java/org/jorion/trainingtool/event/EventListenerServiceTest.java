package org.jorion.trainingtool.event;

import org.jorion.trainingtool.service.EmailService;
import org.jorion.trainingtool.type.RegistrationEvent;
import org.jorion.trainingtool.type.RegistrationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventListenerServiceTest {

    @Mock
    private EmailService mockEmailService;

    @Test
    void testOnUpdateStatusEvent_SendMail()
            throws Exception {

        UpdateEventDTO dto = UpdateEventDTO.builder()
                .regId(1L)
                .regTitle("TITLE")
                .regStatus(RegistrationStatus.DRAFT)
                .regEvent(RegistrationEvent.SUBMIT)
                .build();

        UpdatedStatusEvent event = new UpdatedStatusEvent(new Object(), dto);

        // Event = Submit: mail sent
        EventListenerService service = new EventListenerService();
        ReflectionTestUtils.setField(service, "emailService", mockEmailService);
        service.onUpdateStatusEvent(event);
        verify(mockEmailService, times(1)).sendEmail(dto);
    }

    @Test
    void testOnUpdateStatusEvent_NotSendMail()
            throws Exception {

        UpdateEventDTO dto = UpdateEventDTO.builder()
                .regId(1L)
                .regTitle("TITLE")
                .regStatus(RegistrationStatus.DRAFT)
                .regEvent(RegistrationEvent.SAVE)
                .build();

        UpdatedStatusEvent event = new UpdatedStatusEvent(new Object(), dto);

        // Event = Save: mail not sent
        EventListenerService service = new EventListenerService();
        ReflectionTestUtils.setField(service, "emailService", mockEmailService);
        service.onUpdateStatusEvent(event);
        verify(mockEmailService, never()).sendEmail(dto);
    }

    @Test
    void testOnAuthenticationEvent() {

        // if successful, no exception
        EventListenerService service = new EventListenerService();

        Authentication auth = new TestingAuthenticationToken(null, null);
        AuthenticationSuccessEvent successEvent = new AuthenticationSuccessEvent(auth);
        service.onAuthenticationEvent(successEvent);

        LogoutSuccessEvent logoutEvent = new LogoutSuccessEvent(auth);
        service.onAuthenticationEvent(logoutEvent);
    }
}
