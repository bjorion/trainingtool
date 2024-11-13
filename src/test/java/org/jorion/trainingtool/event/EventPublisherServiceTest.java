package org.jorion.trainingtool.event;

import org.jorion.trainingtool.common.EntityUtils;
import org.jorion.trainingtool.registration.Registration;
import org.jorion.trainingtool.user.User;
import org.jorion.trainingtool.type.RegistrationEvent;
import org.jorion.trainingtool.type.RegistrationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EventPublisherServiceTest {

    @Mock
    private ApplicationEventPublisher mockEventPublisher;

    @Test
    void testPublishUpdateEvent() {

        EventPublisherService service = new EventPublisherService();
        ReflectionTestUtils.setField(service, "applicationPublisher", mockEventPublisher);

        User actor = EntityUtils.createUser("actor");
        actor.setEmail("actor@example.org");

        User member = EntityUtils.createUser("member");
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
