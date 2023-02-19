package org.jorion.trainingtool.service;

import lombok.extern.slf4j.Slf4j;
import org.jorion.trainingtool.dto.UpdateEventDTO;
import org.jorion.trainingtool.entity.Registration;
import org.jorion.trainingtool.entity.User;
import org.jorion.trainingtool.event.UpdatedStatusEvent;
import org.jorion.trainingtool.type.RegistrationEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Service responsible for publishing Spring events.
 */
@Slf4j
@Service
public class EventPublisherService {

    @Autowired
    private ApplicationEventPublisher applicationPublisher;

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
