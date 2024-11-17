package org.jorion.trainingtool.event;

import lombok.Getter;
import org.jorion.trainingtool.export.UpdateEventDTO;
import org.springframework.context.ApplicationEvent;

/**
 * Event to be published when a registration status is modified.
 */
@Getter
public class UpdatedStatusEvent extends ApplicationEvent {

    private final UpdateEventDTO content;

    public UpdatedStatusEvent(Object source, UpdateEventDTO message) {

        super(source);
        this.content = message;
    }

}
