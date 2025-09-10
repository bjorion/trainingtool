package org.jorion.trainingtool.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jorion.trainingtool.export.UpdateEventDTO;
import org.jorion.trainingtool.registration.Registration;
import org.jorion.trainingtool.type.RegistrationEvent;
import org.jorion.trainingtool.user.User;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Service responsible for publishing Spring events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisherService {

    private final ApplicationEventPublisher applicationPublisher;

    public void publishUpdateEvent(RegistrationEvent regEvent, Registration reg, User actor) {

        log.debug("publishUpdateEvent");
        User member = reg.getMember();

        UpdateEventDTO dto = UpdateEventDTO.builder()
                .regId(reg.getId())
                .regStartDate(reg.getStartDate())
                .regTitle(reg.getTitle())
                .regJustification(reg.getJustification())
                .regStatus(reg.getStatus())
                .regEvent(regEvent)
                .memberId(member.getId())
                .memberEmail(member.getEmail())
                .memberFirstname(member.getFirstName())
                .memberLastname(member.getLastName())
                .memberFullname(member.getFullName())
                .actorEmail(actor.getEmail())
                .actorName(actor.getFullName())
                .manager(member.getManagerName())
                .build();

        UpdatedStatusEvent event = new UpdatedStatusEvent(this, dto);
        applicationPublisher.publishEvent(event);
    }
}
