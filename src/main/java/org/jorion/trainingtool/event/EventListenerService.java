package org.jorion.trainingtool.event;

import lombok.extern.slf4j.Slf4j;
import org.jorion.trainingtool.export.EmailService;
import org.jorion.trainingtool.export.UpdateEventDTO;
import org.jorion.trainingtool.type.RegistrationEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Service responsible for listening to published events.
 * <p>
 * Note: currently, the workflow Publisher/Listener is synchronous. It would be possible to make it asynchronous with
 * some Spring annotations (by starting a new thread). This is a possible evolution to be studied.
 * <p>
 * Currently, there is only one listener whose only action is to call the Email service. If additional actions need to
 * be implemented (ie: send an SMS, save information in a log file...), we can implement them all in this listener, or
 * have one listener per action. This will have to be investigated.
 */
@Slf4j
@Service
public class EventListenerService {

    @Autowired
    private EmailService emailService;

    private static String joinAuthorities(Collection<? extends GrantedAuthority> auths) {

        return auths.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(":"));
    }

    /**
     * Listen to the event generated when a registration status is updated.
     *
     * @param event the event containing the object {@link UpdateEventDTO}.
     */
    @EventListener
    public void onUpdateStatusEvent(UpdatedStatusEvent event) {

        UpdateEventDTO dto = event.getContent();
        try {
            if (RegistrationEvent.EMAIL_SET.contains(dto.getRegEvent())) {
                emailService.sendEmail(event.getContent());
            }
        } catch (Exception e) {
            log.error("Could not send Email", e);
        }
    }

    /**
     * Listen to the authentication event for logging purposes.
     *
     * @param event the event containing the Principal
     */
    @EventListener
    public void onAuthenticationEvent(AbstractAuthenticationEvent event) {

        if (event instanceof AuthenticationSuccessEvent) {
            var auth = event.getAuthentication();
            var principal = auth.getPrincipal();

            if (principal instanceof LdapUserDetails user) {
                if (log.isDebugEnabled()) {
                    log.debug("Principal is Ldap User [{}] with authorities [{}]",
                            user.getUsername(), joinAuthorities(user.getAuthorities()));
                }
            } else if (principal instanceof User user) {
                if (log.isDebugEnabled()) {
                    log.debug("Principal is Spring User [{}] with authorities [{}]",
                            user.getUsername(), joinAuthorities(user.getAuthorities()));
                }
            } else if (principal != null) {
                log.error("Principal is unknown: {}", principal.getClass().getName());
            } else {
                log.error("Principal is null in Authentication");
            }
        } else if (event instanceof AbstractAuthenticationFailureEvent) {
            log.info("Authentication Failure: {}",
                    ((AbstractAuthenticationFailureEvent) event).getException().getMessage());
        }
    }
}
