package org.jorion.trainingtool.registration;

import org.jorion.trainingtool.type.RegistrationStatus;
import org.jorion.trainingtool.user.User;

public class RandomRegistration {

    public static Registration createRegistration(User user, RegistrationStatus status) {

        var r = new Registration();
        r.setMember(user);
        r.setStatus(status);
        return r;
    }
}
