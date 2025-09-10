package org.jorion.trainingtool.event;

import org.jorion.trainingtool.registration.Registration;
import org.jorion.trainingtool.type.RegistrationEvent;
import org.jorion.trainingtool.type.RegistrationStatus;
import org.jorion.trainingtool.user.RandomUser;
import org.jorion.trainingtool.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventPublisherServiceTest {

    @Mock
    private ApplicationEventPublisher mockEventPublisher;

    @InjectMocks
    private EventPublisherService service;

    @Test
    void testPublishUpdateEvent() {

        User actor = RandomUser.createUser("actor");
        actor.setEmail("actor@example.org");

        User member = RandomUser.createUser("member");
        member.setId(1L);
        member.setManagerName("actor");

        Registration reg = new Registration();
        reg.setId(10L);
        reg.setMember(member);
        reg.setTitle("title");
        reg.setStatus(RegistrationStatus.SUBMITTED_TO_MANAGER);

        service.publishUpdateEvent(RegistrationEvent.ASSIGN, reg, actor);
        verify(mockEventPublisher, times(1)).publishEvent(ArgumentMatchers.any());
    }
}
