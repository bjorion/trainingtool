package org.jorion.trainingtool.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import org.jorion.trainingtool.dto.ReportDTO;
import org.jorion.trainingtool.entities.Registration;
import org.jorion.trainingtool.entities.User;
import org.jorion.trainingtool.repositories.IRegistrationRepository;
import org.jorion.trainingtool.types.Provider;
import org.jorion.trainingtool.types.RegistrationEvent;
import org.jorion.trainingtool.types.RegistrationStatus;
import org.jorion.trainingtool.types.YesNo;
import org.jorion.trainingtool.util.MiscUtils;
import org.jorion.trainingtool.util.SsinUtils;

/**
 * Methods used to access registrations.
 */
@Service
public class RegistrationService
{
    // --- Constants ---
    private static final Logger LOG = LoggerFactory.getLogger(RegistrationService.class);

    // --- Variables ---
    @Value("${app.workflow.hrIfBillable:false}")
    private boolean hrIfBillable;

    @Autowired
    private IRegistrationRepository registrationRepository;

    @Autowired
    private EventPublisherService eventPublisher;

    @Autowired
    private UserService userService;

    // --- Methods ---
    /**
     * Return a registration given its id and the user. If the user is a supervisor, the id is ignored, as a supervisor
     * can see all registrations. If the user is NOT a supervisor, then the registration must be assigned to him.
     *
     * @param user the current user
     * @param regId the registration id
     * @return the registration corresponding to the given id, or null if not found or not allowed
     */
    public Registration findById(User user, Long regId)
    {
        Optional<Registration> registration = null;
        if (user.isSupervisor()) {
            registration = registrationRepository.findById(regId);
        }
        else {
            registration = registrationRepository.findRegistrationByIdAndUsername(regId, user.getUsername());
        }
        return registration.orElseGet(() -> null);
    }

    /**
     * Returns a {@link Page} of {@link Registration}s meeting the paging restriction provided in the
     * {@code PageRequest} object.
     *
     * @param pageRequest the page request
     * @return a page of registrations
     */
    public Page<Registration> findAll(PageRequest pageRequest)
    {
        return registrationRepository.findAll(pageRequest);
    }

    /**
     * Returns a {@link Page} of {@link Registration}s meeting the paging restriction provided in the
     * {@code PageRequest} object.
     *
     * @param username the current user's username
     * @param manager the current user's manager
     * @param pageRequest the page request
     * @return a page of registrations
     */
    public Page<Registration> findAll(String username, String manager, PageRequest pageRequest)
    {
        return registrationRepository.findByManager(username, manager, pageRequest);
    }

    /**
     * Returns a {@link Page} of {@link Registration}s meeting the restriction provided in the {@code ReportDTO} object.
     *
     * @param report the criteria to select the registrations
     * @return a page of registrations
     */
    public Page<Registration> findAllByExample(ReportDTO report)
    {
        // @formatter:off
        List<Registration> registrations = registrationRepository.findAllByExample(
                    MiscUtils.likeSqlString(report.getLastname()),
                    report.getStartDate(),
                    report.getStatus(),
                    report.getUsername(),
                    report.getManager()
        );
        // @formatter:on
        Page<Registration> page = new PageImpl<>(registrations);
        return page;
    }

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
    public List<Registration> findPendingByUser(User user)
    {
        List<Registration> results = new ArrayList<>();

        String manager = null;
        EnumSet<RegistrationStatus> set = EnumSet.of(RegistrationStatus.NONE);

        if (user.isManager()) {
            manager = user.getUsername();
            set.add(RegistrationStatus.SUBMITTED_TO_MANAGER);
        }
        if (user.isHr()) {
            set.add(RegistrationStatus.SUBMITTED_TO_HR);
        }
        if (user.isTraining()) {
            set.add(RegistrationStatus.SUBMITTED_TO_TRAINING);
            set.add(RegistrationStatus.SUBMITTED_TO_PROVIDER);
        }
        if (user.isAdmin()) {
            set.addAll(RegistrationStatus.PENDING_SET);
        }

        if (set.size() > 1) {
            results = registrationRepository.findRegistrationsByStatusesAndManager(set, manager);
        }
        return results;
    }

