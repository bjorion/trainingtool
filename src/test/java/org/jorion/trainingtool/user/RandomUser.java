package org.jorion.trainingtool.user;

public class RandomUser {

    public static User createUser(String username) {

        var user = new User();
        user.setUserName(username);
        return user;
    }

    public static User.UserBuilder buildUser(String username) {

        return User.builder()
                .id(1L)
                .userName(username)
                .firstName("firstname")
                .lastName("lastname")
                .pnr("pnr");
    }
}
