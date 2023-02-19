package org.jorion.trainingtool.service.validator;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jorion.trainingtool.entity.User;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class UserValidator {
    // --- Constants ---
    private final User user;

    // --- Methods ---

    /**
     * @return A collection of errors in case of business errors, or an empty collection otherwise.
     */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        boolean fnBlank = StringUtils.isBlank(user.getFirstName()) || "*".equals(user.getFirstName().trim());
        boolean lnBlank = StringUtils.isBlank(user.getLastName()) || "*".equals(user.getLastName().trim());
        if (fnBlank && lnBlank) {
            errors.add("missing");
        }
        return errors;
    }
}
