package org.jorion.trainingtool.repository;

import org.jorion.trainingtool.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository to retrieve the users from the database.
 */
public interface IUserRepository extends JpaRepository<User, Long> {

    String SQL_USER_BY_EXAMPLE = """
            SELECT u FROM User u
            WHERE lower(u.firstName) LIKE lower(:firstName)
            AND lower(u.lastName) LIKE lower(:lastName)
            ORDER BY u.lastName ASC, u.firstName ASC
            """;

    /**
     * Retrieve a user given his username.
     *
     * @param userName the user userName
     * @return an Optional instance containing the User entity
     */
    Optional<User> findUserByUserName(String userName);

    /**
     * Retrieve users by their firstnames and their lastnames.
     *
     * @param firstName the user firstName
     * @param lastName  the user lastName
     * @return a list of users corresponding to the given criteria. Can be empty, not null.
     */
    @Query(SQL_USER_BY_EXAMPLE)
    List<User> findAllByExample(@Param("firstName") String firstName, @Param("lastName") String lastName);
}
