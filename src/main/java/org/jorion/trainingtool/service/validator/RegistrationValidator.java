package org.jorion.trainingtool.service.validator;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jorion.trainingtool.entity.Registration;
import org.jorion.trainingtool.entity.User;
import org.jorion.trainingtool.type.Provider;
import org.jorion.trainingtool.type.RegistrationStatus;
import org.jorion.trainingtool.util.DateUtils;
import org.jorion.trainingtool.util.SsinUtils;
import org.jorion.trainingtool.util.StrUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@NoArgsConstructor
public class RegistrationValidator {

    @Value("${app.date.weeksToSubtract}")
    private int weeksToSubtract;

    /**
     * Justification is mandatory for a supervisor.
     *
     * @return False if role = supervisor and justification is empty, true otherwise
     */
    public static boolean isJustificationValid(User user, Registration registration) {

        boolean flag = true;
        if (user.isSupervisor() && StringUtils.isBlank(registration.getJustification())) {
            flag = false;
        }
        return flag;
    }

    /**
     * @return True if SSIN is mandatory and given, or not mandatory
     */
    public static boolean isSsinValid(User user, Registration registration) {

        return (!StringUtils.isBlank(registration.getSsin())) || !isSsinMandatory(user, registration);
    }

    /**
     * SSIN is mandatory if the provider requires it AND if the user is a member (a supervisor may not know the member's
     * SSIN).
     *
     * @param user         the current user
     * @param registration the registration
     * @return True if the SSIN is mandatory
     */
    public static boolean isSsinMandatory(User user, Registration registration) {

        boolean ssinMandatory = !user.isSupervisor() && (registration.getProvider() != null) && registration.getProvider().isSsinMandatory();
        return ssinMandatory;
    }

    /**
     * Start date and End date are mandatory if the registration status is SUBMITTED_TO_TRAINING later.
     *
     * @return True if the both startDate/endDate are given, or if registration status is before SUBMITTED_TO_TRAINING
     */
    public static boolean isDateValid(User user, Registration registration) {

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
    public static boolean isBillabilityValid(User user, Registration registration) {

        return registration.getBillable() != null || !user.isSupervisor();
    }

    /**
     * Business rules for the fields:
     *
     * <dl>
     * <dt>SECTOR
     * <dd>mandatory
     *
     * <dt>TITLE
     * <dd>mandatory
     *
     * <dt>DESCRIPTION
     * <dd>not mandatory
     *
     * <dt>PROVIDER
     * <dd>mandatory
     * <dt>PROVIDER NAME
     * <dd>only available if provider = Other. Mandatory if available.
     *
     * <dt>SSIN
     * <dd>mandatory when provider = Cevora and user = member (not mandatory for a Manager creating a request for a
     * member). Value must be valid.
     *
     * <dt>URL
     * <dd>mandatory. Value must be valid (start with http:// or https://)
     *
     * <dt>DATES
     * <dd>mandatory when provider = Cevora, Coursera, Academia. Always mandatory when user = be-training. Start date
     * must be gte today - "weeksToSubtract" weeks, end date gte start date
     *
     * <dt>PERIOD
     * <dd>mandatory
     *
     * <dt>TOTAL HOURS
     * <dd>mandatory when provider = Cevora, Coursera, Academia mandatory when provider = Other and user = betraining
     *
     * <dt>LOCATION
     * <dd>not mandatory
     *
     * <dt>PRICE
     * <dd>not mandatory value must be a positive number
     *
     * <dt>BILLABILITY
     * <dd>mandatory for Manager and above
     *
     * <dt>CBA COMPLIANT
     * <dd>Only available for Manager and above not mandatory (but 'no' by default)
     *
     * <dt>COMMENT
     * <dd>not mandatory
     *
     * <dt>JUSTIFICATION
     * <dd>mandatory for Manager (and above) not available for members
     * </dl>
     *
     * @param user         the user executing the action
     * @param member       the member taking the training
     * @param registration the registration object
     * @return A collection of errors in case of business errors, or an empty collection otherwise.
     */
    public List<String> validate(User user, User member, Registration registration) {

        List<String> errors = new ArrayList<>();
        boolean ssinMandatory = isSsinMandatory(user, registration);
        boolean fieldMandatory = (registration.getProvider() != null) && registration.getProvider().isFieldMandatory();

        // member sector
        if (member != null) {
            if (StringUtils.isBlank(member.getSector())) {
                errors.add("sector");
            }
        }

        // Title mandatory
        if (StringUtils.isBlank(registration.getTitle())) {
            errors.add("title");
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
        // SSIN must be valid
        if (!SsinUtils.isSsinValid(registration.getSsin(), ssinMandatory)) {
            errors.add("ssin");
        }
        // URL must be valid
        if (StringUtils.isBlank(registration.getUrl()) || !StrUtils.isUrlValid(registration.getUrl(), false)) {
            errors.add("url");
        }
        // Dates are mandatory for Coursera, Cevora, Academia
        // For other provider, mandatory only for BeTraining
        boolean dateMandatory = fieldMandatory || (registration.getProvider() == Provider.OTHER && user.isTraining());
        // Today <= StartDate
        if (!DateUtils.isDateBefore(LocalDate.now().minusWeeks(weeksToSubtract), registration.getStartDate(), true, dateMandatory)) {
            errors.add("startDate");
        }
        // Start date <= EndDate
        if (!DateUtils.isDateBefore(registration.getStartDate(), registration.getEndDate(), true, dateMandatory)) {
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
        if ("-1".equals(StrUtils.parsePositiveFloat(registration.getPrice()))) {
            errors.add("price");
        }
        // Billability
        if (!isBillabilityValid(user, registration)) {
            errors.add("billable");
        }
        // Justification
        if (!isJustificationValid(user, registration)) {
            errors.add("justification");
        }

        return errors;
    }

    public void setWeeksToSubtract(int weeksToSubtract) {
        this.weeksToSubtract = weeksToSubtract;
    }
}
