package org.jorion.trainingtool.service.validator;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jorion.trainingtool.entity.Training;
import org.jorion.trainingtool.type.Provider;
import org.jorion.trainingtool.util.DateUtils;
import org.jorion.trainingtool.util.StrUtils;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class TrainingValidator {

    private final Training training;

    /**
     * Business rules for the fields:
     *
     * <dl>
     * <dt>TITLE
     * <dd>mandatory
     * <dt>DESCRIPTION
     * <dd>mandatory
     * <dt>PROVIDER
     * <dd>mandatory
     * <dt>PROVIDER NAME
     * <dd>only available if provider = Other. Mandatory if available.
     * <dt>DATES
     * <dd>end date must be after or equal to start date
     * <dt>PRICE
     * <dd>not mandatory value must be a positive number
     * </dl>
     */
    public List<String> validate() {

        List<String> errors = new ArrayList<>();

        // Enabling dates
        if (!DateUtils.isDateBefore(training.getEnabledFrom(), training.getEnabledUntil(), true, false)) {
            errors.add("enabledUntil");
        }

        // Title mandatory
        if (StringUtils.isBlank(training.getTitle())) {
            errors.add("title");
        }
        // Description mandatory
        if (StringUtils.isBlank(training.getDescription())) {
            errors.add("description");
        }
        // Provider mandatory
        if (training.getProvider() == null) {
            errors.add("provider");
        }
        // Provider alternative name
        if (training.getProvider() == Provider.OTHER) {
            if (StringUtils.isBlank(training.getProviderOther())) {
                errors.add("provider.other");
            }
        }
        // URL must be valid
        if (StringUtils.isBlank(training.getUrl()) || !StrUtils.isUrlValid(training.getUrl(), false)) {
            errors.add("url");
        }
        // Price value must be a valid positive decimal number
        if ("-1".equals(StrUtils.parsePositiveFloat(training.getPrice()))) {
            errors.add("price");
        }

        // Start date <= EndDate
        if (!DateUtils.isDateBefore(training.getStartDate(), training.getEndDate(), true, false)) {
            errors.add("endDate");
        }
        return errors;
    }

}
