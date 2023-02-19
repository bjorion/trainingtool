package org.jorion.trainingtool.event;

import org.jorion.trainingtool.dto.UpdateEventDTO;
import org.springframework.context.ApplicationEvent;

/**
 * Event to be published when a registration status is modified.
 */
public class UpdatedStatusEvent extends ApplicationEvent {

    private UpdateEventDTO content;

    public UpdatedStatusEvent(Object source, UpdateEventDTO message) {

        super(source);
        this.content = message;
    }

    public UpdateEventDTO getContent() {
        return content;
    }
}
