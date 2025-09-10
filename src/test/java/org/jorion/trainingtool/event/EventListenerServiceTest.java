package org.jorion.trainingtool.event;

import org.jorion.trainingtool.export.EmailService;
import org.jorion.trainingtool.export.UpdateEventDTO;
import org.jorion.trainingtool.type.RegistrationEvent;
import org.jorion.trainingtool.type.RegistrationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.core.Authentication;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventListenerServiceTest {

    @Mock
    private EmailService mockEmailService;

    @InjectMocks
    private EventListenerService service;

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
        service.onUpdateStatusEvent(event);
        verify(mockEmailService, never()).sendEmail(dto);
    }

    @Test
    void testOnAuthenticationEvent() {

        // if successful, no exception
        Authentication auth = new TestingAuthenticationToken(null, null);
        AuthenticationSuccessEvent successEvent = new AuthenticationSuccessEvent(auth);
        service.onAuthenticationEvent(successEvent);

        LogoutSuccessEvent logoutEvent = new LogoutSuccessEvent(auth);
        service.onAuthenticationEvent(logoutEvent);
    }
}