    /**
     * Modify the registration status according to the current status and given event. Status is the only field that
     * will be changed (apart from the audit fields).
     *
     * @param user the current user
     * @param regId the registration id
     * @param regEvent the type of event applied to the registration (approve, refuse, update, reject)
     * @return The modified registration
     * @throws AccessDeniedException if the registration id is not null but the registration was not found
     * @see #isApprovable(User, Registration)
     * @see #isRefusable(User, Registration)
     * @see #isRejectable(User, Registration)
     */
    public Registration updateRegistrationStatus(User user, Long regId, RegistrationEvent regEvent)
    {
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
            LOG.warn("Cannot execute the action [{}] on registration [{}] for user [{}]",
                    new Object[] { regEvent.name(), registration.getId(), user.getUsername() });
            return registration;
        }

        // Save the registration to the DB
        registration.setStatus(newStatus);
        Registration newRegistration = registrationRepository.save(registration);
        LOG.info("Registration [{}] updated with status [{}]", newRegistration.getId(), newRegistration.getStatus());

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
     * @param user the current user
     * @param dtoReg the registration to update/create
     * @return The registration updated or inserted
     * @throws AccessDeniedException if the registration id is not null but the registration was not found
     */
    public Registration saveRegistration(User user, Registration dtoReg)
    {
        Registration registration;
        boolean assignSomebodyElse = false;

        // new registration
        if (dtoReg.getId() == null) {
            registration = new Registration();
            registration.setMember(dtoReg.getMember());
            assignSomebodyElse = !user.getUsername().equals(dtoReg.getMember().getUsername());
        }
        // existing registration
        else {
            registration = findById(user, dtoReg.getId());
        }
        if (registration == null) {
            throw new AccessDeniedException("Save: Registration not found or not viewable");
        }

        // Save the registration to the DB
        registration.convertFrom(dtoReg);
        registration = registrationRepository.save(registration);

        // Update the registration object if linked to the user
        boolean replace = user.replaceRegistration(registration);
        LOG.info("Registration saved [{}] with status [{}]. Replace in User [{}]", registration.getId(), registration.getStatus(), replace);

        // Publish the event
        RegistrationEvent regEvent = assignSomebodyElse ? RegistrationEvent.ASSIGN : RegistrationEvent.SAVE;
        eventPublisher.publishUpdateEvent(regEvent, registration, user);
        return registration;
    }

    /**
     * Create new registrations for the given members.
     * 
     * @param user the current user (must be a manager)
     * @param dtoReg the registration to insert
     * @param usernames the member usernames
     */
    public void saveRegistrations(User user, Registration dtoReg, String[] usernames)
    {
        Assert.isTrue(user.isSupervisor(), "User [" + user.getUsername() + "] does not have the rights for a bulk save");
        for (String username : usernames) {
            Registration registration = new Registration();
            registration.convertFrom(dtoReg);
            User member = userService.findUserByUsernameOrCreate(username);
            registration.setMember(member);
            registration = registrationRepository.save(registration);
            LOG.info("Registration saved [{}] for user [{}]", registration.getId(), username);
            eventPublisher.publishUpdateEvent(RegistrationEvent.ASSIGN, registration, user);
        }
        registrationRepository.flush();
    }

    /**
     * Delete a registration (for good).
     *
     * @param user the current user
     * @param registration the registartion to delete
     * @return true if the registration was deleted
     * @see #isDeletable(User, Registration)
     */
    public boolean deleteRegistration(User user, Registration registration)
    {
        if (!isDeletable(user, registration)) {
            LOG.warn("Cannot delete registration [{}] for user [{}]", registration.getId(), user.getUsername());
            return false;
        }

        registrationRepository.delete(registration);
        registrationRepository.flush();
        boolean remove = user.removeRegistration(registration);
        LOG.info("Registration deleted [{}]. Remove in User [{}]", registration.getId(), remove);
        return true;
    }

    // check permissions
    /**
     * A registration is editable if it's approvable.
     * 
     * @return true if the registration can be edited by the current user
     * @see #isApprovable(User, Registration)
     */
    public static boolean isEditable(User user, Registration registration)
    {
        return isApprovable(user, registration);
    }

