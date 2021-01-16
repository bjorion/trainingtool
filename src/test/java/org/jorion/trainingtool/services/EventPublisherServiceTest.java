package org.jorion.trainingtool.services;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import org.jorion.trainingtool.entities.Registration;
import org.jorion.trainingtool.entities.User;
import org.jorion.trainingtool.types.RegistrationEvent;
import org.jorion.trainingtool.types.RegistrationStatus;

/**
 * Unit test for {@link EventPublisherService}.
 */
public class EventPublisherServiceTest
{
    // --- Variables ---
    @Mock
    ApplicationEventPublisher mockEventPublisher;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    // --- Methods ---
    @Test
    public void testPublishUpdateEvent()
    {
        EventPublisherService service = new EventPublisherService();
        ReflectionTestUtils.setField(service, "applicationPublisher", mockEventPublisher);
        
        User actor = new User("actor");
        actor.setEmail("actor@jorion.org");
        
        User member = new User("member");
        member.setId(1L);
        member.setManagername("actor");
        
        Registration reg = new Registration();
        reg.setId(10L);
        reg.setMember(member);
        reg.setTitle("title");
        reg.setStatus(RegistrationStatus.SUBMITTED_TO_MANAGER);
        
        service.publishUpdateEvent(RegistrationEvent.ASSIGN, reg, actor);
        
        verify(mockEventPublisher, times(1)).publishEvent(ArgumentMatchers.any());
    }
}
