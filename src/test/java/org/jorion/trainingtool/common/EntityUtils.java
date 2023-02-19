package org.jorion.trainingtool.common;

import org.jorion.trainingtool.entity.Registration;
import org.jorion.trainingtool.entity.Training;
import org.jorion.trainingtool.entity.User;
import org.jorion.trainingtool.type.Provider;
import org.jorion.trainingtool.type.RegistrationStatus;

public class EntityUtils {
    public static User createUser(String username) {

        User user = new User();
        user.setUserName(username);
        return user;
    }

    public static Registration createRegistration(User user, RegistrationStatus status) {

        Registration r = new Registration();
        r.setMember(user);
        r.setStatus(status);
        return r;
    }

    public static User.UserBuilder createUserBuilder(String username) {

        return User.builder()
                .id(1L)
                .userName(username)
                .firstName("firstname")
                .lastName("lastname")
                .pnr("pnr")
                ;
    }

    public static Training.TrainingBuilder createTrainingBuilder() {

        return Training.builder()
                .id(1L)
                .title("UnitTest: Learn Java in 21 Days")
                .description("UnitTest: Java for Beginners")
                .provider(Provider.CEVORA)
                .url("http://www.cevora.be")
                .enabled(true)
                ;

    }
}
