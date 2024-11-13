package org.jorion.trainingtool.registration;

import lombok.extern.slf4j.Slf4j;
import org.jorion.trainingtool.report.ReportDTO;
import org.jorion.trainingtool.event.EventPublisherService;
import org.jorion.trainingtool.type.RegistrationEvent;
import org.jorion.trainingtool.type.RegistrationStatus;
import org.jorion.trainingtool.type.YesNo;
import org.jorion.trainingtool.user.User;
import org.jorion.trainingtool.user.UserService;
import org.jorion.trainingtool.util.StrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;

/**
 * Methods used to access registrations.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class RegistrationService {

    @Value("${app.workflow.hrIfBillable:false}")
    private boolean hrIfBillable;

    @Autowired
    private IRegistrationRepository registrationRepository;

    @Autowired
    private EventPublisherService eventPublisher;

    @Autowired
    private UserService userService;

    @Autowired
    private RegistrationValidator validator;

    /**
     * A registration is editable if
     * <ul>
     * <li>it's approvable OR
     * <li>if the current user = training AND status = APPROVED_BY_PROVIDER
     * </ul>
     *
     * @return true if the registration can be edited by the current user
     * @see #isApprovable(User, Registration)
     */
    public static boolean isEditable(User user, Registration registration) {

        RegistrationStatus status = registration.getStatus();
        return isApprovable(user, registration) || (status == RegistrationStatus.APPROVED_BY_PROVIDER && user.isTraining());
    }

    /**
     * A registration is submittable if it's DRAFT and it belongs to the current user.
     *
     * @return true if the registration can be submitted by the current user
     */
    public static boolean isSubmittable(User user, Registration registration) {

        RegistrationStatus status = registration.getStatus();
        return (status == RegistrationStatus.DRAFT && registration.belongsTo(user.getUserName()));
    }

    /**
     * A registration is acceptable when the current user did NOT create the original registration (then he will have to
     * accept it).
     *
     * @return true if the registration was not created by the current user
     */
    public static boolean isAcceptable(User user, Registration registration) {

        return !user.getUserName().equals(registration.getCreatedBy());
    }

    /**
     * A registration is approvable if
     * <ul>
     * <li>it's DRAFT and belongs to the current user, OR
     * <li>it's PENDING AND the current user has the right on that state AND the registration does not belong to the
     * current user (to avoid a manager to approve his own registration)
     * </ul>
     * A possible change would be to avoid another manager to approve the registration unless he's explicitly the user's
     * manager.
     *
     * @return true if the registration can be approved by the current user
     */
    public static boolean isApprovable(User user, Registration registration) {

        // submittable implies approvable
        if (isSubmittable(user, registration)) {
            return true;
        }
        return isApprovableOrRefusable(user, registration);
    }

    /**
     * A registration is refusable if it's PENDING - NOT DRAFT, if the current user has the rights on it and if it does
     * not belong to the current user (a manager cannot refuse its own request).
     *
     * @return true if the registration can be refused by the current user
     */
    public static boolean isRefusable(User user, Registration registration) {

        return isApprovableOrRefusable(user, registration);
    }

    private static boolean isApprovableOrRefusable(User user, Registration registration) {

        RegistrationStatus status = registration.getStatus();
        return status.isPendingNotDraft() && status.isResponsible(user.getRoles()) && !registration.belongsTo(user.getUserName());
    }

    /**
     * A registration is rejectable if it's SUBMITTED_TO_MANAGER, if the current user has the right and if it does not
     * belong to the current user (a manager cannot reject his own request).
     *
     * @return true if the registration can be sent back to the member by the current user
     */
    public static boolean isRejectable(User user, Registration registration) {

        RegistrationStatus status = registration.getStatus();
        return status == RegistrationStatus.SUBMITTED_TO_MANAGER && status.isResponsible(user.getRoles()) && !registration.belongsTo(user.getUserName());
    }

    /**
     * A registration is deletable only by its creator and it's DRAFT or REFUSED.
     *
     * @return true if the registration can be deleted by the current user
     */
    public static boolean isDeletable(User user, Registration registration) {

        if (!registration.belongsTo(user.getUserName())) {
            return false;
        }
        RegistrationStatus status = registration.getStatus();
        return (status == RegistrationStatus.DRAFT || RegistrationStatus.REFUSED_SET.contains(status));
    }

    /**
     * Return a registration given its id and the user. If the user is a supervisor, the id is ignored, as a supervisor
     * can see all registrations. If the user is NOT a supervisor, then the registration must be assigned to him.
     *
     * @param user  the current user
     * @param regId the registration id
     * @return the registration corresponding to the given id, or null if not found or not allowed
     */
    public Registration findById(User user, Long regId) {

        Optional<Registration> registration;
        if (user.isOffice()) {
            registration = registrationRepository.findById(regId);
        } else if (user.isManager()) {
            registration = registrationRepository.findRegistrationByIdAndManager(regId, user.getUserName());
        } else {
            registration = registrationRepository.findRegistrationByIdAndUser(regId, user.getUserName());
        }
        return registration.orElse(null);
    }

    /**
     * Returns a {@link Page} of {@link Registration}s belonging to the given user.
     *
     * @param user        the current user
     * @param memberName  the member name
     * @param pageRequest the page request
     * @return a page of registrations belonging to the given user
     * @throws AccessDeniedException if the user (Principal) may not access data of the given member
     */
    public Page<Registration> findAllByMemberName(User user, String memberName, PageRequest pageRequest) {

        User member = userService.findUserByUserName(memberName);
        if (!UserService.isAuthorized(user, member)) {
            throw new AccessDeniedException("User [" + user.getUserName() + "] cannot access registrations of [" + memberName + "]");
        }
        return registrationRepository.findByUserName(memberName, pageRequest);
    }

    /**
     * Returns a {@link Page} of {@link Registration}s belonging to the given manager, ie. registrations for users the
     * manager is responsible for, or for the manager himself.
     *
     * @param managerName the manager name
     * @param pageRequest the page request
     * @return a page of registrations belonging to the given manager
     */
    public Page<Registration> findAllByManager(String managerName, PageRequest pageRequest) {

        return registrationRepository.findByManagerName(managerName, pageRequest);
    }

    /**
     * Returns a {@link Page} of {@link Registration}s meeting the restriction provided in the {@code ReportDTO} object.
     *
     * @param report the criteria to select the registrations
     * @return a page of registrations
     */
    public Page<Registration> findAllByExample(ReportDTO report) {

        List<Registration> registrations = registrationRepository.findAllByExample(
                StrUtils.likeSqlString(report.getLastname()),
                report.getStartDate(),
                report.getStatus(),
                report.getUserName(),
                report.getManagerName()
        );

        return new PageImpl<>(registrations);
    }

    // static methods

    /**
     * Find all registrations submitted to the current user (in other words, pending), ie. registrations for which he
     * must take an action.
     * <ul>
     * <li>MEMBER will not see anything
     * <li>MANAGER only sees submitted registrations for members reporting to them
     * <li>HR, TRAINING and ADMIN people see submitted registrations from all members
     * </ul>
     *
     * @param user the current user
     * @return A list of registrations. Can be empty, but not null.
     */
    public List<Registration> findPendingByUser(User user) {

        List<Registration> results = new ArrayList<>();

        // As a manager, you can only see registrations for member you're responsible of
        if (user.isManager()) {
            String manager = user.getUserName();
            EnumSet<RegistrationStatus> statuses = EnumSet.of(RegistrationStatus.SUBMITTED_TO_MANAGER);
            results = registrationRepository.findRegistrationsByStatusesAndManager(statuses, manager);
        }

        // As HR, training or admin, you can see registrations for all members (for the given status)
        EnumSet<RegistrationStatus> statuses = EnumSet.noneOf(RegistrationStatus.class);
        if (user.isHr()) {
            statuses.add(RegistrationStatus.SUBMITTED_TO_HR);
        }
        if (user.isTraining()) {
            statuses.add(RegistrationStatus.SUBMITTED_TO_TRAINING);
            statuses.add(RegistrationStatus.SUBMITTED_TO_PROVIDER);
        }
        if (user.isAdmin()) {
            statuses.addAll(RegistrationStatus.PENDING_SET);
        }
        if (!statuses.isEmpty()) {
            List<Registration> regs = registrationRepository.findRegistrationsByStatuses(statuses);
            results.addAll(regs);
        }
        return results;
    }

    /**
     * @return a map with key = Registration Status and value = number of occurences for the status
     */
    public Map<RegistrationStatus, Integer> computeStats() {

        List<Registration> regs = registrationRepository.findAll();
        Map<RegistrationStatus, Integer> map = new HashMap<>();
        regs.forEach(e -> map.compute(e.getStatus(), (key, value) -> value == null ? 1 : value + 1));
        return map;
    }

    /**
     * Modify the registration status according to the current status and given event. Status is the only field that
     * will be changed (apart from the audit fields).
     *
     * @param user          the current user
     * @param regId         the registration id
     * @param regEvent      the type of event applied to the registration (approve, refuse, update, reject)
     * @param justification an optional string to add in the justification field
     * @return The modified registration
     * @throws AccessDeniedException if the registration id is not null but the registration was not found
     * @see #isApprovable(User, Registration)
     * @see #isRefusable(User, Registration)
     * @see #isRejectable(User, Registration)
     */
    @Transactional
    public Registration updateRegistrationStatus(User user, Long regId, RegistrationEvent regEvent, String justification) {

        final Registration registration = findById(user, regId);
        if (registration == null) {
            throw new AccessDeniedException("Status Update: Registration not found or not viewable - " + regId);
        }

        boolean ok;
        boolean hrWorkflow = hrIfBillable && YesNo.isYes(registration.getBillable());
        RegistrationStatus status = registration.getStatus();
        RegistrationStatus newStatus = null;

        switch (regEvent) {
            case SUBMIT:
                ok = isApprovable(user, registration);
                newStatus = status.getNextStatusAfterApproval(hrWorkflow);
                break;
            case REFUSE:
                ok = isRefusable(user, registration);
                newStatus = status.getNextStatusAfterRefusal();
                break;
            case SEND_BACK:
                ok = isRejectable(user, registration);
                newStatus = RegistrationStatus.DRAFT;
                break;
            default:
                // other statuses not allowed here
                ok = false;
                break;
        }

        if (!ok) {
            log.warn("Cannot execute the action [{}] on registration [{}] for user [{}]",
                    regEvent.name(), registration.getId(), user.getUserName());
            return registration;
        }

        // Save the registration to the DB
        registration.setStatus(newStatus);
        registration.addJustification(justification);
        Registration newRegistration = registrationRepository.save(registration);
        log.info("Registration [{}] updated with status [{}]", newRegistration.getId(), newRegistration.getStatus());

        // Update the registration object if linked to the user
        Registration userRegistration = user.findRegistrationById(newRegistration.getId());
        if (userRegistration != null) {
            userRegistration.setStatus(newStatus);
        }

        // Publish the event
        eventPublisher.publishUpdateEvent(regEvent, newRegistration, user);
        return newRegistration;
    }

    /**
     * Update or Create a registration.
     *
     * @param user   the current user
     * @param dtoReg the registration to update/create
     * @return The registration updated or inserted
     * @throws AccessDeniedException if the registration id is not null but the registration was not found
     */
    @Transactional
    public Registration saveRegistration(User user, Registration dtoReg) {

        Registration registration;
        boolean assignSomebodyElse = false;

        // new registration
        if (dtoReg.getId() == null) {
            registration = new Registration();
            registration.setMember(dtoReg.getMember());
            assignSomebodyElse = !user.getUserName().equals(dtoReg.getMember().getUserName());
        }
        // existing registration
        else {
            registration = findById(user, dtoReg.getId());
            if (registration == null) {
                throw new AccessDeniedException("Save: Registration not found or not viewable");
            }
        }

        // Save the registration to the DB
        registration.convertFrom(dtoReg);
        registration = registrationRepository.save(registration);

        // Update the registration object if linked to the user
        boolean replace = user.replaceRegistration(registration);
        log.info("Registration saved [{}] with status [{}]. Replace in User? [{}]", registration.getId(), registration.getStatus(), replace);

        // Publish the event
        RegistrationEvent regEvent = assignSomebodyElse ? RegistrationEvent.ASSIGN : RegistrationEvent.SAVE;
        eventPublisher.publishUpdateEvent(regEvent, registration, user);
        return registration;
    }

    /**
     * Create new registrations for the given members.
     *
     * @param user      the current user (must be a manager)
     * @param dtoReg    the registration to insert
     * @param userNames the member usernames
     */
    @Transactional
    public void saveRegistrations(User user, Registration dtoReg, String[] userNames) {

        Assert.isTrue(user.isSupervisor(), "User [" + user.getUserName() + "] does not have the rights for a bulk save");
        for (String userName : userNames) {
            Registration registration = new Registration();
            registration.convertFrom(dtoReg);
            User member = userService.findUserByUserNameOrCreate(userName);
            registration.setMember(member);
            registration = registrationRepository.save(registration);
            log.info("Registration saved [{}] for user [{}]", registration.getId(), userName);
            eventPublisher.publishUpdateEvent(RegistrationEvent.ASSIGN, registration, user);
        }
        registrationRepository.flush();
    }

    /**
     * Delete a registration (for good).
     *
     * @param user         the current user
     * @param registration the registartion to delete
     * @return true if the registration was deleted
     * @see #isDeletable(User, Registration)
     */
    public boolean deleteRegistration(User user, Registration registration) {

        if (!isDeletable(user, registration)) {
            log.warn("Cannot delete registration [{}] for user [{}]", registration.getId(), user.getUserName());
            return false;
        }

        registrationRepository.delete(registration);
        registrationRepository.flush();
        boolean remove = user.removeRegistration(registration);
        log.info("Registration deleted [{}]. Remove in User [{}]", registration.getId(), remove);
        return true;
    }

    /**
     * @return A list of invalid fields
     */
    public List<String> checkBusinessErrors(User user, User member, Registration registration) {

        return validator.validate(user, member, registration);
    }

}
