package org.jorion.trainingtool.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import org.jorion.trainingtool.entities.User;

/**
 * Repository to retrieve the users from the database.
 */
@Transactional(readOnly = true)
public interface IUserRepository extends JpaRepository<User, Long>
{
    // @formatter:off
    String SQL_USER_BY_EXAMPLE = "SELECT u FROM User u " +
            "WHERE lower(u.firstname) LIKE :firstname " +
            "AND lower(u.lastname) LIKE :lastname " +
            "ORDER BY u.lastname ASC, u.firstname ASC ";
    // @formatter:on

    /**
     * Retrieve a user given his username.
     * 
     * @param username the user username
     * @return an Optional instance containing the User entity
     */
    Optional<User> findUserByUsername(String username);

    /**
     * Retrieve users by their firstnames and their lastnames.
     * 
     * @param firstname the user firstname
     * @param lastname the user lastname
     * @return a list of users corresponding to the given criteria. Can be empty, not null.
     */
    @Query(SQL_USER_BY_EXAMPLE)
    List<User> findAllByExample(String firstname, String lastname);
}