    /**
     * A registration is submittable if it's DRAFT and it belongs to the current user.
     * 
     * @return true if the registration can be submitted by the current user
     */
    public static boolean isSubmittable(User user, Registration registration)
    {
        return (registration.getStatus() == RegistrationStatus.DRAFT && registration.belongsTo(user.getUsername()));
    }

    /**
     * A registration is acceptable when the current user did NOT create the original registration (then he will have to
     * accept it).
     * 
     * @return true if the registration was not created by the current user
     */
    public static boolean isAcceptable(User user, Registration registration)
    {
        return !user.getUsername().equals(registration.getCreatedBy());
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
    public static boolean isApprovable(User user, Registration registration)
    {
        // submittable implies approvable
        if (isSubmittable(user, registration)) {
            return true;
        }
        RegistrationStatus status = registration.getStatus();
        if (status.isPendingNotDraft() && status.isResponsible(user.getRoles()) && !registration.belongsTo(user.getUsername())) {
            return true;
        }
        return false;
    }

    /**
     * A registration is refusable if it's PENDING - NOT DRAFT, if the current user has the rights on it and if it does
     * not belong to the current user (a manager cannot refuse its own request).
     * 
     * @return true if the registration can be refused by the current user
     */
    public static boolean isRefusable(User user, Registration registration)
    {
        RegistrationStatus status = registration.getStatus();
        return status.isPendingNotDraft() && status.isResponsible(user.getRoles()) && !registration.belongsTo(user.getUsername());
    }

    /**
     * A registration is rejectable if it's SUBMITTED_TO_MANAGER, if the current user has the right and if it does not
     * belong to the current user (a manager cannot reject his own request).
     * 
     * @return true if the registration can be sent back to the member by the current user
     */
    public static boolean isRejectable(User user, Registration registration)
    {
        RegistrationStatus status = registration.getStatus();
        return status == RegistrationStatus.SUBMITTED_TO_MANAGER && status.isResponsible(user.getRoles()) && !registration.belongsTo(user.getUsername());
    }

    /**
     * A registration is deletable only by its creator and it's DRAFT.
     * 
     * @return true if the registration can be deleted by the current user
     */
    public static boolean isDeletable(User user, Registration registration)
    {
        return (registration.getStatus() == RegistrationStatus.DRAFT && registration.belongsTo(user.getUsername()));
    }

    // check validity
    /**
     * Motivation is mandatory for a supervisor.
     * 
     * @return False if role = supervisor and motivation is empty, true otherwise
     */
    public static boolean isMotivationValid(User user, Registration registration)
    {
        boolean flag = true;
        if (user.isSupervisor() && StringUtils.isBlank(registration.getMotivation())) {
            flag = false;
        }
        return flag;
    }

    /**
     * @return True if SSIN is mandatory and given, or not mandatory
     */
    public static boolean isSsinValid(User user, Registration registration)
    {
        return (!StringUtils.isBlank(registration.getSsin())) || !isSsinMandatory(user, registration);
    }

    /**
     * SSIN is mandatory if the provider requires it AND if the user is a member (a supervisor may not know the member's
     * SSIN).
     * 
     * @param user the current user
     * @param registration the registration
     * @return True if the SSIN is mandatory
     */
    public static boolean isSsinMandatory(User user, Registration registration)
    {
        boolean ssinMandatory = !user.isSupervisor() && (registration.getProvider() != null) && registration.getProvider().isSsinMandatory();
        return ssinMandatory;
    }

    /**
     * Start date and End date are mandatory if the registration status is SUBMITTED_TO_TRAINING later.
     * 
     * @return True if the both startDate/endDate are given, or if registration status is before SUBMITTED_TO_TRAINING
     */
    public static boolean isDateValid(User user, Registration registration)
    {
        boolean flag = true;
        RegistrationStatus status = registration.getStatus();
        if (status.isGTE(RegistrationStatus.SUBMITTED_TO_TRAINING)) {
            flag = (registration.getStartDate() != null) && (registration.getEndDate() != null);
        }
        return flag;
    }

    /**
     * @return True if billability is set, or if user = member
     */
    public static boolean isBillabilityValid(User user, Registration registration)
    {
        return registration.getBillable() != null || !user.isSupervisor();
    }

    /**
     * Business rules for the fields:
     * 
     * <pre>
     * SECTOR: mandatory
     * 
     * PROVIDER: mandatory
     * PROVIDER NAME: only available if provider = Other. Mandatory if available.
     * 
     * TITLE: mandatory
     * 
     * SSIN: mandatory when provider = Cevora and user = member (not mandatory for a Manager creating a request for a member)
     * value must be valid
     * 
     * DESCRIPTION: not mandatory
     * 
     * URL: mandatory
     * value must be valid (start with http:// or https://)
     * 
     * DATES: mandatory when provider = Cevora, Coursera, Academia
     * always mandatory when user = betraining
     * start date gte today - "weeksToSubstract" weeks, end date gte start date
     * 
     * PERIOD: mandatory
     * 
     * TOTAL HOURS:
     * mandatory when provider = Cevora, Coursera, Academia
     * mandatory when provider = Other and user = betraining
     * 
     * LOCATION: not mandatory
     * 
     * PRICE: not mandatory
     * value must be a positive number
     * 
     * BILLABILITY: mandatory for Manager and above
     * 
     * CBA COMPLIANT: Only available for Manager and above
     * not mandatory (but 'no' by default)
     * 
     * COMMENT: not mandatory
     * 
     * MOTIVATION: mandatory for Manager (and above)
     * not available for members
     * </pre>
     * 
     * @param user the user executing the action
     * @param member the member taking the training
     * @param registration the registration object
     * @return A collection of errors in case of business errors, or an empty collection otherwise.
     */
    public static List<String> checkBusinessErrors(User user, User member, Registration registration)
    {
        final int weeksToSubtract = 3;
                
        List<String> errors = new ArrayList<>();
        boolean ssinMandatory = isSsinMandatory(user, registration);
        boolean fieldMandatory = (registration.getProvider() == null) ? false : registration.getProvider().isFieldMandatory();

        // member sector
        if (member != null) {
            if (StringUtils.isBlank(member.getSector())) {
                errors.add("sector");
            }
        }

        // Provider mandatory
        if (registration.getProvider() == null) {
            errors.add("provider");
        }
        // Provider alternative name
        if (registration.getProvider() == Provider.OTHER) {
            if (StringUtils.isBlank(registration.getProviderOther())) {
                errors.add("provider.other");
            }
        }
        // Title mandatory
        if (StringUtils.isBlank(registration.getTitle())) {
            errors.add("title");
        }
        // SSIN must be valid
        if (!SsinUtils.isSsinValid(registration.getSsin(), ssinMandatory)) {
            errors.add("ssin");
        }
        // URL must be valid
        if (StringUtils.isBlank(registration.getUrl()) || !MiscUtils.isUrlValid(registration.getUrl(), false)) {
            errors.add("url");
        }
        // Dates are mandatory for Coursera, Cevora, Academia
        // For other provider, mandatory only for BeTraining
        boolean dateMandatory = fieldMandatory || (registration.getProvider() == Provider.OTHER && user.isTraining());
        // Today <= StartDate
        if (!MiscUtils.isDateBefore(LocalDate.now().minusWeeks(weeksToSubtract), registration.getStartDate(), true, dateMandatory)) {
            errors.add("startDate");
        }
        // Start date <= EndDate
        if (!MiscUtils.isDateBefore(registration.getStartDate(), registration.getEndDate(), true, dateMandatory)) {
            errors.add("endDate");
        }
        // Period mandatory
        if (registration.getPeriod() == null) {
            errors.add("period");
        }
        // Total Hours
        boolean totalHourMandatory = fieldMandatory || (registration.getProvider() == Provider.OTHER && user.isTraining());
        if (totalHourMandatory && StringUtils.isBlank(registration.getTotalHour())) {
            errors.add("totalHour");
        }
        // Price value must be a valid positive decimal number
        if ("-1".equals(MiscUtils.parsePositiveFloat(registration.getPrice()))) {
            errors.add("price");
        }
        // Billability
        if (!isBillabilityValid(user, registration)) {
            errors.add("billable");
        }
        // Motivation
        if (!isMotivationValid(user, registration)) {
            errors.add("motivation");
        }

        return errors;
    }

}
