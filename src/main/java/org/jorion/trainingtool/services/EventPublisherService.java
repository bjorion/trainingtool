package org.jorion.trainingtool.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import org.jorion.trainingtool.dto.UpdateEventDTO;
import org.jorion.trainingtool.entities.Registration;
import org.jorion.trainingtool.entities.User;
import org.jorion.trainingtool.events.UpdatedStatusEvent;
import org.jorion.trainingtool.types.RegistrationEvent;

/**
 * Service responsible for publishing Spring events.
 */
@Service
public class EventPublisherService
{
    // --- Constants ---
    private static final Logger LOG = LoggerFactory.getLogger(EventPublisherService.class);

    // --- Variables ---
    @Autowired
    private ApplicationEventPublisher applicationPublisher;

    // --- Methods ---
    public void publishUpdateEvent(RegistrationEvent regEvent, Registration reg, User actor)
    {
        LOG.debug("publishUpdateEvent");
        UpdateEventDTO.Builder builder = new UpdateEventDTO.Builder();
        User member = reg.getMember();

        // @formatter:off
		builder.withRegId(reg.getId())
				.withRegStartDate(reg.getStartDate())
				.withRegTitle(reg.getTitle())
				.withRegMotivation(reg.getMotivation())
				.withRegStatus(reg.getStatus())
				.withRegEvent(regEvent)
				.withMemberId(member.getId())
		        .withMemberEmail(member.getEmail())
		        .withMemberFirstname(member.getFirstname())
		        .withMemberLastname(member.getLastname())
		        .withMemberFullname(member.getFullname())
		        .withActorEmail(actor.getEmail())
		        .withActorName(actor.getFullname())
		        .withManager(member.getManagername());
		// @formatter:on

        UpdateEventDTO dto = builder.build();
        UpdatedStatusEvent event = new UpdatedStatusEvent(this, dto);
        applicationPublisher.publishEvent(event);
    }
}
