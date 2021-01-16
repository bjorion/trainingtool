package org.jorion.trainingtool.events;

import org.springframework.context.ApplicationEvent;

import org.jorion.trainingtool.dto.UpdateEventDTO;

/**
 * Event to be published when a registration status is modified.
 */
public class UpdatedStatusEvent extends ApplicationEvent
{
	// --- Variables ---
	private UpdateEventDTO content;

	// --- Methods ---
	public UpdatedStatusEvent(Object source, UpdateEventDTO message)
	{
		super(source);
		this.content = message;
	}

	public UpdateEventDTO getContent()
	{
		return content;
	}
}
